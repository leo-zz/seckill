package com.leozz.util.cache;

import com.leozz.dao.UserMapper;
import com.leozz.entity.SecActivity;
import com.leozz.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        if(userMap ==null){
            userMap =new ConcurrentHashMap<>();
        }
        //先从缓存中查找
        User user = userMap.get(userId);
        if(user==null){
            //缓存中没有再查数据库
            user= userMapper.selectByPrimaryKey(userId);
            userMap.put(userId,user);
        }
        return user;
    }

    public void updateUserPointById(Long userId) {
    }
}
