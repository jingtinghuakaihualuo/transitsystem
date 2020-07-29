package com.example.transitsystem.vo;

import org.springframework.util.StringUtils;

public class EquipmentInfoRequest {
    String sno;
    String mac;
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

    public boolean checkParam() {
        if(!StringUtils.isEmpty(getTokenId()) || (!StringUtils.isEmpty(getSno()) && !StringUtils.isEmpty(getMac()))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "EquipmentInfoRequest{" +
                "sno='" + sno + '\'' +
                ", mac='" + mac + '\'' +
                ", tokenId=" + tokenId +
                '}';
    }
}
