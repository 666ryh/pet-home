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
 * 宠物健康记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("pet_health_record")
@Schema(description = "宠物健康记录信息")
public class PetHealthRecord {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Long id;
    
    @Schema(description = "宠物ID")
    private Long petId;
    
    @Schema(description = "主人ID")
    private Long userId;
    
    @Schema(description = "记录类型(体检/疫苗/就诊/手术)")
    private String recordType;
    
    @Schema(description = "记录日期")
    private LocalDateTime recordDate;
    
    @Schema(description = "医院名称")
    private String hospital;
    
    @Schema(description = "医生姓名")
    private String doctor;
    
    @Schema(description = "体重(kg)")
    private BigDecimal weight;
    
    @Schema(description = "体温(℃)")
    private BigDecimal temperature;
    
    @Schema(description = "心率(次/分)")
    private Integer heartRate;
    
    @Schema(description = "血压")
    private String bloodPressure;
    
    @Schema(description = "呼吸频率(次/分)")
    private Integer respiratoryRate;
    
    @Schema(description = "皮肤状况")
    private String skinCondition;
    
    @Schema(description = "毛发状况")
    private String furCondition;
    
    @Schema(description = "眼部状况")
    private String eyeCondition;
    
    @Schema(description = "耳部状况")
    private String earCondition;
    
    @Schema(description = "口腔状况")
    private String oralCondition;
    
    @Schema(description = "四肢状况")
    private String limbCondition;
    
    @Schema(description = "诊断和检查结果")
    private String diagnosisResults;
    
    @Schema(description = "健康建议(包括饮食和活动)")
    private String healthSuggestions;
    
    @Schema(description = "备注")
    private String notes;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 