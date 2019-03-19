package com.leozz.dao;

import com.leozz.entity.SecOrder;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface SecOrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SecOrder record);

    int insertSelective(SecOrder record);

    SecOrder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SecOrder record);

    int updateByPrimaryKey(SecOrder record);

    int selectOrderCountByUserAndActivity( Map<String, Long> paramMap);

}