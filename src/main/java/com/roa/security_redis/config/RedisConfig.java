package com.roa.security_redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Bean
    public JedisConnectionFactory redisConnection(){
        RedisStandaloneConfiguration redis = new RedisStandaloneConfiguration();
        redis.setHostName("localhost");
        redis.setPort(6379);
        redis.setDatabase(2);
        return new JedisConnectionFactory(redis);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnection){
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnection);
        return template;
    }
}
