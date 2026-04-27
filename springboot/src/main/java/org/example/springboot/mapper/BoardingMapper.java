package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.Boarding;

/**
 * 寄养信息 Mapper 接口
 */
@Mapper
public interface BoardingMapper extends BaseMapper<Boarding> {
} 