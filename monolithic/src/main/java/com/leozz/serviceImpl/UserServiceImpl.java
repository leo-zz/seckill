package com.leozz.serviceImpl;

import com.leozz.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 10:12
 */
@Service
public class UserServiceImpl implements UserService {
    @Override
    public int getAvaliableMembershipPoint(Long userId) {
        return 0;
    }

    @Override
    public boolean frozenMembershipPoint(Long userId, int point) {
        return false;
    }

    @Override
    public boolean deductMembershipPoint(Long userId, int point) {
        return false;
    }

    @Override
    public boolean addMembershipPoint(Long userId, int point) {
        return false;
    }
}
