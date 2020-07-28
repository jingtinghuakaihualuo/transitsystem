package com.example.transitsystem.service;

import com.example.transitsystem.base.OpenApiResult;
import com.example.transitsystem.vo.UploadFileRequest;
import org.springframework.web.multipart.MultipartFile;

public interface PicManagerService {
    OpenApiResult picUpload(MultipartFile[] files, UploadFileRequest param);

    void doClearFile();
}
