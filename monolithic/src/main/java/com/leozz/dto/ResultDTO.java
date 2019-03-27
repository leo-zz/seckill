package com.leozz.dto;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 17:34
 */
public class ResultDTO<T> {

    private boolean result;
    private T data;
    private String msg;

    public ResultDTO(boolean result, String msg) {
        this.result = result;
        this.msg = msg;
    }

    public ResultDTO(boolean result, T data, String msg) {
        this.result = result;
        this.data = data;
        this.msg = msg;
    }

    public ResultDTO(boolean result) {
        this.result = result;
    }

    public boolean isResult() {
        return result;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
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
