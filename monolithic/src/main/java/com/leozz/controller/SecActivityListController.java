package com.leozz.controller;

import com.leozz.dto.PreSubmitOrderDTO;
import com.leozz.dto.SecActivityDTO;
import com.leozz.service.SecActivityService;
import com.leozz.service.SecOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 8:33
 */
@RestController
@RequestMapping("/secactivity")
public class SecActivityListController {

    @Autowired
    private SecActivityService secActivityService;

    @Autowired
    private SecOrderService secOrderService;
    /**
     * 获取当前秒杀活动列表
     * @param userId userId 用户id
     * @return 秒杀活动列表页需要的数据
     */
    @RequestMapping("/list")
    public List<SecActivityDTO> getSecActivityList(Long userId){
        //根据userId获取秒杀活动列表，是否要通过mybatis关联查询？
        return secActivityService.getSecActivityList(userId);
    }

    /**
     * 点击按钮，参与秒杀活动，参与成功则跳转到预下单页面。
     * @param secActivityId
     * @param userId
     * @return 下单页需要的数据
     */
    @RequestMapping("/partake")
    public PreSubmitOrderDTO partakeSecActivity(Long secActivityId, Long userId){

        PreSubmitOrderDTO preSubmitOrderDTO =null;
        //尝试参与秒杀活动
        boolean b = secActivityService.partakeSecActivity(secActivityId, userId);
        if(b){
            //获取预下单页面的信息
            preSubmitOrderDTO = secOrderService.preSubmitOrder(secActivityId, userId);
        }else{
            preSubmitOrderDTO = new PreSubmitOrderDTO(false);
        }
        return preSubmitOrderDTO;
    }
}
