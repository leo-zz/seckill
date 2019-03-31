package com.leozz.dao;

import com.leozz.dto.CouponTypeDTO;
import com.leozz.entity.CouponType;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
public interface CouponMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CouponType record);

    int insertSelective(CouponType record);

    CouponType selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CouponType record);

    int updateByPrimaryKey(CouponType record);

    List<CouponTypeDTO> selectUsableCouponByUserId(HashMap<String, Object> paramMap);
}