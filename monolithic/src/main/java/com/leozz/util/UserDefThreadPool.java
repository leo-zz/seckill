package com.leozz.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: leo-zz
 * @Date: 2019/3/15 10:42
 */
public class UserDefThreadPool {
    public  static ExecutorService jobExecutor =new ThreadPoolExecutor(5, 5,
            1000, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
            new ThreadFactory() {
        private AtomicInteger num=new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"jobExecutor-"+num.incrementAndGet()+"pool");
        }
    });
}
