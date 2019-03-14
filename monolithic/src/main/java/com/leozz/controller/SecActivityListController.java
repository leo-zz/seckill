package com.leozz.controller;

import com.leozz.dto.SecActivityListPage;
import com.leozz.dto.PreSubmitOrderPage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 8:33
 */
@RestController
@RequestMapping("/secactivity")
public class SecActivityListController {

    /**
     * 获取当前秒杀活动列表
     * @param userId userId 用户id
     * @return 秒杀活动列表页需要的数据
     */
    @RequestMapping("/list")
    public SecActivityListPage getSecActivityList(Long userId){
        return null;
    }

    /**
     * 点击按钮，参与秒杀活动，参与成功则跳转到预下单页面。
     * @param secActivityId
     * @param userId
     * @return 下单页需要的数据
     */
    @RequestMapping("/partake")
    public PreSubmitOrderPage partakeSecActivity(Long secActivityId, Long userId){
        return null;
    }
}
