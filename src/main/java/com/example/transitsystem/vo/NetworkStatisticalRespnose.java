package com.example.transitsystem.vo;

public class NetworkStatisticalRespnose {

    private String sno;
    private String mac;

    private Double totalFlow;

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

    public Double getTotalFlow() {
        return totalFlow;
    }

    public void setTotalFlow(Double totalFlow) {
        this.totalFlow = totalFlow;
    }

    @Override
    public String toString() {
        return "Statistical{" +
                "sno='" + sno + '\'' +
                ", mac='" + mac + '\'' +
                ", totalFlow=" + totalFlow +
                '}';
    }
}
