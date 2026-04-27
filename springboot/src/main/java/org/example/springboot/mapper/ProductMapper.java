package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.entity.Product;

import java.util.List;

/**
 * 商品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 获取热门商品列表
     * @param limit 限制数量
     * @return 热门商品列表
     */
    @Select("SELECT * FROM product WHERE status = 1 ORDER BY id DESC LIMIT #{limit}")
    List<Product> getHotProducts(Integer limit);
} 