package com.order.service.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RedisIntegrationTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void shouldSaveAndRetrieveDataFromRedis() {
        // Arrange
        String key = "test:key";
        String value = "Redis is working!";

        // Act
        redisTemplate.opsForValue().set(key, value);
        Object retrievedValue = redisTemplate.opsForValue().get(key);

        // Assert
        assertThat(retrievedValue).isNotNull();
        assertThat(retrievedValue).isEqualTo(value);

        // Clean up
        redisTemplate.delete(key);
    }
}
