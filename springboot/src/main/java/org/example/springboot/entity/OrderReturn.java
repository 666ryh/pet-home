package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_return")
@Schema(description = "订单退货信息")
public class OrderReturn {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "退货ID")
    private Long id;
    
    @Schema(description = "订单ID")
    private Long orderId;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "退货原因")
    private String reason;
    
    @Schema(description = "退货状态")
    private String status;
    
    @Schema(description = "问题描述")
    private String description;
    
    @Schema(description = "图片凭证")
    private String images;
    
    @Schema(description = "退款金额")
    private BigDecimal refundAmount;
    
    @Schema(description = "处理人")
    private String operator;
    
    @Schema(description = "处理备注")
    private String operatorNote;
    
    @Schema(description = "申请退货前的订单状态")
    private String previousOrderStatus;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @Schema(description = "处理时间")
    private LocalDateTime processTime;
} 