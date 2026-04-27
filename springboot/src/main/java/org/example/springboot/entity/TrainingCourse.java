package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("training_course")
@Schema(description = "训练课程")
public class TrainingCourse {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "课程ID")
    private Long id;
    
    @Schema(description = "课程名称")
    private String name;

    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "价格")
    private BigDecimal price;
    
    @Schema(description = "时长(分钟)")
    private Integer duration;
    
    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "最大参与人数")
    private Integer maxParticipants;
    
    @Schema(description = "状态(0:停用,1:启用)")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @TableField(exist = false)
    @Schema(description = "分类")
    private String category;


} 