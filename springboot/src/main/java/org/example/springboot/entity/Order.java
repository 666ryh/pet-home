package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("`order`") // 由于order是关键字，需要用反引号
@Schema(description = "订单信息")
public class Order {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "订单ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "订单编号")
    private String orderNo;
    
    @Schema(description = "商品ID")
    private Long productId;

    
    @Schema(description = "商品数量")
    private Integer quantity;
    
    @Schema(description = "商品单价")
    private BigDecimal price;
    
    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;
    
    @Schema(description = "支付方式")
    private String paymentMethod;
    
    @Schema(description = "支付时间")
    private LocalDateTime paymentTime;
    
    @Schema(description = "订单状态(待付款/待发货/待收货/已完成/已取消/退货中/已退货)")
    private String status;
    
    @Schema(description = "收货地址")
    private String address;
    
    @Schema(description = "联系人")
    private String contactName;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "订单备注")
    private String remark;
    
    @Schema(description = "是否已退货")
    private Boolean isReturned;
    
    @Schema(description = "退货时间")
    private LocalDateTime returnTime;
    
    @Schema(description = "是否已评价")
    private Boolean isReviewed;
    
    @Schema(description = "评价时间")
    private LocalDateTime reviewTime;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @Schema(description = "支付截止时间")
    private LocalDateTime paymentDeadline;

    @TableField(exist = false)
    @Schema(description = "商品名称")
    private String productName;

    @TableField(exist = false)
    @Schema(description = "商品图片")
    private String productImage;

} 