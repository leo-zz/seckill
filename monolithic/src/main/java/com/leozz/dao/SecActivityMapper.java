package com.leozz.dao;

import com.leozz.entity.SecActivity;

public interface SecActivityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SecActivity record);

    int insertSelective(SecActivity record);

    SecActivity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SecActivity record);

    int updateByPrimaryKey(SecActivity record);
}