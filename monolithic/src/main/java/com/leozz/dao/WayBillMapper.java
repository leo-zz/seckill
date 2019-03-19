package com.leozz.dao;

import com.leozz.entity.WayBill;
import org.springframework.stereotype.Repository;

@Repository
public interface WayBillMapper {
    int deleteByPrimaryKey(Long id);

    int insert(WayBill record);

    int insertSelective(WayBill record);

    WayBill selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WayBill record);

    int updateByPrimaryKey(WayBill record);
}