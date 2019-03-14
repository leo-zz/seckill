package com.leozz.serviceImpl;

import com.leozz.entity.PointRecord;
import com.leozz.service.PointRecordService;
import org.springframework.stereotype.Service;

/**
 * @Author: leo-zz
 * @Date: 2019/3/14 10:10
 */
@Service
public class PointRecordServiceImpl implements PointRecordService {
    @Override
    public boolean recordMembershipPointChange(PointRecord record) {
        return false;
    }
}
