package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.Shipping;

/**
 * 发货信息Mapper接口
 */
@Mapper
public interface ShippingMapper extends BaseMapper<Shipping> {
} 