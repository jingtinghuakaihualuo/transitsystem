package com.example.transitsystem.controller;

import com.example.transitsystem.base.OpenApiResult;
import com.example.transitsystem.base.ResultEnum;
import com.example.transitsystem.service.PicManagerService;
import com.example.transitsystem.vo.UploadFileRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传
 */
@Slf4j
@RestController
public class PicManagerController {


    @Autowired
    PicManagerService picManagerService;

    @RequestMapping("/file/upload")
    @ResponseBody
    public OpenApiResult fileUpload(@RequestParam("files") MultipartFile[] files, UploadFileRequest param) {
        if (files == null || files.length <= 0) {
            return new OpenApiResult(ResultEnum.REQUESTPARAMERROR);
        }

        log.info("==> PicManagerController:fileUpload(). param={}", param);
        OpenApiResult result = null;
        if (param.checkParam()) {
            return new OpenApiResult(ResultEnum.REQUESTPARAMERROR);
        }
        try {
            result = picManagerService.picUpload(files, param);
        } catch (Exception e) {
            return new OpenApiResult(ResultEnum.SYSTEMEXCEPTION);
        }
        return result;
    }
}
