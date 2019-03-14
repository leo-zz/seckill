package com.leozz.serviceImpl;

import com.leozz.dao.GoodsMapper;
import com.leozz.dao.SecActivityMapper;
import com.leozz.dto.ResultDTO;
import com.leozz.dto.SecActivityListPage;
import com.leozz.dto.SecActivityDTO;
import com.leozz.entity.Goods;
import com.leozz.entity.SecActivity;
import com.leozz.service.SecActivityService;
import com.leozz.service.SecOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 10:11
 */
@Service("secActivityService")
public class SecActivityServiceImpl implements SecActivityService {


    @Autowired
    SecActivityMapper secActivityMapper;

    @Autowired
    GoodsMapper goodsMapper;

    @Autowired
    SecOrderService secOrderService;

    @Override
    public boolean beginSecActivityList(SecActivity activity) {
        return false;
    }

    @Override
    public List<SecActivityDTO> getSecActivityList(Long userId) {
        //查询开始前后1小时，且状态为1,2,3的秒杀活动
        //TODO 使用缓存提升速度，需要有定时任务更新秒杀活动的状态
        List<SecActivity> secActivityList = secActivityMapper.selectRecentActivityList();
        List<SecActivityDTO> secActivityDTOS = new ArrayList<>(secActivityList.size());

        //遍历所有秒杀活动
        for (SecActivity secActivity : secActivityList) {
            SecActivityDTO secActivityDTO = new SecActivityDTO();

            //获取活动商品的信息
            Goods goods = goodsMapper.selectByPrimaryKey(secActivity.getId());
            secActivityDTO.setGoodsImg(goods.getGoodsImg());
            secActivityDTO.setGoodsPrice(goods.getGoodsPrice().doubleValue());
            secActivityDTO.setGoodsTitle(goods.getGoodsTitle());
            secActivityDTO.setSeckillPrice(secActivity.getSeckillPrice().doubleValue());
            secActivityDTO.setStockPercent(secActivity.getStockPercent());

            //一次秒杀活动中，同一个用户只能参与一次。
            boolean b = secOrderService.hasUserPlacedOrder(secActivity.getId(), userId);
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
        SecActivity secActivity = secActivityMapper.selectByPrimaryKey(secActivityId);

        // 当前用户是否参与过此活动

        // 使用漏桶算法进行限流，比如100件商品只允许1000人进入下单页面




        // 重新检查秒杀活动的状态
        //TODO 活动的状态如何能及时更新？定时任务？
//        Date endDate = secActivity.getEndDate();
//
        //TODO 如何处理并发问题？
        switch (secActivity.getStatus()) {
            case 1:
                //检测活动是否已经开始
                if(System.currentTimeMillis() > secActivity.getStartDate().getTime()){
                    secActivityMapper.updateStatus(2);
                }
                return new ResultDTO(false,"未开始");
            case 2:
                // 检查库存是否充足
                // TODO 此处不用增加措施防止超卖？加锁？原子性？
                Integer stockCount = secActivity.getStockCount();
                Integer blockedStockCount=secActivity.getBlockedStockCount();
                if(stockCount>blockedStockCount){
                    return new ResultDTO(true);
                }else{
                    return new ResultDTO(false,"已抢完");
                }
            case 3:
                return new ResultDTO(false,"已抢完");
            default:
                return new ResultDTO(false,"已结束");
        }



        return false;
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
