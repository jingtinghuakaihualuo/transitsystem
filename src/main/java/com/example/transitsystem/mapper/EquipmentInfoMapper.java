package com.example.transitsystem.mapper;

import com.example.transitsystem.bean.EquipmentInfo;
import com.example.transitsystem.bean.EquipmentInfoExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentInfoMapper {
    int countByExample(EquipmentInfoExample example);

    int deleteByExample(EquipmentInfoExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(EquipmentInfo record);

    int insertSelective(EquipmentInfo record);

    List<EquipmentInfo> selectByExample(EquipmentInfoExample example);

    EquipmentInfo selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") EquipmentInfo record, @Param("example") EquipmentInfoExample example);

    int updateByExample(@Param("record") EquipmentInfo record, @Param("example") EquipmentInfoExample example);

    int updateByPrimaryKeySelective(EquipmentInfo record);

    int updateByPrimaryKey(EquipmentInfo record);
}