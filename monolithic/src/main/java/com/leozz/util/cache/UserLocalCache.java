package com.leozz.util.cache;

import com.leozz.dao.UserMapper;
import com.leozz.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: leo-zz
 * @Date: 2019/3/18 14:47
 */
@Component
public class UserLocalCache {

    @Autowired
    UserMapper userMapper;


    public User selectUserById(Long userId) {
        //先从缓存中查找

        //缓存中没有再查数据库
        return  userMapper.selectByPrimaryKey(userId);

    }

    public void updateUserPointById(Long userId) {
    }
}
