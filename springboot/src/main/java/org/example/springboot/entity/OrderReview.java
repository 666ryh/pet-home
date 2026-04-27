package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_review")
@Schema(description = "订单评价信息")
public class OrderReview {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "评价ID")
    private Long id;
    
    @Schema(description = "订单ID")
    private Long orderId;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "商品ID")
    private Long productId;
    
    @Schema(description = "评分(1-5星)")
    private Integer rating;
    
    @Schema(description = "评价内容")
    private String content;
    
    @Schema(description = "评价图片")
    private String images;
    
    @Schema(description = "是否匿名")
    private Boolean isAnonymous;
    
    @Schema(description = "商家回复")
    private String reply;
    
    @Schema(description = "回复时间")
    private LocalDateTime replyTime;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    // 添加用户信息关联
    @TableField(exist = false)
    @Schema(description = "用户信息")
    private User user;
} 