package com.leozz.util.cache;

import com.leozz.dao.GoodsMapper;
import com.leozz.dao.SecActivityMapper;
import com.leozz.entity.Goods;
import com.leozz.entity.SecActivity;
import com.leozz.util.UserDefThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring容器中的单体对象，并不是单例模式。
 * 秒杀活动的本地缓存，存放秒杀活动对象，用于对不同的活动进行加锁；
 * 要践行：先读缓存再读数据库，先写数据库再写缓存。
 *
 * @Author: leo-zz
 * @Date: 2019/3/15 10:20
 */
@Component
public class ActivitiesLocalCache {

    private final static Logger logger = LoggerFactory.getLogger(ActivitiesLocalCache.class);

    @Autowired
    private SecActivityMapper secActivityMapper;

    @Autowired
    GoodsMapper goodsMapper;

    private List<SecActivity> secActivityList = null;
    private Map<Long, SecActivity> secActivityMap = null;
    private Map<Long, Goods> goodsMap = null;


    private final int UpdateInterval = 1000;

    private Long updateTimeStramp = 0L;


    private volatile boolean updatedFlag = false;


    public List<SecActivity> getActivityList() {

        if (secActivityMap == null) {
            //第一次获取列表时，刷新本地缓存
            secActivityList = secActivityMapper.selectRecentActivityList();
            secActivityMap = new ConcurrentHashMap<>(secActivityList.size());
            goodsMap = new ConcurrentHashMap<>(secActivityList.size());
            UserDefThreadPool.jobExecutor.execute(() -> {
                updateCache(secActivityList);
            });
        } else {
            //检查是否需要更新缓存
            CheckIsUpdated();
        }
        return secActivityList;
    }

    public SecActivity getSecActivityById(Long secActivityId) {
        if (secActivityMap == null) {
            //第一次获取列表时，刷新本地缓存
            secActivityList = secActivityMapper.selectRecentActivityList();
            secActivityMap = new ConcurrentHashMap<>(secActivityList.size());
            goodsMap = new ConcurrentHashMap<>(secActivityList.size());
            updateCache(secActivityList);
        } else {
            //检查是否需要更新缓存
            CheckIsUpdated();
        }
        return secActivityMap.get(secActivityId);
    }

    private void CheckIsUpdated() {
        //超过1s未更新时，刷新本地缓存
        if (System.currentTimeMillis() > (updateTimeStramp + UpdateInterval)) {
            //如果当前未刷新完毕，则等待刷新。
            int count = 5;
            while (--count > 0) {
                if (updatedFlag) {
                    updatedFlag = false;
                    secActivityList = secActivityMapper.selectRecentActivityList();
                    UserDefThreadPool.jobExecutor.execute(() -> {
                        updateCache(secActivityList);
                    });
                    break;
                } else {
                    //如果当前处于刷新状态，那么等待刷新完毕然后返回
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (count <= 0)
                logger.error("获取最新活动列表失败。");
        }
    }


    //加锁，确保同时只能更新一次
    //每次刷新缓存都是重新覆盖，有无优化空间？
    public void updateCache(List<SecActivity> secActivityList) {
        synchronized (this) {
            //问题1：每次刷新前都会清空，可能会导致其他地方出现空指针问题？
            //secActivityMap.clear();
            ConcurrentHashMap<Long, SecActivity> activityMapBack = new ConcurrentHashMap<>(secActivityList.size());
            ConcurrentHashMap<Long, Goods> goodsMapBack = new ConcurrentHashMap<>(secActivityList.size());
            secActivityList.forEach(secActivity -> {
                Long id = secActivity.getId();
                activityMapBack.put(id, secActivity);
                Long goodsId = secActivity.getGoodsId();
                //刷新商品信息
                if(!goodsMapBack.containsKey(goodsId)){
                    if(goodsMap.containsKey(goodsId)){
                        goodsMapBack.put(id,goodsMap.get(id));
                    }else{
                        goodsMapBack.put(id,goodsMapper.selectByPrimaryKey(id));
                    }
                }

            });
            //为解决问题1引入的策略：原始的secActivityMap需要GC回收，可能会增大GC压力
            secActivityMap = activityMapBack;
            goodsMap= goodsMapBack;
            updatedFlag = true;
            updateTimeStramp = System.currentTimeMillis();
        }
    }

    public void updateStatusById(Long secActivityId, byte i) {
        secActivityMap.get(secActivityId).setStatus(i);
        //先写入缓存再存入数据库，顺序对吗？
        secActivityMapper.updateStatusById(secActivityId,i);
    }

    //秒杀活动中都是先拿到活动列表，才会拿取商品，因此不考虑goodsMap=null的情况。
    public Goods getGoodsByActivityId(Long id) {
        return goodsMap.get(id);
    }

    //加锁
    public void updateById(Long activityId) {
    }
}
