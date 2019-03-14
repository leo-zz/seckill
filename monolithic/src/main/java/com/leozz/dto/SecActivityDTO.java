package com.leozz.dto;

import com.leozz.entity.SecActivity;

import java.util.List;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 8:45
 */
public class SecActivityDTO {

    private String goodsImg;//商品图片
    private String goodsTitle;//商品标题
    private double goodsPrice;//商品售价
    private double seckillPrice;//秒杀价格
    private byte stockPercent;//范围在0-100，选择byte类型节省空间
    private boolean isClickable;//是否可以抢购
    private String buttonContent;//按钮上的内容

    public double getSeckillPrice() {
        return seckillPrice;
    }

    public void setSeckillPrice(double seckillPrice) {
        this.seckillPrice = seckillPrice;
    }

    public String getGoodsImg() {
        return goodsImg;
    }

    public void setGoodsImg(String goodsImg) {
        this.goodsImg = goodsImg;
    }

    public String getGoodsTitle() {
        return goodsTitle;
    }

    public void setGoodsTitle(String goodsTitle) {
        this.goodsTitle = goodsTitle;
    }

    public double getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(double goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    public byte getStockPercent() {
        return stockPercent;
    }

    public void setStockPercent(byte stockPercent) {
        this.stockPercent = stockPercent;
    }

    public boolean isClickable() {
        return isClickable;
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }

    public String getButtonContent() {
        return buttonContent;
    }

    public void setButtonContent(String buttonContent) {
        this.buttonContent = buttonContent;
    }
}
