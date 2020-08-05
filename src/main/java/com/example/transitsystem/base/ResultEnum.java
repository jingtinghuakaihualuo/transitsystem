package com.example.transitsystem.base;

public enum ResultEnum {
    SUCCESS("0000", "成功"),
    FAIL("0001", "失败"),
    REQUESTPARAMERROR("0002", "请求参数非法"),
    NOTONLINE("0003","设备不在线"),
    EQUIPMENTNOTFUND("0004", "未找到设备"),
    DATABASEERROR("0005", "数据库错误"),
    EQUIPMENTNOTEXIST("0006", "设备不存在"),
    RESPONSETIMEOUT("0007", "响应超时"),
    SYSTEMEXCEPTION("8888", "系统异常");

    ResultEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private String code;
    private String msg;

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
}
