package com.leozz.dto;

import com.leozz.entity.Coupon;
import com.leozz.entity.DeliveryAddr;

import java.util.List;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 8:50
 */
public class PreSubmitOrderDTO {
    //秒杀活动预下单参与结果。
    private boolean result = false;
    private String msg;

    //秒杀活动信息，商品信息省略
    private Long countDownTime;   //倒计时，单位是ms
    private int stockPercent;   //库存百分比

    //收件人信息
    private DeliveryAddr deliveryAddr;

    //优惠券信息
    private List<CouponDTO> coupons;
//    private Coupon fullrangeCoupon;//全品类券
//    private Coupon coupon;//普通券

    //积分信息
    private int point;


    public PreSubmitOrderDTO() {
    }

    public PreSubmitOrderDTO(boolean result, String msg) {
        this.result = result;
        this.msg = msg;
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

    public Long getCountDownTime() {
        return countDownTime;
    }

    public void setCountDownTime(Long countDownTime) {
        this.countDownTime = countDownTime;
    }

    public int getStockPercent() {
        return stockPercent;
    }

    public void setStockPercent(int stockPercent) {
        this.stockPercent = stockPercent;
    }

    public DeliveryAddr getDeliveryAddr() {
        return deliveryAddr;
    }

    public void setDeliveryAddr(DeliveryAddr deliveryAddr) {
        this.deliveryAddr = deliveryAddr;
    }

    public List<CouponDTO> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<CouponDTO> coupons) {
        this.coupons = coupons;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }
}
