package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.DTO.PetHealthRecordDTO;
import org.example.springboot.entity.PetHealthRecord;

/**
 * 宠物健康记录Mapper接口
 */
@Mapper
public interface PetHealthRecordMapper extends BaseMapper<PetHealthRecord> {
    
    /**
     * 分页查询健康记录并关联宠物和用户信息
     */
    @Select("SELECT r.*, p.name as pet_name, u.name as user_name " +
            "FROM pet_health_record r " +
            "LEFT JOIN pet p ON r.pet_id = p.id " +
            "LEFT JOIN user u ON r.user_id = u.id " +
            "WHERE (#{petId} IS NULL OR r.pet_id = #{petId}) " +
            "AND (#{recordType} IS NULL OR r.record_type = #{recordType}) " +
            "ORDER BY r.record_date DESC")
    Page<PetHealthRecordDTO> selectHealthRecordDtoPage(Page<PetHealthRecordDTO> page, 
                                                  @Param("petId") Long petId, 
                                                  @Param("recordType") String recordType);
} 