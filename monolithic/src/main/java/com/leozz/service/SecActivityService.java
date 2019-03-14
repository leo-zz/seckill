package com.leozz.service;

import com.leozz.dto.SecActivityListPage;
import com.leozz.dto.SecActivityDTO;
import com.leozz.entity.SecActivity;

import java.util.List;

/**
 * @Author: leo-zz
 * @Date: 2019/3/13 10:43
 */
public interface SecActivityService {

    //定时任务或者管理员发起秒杀活动，发起秒杀活动后无论活动是否开始，都应该先冻结对应商品的库存
    //避免活动开始时商品数量不足
    boolean beginSecActivityList(SecActivity activity);


    /**
     *  获取当前秒杀活动列表，每条信息包括活动商品信息{@link GoodsService#getGoodsList()}，
     *  活动库存百分比与活动状态等内容。
     *  一次秒杀活动中，同一个用户只能参与一次{@link SecOrderService#hasUserPlacedOrder(java.lang.Long, java.lang.Long)}
     * @param userId 用户id
     * @return  秒杀活动列表
     */

    List<SecActivityDTO> getSecActivityList(Long userId);

    /**
     *  点击抢购按钮后触发的逻辑，重新检查秒杀活动的状态、库存，以及当前用户是否参与过此活动{@link SecActivityService#partakeSecActivity(java.lang.Long, java.lang.Long)}；
     *  在此处使用漏桶算法进行限流，比如100件商品只允许1000人进入下单页面。
     * @param secActivityId 活动编号的id
     * @param userId 用户id
     * @return 如果并发请求数超过限流阈值，或者活动结束，或者库存不足，则返回false，
     *  否则返回true，表示可以进入订单创建环节。
     */
    boolean partakeSecActivity(Long secActivityId, Long userId);

    /**
     * 根据活动编号的id，查询秒杀活动的状态和库存,一次秒杀活动中，同一个用户只能参与一次{@link SecOrderService#hasUserPlacedOrder(java.lang.Long, java.lang.Long)}
     * 注意，如何高效的查询库存状态，防止超卖
     * @param secActivityId 活动编号的id
     * @param userId 用户id
     * @return true表示活动未结束，并且库存充足，否则返回false。
     */
    boolean checkSecActivityStatusAndStock(Long secActivityId,Long userId);


    //冻结秒杀活动中的商品库存，利用缓存，以批量提交的方式减轻数据库压力。
    boolean frozenGoodsStock();

    //扣减已冻结的秒杀活动中的商品库存，利用缓存，以批量提交的方式减轻数据库压力。
    boolean deductGoodsStock();
}
