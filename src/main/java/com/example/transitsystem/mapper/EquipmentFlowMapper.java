package com.example.transitsystem.mapper;

import com.example.transitsystem.bean.EquipmentFlow;
import com.example.transitsystem.bean.EquipmentFlowExample;
import com.example.transitsystem.vo.NetworkStatisticalRequest;
import com.example.transitsystem.vo.NetworkStatisticalRespnose;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentFlowMapper {
    int countByExample(EquipmentFlowExample example);

    int deleteByExample(EquipmentFlowExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(EquipmentFlow record);

    int insertSelective(EquipmentFlow record);

    List<EquipmentFlow> selectByExample(EquipmentFlowExample example);

    EquipmentFlow selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") EquipmentFlow record, @Param("example") EquipmentFlowExample example);

    int updateByExample(@Param("record") EquipmentFlow record, @Param("example") EquipmentFlowExample example);

    int updateByPrimaryKeySelective(EquipmentFlow record);

    int updateByPrimaryKey(EquipmentFlow record);

    List<NetworkStatisticalRespnose> getNetworkStatistical(@Param("request") NetworkStatisticalRequest request);

    List<NetworkStatisticalRespnose> getNetworkStatisticalNew();
}