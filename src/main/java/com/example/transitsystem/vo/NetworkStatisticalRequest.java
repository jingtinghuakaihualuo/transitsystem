package com.example.transitsystem.vo;

public class NetworkStatisticalRequest {
    private String sno;
    private String mac;

    private Integer minSize;
    private Integer maxSize;
    private Long startTime;
    private Long endTime;


    private Integer offset;
    private Integer count;

    public NetworkStatisticalRequest() {
        if(this.offset == null) {
            this.offset = 1;
        }
        if(this.count == null) {
            this.count = 10;
        }
    }

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

    public Integer getMinSize() {
        return minSize;
    }

    public void setMinSize(Integer minSize) {
        this.minSize = minSize;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "NetworkStatisticalRequest{" +
                "sno='" + sno + '\'' +
                ", mac='" + mac + '\'' +
                ", minSize=" + minSize +
                ", maxSize=" + maxSize +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", offset=" + offset +
                ", count=" + count +
                '}';
    }
}
