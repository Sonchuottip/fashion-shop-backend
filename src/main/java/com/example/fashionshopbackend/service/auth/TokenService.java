package com.example.fashionshopbackend.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String tokenKey = "jwt:token:";

    @Autowired
    public TokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveToken(String userid, String token, long expireTime) {
        String key = tokenKey + userid;
        // Lưu token vào danh sách
        redisTemplate.opsForList().rightPush(key, token);
        // Đặt thời gian hết hạn cho khóa
        redisTemplate.expire(key, expireTime, TimeUnit.MILLISECONDS);
    }

    public List<String> getTokens(String userid) {
        String key = tokenKey + userid;
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) {
            return Collections.emptyList();
        }
        return redisTemplate.opsForList().range(key, 0, size - 1);
    }

    public void revokeToken(String userid) {
        redisTemplate.delete(tokenKey + userid);
    }

    public boolean isTokenRevoke(String userid) {
        List<String> tokens = getTokens(userid);
        return tokens.isEmpty();
    }

    public boolean isSpecificTokenRevoke(String userid, String token) {
        List<String> tokens = getTokens(userid);
        return !tokens.contains(token);
    }
}
