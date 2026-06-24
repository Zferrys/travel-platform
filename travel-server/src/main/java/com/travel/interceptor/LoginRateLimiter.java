package com.travel.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 登录限流器
 * 同一 IP：5次失败 → 锁定15分钟
 * 同一用户名：10次失败 → 锁定30分钟
 */
@Component
public class LoginRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimiter.class);

    /** IP 最大失败次数 */
    private static final int IP_MAX_FAILS = 5;
    /** IP 锁定时长（毫秒） */
    private static final long IP_LOCK_TIME = 15 * 60 * 1000;

    /** 用户名最大失败次数 */
    private static final int USER_MAX_FAILS = 10;
    /** 用户名锁定时长（毫秒） */
    private static final long USER_LOCK_TIME = 30 * 60 * 1000;

    /** IP 失败记录 */
    private final ConcurrentHashMap<String, FailRecord> ipRecords = new ConcurrentHashMap<>();
    /** 用户名失败记录 */
    private final ConcurrentHashMap<String, FailRecord> userRecords = new ConcurrentHashMap<>();

    private ScheduledExecutorService cleaner;

    @PostConstruct
    public void init() {
        cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "login-limiter-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(this::cleanExpired, 10, 10, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void destroy() {
        if (cleaner != null) cleaner.shutdownNow();
    }

    /** 清理过期记录 */
    private void cleanExpired() {
        long now = System.currentTimeMillis();
        cleanMap(ipRecords, now, IP_LOCK_TIME * 2);
        cleanMap(userRecords, now, USER_LOCK_TIME * 2);
    }

    private void cleanMap(ConcurrentHashMap<String, FailRecord> map, long now, long keepTime) {
        Iterator<Map.Entry<String, FailRecord>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, FailRecord> entry = it.next();
            FailRecord r = entry.getValue();
            long lu = r.lockUntil.get();
            if (lu > 0 && now > lu + keepTime) {
                it.remove();
            } else if (lu == 0 && now > r.firstFailTime + keepTime) {
                it.remove();
            }
        }
    }

    /**
     * 检查是否被锁定
     * @return null 表示通过，否则返回错误消息
     */
    public String checkLocked(String ip, String username) {
        // 检查 IP
        FailRecord ipRecord = ipRecords.get(ip);
        if (ipRecord != null && ipRecord.isLocked(IP_MAX_FAILS, IP_LOCK_TIME)) {
            long remaining = (ipRecord.lockUntil.get() - System.currentTimeMillis()) / 1000 / 60;
            log.warn("IP {} 被锁定，剩余 {} 分钟", maskIp(ip), remaining);
            return "登录失败次数过多，请" + (remaining > 0 ? remaining + "分钟" : "稍") + "后再试";
        }

        // 检查用户名
        FailRecord userRecord = userRecords.get(username);
        if (userRecord != null && userRecord.isLocked(USER_MAX_FAILS, USER_LOCK_TIME)) {
            long remaining = (userRecord.lockUntil.get() - System.currentTimeMillis()) / 1000 / 60;
            log.warn("用户 {} 被锁定，剩余 {} 分钟", username, remaining);
            return "该账号登录失败次数过多，请" + (remaining > 0 ? remaining + "分钟" : "稍") + "后再试";
        }

        return null;
    }

    /**
     * 记录登录失败
     */
    public void recordFail(String ip, String username) {
        ipRecords.compute(ip, (k, v) -> {
            FailRecord r = (v == null) ? new FailRecord() : v;
            r.fail();
            return r;
        });
        userRecords.compute(username, (k, v) -> {
            FailRecord r = (v == null) ? new FailRecord() : v;
            r.fail();
            return r;
        });
    }

    /**
     * 登录成功后清除失败记录
     */
    public void clearFail(String ip, String username) {
        ipRecords.remove(ip);
        userRecords.remove(username);
    }

    /** 获取客户端真实 IP */
    public static String getClientIp(javax.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String maskIp(String ip) {
        if (ip == null) return "unknown";
        int lastDot = ip.lastIndexOf('.');
        return lastDot > 0 ? ip.substring(0, lastDot) + ".*" : ip;
    }

    /** 失败记录内部类（线程安全） */
    private static class FailRecord {
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicLong lockUntil = new AtomicLong(0);
        final long firstFailTime = System.currentTimeMillis();

        void fail() {
            count.incrementAndGet();
        }

        /**
         * 检查是否应锁定。使用 CAS 保证只有一个线程能设置锁定时间。
         * @return true 表示已锁定
         */
        boolean isLocked(int maxFails, long lockTime) {
            long currentLock = lockUntil.get();
            // 已在锁定期内
            if (currentLock > 0 && System.currentTimeMillis() < currentLock) {
                return true;
            }
            // 失败次数达标且未锁定，尝试获取锁
            if (count.get() >= maxFails && currentLock == 0) {
                long newLock = System.currentTimeMillis() + lockTime;
                if (lockUntil.compareAndSet(0, newLock)) {
                    // 当前线程成功设置锁定
                    count.set(0);
                    return true;
                }
                // 其他线程已抢先锁定，重新检查
                long afterLock = lockUntil.get();
                return afterLock > 0 && System.currentTimeMillis() < afterLock;
            }
            return false;
        }
    }
}
