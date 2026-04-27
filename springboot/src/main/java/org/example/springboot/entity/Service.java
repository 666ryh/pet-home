package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务实体类
 */
@Data
@TableName("service")
@Schema(description = "服务实体")
public class Service {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "服务ID")
    private Long id;
    
    @Schema(description = "服务名称")
    private String name;
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "价格")
    private BigDecimal price;
    
    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "时长(分钟)")
    private Integer duration;
    
    @Schema(description = "状态(0:停用,1:启用)")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 