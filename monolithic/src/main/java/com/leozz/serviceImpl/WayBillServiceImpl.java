package com.leozz.serviceImpl;

import com.leozz.entity.SecOrder;
import com.leozz.service.WayBillService;
import org.springframework.stereotype.Service;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 10:12
 */
@Service
public class WayBillServiceImpl implements WayBillService {
    @Override
    public boolean createWayBill(SecOrder order) {
        return false;
    }
}
