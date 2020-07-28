package com.example.transitsystem.controller;

import com.example.transitsystem.base.OpenApiResult;
import com.example.transitsystem.base.ResultEnum;
import com.example.transitsystem.bean.EquipmentInfo;
import com.example.transitsystem.service.EquipmentManagerService;
import com.example.transitsystem.utils.TimeStampUtil;
import com.example.transitsystem.vo.EquipmentInfoRequest;
import com.example.transitsystem.vo.NetworkStatisticalRequest;
import com.example.transitsystem.vo.NetworkStatisticalRespnose;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/equipment")
@Controller
@Slf4j
public class EquipmentManagerController {

    @Autowired
    EquipmentManagerService equipmentManagerService;

    /**
     * 获取设备状态
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/info")
    public OpenApiResult getEquipmentInfo(@RequestBody EquipmentInfoRequest request) {
        log.info("==> EquipmentManagerController:getEquipmentStatus(). request={}", request);
        OpenApiResult<EquipmentInfo> result = null;
        if(!request.checkParam()) {
            return new OpenApiResult(ResultEnum.REQUESTPARAMERROR);
        }
        try {
            result =  equipmentManagerService.getEquipmentInfo(request);
        } catch (Exception e) {
            return new OpenApiResult(ResultEnum.SYSTEMEXCEPTION);
        }
        return result;
    }

    /**
     * 断开设备与服务器之间的连接
     * @param request
     * @return
     */
    @RequestMapping("/logout")
    public OpenApiResult logout(@RequestBody EquipmentInfoRequest request) {
        return null;
    }

    /**
     * 获取设备状态
     * @param
     * @return
     */
    @RequestMapping("/info/list")
    public String getEquipmentInfoList(@RequestParam(value = "start", defaultValue = "1")Integer start, @RequestParam(value = "size", defaultValue = "10")Integer size, @RequestParam(value = "sno", required = false)String sno, Model model) {
        log.info("==> EquipmentManagerController:getEquipmentInfoList(). ");
        PageHelper.startPage(start, size);
        if(StringUtils.isEmpty(sno)) {
            sno = null;
        }
        List<EquipmentInfo> userList = equipmentManagerService.getEquipmentInfoList(sno);
        PageInfo<EquipmentInfo> page = new PageInfo<>(userList);
        model.addAttribute("pages", page);
        model.addAttribute("sno", sno);
        return "index";
    }

    @RequestMapping("/import")
    public String toImportHtml() {
        return "import";
    }

    @RequestMapping("/batch/import")
    public String EquipmentBatchImport(@RequestParam("file") MultipartFile file, Model model) {
        log.info("==> EquipmentManagerController:getEquipmentInfoList(). ");
        String msg = equipmentManagerService.EquipmentBatchImport(file);
        if(msg != null || !"".equals(msg.trim())) {
            model.addAttribute("msg", msg);
        }
        return "redirect:../info/list";
    }

    @RequestMapping("/delete")
    public String EquipmentDelete(@RequestParam(value = "id", required = true) Integer id, Model model) {
        log.info("==> EquipmentManagerController:EquipmentDelete(). ");
        String msg = equipmentManagerService.disableEquipment(id);
        if(msg != null || !"".equals(msg.trim())) {
            model.addAttribute("msg", msg);
        }
        return "redirect:info/list";
    }

    @RequestMapping("/delete2")
    public String EquipmentDelete(@RequestParam(value = "sno", required = true) String sno,
                                  @RequestParam(value = "mac", required = true) String mac,
                                  Model model) {
        log.info("==> EquipmentManagerController:EquipmentDelete(). ");
        String msg = equipmentManagerService.disableEquipment(sno, mac);
        if(msg != null || !"".equals(msg.trim())) {
            model.addAttribute("msg", msg);
        }
        return "redirect:traffic";
    }

    /**
     * 流量统计
     * @param
     * @return
     */
    @RequestMapping("/traffic")
    public String getEquipmentTraffic(@RequestParam(value = "sno", required = false) String sno,
                                      @RequestParam(value = "mac", required = false) String mac,
                                      @RequestParam(value = "offset", defaultValue = "1") Integer offset,
                                      @RequestParam(value = "count", defaultValue = "10") Integer count,
                                      @RequestParam(value = "minSize", required = false) Integer minSize,
                                      @RequestParam(value = "maxSize", required = false) Integer maxSize,
                                      @RequestParam(value = "startTime", required = false) String startTime,
                                      @RequestParam(value = "endTime", required = false) String endTime,
                                      Model model) {
        log.info("==> EquipmentManagerController:getEquipmentTraffic(). ");
        PageHelper.startPage(offset, count, "totalFlow desc");
        NetworkStatisticalRequest request = new NetworkStatisticalRequest();
        request.setSno(sno);
        request.setMac(mac);
        request.setOffset(offset);
        request.setCount(count);
        request.setMinSize(minSize);
        request.setMaxSize(maxSize);
        request.setStartTime(TimeStampUtil.getLongDatetime(startTime));
        request.setEndTime(TimeStampUtil.getLongDatetime(endTime));
        List<NetworkStatisticalRespnose> list = equipmentManagerService.getNetworkTraffic(request);
        PageInfo<NetworkStatisticalRespnose> page = new PageInfo<>(list);
        model.addAttribute("pages", page);
        model.addAttribute("sno", sno);
        model.addAttribute("mac", mac);
        model.addAttribute("minSize", minSize);
        model.addAttribute("maxSize", maxSize);
//        model.addAttribute("startTime", startTime);
//        model.addAttribute("endTime", endTime);
        return "traffic";
    }

}
