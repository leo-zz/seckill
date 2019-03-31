package com.leozz.util;

import com.leozz.entity.SecActivity;
import com.leozz.util.cache.ActivitiesLocalCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * @Author: leo-zz
 * @Date: 2019/3/15 10:11
 */
@Component
public class FlowLimiter {
    @Autowired
    ActivitiesLocalCache activitiesLocalCache;

    private final Integer limit_mulitple=2;

    private  Map<Long,Semaphore> limiter=new ConcurrentHashMap<>();

    //为指定的活动添加限流器
    //TODO 活动结束后要能够自动回收对应的限流器
    public  Semaphore getLimiter(Long secActivityId,Integer seckillCount){
        boolean b = limiter.containsKey(secActivityId);
        if(b){
            return limiter.get(secActivityId);
        }else{
            //TODO 要考虑加锁的情况，防止并发创建限流器。
            SecActivity secActivity = activitiesLocalCache.getSecActivityById(secActivityId);
            synchronized (secActivity){
                //加锁后重新检测是否已经创建限流器
                if(limiter.containsKey(secActivityId))
                    return limiter.get(secActivityId);
                else {
                    Semaphore semaphore = new Semaphore(seckillCount * limit_mulitple);
                    limiter.put(secActivityId,semaphore);
                    return semaphore;
                }
            }
        }
    }


}
