package com.example.transitsystem.base;

public class SocketApiRespnose {
    private String api;
    private String code;
    private String msg;
    private Integer respNo;
    private Long respDate;
    private Object data;

    public SocketApiRespnose() {
        this.respDate = System.currentTimeMillis();
    }

    public SocketApiRespnose(ResultEnum resultEnum,String api,Integer respNo) {
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getMsg();
        this.respNo = respNo;
        this.respDate = System.currentTimeMillis();
        this.api = api;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getRespNo() {
        return respNo;
    }

    public void setRespNo(Integer respNo) {
        this.respNo = respNo;
    }

    public Long getRespDate() {
        return respDate;
    }

    public void setRespDate(Long respDate) {
        this.respDate = respDate;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SocketApiRespnose{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", respNo=" + respNo +
                ", respDate=" + respDate +
                ", data=" + data +
                '}';
    }
}
