package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发货信息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("shipping")
@Schema(description = "发货信息")
public class Shipping {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "发货ID")
    private Long id;
    
    @Schema(description = "订单ID")
    private Long orderId;
    
    @Schema(description = "订单编号")
    private String orderNo;
    
    @Schema(description = "发货单号")
    private String shippingNo;
    
    @Schema(description = "快递公司")
    private String deliveryCompany;
    
    @Schema(description = "快递单号")
    private String trackingNo;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "发货时间")
    private LocalDateTime deliveryTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "收货时间")
    private LocalDateTime receiptTime;
    
    @Schema(description = "配送状态(待发货/已发货/已签收/已退回)")
    private String shippingStatus;
    
   
    
    @Schema(description = "操作人")
    private String operator;
    
    @Schema(description = "备注")
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 