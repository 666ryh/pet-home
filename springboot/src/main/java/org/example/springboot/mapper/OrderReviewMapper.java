package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.springboot.entity.OrderReview;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderReviewMapper extends BaseMapper<OrderReview> {
} 