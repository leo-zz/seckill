package com.leozz.service;


import com.leozz.entity.PointRecord;

/**
 * @Author: leo-zz
 * @Date: 2019/3/13 10:50
 */
public interface PointRecordService {

    /**
     *  用户购物时可以使用积分抵现，积分扣除；
     *  购物成功时奖励积分，积分增加；
     *  @param record 变更记录的实体信息
     *  @return 变更记录是否记录成功
     */
    boolean recordMembershipPointChange(PointRecord record);

}
