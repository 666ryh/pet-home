package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet")
@Schema(description = "宠物信息")
public class Pet {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "宠物ID")
    private Long id;
    
    @Schema(description = "宠物名称")
    private String name;
    
    @Schema(description = "宠物分类ID")
    private Long categoryId;
    

    @Schema(description = "分类名称")
    @TableField(exist = false)
    private String categoryName;
    
    @Schema(description = "品种")
    private String breed;
    
    @Schema(description = "年龄")
    private Integer age;
    
    @Schema(description = "性别")
    private String gender;
    
    @Schema(description = "健康状况")
    private String healthStatus;
    
    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "图片")
    private String images;
    
    @Schema(description = "领养状态")
    private String adoptionStatus;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 