package com.example.transitsystem.vo;

import org.springframework.util.StringUtils;

public class EquipmentInfoRequest {
    String sno;
    String mac;
    Long tokenId;

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

    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    public boolean checkParam() {
        if(getTokenId() != null || (!StringUtils.isEmpty(getSno()) && !StringUtils.isEmpty(getMac()))) {
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
