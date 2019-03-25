package com.leozz.dao;

import com.leozz.dto.CouponDTO;
import com.leozz.entity.Coupon;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
public interface CouponMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Coupon record);

    int insertSelective(Coupon record);

    Coupon selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Coupon record);

    int updateByPrimaryKey(Coupon record);

    List<CouponDTO> selectUsableCouponByUserId(HashMap<String, Object> paramMap);
}