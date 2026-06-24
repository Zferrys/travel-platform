package com.travel.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 限流拦截器 — 基于令牌桶算法（简化版）
 * 针对秒杀等高并发接口进行 IP 级别限流
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    /** 每10秒最大请求数 */
    private static final int MAX_REQUESTS = 10;
    private static final long TIME_WINDOW = 10_000L;

    private final ConcurrentHashMap<String, Long> ipRequestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> ipResetTimes = new ConcurrentHashMap<>();

    private ScheduledExecutorService cleaner;

    @PostConstruct
    public void init() {
        cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limiter-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(this::cleanExpired, 5, 5, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void destroy() {
        if (cleaner != null) cleaner.shutdownNow();
    }

    private void cleanExpired() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> it = ipResetTimes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> entry = it.next();
            if (now > entry.getValue() + TIME_WINDOW * 2) {
                ipRequestCounts.remove(entry.getKey());
                it.remove();
            }
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        String ip = getClientIp(request);
        long currentTime = System.currentTimeMillis();

        // 单次 compute() 原子地完成：窗口重置 + 计数递增 + 超限检查
        AtomicBoolean exceeded = new AtomicBoolean(false);
        ipRequestCounts.compute(ip, (k, count) -> {
            Long resetTime = ipResetTimes.get(ip);
            if (resetTime == null || currentTime > resetTime) {
                // 窗口过期 → 重置计数为 1，更新时间窗口
                ipResetTimes.put(ip, currentTime + TIME_WINDOW);
                return 1L;
            }
            long next = (count == null) ? 1L : count + 1;
            if (next > MAX_REQUESTS) {
                exceeded.set(true);
            }
            return next;
        });

        if (exceeded.get()) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
            log.warn("IP: {} 触发限流，{}秒内请求超过{}次", ip, TIME_WINDOW / 1000, MAX_REQUESTS);
            return false;
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
