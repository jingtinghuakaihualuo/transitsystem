package com.example.transitsystem.service.impl;

import com.example.transitsystem.base.ResultEnum;
import com.example.transitsystem.base.SocketApiRespnose;
import com.example.transitsystem.bean.EquipmentFlow;
import com.example.transitsystem.bean.EquipmentFlowExample;
import com.example.transitsystem.mapper.EquipmentFlowMapper;
import com.example.transitsystem.service.ClientSocket;
import com.example.transitsystem.utils.FileUtils;
import com.example.transitsystem.utils.TimeStampUtil;
import com.example.transitsystem.vo.UploadFileRequest;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class UpLoadFileHandler implements Runnable{

    public static ExecutorService executor = Executors.newWorkStealingPool();
    private ClientSocket clientSocket;
    private String[] fileNames;//最后一位是前面所有文件的前缀
    private UploadFileRequest param;

    private EquipmentFlowMapper flowMapper;

    private  String filePath;

    public UpLoadFileHandler(ClientSocket clientSocket,String filePath, String[] fileNames, UploadFileRequest param, EquipmentFlowMapper flowMapper) {
        this.clientSocket = clientSocket;
        this.filePath = filePath;
        this.fileNames = fileNames;
        this.param = param;
        this.flowMapper = flowMapper;
    }

    @Override
    public void run() {
        try {

            String sendString = getSocketRespnose();
            //更新流量统计
            modifyFlow(sendString, param, 1);
            try {
                clientSocket.send(sendString);
            } catch (Exception e) {
                log.error("发送文件失败,可能是网络波动导致.e={}", e);
                //如果发送数据失败，回退流量计算
                //更新流量统计
                modifyFlow(sendString, param, -1);
            }

        } catch (Exception e) {
            log.error("upload file error, e=", e);
        }

    }

    private void modifyFlow(String sendString, UploadFileRequest param, int type) {
        List<EquipmentFlow> list = null;
        EquipmentFlowExample example = new EquipmentFlowExample();
        EquipmentFlowExample.Criteria criteria = example.createCriteria();
        criteria.andSnoEqualTo(param.getSno());
        criteria.andMacEqualTo(param.getMac());
        list = flowMapper.selectByExample(example);
        if(list.size() > 1) {
            throw new RuntimeException("数据库数据错误");
        } else if(list.size() == 0) {
            //新设备第一次推送失败 不需要减
            if(type == 1) {
                EquipmentFlow flow = new EquipmentFlow();
                flow.setSno(param.getSno());
                flow.setMac(param.getMac());
                flow.setSize((float)(sendString.getBytes().length / 1024.0));
                flow.setUpdateTime(TimeStampUtil.getCurentTimeStamp10());
                flow.setCreateTime(flow.getUpdateTime());
                flowMapper.insertSelective(flow);
            }
        } else {
            EquipmentFlow flow = list.get(0);
            if (type == 1) {
                flow.setSize(flow.getSize() + (float)(sendString.getBytes().length / 128.0));
            } else {
                flow.setSize(flow.getSize() - (float)(sendString.getBytes().length / 128.0));
            }
            flow.setUpdateTime(TimeStampUtil.getCurentTimeStamp10());
            flowMapper.updateByPrimaryKey(flow);
        }
    }

    private String getSocketRespnose() throws IOException {
        log.info("###############################组装返回数据时间测试startTime=" + System.currentTimeMillis()/1000);
        //组装发送的数据
        SocketApiRespnose socketApiRespnose = new SocketApiRespnose();
        JsonObject sendJson = new JsonObject();
        JsonArray fileArray = new JsonArray();
        try {
            for (int i = 0; i < fileNames.length - 1; i++) {
                String fileRealName = fileNames[i].replace(fileNames[fileNames.length -1], "");
                if (fileRealName.startsWith("head_")) {
                    sendJson.addProperty("name", param.getName());
                    sendJson.addProperty("headPic", FileUtils.getFileByteArray(new FileInputStream(filePath + fileNames[i])));
                } else {
                    JsonObject subFile = new JsonObject();
                    subFile.addProperty("fileName", fileRealName);
                    subFile.addProperty("file", FileUtils.getFileByteArray(new FileInputStream(filePath + fileNames[i])));
                    fileArray.add(subFile);
                }
            }
            sendJson.add("files", fileArray);
            socketApiRespnose.setCode(ResultEnum.SUCCESS.getCode());
            socketApiRespnose.setMsg(ResultEnum.SUCCESS.getMsg());
            socketApiRespnose.setData(sendJson);

        } catch (IOException e) {
            throw new IOException(ResultEnum.SYSTEMEXCEPTION.getMsg() + "e=" +e.getMessage());
        } finally {
            //删除临时文件
             for (int i = 0 ; i < fileNames.length - 1; i++) {
                 File file = new File(filePath + fileNames[i]);
                 if(file.exists()) {
                     file.delete();
                 }
             }
        }

        String retStr = new GsonBuilder().create().toJson(socketApiRespnose);
        log.info("################################组装返回数据时间测试endTime=" + System.currentTimeMillis()/1000);
        return retStr;
    }

}
