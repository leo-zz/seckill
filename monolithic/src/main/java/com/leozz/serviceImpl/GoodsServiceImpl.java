package com.leozz.serviceImpl;

import com.leozz.entity.Goods;
import com.leozz.service.GoodsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 10:10
 */
@Service
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
