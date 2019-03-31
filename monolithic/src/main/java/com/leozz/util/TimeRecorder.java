package com.leozz.util;

/**
 * @Author: leo-zz
 * @Date: 2019/3/28 15:03
 */
public class TimeRecorder {

    public static ThreadLocal<Long> accessTime=new ThreadLocal<>();
}
