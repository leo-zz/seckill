package com.leozz.service;

/**
 * @Author: leo-zz
 * @Date: 2019/3/13 10:50
 */
public interface UserService {

    /**
     *  获取用户可用的积分分值。
     * @param  userId 用户Id
     * @return 用户积分分值
     */
    int  getAvaliableMembershipPoint(Long userId);

    //冻结用户积分
    boolean frozenMembershipPoint(Long userId,int point);

    //扣减冻结用户积分
    boolean deductMembershipPoint(Long userId,int point);

    //增加积分
    boolean addMembershipPoint(Long userId,int point);
}
