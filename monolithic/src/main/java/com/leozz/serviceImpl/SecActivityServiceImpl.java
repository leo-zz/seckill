package com.leozz.serviceImpl;

import com.leozz.dto.ResultDTO;
import com.leozz.dto.SecActivityDTO;
import com.leozz.entity.Goods;
import com.leozz.entity.SecActivity;
import com.leozz.service.SecActivityService;
import com.leozz.util.FlowLimiter;
import com.leozz.util.TimeRecorder;
import com.leozz.util.cache.ActivitiesLocalCache;
import com.leozz.util.cache.OrderLocalCache;
import io.swagger.models.auth.In;
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
    public ResultDTO<List<SecActivityDTO>> getSecActivityList(Long userId) {
        //查询开始前后1小时，且状态为1,2,3的秒杀活动
        //TODO 使用缓存提升速度，使用定时任务更新秒杀活动的状态？
        List<SecActivity> secActivityList = activitiesLocalCache.getActivityList();
        if (secActivityList.size() <= 0) {
            throw new RuntimeException("目前没有秒杀活动");
        }
        List<SecActivityDTO> activityDTOS = getSecActivityDTOByEntity(userId, secActivityList);
        return new ResultDTO<List<SecActivityDTO>>(true,activityDTOS, "获取成功");
    }

    @Override
    public ResultDTO partakeSecActivity(Long secActivityId, Long userId) {
        SecActivity secActivity = activitiesLocalCache.getSecActivityById(secActivityId);
        if (secActivity == null) {
            return new ResultDTO(false, "活动不存在");
        }
        return checkActivityStatusAndStock(userId, secActivity);
    }


    @Override
    public boolean hasUserPartaked(Long activityId, Long userId) {
        //查询用户是否存在秒杀活动对应的订单
        //TODO 需要加锁，避免下单后数据库更新订单的时间差内，有用户重复下单；比如给用户添加一个秒杀锁，一个用户只能参与一个秒杀活动。
        return orderLocalCache.selectByUserAndActivity(activityId, userId);
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

    /**
     * 遍历活动列表，生成页面需要的DTO
     * @param userId
     * @param secActivityList
     * @return
     */
    private List<SecActivityDTO> getSecActivityDTOByEntity(Long userId, List<SecActivity> secActivityList) {
        List<SecActivityDTO> secActivityDTOS = new ArrayList<>(secActivityList.size());

        //遍历所有秒杀活动
        for (SecActivity secActivity : secActivityList) {
            SecActivityDTO secActivityDTO = new SecActivityDTO();
            Long activityId = secActivity.getId();

            //跟进商品id获取活动商品的信息
            Goods goods = activitiesLocalCache.getGoodsByActivityId(secActivity.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品信息错误");
            }
            secActivityDTO.setActivityId(activityId);
            secActivityDTO.setGoodsImg(goods.getGoodsImg());
            secActivityDTO.setGoodsPrice(goods.getGoodsPrice().doubleValue());
            secActivityDTO.setGoodsTitle(goods.getGoodsTitle());
            secActivityDTO.setSeckillPrice(secActivity.getSeckillPrice().doubleValue());

            setActivityStatusAndStock(userId, secActivity, secActivityDTO);
            secActivityDTOS.add(secActivityDTO);
        }
        return secActivityDTOS;
    }

    /**
     * 计算活动库存百分比，推断活动状态，并注入到secActivityDTO中。
     * @param userId
     * @param secActivity
     * @param secActivityDTO
     */
    private void setActivityStatusAndStock(Long userId, SecActivity secActivity, SecActivityDTO secActivityDTO) {
        Long activityId=secActivity.getId();
        Integer seckillBlockedStock = secActivity.getSeckillBlockedStock();
        Integer seckillStock = secActivity.getSeckillStock();

        int saleableCount = seckillStock - seckillBlockedStock;

        //TODO 如果库存显示要求不精确的，可以不用每次都计算。
        secActivityDTO.setStockPercent(calcStockPercent(secActivity));

        long startTime = secActivity.getStartDate().getTime();
        long endTime = secActivity.getEndDate().getTime();
        long now = TimeRecorder.accessTime.get();
        if (now < startTime) {
            secActivityDTO.setButtonContent("未开始");
            secActivityDTO.setClickable(false);
        } else if (now >= endTime) {
            secActivityDTO.setButtonContent("已结束");
            secActivityDTO.setClickable(false);
        } else {
            if (saleableCount > 0) {
                //一次秒杀活动中，同一个用户只能参与一次。
                boolean b = secActivityService.hasUserPartaked(activityId, userId);
                if (b) {
                    //如果用户已参与过，则给活动增加已参与标签。
                    secActivityDTO.setButtonContent("已参与");
                    secActivityDTO.setClickable(false);
                } else {
                    secActivityDTO.setButtonContent("抢购中");
                    secActivityDTO.setClickable(true);
                }
            } else if (seckillBlockedStock > 0) {
                secActivityDTO.setButtonContent("有用户未付款，还有机会，请刷新");
                secActivityDTO.setClickable(false);
            } else {
                secActivityDTO.setButtonContent("已结束");
                secActivityDTO.setClickable(false);
            }

        }
    }

    /**
     * 检查活动状态是否在抢购中，如果在抢购中则检查活动库存是否充足，此处使用了限流器防止服务器流量过大。
     * @param userId
     * @param secActivity
     * @return
     */
    private ResultDTO checkActivityStatusAndStock(Long userId, SecActivity secActivity) {
        //增加活动状态的检测，只有抢购中状态下的商品能够继续操作
        long startTime = secActivity.getStartDate().getTime();
        long endTime = secActivity.getEndDate().getTime();
        long now = TimeRecorder.accessTime.get();

        Long secActivityId=secActivity.getId();

        if (now < startTime) {
            //可以跟进此行为辨识用户是否借助软件秒杀
            return new ResultDTO(false, "活动未开始");
        } else if (now >= endTime) {
            return new ResultDTO(false, "活动已结束");
        } else {
            Integer seckillBlockedStock = secActivity.getSeckillBlockedStock();
            Integer seckillStock = secActivity.getSeckillStock();

            int saleableCount = seckillStock - seckillBlockedStock;
            if (saleableCount > 0) {
                //"抢购中"
                // 当前用户是否参与过此活动，一次秒杀活动中，同一个用户只能参与一次。
                boolean b = secActivityService.hasUserPartaked(secActivityId, userId);
                if (b) {
                    //如果用户已参与过，则给活动增加已参与标签。
                    return new ResultDTO(false, "只能参与一次。");
                }
                return checkStockByLimiter(secActivity);

            } else if (seckillBlockedStock > 0) {
                return new ResultDTO(false, "活动火爆，请重试");
            }else{
                return new ResultDTO(false, "商品已售完");
            }
        }
    }

    /**
     * 检查活动库存是否充足，此处使用了限流器防止服务器流量过大
     * @param secActivity
     * @return
     */
    private ResultDTO checkStockByLimiter(SecActivity secActivity) {
        // 使用漏桶算法进行限流，允许秒杀商品梳理3倍的人数参与抢购，比如100件商品最多允许300人进入下单页面
        Integer seckillCount = secActivity.getSeckillCount();
        long secActivityId=secActivity.getId();
        Semaphore limiter = flowLimiter.getLimiter(secActivityId, seckillCount);
        try {
            limiter.acquire();
            // 重新检查秒杀活动的状态
            //活动的状态如何能及时更新？
            //TODO 如何处理并发问题？对活动加锁。
            synchronized (secActivity) {
                //重新获取库存
                secActivity = activitiesLocalCache.getSecActivityById(secActivityId);
                Integer stockCount = secActivity.getSeckillCount();
                Integer blockedStockCount = secActivity.getSeckillBlockedStock();
                if (stockCount > blockedStockCount) {
                    return new ResultDTO(true);
                } else {
                    return new ResultDTO(false, "已抢完");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new ResultDTO(false, "活动太火爆，请稍后重试");
        }
    }

    /**
     * 计算活动的可卖库存百分比
     * @param secActivity
     * @return
     */
    public byte calcStockPercent(SecActivity secActivity) {
        Integer seckillCount = secActivity.getSeckillCount();
        Integer seckillStock = secActivity.getSeckillStock();
        Integer seckillBlockedStock = secActivity.getSeckillBlockedStock();
        Integer i = (seckillStock - seckillBlockedStock) * 100 / seckillCount;
        return i.byteValue();
    }
}
