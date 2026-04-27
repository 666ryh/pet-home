package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("product")
@Schema(description = "商品信息")
public class Product {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "商品ID")
    private Long id;
    
    @Schema(description = "商品名称")
    private String name;
    
    @Schema(description = "商品分类")
    private String category;
    
    @Schema(description = "商品价格")
    private BigDecimal price;
    
    @Schema(description = "商品库存")
    private Integer stock;
    
    @Schema(description = "商品描述")
    private String description;
    
    @Schema(description = "商品图片URL，多个图片用逗号分隔")
    private String images;
    
    @Schema(description = "商品状态：0下架，1上架")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    private Integer salesCount;
} 