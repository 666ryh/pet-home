package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 训练预约操作日志实体类
 */
@Data
@TableName("training_appointment_operation_log")
@Schema(description = "训练预约操作日志实体")
public class TrainingAppointmentOperationLog {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long id;
    
    @Schema(description = "预约ID")
    private Long appointmentId;
    
    @Schema(description = "操作人ID")
    private Long operatorId;
    
    @Schema(description = "操作人姓名")
    private String operatorName;
    
    @Schema(description = "操作动作")
    private String action;
    
    @Schema(description = "操作前状态")
    private String beforeStatus;
    
    @Schema(description = "操作后状态")
    private String afterStatus;
    
    @Schema(description = "备注")
    private String remark;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
} 