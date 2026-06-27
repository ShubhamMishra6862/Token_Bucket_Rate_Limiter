package com.sm.ratelimiter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Wrapper class to prevent JedisPool from being exposed as an MBean.
 * This avoids JMX registration conflicts.
 */
class JedisPoolWrapper extends JedisPool {
    public JedisPoolWrapper(JedisPoolConfig poolConfig, String host, int port, int timeout) {
        super(poolConfig, host, port, timeout);
    }
}

@Component
@Data
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {
    
    private String host = "localhost";

    private int port = 6379;

    private int timeout = 2000;

    //Java client library for Redis. 
    //Lets Java application communicate with Redis server

    @Bean(name = "jedisPool")
    public JedisPool getJedisPool() {
        //Jedispool keeps multiple connections ready to reuse. 
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(5);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setNumTestsPerEvictionRun(-1);
        poolConfig.setMinEvictableIdleTimeMillis(60000);
        return new JedisPoolWrapper(poolConfig, host, port, timeout);
    }
}
