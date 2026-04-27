package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.DTO.PetVaccinationDTO;
import org.example.springboot.entity.PetVaccination;

/**
 * 宠物疫苗接种记录Mapper接口
 */
@Mapper
public interface PetVaccinationMapper extends BaseMapper<PetVaccination> {
    
    /**
     * 分页查询疫苗接种记录并关联宠物信息
     */
    @Select("SELECT v.*, p.name as pet_name " +
            "FROM pet_vaccination v " +
            "LEFT JOIN pet p ON v.pet_id = p.id " +
            "WHERE (#{petId} IS NULL OR v.pet_id = #{petId}) " +
            "ORDER BY v.vaccination_date DESC")
    Page<PetVaccinationDTO> selectVaccinationDtoPage(Page<PetVaccinationDTO> page, 
                                                @Param("petId") Long petId);
} 