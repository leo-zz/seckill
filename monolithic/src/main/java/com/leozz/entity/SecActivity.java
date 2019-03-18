package com.leozz.entity;

import java.math.BigDecimal;
import java.util.Date;

public class SecActivity {
    private Long id;

    private Long goodsId;

    private BigDecimal seckillPrice;

    private Integer stockPercent;

    private Integer seckillCount;

    private Integer seckillStock;

    private Integer seckillBlockedStock;

    private Date startDate;

    private Date endDate;

    private Byte status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public BigDecimal getSeckillPrice() {
        return seckillPrice;
    }

    public void setSeckillPrice(BigDecimal seckillPrice) {
        this.seckillPrice = seckillPrice;
    }

    public Integer getStockPercent() {
        return stockPercent;
    }

    public void setStockPercent(Integer stockPercent) {
        this.stockPercent = stockPercent;
    }

    public Integer getSeckillCount() {
        return seckillCount;
    }

    public void setSeckillCount(Integer seckillCount) {
        this.seckillCount = seckillCount;
    }

    public Integer getSeckillStock() {
        return seckillStock;
    }

    public void setSeckillStock(Integer seckillStock) {
        this.seckillStock = seckillStock;
    }

    public Integer getSeckillBlockedStock() {
        return seckillBlockedStock;
    }

    public void setSeckillBlockedStock(Integer seckillBlockedStock) {
        this.seckillBlockedStock = seckillBlockedStock;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }
}