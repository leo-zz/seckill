package com.leozz.dto;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 8:50
 */
public class PreSubmitOrderDTO {
    //秒杀活动预下单参与结果。
    private boolean result =false;

    public PreSubmitOrderDTO(boolean result) {
        this.result = result;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
