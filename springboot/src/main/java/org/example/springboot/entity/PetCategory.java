package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("pet_category")
@Schema(description = "宠物分类")
public class PetCategory {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "分类ID")
    private Long id;
    
    @Schema(description = "分类名称")
    private String name;
    
    @Schema(description = "分类描述")
    private String description;

    
    @Schema(description = "排序")
    private Integer sort;
    
    @Schema(description = "父分类ID")
    private Long parentId;
    
    @Schema(description = "状态(0:禁用,1:正常)")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "子分类列表")
    private List<PetCategory> children;
} 