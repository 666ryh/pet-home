package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.ServiceAppointment;

/**
 * 服务预约Mapper接口
 */
@Mapper
public interface ServiceAppointmentMapper extends BaseMapper<ServiceAppointment> {
} 