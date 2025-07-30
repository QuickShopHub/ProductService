package com.myshop.productservice.config;

import com.myshop.productservice.repository.Product;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Product> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Product> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Ключи — строки
        template.setKeySerializer(new StringRedisSerializer());

        // Значения — JSON
        Jackson2JsonRedisSerializer<Product> serializer = new Jackson2JsonRedisSerializer<>(Product.class);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}