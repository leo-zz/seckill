package com.leozz.service;

import com.leozz.entity.Goods;

import java.util.List;

/**
 * @Author: leo-zz
 * @Date: 2019/3/13 9:45
 */
public interface GoodsService {
    /**
     *  获取商品信息，包含商品标题、封面、标签等信息
     * @return
     */
    List<Goods> getGoodsList();
}
