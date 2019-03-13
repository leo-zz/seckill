package com.leozz.dao;

import com.leozz.entity.SecOrder;

public interface SecOrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SecOrder record);

    int insertSelective(SecOrder record);

    SecOrder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SecOrder record);

    int updateByPrimaryKey(SecOrder record);
}