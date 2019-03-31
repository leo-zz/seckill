package com.leozz.entity;

import java.util.Date;

public class PointRecord {
    private Long id;

    private Long activityId;

    private Long userId;

    /**
     * 变动原因，0购物奖励，1购物扣除，2退货退还，3退货取消，4签到奖励
     */
    private Byte cause;

    /**
     * 积分变动状态，0冻结中，1已确认，2已撤销
     */
    private Byte status;

    private Integer updateAmount;

    private Date updateDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Byte getCause() {
        return cause;
    }

    public void setCause(Byte cause) {
        this.cause = cause;
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