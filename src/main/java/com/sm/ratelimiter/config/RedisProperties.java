package com.sm.ratelimiter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


@Configuration
@Data
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties{

    private String host;
    private int port;
    private int timeout;
    private String password;
    private Ssl ssl = new Ssl();

    @Data
    public static class Ssl {
        private boolean enabled;
    }

    @Profile("!prod")
    @Bean(name="jedisPool")
    public JedisPool getJedisPool(){
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        return new JedisPool(
                poolConfig,
                host,
                port,
                timeout
        );
    }

    @Profile("prod")
    @Bean(name = "jedisPool")
    public JedisPool getJedisPoolProd() {

        JedisPoolConfig poolConfig = new JedisPoolConfig();

        return new JedisPool(
                poolConfig,
                host,
                port,
                timeout,
                password,
                ssl.isEnabled()
        );
    }
}
