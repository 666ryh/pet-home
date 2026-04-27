package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务预约实体类
 */
@Data
@TableName("service_appointment")
@Schema(description = "服务预约实体")
public class ServiceAppointment {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "预约ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "服务ID")
    private Long serviceId;
    
    @Schema(description = "预约时间")
    private LocalDateTime appointmentTime;
    
    @Schema(description = "状态(已预约/已确认/已完成/已取消)")
    private String status;
    
    @Schema(description = "宠物名称")
    private String petName;
    
    @Schema(description = "特殊要求")
    private String requirements;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 