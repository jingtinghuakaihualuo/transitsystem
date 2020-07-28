package com.example.transitsystem.service.impl;

import com.example.transitsystem.base.OpenApiResult;
import com.example.transitsystem.base.ResultEnum;
import com.example.transitsystem.bean.EquipmentInfo;
import com.example.transitsystem.bean.EquipmentInfoExample;
import com.example.transitsystem.controller.DelongServerSocket;
import com.example.transitsystem.mapper.EquipmentFlowMapper;
import com.example.transitsystem.mapper.EquipmentInfoMapper;
import com.example.transitsystem.service.ClientSocket;
import com.example.transitsystem.service.EquipmentManagerService;
import com.example.transitsystem.service.PicManagerService;
import com.example.transitsystem.vo.UploadFileRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PicManagerServiceImpl implements PicManagerService {

    @Autowired
    EquipmentInfoMapper infoMapper;

    @Autowired
    EquipmentFlowMapper flowMapper;

    @Autowired
    EquipmentManagerService equipmentManagerService;

    @Value("${file.tmp.path}")
    String fileTmpPaht;

    @Override
    public OpenApiResult picUpload(MultipartFile[] files, UploadFileRequest param) {

        //根据sn获取对应的socket
        List<EquipmentInfo> list = null;
        EquipmentInfoExample example = new EquipmentInfoExample();
        EquipmentInfoExample.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmpty(param.getSno())) {
            criteria.andSnoEqualTo(param.getSno());
        }
        if (!StringUtils.isEmpty(param.getMac())) {
            criteria.andMacEqualTo(param.getMac());
        }

        list = infoMapper.selectByExample(example);


        if (list.size() > 1) {
            return new OpenApiResult(ResultEnum.DATABASEERROR);
        } else if (list.size() == 0) {
            return new OpenApiResult(ResultEnum.EQUIPMENTNOTEXIST);
        }

        EquipmentInfo equipmentInfo = list.get(0);
        ClientSocket clientSocket = DelongServerSocket.tokenMappingclient.get(equipmentInfo.getTokenId());
        if (clientSocket == null) {
            return new OpenApiResult(ResultEnum.NOTONLINE);
        }

        equipmentManagerService.updateToken(clientSocket, equipmentInfo);


        log.info("#############################copy file startTime=" + System.currentTimeMillis() / 1000);
        //因为多线程操作MultipartFile的时候临时文件可能关闭，所以需要将文件写入服务器。由服务器多线程操作读写
        String[] fileName = new String[files.length + 1];
        try {
            String uuid = UUID.randomUUID().toString();
            for (int i = 0; i < files.length; i++) {
                String oldName = files[i].getOriginalFilename();
                String newName = uuid + oldName;
                fileName[i] = newName;
                File file = new File(fileTmpPaht + newName);
                InputStream is = null;
                BufferedInputStream bis = null;
                FileOutputStream fos = null;
                BufferedOutputStream bos = null;
                try {
                    is = files[i].getInputStream();
                    bis = new BufferedInputStream(is, 1024 * 1024 * 5);
                    fos = new FileOutputStream(file);
                    bos = new BufferedOutputStream(fos, 1024 * 1024 * 5);   //5M

                    byte[] bytes = new byte[1024 * 50];
                    int len = 0;
                    while ((len = bis.read(bytes)) != -1) {
                        bos.write(bytes, 0, len);
                    }
                    bos.flush();
                } catch (IOException e) {
                    log.error("复制临时文件到服务器异常，e={}", e);
                } finally {
                    is.close();
                    bis.close();
                    fos.close();
                    bos.close();
                }
            }
            fileName[files.length] = uuid;
        } catch (Exception e) {
            log.error("复制文件异常,e={}", e);
        }
        log.info("################################3copy file endTime=" + System.currentTimeMillis() / 1000);

        UpLoadFileHandler fileHandler = new UpLoadFileHandler(clientSocket, fileTmpPaht, fileName, param, flowMapper);

        UpLoadFileHandler.executor.submit(fileHandler);

        return new OpenApiResult(ResultEnum.SUCCESS);
    }

    @Override
    public void doClearFile() {
        File file = new File(fileTmpPaht);
        if (file.exists()) {
            File[] fileList = file.listFiles();
            for (int i = 0 ; i < fileList.length; i++) {
                File subFile = fileList[i];
                try {
                    subFile.delete();
                } catch (Exception e) {
                    log.error("文件被占用");
                }
            }
        }
    }

}
