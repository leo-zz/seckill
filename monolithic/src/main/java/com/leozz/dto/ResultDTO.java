package com.leozz.dto;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 17:34
 */
public class ResultDTO {

    boolean result;
    String msg;

    public ResultDTO(boolean result, String msg) {
        this.result = result;
        this.msg = msg;
    }

    public ResultDTO(boolean result) {
        this.result = result;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
