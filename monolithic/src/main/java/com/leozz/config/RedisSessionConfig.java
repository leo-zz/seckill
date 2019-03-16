package com.leozz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @Author: leo-zz
 * @Date: 2019/3/15 16:06
 */
@Configuration
@EnableRedisHttpSession
//分布式session，参考https://www.cnblogs.com/carrychan/p/9548013.html
public class RedisSessionConfig {
}
