package com.example.transitsystem.base;

public class SocketApiRequest {
    private String api;
    private Integer reqNo;
    private Long reqDate;
    private Object data;

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public Integer getReqNo() {
        return reqNo;
    }

    public void setReqNo(Integer reqNo) {
        this.reqNo = reqNo;
    }

    public Long getReqDate() {
        return reqDate;
    }

    public void setReqDate(Long reqDate) {
        this.reqDate = reqDate;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SocketApiRequest{" +
                "api='" + api + '\'' +
                ", reqNo=" + reqNo +
                ", reqDate=" + reqDate +
                ", data=" + data +
                '}';
    }
}
