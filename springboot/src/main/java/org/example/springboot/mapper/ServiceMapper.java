package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.Service;

/**
 * 服务Mapper接口
 */
@Mapper
public interface ServiceMapper extends BaseMapper<Service> {
} 