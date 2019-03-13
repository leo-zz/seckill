package com.leozz.dao;

import com.leozz.entity.WayBill;

public interface WayBillMapper {
    int deleteByPrimaryKey(Long id);

    int insert(WayBill record);

    int insertSelective(WayBill record);

    WayBill selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WayBill record);

    int updateByPrimaryKey(WayBill record);
}