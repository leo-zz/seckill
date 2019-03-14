package com.leozz.serviceImpl;

import com.leozz.entity.SecActivity;
import com.leozz.service.SecActivityService;

import java.util.List;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 10:11
 */
public class SecActivityServiceImpl implements SecActivityService {
    @Override
    public boolean beginSecActivityList(SecActivity activity) {
        return false;
    }

    @Override
    public List<SecActivity> getSecActivityList(Long userId) {
        return null;
    }

    @Override
    public boolean partakeSecActivity(Long secActivityId, Long userId) {
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
