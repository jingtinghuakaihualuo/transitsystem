package com.example.transitsystem.vo;

import org.springframework.util.StringUtils;

public class EquipmentInfoRequest {
    String sno;
    String mac;
    String name;
    String tokenId;

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

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean checkParam() {
        if(!StringUtils.isEmpty(getTokenId()) || (!StringUtils.isEmpty(getSno()) && !StringUtils.isEmpty(getMac()))) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "EquipmentInfoRequest{" +
                "sno='" + sno + '\'' +
                ", mac='" + mac + '\'' +
                ", name='" + name + '\'' +
                ", tokenId='" + tokenId + '\'' +
                '}';
    }
}
