package com.leozz.service;

import com.leozz.entity.Goods;

import java.util.List;

/**
 * @Author: leo-zz
 * @Date: 2019/3/13 9:45
 */
public interface GoodsService {

    /**
     *  获取商品信息，包含商品标题、封面、标签等信息
     * @return
     */
    List<Goods> getGoodsList();

    /**
     *  更新商品库存流程，在秒杀活动结束后，将秒杀活动中卖出的商品扣除，
     *  未卖出的商品更新到库存中，恢复秒杀商品冻结的库存信息。
     *  如果活动还未结束，不能调用此方法。
     * @param activityId 活动id
     * @return 是否扣除成功
     */
    boolean updateGoodsStockAfterActivity(Long activityId);

    //新增商品信息

    //修改商品信息



}
