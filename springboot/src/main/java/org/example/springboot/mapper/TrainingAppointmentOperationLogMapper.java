package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.TrainingAppointmentOperationLog;

/**
 * 训练预约操作日志Mapper接口
 */
@Mapper
public interface TrainingAppointmentOperationLogMapper extends BaseMapper<TrainingAppointmentOperationLog> {
} 