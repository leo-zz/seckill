package com.leozz.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: leo-zz
 * @Date: 2019/3/15 10:42
 */
public class UserDefThreadPool {
    public  static ExecutorService jobExecutor = Executors.newFixedThreadPool(10);

}
