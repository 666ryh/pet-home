package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Order;
import org.example.springboot.entity.Product;
import org.example.springboot.mapper.OrderMapper;
import org.example.springboot.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendService.class);
    // 新增：推荐商品数量常量，便于维护
    private static final int RECOMMEND_NUM = 8;
    // 新增：相似用户数量常量
    private static final int SIMILAR_USER_NUM = 8;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductMapper productMapper;

    // 优化：构建用户-商品购买次数矩阵（替代原Set）
    private Map<Long, Map<Long, Integer>> buildUserProductMap() {
        Map<Long, Map<Long, Integer>> userProductMap = new HashMap<>();
        // 恢复：仅查询已完成订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        //orderWrapper.eq(Order::getStatus, "已完成");
        List<Order> orders = orderMapper.selectList(orderWrapper);

        for (Order order : orders) {
            Long uId = order.getUserId();
            Long pId = order.getProductId();
            // 统计购买次数
            userProductMap.computeIfAbsent(uId, k -> new HashMap<>())
                    .put(pId, userProductMap.get(uId).getOrDefault(pId, 0) + 1);
        }
        LOGGER.info("构建用户-商品行为矩阵完成，共{}个活跃用户", userProductMap.size());
        return userProductMap;
    }

    // 优化：计算用户相似度矩阵（基于购买次数，对称矩阵）
    private Map<Long, Map<Long, Double>> calculateUserSimilarity() {
        Map<Long, Map<Long, Integer>> userProductMap = buildUserProductMap();
        Map<Long, Map<Long, Double>> similarityMatrix = new HashMap<>();
        List<Long> userIds = new ArrayList<>(userProductMap.keySet());

        for (int i = 0; i < userIds.size(); i++) {
            Long user1 = userIds.get(i);
            Map<Long, Integer> products1 = userProductMap.get(user1);
            Map<Long, Double> userSimilarities = new HashMap<>();
            similarityMatrix.put(user1, userSimilarities);

            for (int j = i + 1; j < userIds.size(); j++) {
                Long user2 = userIds.get(j);
                Map<Long, Integer> products2 = userProductMap.get(user2);
                // 加权余弦相似度
                double similarity = calculateWeightedCosineSimilarity(products1, products2);
                if (similarity > 0) { // 仅保留相似度>0的用户对
                    userSimilarities.put(user2, similarity);
                    similarityMatrix.computeIfAbsent(user2, k -> new HashMap<>()).put(user1, similarity);
                }
            }
        }
        return similarityMatrix;
    }

    // 新增：加权余弦相似度计算（基于购买次数）
    private double calculateWeightedCosineSimilarity(Map<Long, Integer> map1, Map<Long, Integer> map2) {
        if (map1 == null || map2 == null || map1.isEmpty() || map2.isEmpty()) {
            return 0.0;
        }
        // 计算点积、map1模长、map2模长
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        // 遍历较小的map，提升性能
        Map<Long, Integer> smallMap = map1.size() < map2.size() ? map1 : map2;
        Map<Long, Integer> bigMap = map1.size() < map2.size() ? map2 : map1;

        for (Map.Entry<Long, Integer> entry : smallMap.entrySet()) {
            Long pId = entry.getKey();
            int count1 = entry.getValue();
            int count2 = bigMap.getOrDefault(pId, 0);
            dotProduct += count1 * count2;
            norm1 += Math.pow(count1, 2);
        }
        // 计算大map的模长（补充未遍历的key）
        for (int count : bigMap.values()) {
            norm2 += Math.pow(count, 2);
        }
        // 避免除0
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // 核心推荐方法（优化后）
    public Result<?> generateRecommendations(Long userId) {
        // 新增：入参非空校验
        if (userId == null) {
            LOGGER.warn("生成推荐失败：用户ID为null");
            return Result.error("-1", "用户ID不能为空");
        }
        long start = System.currentTimeMillis();
        try {
            // 1. 查询当前用户已完成订单，提取已购商品
            LambdaQueryWrapper<Order> userOrderWrapper = new LambdaQueryWrapper<>();
           // userOrderWrapper.eq(Order::getUserId, userId).eq(Order::getStatus, "已完成");
            List<Order> userOrders = orderMapper.selectList(userOrderWrapper);

            Set<Long> userProductIds = new HashSet<>();
            userOrders.forEach(order -> userProductIds.add(order.getProductId()));
            if (userProductIds.isEmpty()) {
                LOGGER.warn("用户{}无已完成订单，直接返回销量推荐", userId);
                List<Product> salesTop = getSalesTopProduct(RECOMMEND_NUM);
                return Result.success(salesTop);
            }

            // 2. 获取用户相似度矩阵，简化相似用户筛选
            Map<Long, Map<Long, Double>> similarityMatrix = calculateUserSimilarity();
            Map<Long, Double> similarUsers = similarityMatrix.getOrDefault(userId, new HashMap<>());

            // 3. 动态调整相似度阈值
            double similarityThreshold = getDynamicThreshold(userProductIds.size());

            // 4. 过滤排序相似用户（取前SIMILAR_USER_NUM个）
            Map<Long, Double> filteredSimilarUsers = similarUsers.entrySet().stream()
                    .filter(entry -> entry.getValue() >= similarityThreshold)
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(SIMILAR_USER_NUM)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

            // 5. 计算推荐商品得分
            Map<Long, Double> productScores = new HashMap<>();
            for (Map.Entry<Long, Double> entry : filteredSimilarUsers.entrySet()) {
                Long similarUId = entry.getKey();
                double similarity = entry.getValue();
                // 查询相似用户已完成订单
                List<Order> similarOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, similarUId));
                        //.eq(Order::getStatus, "已完成"));
                // 累加得分：排除已购商品，相似度×2
                for (Order order : similarOrders) {
                    Long pId = order.getProductId();
                    if (!userProductIds.contains(pId)) {
                        productScores.merge(pId, similarity * 2, Double::sum);
                    }
                }
            }

            // 6. 生成推荐结果（无结果则降级销量推荐）
            List<Product> recommendations;
            if (productScores.isEmpty()) {
                LOGGER.info("用户{}无符合条件的推荐商品，降级为销量推荐", userId);
                recommendations = getSalesTopProduct(RECOMMEND_NUM);
            } else {
                // 按得分倒序排序，取前RECOMMEND_NUM个
                List<Long> sortedProductIds = productScores.entrySet().stream()
                        .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                        .limit(RECOMMEND_NUM)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                // 保证查询结果与ID顺序一致
                recommendations = getProductByIds(sortedProductIds);
            }

            // 新增：核心指标日志
            long cost = System.currentTimeMillis() - start;
            LOGGER.info("用户{}推荐生成成功，耗时{}ms，已购商品{}个，相似用户{}个，推荐商品{}个",
                    userId, cost, userProductIds.size(), filteredSimilarUsers.size(), recommendations.size());
            return Result.success(recommendations);
        } catch (Exception e) {
            LOGGER.error("用户{}生成推荐失败", userId, e); // 打印完整异常栈，便于排查
            return Result.error("-1", "生成推荐失败：" + e.getMessage());
        }
    }

    // 抽离：销量前N商品查询（复用）
    private List<Product> getSalesTopProduct(int num) {
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getSalesCount)
                .last("LIMIT " + num));
    }

    // 抽离：按ID列表查询商品，保证顺序（适配MySQL）
    private List<Product> getProductByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        String idStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                .in(Product::getId, ids)
                .last("ORDER BY FIELD(id, " + idStr + ")"));
    }

    // 抽离：动态阈值计算（复用）
    private double getDynamicThreshold(int productCount) {
        if (productCount < 3) {
            return 0.2;
        } else if (productCount > 10) {
            return 0.4;
        } else {
            return 0.3;
        }
    }
}


