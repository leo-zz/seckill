package com.leozz.util.cache;

import com.leozz.dao.UserMapper;
import com.leozz.entity.SecActivity;
import com.leozz.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: leo-zz
 * @Date: 2019/3/18 14:47
 */
@Component
public class UserLocalCache {

    @Autowired
    UserMapper userMapper;

    private Map<Long, User> userMap = null;


    /**
     *  先从缓存中查找，如果没有再查数据库，并放入缓存中，不考虑用户信息的修改
     *  TODO 后期要考虑用户过多时，放到分布式缓存中，以及数据过期策略（使用LRU缓存）
     * @param userId
     * @return
     */
    public User selectUserById(Long userId) {
        if(userId==null)return null;
        if(userMap ==null){
            userMap =new ConcurrentHashMap<>();
        }
        //先从缓存中查找，ConcurrentHashMap中的key不能为null，因此get前要确保userId不会null
        User user = userMap.get(userId);
        if(user==null){
            //缓存中没有再查数据库
            user= userMapper.selectByPrimaryKey(userId);
            //如果数据库差不到用户信息，返回null
            if(user==null)return null;
            userMap.put(userId,user);
        }
        return user;
    }

    /**
     * 冻结用户的积分，由于用户积分变动不频繁,不再批量提交
     * @param userId
     * @return
     */
    public int updateUserPointById(Long userId) {
        User user = userMap.get(userId);
        Integer blockedMembershipPoint = user.getBlockedMembershipPoint();
        HashMap<String, Object> paraMap = new HashMap<>();
        paraMap.put("userId",userId);
        paraMap.put("blockedPoint",blockedMembershipPoint);
        return userMapper.updateBlockedPointById(paraMap);
    }
}
