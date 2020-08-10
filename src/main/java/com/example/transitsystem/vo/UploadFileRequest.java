package com.example.transitsystem.vo;

import org.springframework.util.StringUtils;

public class UploadFileRequest {
    String sno;
    String mac;
    String name;
    String headPicName;
    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeadPicName() {
        return headPicName;
    }

    public void setHeadPicName(String headPicName) {
        this.headPicName = headPicName;
    }

    public boolean checkParam() {
        if(!StringUtils.isEmpty(getSno()) && !StringUtils.isEmpty(getMac())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UploadFileRequest{" +
                "sno='" + sno + '\'' +
                ", mac='" + mac + '\'' +
                ", name='" + name + '\'' +
                ", headPicName='" + headPicName + '\'' +
                '}';
    }
}
