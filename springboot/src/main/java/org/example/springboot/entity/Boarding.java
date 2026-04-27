package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 寄养信息实体类
 */
@Data
@TableName("boarding")
public class Boarding {
    /**
     * 寄养ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 宠物名称
     */
    private String petName;

    /**
     * 宠物类型
     */
    private String petType;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态(已申请/已接受/进行中/已完成/已取消)
     */
    private String status;

    /**
     * 特殊要求
     */
    private String requirements;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 用户名（非数据库字段）
     */
    @TableField(exist = false)
    private String userName;
} 