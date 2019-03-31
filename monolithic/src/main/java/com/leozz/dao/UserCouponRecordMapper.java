package com.leozz.dao;

import com.leozz.entity.UserCouponRecord;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface UserCouponRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserCouponRecord record);

    int insertSelective(UserCouponRecord record);

    UserCouponRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserCouponRecord record);

    int updateByPrimaryKey(UserCouponRecord record);

    /**
     * 根据订单号查询优惠券信息
     * @param id
     * @return
     */
    List<UserCouponRecord> selectRecordsByActivityId(HashMap<String, Object> id);

    int updateStatusById(Map<String,Object> paraMap);

    /**
     * 检查优惠券是否可用
     * @param paraMap
     * @return 可用则返回1，否则返回0
     */
    int checkCouponIsUsable(HashMap<String, Long> paraMap);
}