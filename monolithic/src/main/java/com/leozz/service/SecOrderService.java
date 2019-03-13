package com.leozz.service;

/**
 * @Author: leo-zz
 * @Date: 2019/3/13 10:50
 */
public interface SecOrderService {


    /**
     *  检查用户是否已经参与过指定活动。
     *  确保一次秒杀活动中，同一个用户只能参与一次
     * @param secActivityId 活动编号的id
     * @param userId 用户Id
     * @return true表示用户已经下单，flase表示用户未下单
     */
    boolean hasUserPlacedtheOrder(Long secActivityId,Long userId);


    int submittheOrder(Long secActivityId,Long userId);


}
