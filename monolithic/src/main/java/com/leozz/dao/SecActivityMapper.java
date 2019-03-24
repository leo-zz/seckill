package com.leozz.dao;

import com.leozz.entity.SecActivity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecActivityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SecActivity record);

    int insertSelective(SecActivity record);

    SecActivity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SecActivity record);

    int updateByPrimaryKey(SecActivity record);

    /**
     *  获取当前最近进行的活动列表，筛选范围为活动开始前30分钟，到活动截止时间。
     * @return
     */
    List<SecActivity> selectRecentActivityList();

    int updateBlockedStockByPrimaryKey(SecActivity secActivity);
}