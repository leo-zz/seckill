package com.leozz.dao;

import com.leozz.entity.PointRecord;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
public interface PointRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PointRecord record);

    int insertSelective(PointRecord record);

    PointRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PointRecord record);

    int updateByPrimaryKey(PointRecord record);

    PointRecord selectRecordByMap(HashMap<String, Object> map);

    int updateUserPointStatusByEntity(PointRecord record);
}