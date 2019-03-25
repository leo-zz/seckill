package com.leozz.dao;

import com.leozz.entity.PointRecord;
import org.springframework.stereotype.Repository;

@Repository
public interface PointRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PointRecord record);

    int insertSelective(PointRecord record);

    PointRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PointRecord record);

    int updateByPrimaryKey(PointRecord record);

    PointRecord selectRecordByOrderId(Long id);
}