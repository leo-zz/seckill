package com.leozz.util.cache;

import com.leozz.dao.PointRecordMapper;
import com.leozz.dao.UserMapper;
import com.leozz.entity.PointRecord;
import com.leozz.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO 用户缓存也需要更新，因为涉及到积分
 *
 * @Author: leo-zz
 * @Date: 2019/3/18 14:47
 */
@Component
public class UserLocalCache {

    @Autowired
    UserMapper userMapper;

    @Autowired
    PointRecordMapper pointRecordMapper;

    private Map<Long, User> userMap = null;


    /**
     * 先从缓存中查找，如果没有再查数据库，并放入缓存中，不考虑用户信息的修改
     * TODO 后期要考虑用户过多时，放到分布式缓存中，以及数据过期策略（使用LRU缓存）
     *
     * @param userId
     * @return
     */
    public User selectUserById(Long userId) {
        if (userId == null) return null;
        if (userMap == null) {
            userMap = new ConcurrentHashMap<>();
        }
        //先从缓存中查找，ConcurrentHashMap中的key不能为null，因此get前要确保userId不会null
        User user = userMap.get(userId);
        if (user == null) {
            //缓存中没有再查数据库
            user = userMapper.selectByPrimaryKey(userId);
            //如果数据库差不到用户信息，返回null
            if (user == null) return null;
            userMap.put(userId, user);
        }
        return user;
    }

    /**
     * 冻结用户的积分，由于用户积分变动不频繁,不再批量提交
     * 冻结积分的同时，往积分记录表中插入变动原因
     *
     * @param userId
     * @param record
     * @return
     */
    public int frozenUserPointById(Long userId, PointRecord record) {
        User user = userMap.get(userId);
        Integer blockedMembershipPoint = user.getBlockedMembershipPoint();
        HashMap<String, Object> paraMap = new HashMap<>();
        paraMap.put("userId", userId);
        paraMap.put("blockedPoint", blockedMembershipPoint);
        int i = userMapper.updateBlockedPointById(paraMap);
        int i1 = pointRecordMapper.insertSelective(record);
        if(i==i1&i==1){
            return 1;
        }else {
            return 0;
        }
    }

    public int deductUserPointById(Long userId, PointRecord record) {
        User user = userMap.get(userId);
        Integer membershipPoint = user.getMembershipPoint();
        Integer blockedMembershipPoint = user.getBlockedMembershipPoint();
        HashMap<String, Object> paraMap = new HashMap<>();
        paraMap.put("userId", userId);
        paraMap.put("membershipPoint", membershipPoint);
        paraMap.put("blockedPoint", blockedMembershipPoint);
        int i = userMapper.updateBlockedPointById(paraMap);
        int i1 = pointRecordMapper.deductUserPointByEntity(record);
        if(i==i1&i==1){
            return 1;
        }else {
            return 0;
        }

    }

    public int updateUserLoginStateById(Long userId) {
        User user = userMap.get(userId);
        user.setLastLoginDate(new Date());
        user.setLoginCount(user.getLoginCount() + 1);
        return userMapper.updateByPrimaryKeySelective(user);
    }
}
