package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 宠物疫苗接种记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("pet_vaccination")
@Schema(description = "宠物疫苗接种记录信息")
public class PetVaccination {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "接种ID")
    private Long id;
    
    @Schema(description = "宠物ID")
    private Long petId;
    
    @Schema(description = "疫苗名称")
    private String vaccineName;
    
    @Schema(description = "接种日期")
    private LocalDateTime vaccinationDate;
    
    @Schema(description = "下次接种日期")
    private LocalDateTime nextDate;
    
    @Schema(description = "接种医院")
    private String hospital;
    
    @Schema(description = "疫苗批号")
    private String batchNumber;
    
    @Schema(description = "备注")
    private String notes;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 