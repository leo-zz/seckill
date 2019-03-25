package com.leozz.serviceImpl;

import com.leozz.dao.WayBillMapper;
import com.leozz.entity.SecOrder;
import com.leozz.entity.WayBill;
import com.leozz.service.WayBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 10:12
 */
@Service
public class WayBillServiceImpl implements WayBillService {

    @Autowired
    WayBillMapper wayBillMapper;
    @Override
    public boolean createWayBill(SecOrder order) {
        WayBill wayBill = new WayBill();
        wayBill.setCreateDate(new Date());
        wayBill.setOrderId(order.getId());
        int i = wayBillMapper.insertSelective(wayBill);
        return i==1;
    }
}
