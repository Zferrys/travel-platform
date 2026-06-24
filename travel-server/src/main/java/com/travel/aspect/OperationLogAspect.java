package com.travel.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 操作日志 AOP
 * 记录关键操作的执行时间、参数、结果
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    /** 切点：Controller 层所有方法 */
    @Pointcut("execution(* com.travel.controller..*.*(..))")
    public void controllerLayer() {}

    /** 围绕通知：记录执行时间 + 异常 */
    @Around("controllerLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;

            if (elapsed > 500) {
                log.warn("慢请求: {}.{} 耗时 {}ms", className, method, elapsed);
            } else if (log.isDebugEnabled()) {
                log.debug("{}.{} 耗时 {}ms", className, method, elapsed);
            }
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("{}.{} 异常 ({}ms): {}", className, method, elapsed, e.getMessage());
            throw e;
        }
    }
}
