package com.leozz.dao;

import com.leozz.entity.Coupon;
import com.leozz.entity.UserCouponRecord;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCouponRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserCouponRecord record);

    int insertSelective(UserCouponRecord record);

    UserCouponRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserCouponRecord record);

    int updateByPrimaryKey(UserCouponRecord record);

    List<UserCouponRecord> selectRecordsByOrder(Long id);
}