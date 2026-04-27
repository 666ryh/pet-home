package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("training_appointment")
@Schema(description = "训练预约")
public class TrainingAppointment {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "预约ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "课程ID")
    private Long courseId;
    
    @Schema(description = "宠物名称")
    private String petName;
    
    @Schema(description = "预约时间")
    private LocalDateTime appointmentTime;
    
    @Schema(description = "状态(已预约/已确认/已完成/已取消)")
    private String status;
    
    @Schema(description = "特殊要求")
    private String requirements;
    
    @Schema(description = "进度(0-100)")
    private Integer progress;

    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @Schema(description = "训练满意度(1-5)")
    private Integer rating;
    
    @Schema(description = "反馈内容")
    private String feedback;
    
    @Schema(description = "反馈时间")
    private LocalDateTime feedbackTime;

    @TableField(exist = false)
    private TrainingCourse course;
} 