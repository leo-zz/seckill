package com.leozz.serviceImpl;

import com.leozz.dto.ResultDTO;
import com.leozz.dto.SecActivityDTO;
import com.leozz.entity.Goods;
import com.leozz.entity.SecActivity;
import com.leozz.service.SecActivityService;
import com.leozz.util.FlowLimiter;
import com.leozz.util.cache.ActivitiesLocalCache;
import com.leozz.util.cache.OrderLocalCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 10:11
 */
@Service("secActivityService")
public class SecActivityServiceImpl implements SecActivityService {



    @Autowired
    FlowLimiter flowLimiter;

    @Autowired
    ActivitiesLocalCache activitiesLocalCache;

    @Autowired
    OrderLocalCache orderLocalCache;

    @Autowired
    SecActivityService secActivityService;

    @Override
    public boolean beginSecActivityList(SecActivity activity) {
        return false;
    }

    @Override
    public List<SecActivityDTO> getSecActivityList(Long userId) {
        //查询开始前后1小时，且状态为1,2,3的秒杀活动
        //TODO 使用缓存提升速度，使用定时任务更新秒杀活动的状态？
        List<SecActivity> secActivityList = activitiesLocalCache.getActivityList();
        List<SecActivityDTO> secActivityDTOS = new ArrayList<>(secActivityList.size());

        //遍历所有秒杀活动
        for (SecActivity secActivity : secActivityList) {
            SecActivityDTO secActivityDTO = new SecActivityDTO();
            Long activityId = secActivity.getId();

            //获取活动商品的信息
            Goods goods = activitiesLocalCache.getGoodsByActivityId(activityId);
            secActivityDTO.setGoodsImg(goods.getGoodsImg());
            secActivityDTO.setGoodsPrice(goods.getGoodsPrice().doubleValue());
            secActivityDTO.setGoodsTitle(goods.getGoodsTitle());
            secActivityDTO.setSeckillPrice(secActivity.getSeckillPrice().doubleValue());
            secActivityDTO.setStockPercent(secActivity.getStockPercent().byteValue());

            //一次秒杀活动中，同一个用户只能参与一次。
            boolean b = secActivityService.hasUserPartaked(activityId, userId);
            if (b) {
                //如果用户已参与过，则给活动增加已参与标签。
                secActivityDTO.setButtonContent("已参与");
                secActivityDTO.setClickable(false);
            } else {
                switch (secActivity.getStatus()) {
                    case 1:
                        secActivityDTO.setButtonContent("未开始");
                        secActivityDTO.setClickable(false);
                        break;
                    case 2:
                        secActivityDTO.setButtonContent("抢购");
                        secActivityDTO.setClickable(true);
                        break;
                    case 3:
                        secActivityDTO.setButtonContent("已抢完");
                        secActivityDTO.setClickable(true);
                        break;
                    default:
                        secActivityDTO.setButtonContent("已结束");
                        secActivityDTO.setClickable(false);
                        break;
                }
            }
            secActivityDTOS.add(secActivityDTO);
        }
        return secActivityDTOS;
    }

    @Override
    public ResultDTO partakeSecActivity(Long secActivityId, Long userId) {
        SecActivity secActivity = activitiesLocalCache.getSecActivityById(secActivityId);
        // 当前用户是否参与过此活动，一次秒杀活动中，同一个用户只能参与一次。
        boolean b = secActivityService.hasUserPartaked(secActivityId, userId);
        if (b) {
            //如果用户已参与过，则给活动增加已参与标签。
            return new ResultDTO(false,"一位用户只能参与一次。");
        }
        // 使用漏桶算法进行限流，允许秒杀商品梳理3倍的人数参与抢购，比如100件商品最多允许300人进入下单页面
        Integer seckillCount = secActivity.getSeckillCount();
        Semaphore limiter = flowLimiter.getLimiter(secActivityId, seckillCount);
        try {
            limiter.acquire();
            // 重新检查秒杀活动的状态
            //TODO 活动的状态如何能及时更新？定时任务？
            //TODO 如何处理并发问题？
            switch (secActivity.getStatus()) {
                //未开始
                case 1:
                    //检测活动是否已经开始，确保时间到达后，第一个请求进入时更新活动状态，后续请求不再重新更新状态，
                    // 且确保第一个请求能够公平地进行后续请求。
                    if(System.currentTimeMillis() > secActivity.getStartDate().getTime()){

                        activitiesLocalCache.updateStatusById(secActivityId,(byte)2);
                    }
                    return new ResultDTO(false,"未开始");
                //已抢完
                case 2:
                    // 检查库存是否充足
                    // TODO 此处不用增加措施防止超卖？加锁？原子性？
                    Integer stockCount = secActivity.getSeckillCount();
                    Integer blockedStockCount=secActivity.getSeckillBlockedStock();
                    if(stockCount>blockedStockCount){
                        return new ResultDTO(true);
                    }else{
                        return new ResultDTO(false,"已抢完");
                    }
                 //
                case 3:
                    return new ResultDTO(false,"已抢完");
                default:
                    return new ResultDTO(false,"已结束");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new ResultDTO(false,"活动太火爆，请稍后重试");
        }
    }


    @Override
    public boolean hasUserPartaked(Long activityId, Long userId) {
        //查询用户是否存在秒杀活动对应的订单
        //TODO 需要加锁，避免下单后数据库更新订单的时间差内，有用户重复下单；比如给用户添加一个秒杀锁，一个用户只能参与一个秒杀活动。
        return  orderLocalCache.selectByUserAndActivity(activityId, userId);
    }


    @Override
    public boolean checkSecActivityStatusAndStock(Long secActivityId, Long userId) {
        return false;
    }

    @Override
    public boolean frozenGoodsStock() {
        return false;
    }

    @Override
    public boolean deductGoodsStock() {
        return false;
    }


}
