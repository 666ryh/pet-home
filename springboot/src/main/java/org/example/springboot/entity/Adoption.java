package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("adoption")
@Schema(description = "领养申请")
public class Adoption {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "申请ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "宠物ID")
    private Long petId;
    
    @Schema(description = "状态(已申请/审核中/已通过/已拒绝)")
    private String status;
    
    @Schema(description = "申请理由")
    private String applyReason;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "地址")
    private String address;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 