package com.sm.ratelimiter.service;

import com.sm.ratelimiter.config.RateLimiterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

//store token bucket state in redis
//manage tokens per client
//handle token refill based on time
//provide rate limiting logic.
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenBucketService {

    private final JedisPool jedisPool;

    private final RateLimiterProperties properties;

    private final String TOKENS_KEY_PREFIX = "rate_limiter:tokens:";
    private static final String LAST_REFILL_KEY_PREFIX = "rate_limiter:last_refill:";

   public void setRefillToken(int refillToken){
       properties.setRefillRate(refillToken);
   }

   public boolean isAllowed(String clientId) {
    
    String tokenKey = TOKENS_KEY_PREFIX + clientId;

    try (Jedis jedis = jedisPool.getResource()) {
        refillTokens(clientId, jedis);

        String tokenStr = jedis.get(tokenKey);

        long currentTokens = tokenStr != null ? Long.parseLong(tokenStr) : properties.getCapacity();

        if(currentTokens <= 0) {
            return false;
        }

        long decremented = jedis.decr(tokenKey);
        return decremented >= 0;
    }
   }

   public long getCapacity(String clientId) {
    
        return properties.getCapacity();
    }

    public long getAvailableTokens(String clientId) {

        String tokenKey = TOKENS_KEY_PREFIX + clientId;

        try(Jedis jedis = jedisPool.getResource()) {

            refillTokens(clientId, jedis);
            String tokenStr = jedis.get(tokenKey);
            return tokenStr != null ? Long.parseLong(tokenStr) : properties.getCapacity();

        }
    }
    
    public void refillTokens(String clientId, Jedis jedis) {

        String tokensKey = TOKENS_KEY_PREFIX + clientId;
        String lastRefillKey = LAST_REFILL_KEY_PREFIX + clientId;
        log.info("The refill rate is: {}",properties.getRefillRate());
        long now = System.currentTimeMillis();
        String lastRefillStr = jedis.get(lastRefillKey);
        if(lastRefillStr == null){

            jedis.set(tokensKey, String.valueOf(properties.getCapacity()));
            jedis.set(lastRefillKey, String.valueOf(now));
            return;
        }

        long lastRefillTime = Long.parseLong(lastRefillStr);
        long elapsedTime = now - lastRefillTime;

        if(elapsedTime <= 1000) {
            return;
        }

        long tokensToAdd = (elapsedTime * properties.getRefillRate()) / 1000;
        if(tokensToAdd <= 0) {
            return;
        }

        String tokenStr = jedis.get(tokensKey);

        long currentTokens = tokenStr !=null ? Long.parseLong(tokenStr) : properties.getCapacity();
        long newTokens = Math.min(properties.getCapacity(), currentTokens + tokensToAdd);

        jedis.set(tokensKey, String.valueOf(newTokens));
        jedis.set(lastRefillKey, String.valueOf(now));

    }
}

