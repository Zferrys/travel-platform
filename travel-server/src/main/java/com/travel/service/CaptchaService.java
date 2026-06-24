package com.travel.service;

import com.travel.util.CaptchaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Base64;
import java.util.UUID;

/**
 * 验证码服务
 * 生成图片验证码，验证码存入 Redis（5分钟过期）
 */
@Service
public class CaptchaService {

    private static final Logger log = LoggerFactory.getLogger(CaptchaService.class);
    private static final int EXPIRE_SECONDS = 300; // 5分钟

    @Autowired
    private JedisPool jedisPool;

    /**
     * 生成验证码，返回 { key, base64Image }
     */
    public java.util.Map<String, String> generate() {
        String code = CaptchaUtil.generateCode();
        String key = UUID.randomUUID().toString().replace("-", "");

        // 存入 Redis
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex("captcha:" + key, EXPIRE_SECONDS, code);
        } catch (Exception e) {
            log.error("Redis 存储验证码失败", e);
            // Redis 不可用时，存内存（开发环境 fallback）
            LocalCache.put("captcha:" + key, code, EXPIRE_SECONDS * 1000L);
        }

        // 生成图片
        String base64Image;
        try {
            byte[] imageBytes = CaptchaUtil.generateImage(code);
            base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("验证码图片生成失败", e);
            base64Image = "";
        }

        java.util.Map<String, String> result = new java.util.HashMap<>();
        result.put("key", key);
        result.put("image", base64Image);
        return result;
    }

    /**
     * 校验验证码（校验后删除）
     */
    public boolean verify(String key, String code) {
        if (key == null || code == null) return false;

        try (Jedis jedis = jedisPool.getResource()) {
            String stored = jedis.get("captcha:" + key);
            if (stored == null) {
                // fallback to local cache
                stored = LocalCache.get("captcha:" + key);
            } else {
                jedis.del("captcha:" + key);
            }
            return stored != null && stored.equalsIgnoreCase(code);
        } catch (Exception e) {
            log.error("Redis 验证码校验异常", e);
            String stored = LocalCache.get("captcha:" + key);
            if (stored != null) {
                LocalCache.remove("captcha:" + key);
                return stored.equalsIgnoreCase(code);
            }
            return false; // Redis 不可用且无本地缓存时，拒绝登录（安全优先）
        }
    }

    /**
     * 本地内存缓存（Redis 不可用时的 fallback）
     */
    private static final java.util.Map<String, LocalCache.Entry> localStore =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static class LocalCache {
        static class Entry {
            String value;
            long expireAt;
            Entry(String value, long expireAt) { this.value = value; this.expireAt = expireAt; }
        }
        static void put(String key, String value, long ttlMs) {
            localStore.put(key, new Entry(value, System.currentTimeMillis() + ttlMs));
        }
        static String get(String key) {
            Entry e = localStore.get(key);
            if (e == null) return null;
            if (System.currentTimeMillis() > e.expireAt) { localStore.remove(key); return null; }
            return e.value;
        }
        static void remove(String key) { localStore.remove(key); }
    }
}
