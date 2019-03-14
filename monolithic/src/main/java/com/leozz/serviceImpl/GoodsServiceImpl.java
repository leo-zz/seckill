package com.leozz.serviceImpl;

import com.leozz.entity.Goods;
import com.leozz.service.GoodsService;

import java.util.List;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 10:10
 */
public class GoodsServiceImpl implements GoodsService {
    @Override
    public List<Goods> getGoodsList() {
        return null;
    }

    @Override
    public boolean updateGoodsStockAfterActivity(Long activityId) {
        return false;
    }
}
