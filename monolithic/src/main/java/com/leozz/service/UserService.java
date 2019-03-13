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
}
