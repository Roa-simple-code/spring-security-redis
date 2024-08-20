package com.roa.security_redis.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

        private final StringRedisTemplate redisTemplate;
    @Autowired
    public TokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public void saveToken(String key,String value,Long TTL){
        redisTemplate.opsForValue().set(key,value,TTL, TimeUnit.MILLISECONDS);
    }
    public String getToken(String key){
        return redisTemplate.opsForValue().get(key);
    }
}
