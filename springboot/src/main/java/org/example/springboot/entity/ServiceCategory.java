package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务分类实体类
 */
@Data
@TableName("service_category")
@Schema(description = "服务分类实体")
public class ServiceCategory {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "分类ID")
    private Long id;
    
    @Schema(description = "分类名称")
    private String name;
    
    @Schema(description = "分类描述")
    private String description;
    
    @Schema(description = "分类图标")
    private String icon;
    
    @Schema(description = "排序")
    private Integer sort;
    
    @Schema(description = "状态(0:禁用,1:正常)")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}