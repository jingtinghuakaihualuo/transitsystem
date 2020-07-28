package com.example.transitsystem.service;

import com.example.transitsystem.base.OpenApiResult;
import com.example.transitsystem.bean.EquipmentInfo;
import com.example.transitsystem.vo.EquipmentInfoRequest;
import com.example.transitsystem.vo.NetworkStatisticalRequest;
import com.example.transitsystem.vo.NetworkStatisticalRespnose;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface EquipmentManagerService {
    OpenApiResult getEquipmentInfo(EquipmentInfoRequest request);

    List<NetworkStatisticalRespnose> getNetworkTraffic(NetworkStatisticalRequest request);

    String register(ClientSocket clientSocket, String info);

    Long updateToken(ClientSocket clientSocket, EquipmentInfo equipmentInfo);

    List<EquipmentInfo> getEquipmentInfoList(String sno);

    String EquipmentBatchImport(MultipartFile file);

    void updateEquipmentStatus(Long tokenId);

    String disableEquipment(Integer id);

    String disableEquipment(String sno, String mac);

}
