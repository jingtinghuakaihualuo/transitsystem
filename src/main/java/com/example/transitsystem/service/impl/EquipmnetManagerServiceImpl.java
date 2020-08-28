package com.example.transitsystem.service.impl;

import com.example.transitsystem.base.OpenApiResult;
import com.example.transitsystem.base.ResultEnum;
import com.example.transitsystem.base.SocketApiRequest;
import com.example.transitsystem.base.SocketApiRespnose;
import com.example.transitsystem.bean.EquipmentInfo;
import com.example.transitsystem.bean.EquipmentInfoExample;
import com.example.transitsystem.controller.DelongServerSocket;
import com.example.transitsystem.mapper.EquipmentFlowMapper;
import com.example.transitsystem.mapper.EquipmentInfoMapper;
import com.example.transitsystem.service.ClientSocket;
import com.example.transitsystem.service.EquipmentManagerService;
import com.example.transitsystem.utils.TimeStampUtil;
import com.example.transitsystem.vo.EquipmentInfoRequest;
import com.example.transitsystem.vo.NetworkStatisticalRequest;
import com.example.transitsystem.vo.NetworkStatisticalRespnose;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class EquipmnetManagerServiceImpl implements EquipmentManagerService {

    private Gson gson = new GsonBuilder().create();

    @Autowired
    EquipmentInfoMapper infoMapper;

    @Autowired
    EquipmentFlowMapper flowMapper;

    @Override
    public OpenApiResult getEquipmentInfo(EquipmentInfoRequest request) {

        OpenApiResult result = null;

        try {
            List<EquipmentInfo> list = null;
            EquipmentInfoExample example = new EquipmentInfoExample();
            EquipmentInfoExample.Criteria criteria = example.createCriteria();
            if (!StringUtils.isEmpty(request.getSno()) && !StringUtils.isEmpty(request.getMac())) {
                criteria.andSnoEqualTo(request.getSno());
                criteria.andMacEqualTo(request.getMac());
                list = infoMapper.selectByExample(example);
            } else {
                criteria.andTokenIdEqualTo(Long.valueOf(request.getTokenId()));
                list = infoMapper.selectByExample(example);
            }

            if (list.size() > 1) {
                return new OpenApiResult(ResultEnum.DATABASEERROR);
            }

            EquipmentInfo equipmentInfo = list.get(0);
            ClientSocket clientSocket = null;
            if (equipmentInfo.getTokenId() != null) {
                clientSocket = DelongServerSocket.tokenMappingclient.get(equipmentInfo.getTokenId());
            }

            if (clientSocket != null) {
                Integer reqNo = new Random().nextInt(Integer.MAX_VALUE);
                //发送数据
                SocketApiRequest socketApiRequest = new SocketApiRequest();
                socketApiRequest.setApi("notify");
                socketApiRequest.setReqNo(reqNo);
                socketApiRequest.setReqDate(System.currentTimeMillis()/1000);
                socketApiRequest.setData("notify");

                clientSocket.send(gson.toJson(socketApiRequest));
                //等待响应
                SocketApiRespnose socketApiRespnose = new SocketApiRespnose();
                clientSocket.getMessage().put(reqNo, socketApiRespnose);

                synchronized (socketApiRespnose) {
                    try {
                        socketApiRespnose.wait(10000);

                        socketApiRespnose = clientSocket.getMessage().get(reqNo);
                    } catch (InterruptedException e) {
                        log.error("wait error, e={}", e);
                    } finally {
                        clientSocket.getMessage().remove(reqNo);
                    }
                }

                if (socketApiRespnose.getRespNo() == null) {
                    equipmentInfo.setStatus(Byte.valueOf("2"));
                }
            } else {
                if (equipmentInfo.getStatus() > Byte.valueOf("0")) {
                    equipmentInfo.setStatus(Byte.valueOf("2"));
                }
            }

            result = new OpenApiResult(ResultEnum.SUCCESS);
            result.setData(equipmentInfo);

        } catch (NumberFormatException e) {
            log.error("EquipmnetManagerServiceImpl:getEquipmentInfo(), NumberFormatException={}", e);
            return new OpenApiResult(ResultEnum.REQUESTPARAMERROR);
        } catch (Exception e) {
            log.error("EquipmnetManagerServiceImpl:getEquipmentInfo(), exception={}", e);
            return new OpenApiResult(ResultEnum.SYSTEMEXCEPTION);
        }
        return result;
    }

    @Override
    public List<NetworkStatisticalRespnose> getNetworkTraffic(NetworkStatisticalRequest request) {

        try {
            List<NetworkStatisticalRespnose> list = flowMapper.getNetworkStatistical(request);
            return list;
        } catch (Exception e) {
            log.error("EquipmnetManagerServiceImpl:getEquipmentInfo(), exception={}", e);
        }
        return null;
    }

    @Transactional
    @Override
    public String register(ClientSocket clientSocket, String reqStr) {
        JsonObject jsonObject = gson.fromJson(reqStr, JsonObject.class);
        Integer reqNo;
        EquipmentInfoRequest request = null;
        try {
            reqNo = jsonObject.get("reqNo").getAsInt();
            request = gson.fromJson(jsonObject.get("data"), EquipmentInfoRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("request param error.");
        }

        List<EquipmentInfo> list = null;
        EquipmentInfoExample example = new EquipmentInfoExample();
        EquipmentInfoExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isEmpty(request.getSno()) || StringUtils.isEmpty(request.getMac())) {
            throw new RuntimeException("request param error.");
        } else {
            criteria.andSnoEqualTo(request.getSno());
            criteria.andMacEqualTo(request.getMac());
            criteria.andStatusLessThan(Byte.valueOf("3"));
            list = infoMapper.selectByExample(example);
        }

        if (list.size() == 0) {
            SocketApiRespnose socketApiRespnose = new SocketApiRespnose(ResultEnum.EQUIPMENTNOTEXIST, reqNo);
            return gson.toJson(socketApiRespnose);
        }

        if (list.size() > 1) {
            log.info("request param error.");
            SocketApiRespnose socketApiRespnose = new SocketApiRespnose(ResultEnum.DATABASEERROR, reqNo);
            return gson.toJson(socketApiRespnose);
        }

        EquipmentInfo equipmentInfo = list.get(0);
        JsonObject retJsonObject = new JsonObject();


        //考虑到设备已经注册
        cleanOldClient(clientSocket, equipmentInfo);
        Long newTokenId = updateToken(clientSocket, equipmentInfo);
        retJsonObject.addProperty("tokenId", String.valueOf(newTokenId));
        SocketApiRespnose socketApiRespnose = new SocketApiRespnose(ResultEnum.SUCCESS, reqNo);
        socketApiRespnose.setData(retJsonObject);
        //修改状态
        equipmentInfo.setStatus(Byte.valueOf("1"));
        infoMapper.updateByPrimaryKey(equipmentInfo);
        return gson.toJson(socketApiRespnose);
    }

    @Override
    public Long updateToken(ClientSocket clientSocket, EquipmentInfo equipmentInfo) {

        if (equipmentInfo.getTokenId() == null || tokenMoreThanThreeDay(equipmentInfo)) {
            Long oldTokenId = equipmentInfo.getTokenId();
            String newTokenId = String.valueOf(System.currentTimeMillis()).substring(4, 10) + autoGenericCode(String.valueOf(equipmentInfo.getId()), 6);
            equipmentInfo.setTokenId(Long.valueOf(newTokenId));
            equipmentInfo.setUpdateTime(TimeStampUtil.getCurentTimeStamp10());
            if (oldTokenId == null) {
                equipmentInfo.setStatus(Byte.valueOf("1"));
            }
            infoMapper.updateByPrimaryKey(equipmentInfo);

            //更新
            if (oldTokenId != null) {
                DelongServerSocket.tokenMappingclient.remove(oldTokenId);
            }
            DelongServerSocket.tokenMappingclient.put(Long.valueOf(newTokenId), clientSocket);
            DelongServerSocket.clientMappingToken.put(clientSocket, Long.valueOf(newTokenId));
            return Long.valueOf(newTokenId);
        }

        return equipmentInfo.getTokenId();
    }

    private void cleanOldClient(ClientSocket clientSocket, EquipmentInfo equipmentInfo) {
        if (equipmentInfo.getTokenId() == null) {
            return;
        }
        ClientSocket oldSocketClient = DelongServerSocket.tokenMappingclient.get(Long.valueOf(equipmentInfo.getTokenId()));

        if (oldSocketClient != null && !oldSocketClient.equals(clientSocket)) {
            try {
                oldSocketClient.logout();
            } catch (IOException e) {
                log.error("clean time out socket client error,e={}", e);
            }
            DelongServerSocket.clientMappingToken.remove(oldSocketClient);
        }

        DelongServerSocket.tokenMappingclient.put(Long.valueOf(equipmentInfo.getTokenId()), clientSocket);
        DelongServerSocket.clientMappingToken.put(clientSocket, Long.valueOf(equipmentInfo.getTokenId()));
    }

    @Override
    public List<EquipmentInfo> getEquipmentInfoList(String sno) {
        EquipmentInfoExample example = new EquipmentInfoExample();
        EquipmentInfoExample.Criteria criteria = example.createCriteria();
        if (sno != null) {
            criteria.andSnoEqualTo(sno);
        }
        example.setOrderByClause("id desc");
        List<EquipmentInfo> list = infoMapper.selectByExample(example);
        return list;
    }

    @Override
    @Transactional
    public String EquipmentBatchImport(MultipartFile file) {

        try {
            InputStreamReader reader = new InputStreamReader(file.getInputStream());
            BufferedReader bf = new BufferedReader(reader);
            String line;
            int i = 0;
            while ((line = bf.readLine()) != null) {
                if (i == 0) {
                    i++;
                    continue;
                }
                EquipmentInfoExample example = new EquipmentInfoExample();
                EquipmentInfoExample.Criteria criteria = example.createCriteria();
                //判断sno+mac在数据库是否存在
                String[] info = line.split("\\|");
                criteria.andSnoEqualTo(info[0]);
                criteria.andMacEqualTo(info[1]);

                List<EquipmentInfo> list = infoMapper.selectByExample(example);
                //数据已经存在
                if (list.size() > 0) {
                    log.error("sno=" + info[0] + "mac=" + info[1]  + "数据已经存在，导入失败");
                    return "sno=" + info[0] + "mac=" + info[1] + "数据已经存在，导入失败";
                } else {
                    EquipmentInfo equipmentInfo = new EquipmentInfo();
                    equipmentInfo.setSno(info[0]);
                    equipmentInfo.setMac(info[1]);
                    equipmentInfo.setStatus(Byte.valueOf("0"));
                    equipmentInfo.setCreateTime(TimeStampUtil.getCurentTimeStamp10());
                    equipmentInfo.setUpdateTime(equipmentInfo.getCreateTime());
                    infoMapper.insertSelective(equipmentInfo);
                }
                i++;
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }

    @Override
    public void updateEquipmentStatus(Long tokenId) {
        EquipmentInfoExample example = new EquipmentInfoExample();
        EquipmentInfoExample.Criteria criteria = example.createCriteria();
        criteria.andTokenIdEqualTo(tokenId);
        List<EquipmentInfo> list = infoMapper.selectByExample(example);

        EquipmentInfo equipmentInfo = list.get(0);
        equipmentInfo.setStatus(Byte.valueOf("2"));
        infoMapper.updateByPrimaryKey(equipmentInfo);
    }

    @Override
    public String disableEquipment(Integer id) {
        EquipmentInfoExample example = new EquipmentInfoExample();
        EquipmentInfoExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(id);
        List<EquipmentInfo> list = infoMapper.selectByExample(example);

        try {
            EquipmentInfo equipmentInfo = list.get(0);
            equipmentInfo.setStatus(Byte.valueOf("3"));
            infoMapper.updateByPrimaryKey(equipmentInfo);
        } catch (Exception e) {
            return ResultEnum.SYSTEMEXCEPTION.getMsg();
        }
        return ResultEnum.SUCCESS.getMsg();
    }

    @Override
    public String disableEquipment(String sno, String mac) {
        EquipmentInfoExample example = new EquipmentInfoExample();
        EquipmentInfoExample.Criteria criteria = example.createCriteria();
        criteria.andSnoEqualTo(sno);
        criteria.andMacEqualTo(mac);
        List<EquipmentInfo> list = infoMapper.selectByExample(example);

        try {
            EquipmentInfo equipmentInfo = list.get(0);
            equipmentInfo.setStatus(Byte.valueOf("3"));
            infoMapper.updateByPrimaryKey(equipmentInfo);
        } catch (Exception e) {
            return ResultEnum.SYSTEMEXCEPTION.getMsg();
        }
        return ResultEnum.SUCCESS.getMsg();
    }

    private boolean tokenMoreThanThreeDay(EquipmentInfo equipmentInfo) {
        long currTime = System.currentTimeMillis();
        if (currTime - (equipmentInfo.getUpdateTime() * 1000) > 3 * 24 * 60 * 60 * 1000) {
            log.info("token time out...");
            return true;
        }
        return false;
    }

    /**
     * 不够位数的在前面补0，保留num的长度位数字
     *
     * @param code
     * @return
     */
    private String autoGenericCode(String code, int num) {
        String result = "";
        // 保留num的位数
        // 0 代表前面补充0
        // num 代表长度为4
        // d 代表参数为正数型
        result = String.format("%0" + num + "d", Integer.parseInt(code) + 1);

        return result;
    }


}
