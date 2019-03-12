package com.leozz.entity;

import java.util.Date;

public class ScoreRecord {
    private Long id;

    private Byte type;

    private Integer updateAmount;

    private Date updateDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public Integer getUpdateAmount() {
        return updateAmount;
    }

    public void setUpdateAmount(Integer updateAmount) {
        this.updateAmount = updateAmount;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}