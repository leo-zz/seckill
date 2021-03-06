package com.leozz.dao;

import com.leozz.entity.DeliveryAddr;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryAddrMapper {
    int deleteByPrimaryKey(Long id);

    int insert(DeliveryAddr record);

    int insertSelective(DeliveryAddr record);

    DeliveryAddr selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DeliveryAddr record);

    int updateByPrimaryKey(DeliveryAddr record);

    DeliveryAddr selectDefaultByUserId(Long userId);
}