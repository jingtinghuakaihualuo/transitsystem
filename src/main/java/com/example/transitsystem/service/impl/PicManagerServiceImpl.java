package com.example.transitsystem.service.impl;

import com.example.transitsystem.base.OpenApiResult;
import com.example.transitsystem.base.ResultEnum;
import com.example.transitsystem.base.SocketApiRequest;
import com.example.transitsystem.base.SocketApiRespnose;
import com.example.transitsystem.bean.EquipmentFlow;
import com.example.transitsystem.bean.EquipmentFlowExample;
import com.example.transitsystem.bean.EquipmentInfo;
import com.example.transitsystem.bean.EquipmentInfoExample;
import com.example.transitsystem.controller.DelongServerSocket;
import com.example.transitsystem.mapper.EquipmentFlowMapper;
import com.example.transitsystem.mapper.EquipmentInfoMapper;
import com.example.transitsystem.service.ClientSocket;
import com.example.transitsystem.service.EquipmentManagerService;
import com.example.transitsystem.service.PicManagerService;
import com.example.transitsystem.utils.TimeStampUtil;
import com.example.transitsystem.vo.UploadFileRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.Random;

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

    @Value("${file.clear.timeout}")
    Integer clearTimeOut;

    private Gson gson = new GsonBuilder().create();

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


        //因为多线程操作MultipartFile的时候临时文件可能关闭，所以需要将文件写入服务器。由服务器多线程操作读写
        String[] fileName = new String[files.length];
        long fileSizeTotal = 0;
        try {
            //13位时间戳+文件名  避免重复文件
            long currentTime = System.currentTimeMillis();
            //设置用户头像新名字
            param.setHeadPicName(currentTime + "_" +param.getHeadPicName());
            for (int i = 0; i < files.length; i++) {
                long fileSize = 0;
                String oldName = files[i].getOriginalFilename();
                String newName = currentTime + "_" + oldName;
                fileName[i] = newName;
                File file = new File(fileTmpPaht + newName);
                InputStream is = null;
                BufferedInputStream bis = null;
                FileOutputStream fos = null;
                BufferedOutputStream bos = null;
                try {
                    is = files[i].getInputStream();
                    bis = new BufferedInputStream(is, 1024 * 1024 * 1);
                    fos = new FileOutputStream(file);
                    bos = new BufferedOutputStream(fos, 1024 * 1024 * 1);   //1M

                    byte[] bytes = new byte[1024 * 50];
                    int len = 0;
                    while ((len = bis.read(bytes)) != -1) {
                        bos.write(bytes, 0, len);
                        fileSize = fileSize + len;
                    }
                    bos.flush();
                } catch (IOException e) {
                    log.error("复制临时文件到服务器异常，e={}", e);
                    //异常则不保存该文件
                    fileName[i] = null;
                    fileSize = 0;
                } finally {
                    is.close();
                    bis.close();
                    fos.close();
                    bos.close();
                }
                fileSizeTotal = fileSizeTotal + fileSize;
            }
        } catch (Exception e) {
            log.error("复制文件异常,e={}", e);
        }

        modifyFlow(fileSizeTotal, param);
        Integer reqNo = new Random().nextInt(Integer.MAX_VALUE);
        //发送数据
        String sendStr = getSocketRespnose(fileName, param, reqNo);

        clientSocket.send(sendStr);
        //等待响应
//        SocketApiRespnose socketApiRespnose = new SocketApiRespnose();
//        clientSocket.getMessage().put(reqNo, socketApiRespnose);
//
//        synchronized (socketApiRespnose) {
//            try {
//                socketApiRespnose.wait(10000);
//            } catch (InterruptedException e) {
//                log.error("wait error, e={}", e);
//            }
//        }
//
//        if (socketApiRespnose.equals(clientSocket.getMessage().get(reqNo))) {
//            return new OpenApiResult(ResultEnum.RESPONSETIMEOUT, reqNo);
//        }

        return new OpenApiResult(ResultEnum.SUCCESS);
    }

    private void modifyFlow(long fileSizeTotal, UploadFileRequest param) {
        List<EquipmentFlow> list = null;
        EquipmentFlowExample example = new EquipmentFlowExample();
        EquipmentFlowExample.Criteria criteria = example.createCriteria();
        criteria.andSnoEqualTo(param.getSno());
        criteria.andMacEqualTo(param.getMac());
        list = flowMapper.selectByExample(example);
        if (list.size() > 1) {
            throw new RuntimeException("数据库数据错误");
        } else if (list.size() == 0) {
            //新设备第一次推送失败 不需要减
            EquipmentFlow flow = new EquipmentFlow();
            flow.setSno(param.getSno());
            flow.setMac(param.getMac());
            flow.setSize((float) (fileSizeTotal / 1024.0));
            flow.setUpdateTime(TimeStampUtil.getCurentTimeStamp10());
            flow.setCreateTime(flow.getUpdateTime());
            flowMapper.insertSelective(flow);
        } else {
            EquipmentFlow flow = list.get(0);
            flow.setSize(flow.getSize() + (float) (fileSizeTotal / 1024.0));
            flow.setUpdateTime(TimeStampUtil.getCurentTimeStamp10());
            flowMapper.updateByPrimaryKey(flow);
        }
    }

    private String getSocketRespnose(String[] fileNames, UploadFileRequest param, Integer reqNo) {
        //组装发送的数据
        SocketApiRequest socketApiRequest = new SocketApiRequest();
        JsonObject sendJson = new JsonObject();
        JsonArray fileArray = new JsonArray();

        for (int i = 0; i < fileNames.length ; i++) {
            log.debug("fileName[i] = {}, headPicName={}, fileName == headPicName ={}", fileNames[i], param.getHeadPicName(), fileNames[i].equals(param.getHeadPicName()));
            if (fileNames[i] != null && !"".equals(fileNames[i])) {
                String fileRealName = fileNames[i];
                if (fileRealName.equals(param.getHeadPicName())) {
                    sendJson.addProperty("name", param.getName());
                    sendJson.addProperty("headPicName", param.getHeadPicName());
                }

                fileArray.add(fileNames[i]);
            }
        }
        sendJson.add("fileNames", fileArray);
        sendJson.addProperty("filePath", fileTmpPaht);
        socketApiRequest.setApi("notify/hasNewFile");
        socketApiRequest.setReqNo(reqNo);
        socketApiRequest.setReqDate(System.currentTimeMillis()/1000);
        socketApiRequest.setData(sendJson);

        return gson.toJson(socketApiRequest);
    }

    @Override
    public void doClearFile() {
        File file = new File(fileTmpPaht);
        long currTime = System.currentTimeMillis();
        if (file.exists()) {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                File subFile = fileList[i];
                try {
                    long createTime = Long.valueOf(subFile.getName().substring(0, 13));
                    if (currTime - createTime > clearTimeOut * 1000) {
                        subFile.delete();
                    }
                } catch (Exception e) {
                    log.error("文件被占用");
                }
            }
        }
    }

}
