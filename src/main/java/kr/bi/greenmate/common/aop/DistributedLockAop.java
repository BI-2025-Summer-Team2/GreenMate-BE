package kr.bi.greenmate.common.aop;

import kr.bi.greenmate.common.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import static kr.bi.greenmate.common.util.CustomSpringELParser.getDynamicValue;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(10)
public class DistributedLockAop {

    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(kr.bi.greenmate.common.annotation.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String[] keys = getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.keys());
        RLock[] locks = new RLock[keys.length];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = redissonClient.getLock(REDISSON_LOCK_PREFIX + keys[i]);
        }

        RedissonMultiLock multiLock = new RedissonMultiLock(locks);

        boolean available = false;
        try {
            available = multiLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!available) {
                return false;
            }
            return aopForTransaction.proceed(joinPoint);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Lock Interrupted");
        } finally {
            try {
                multiLock.unlock();
            } catch (IllegalMonitorStateException e) {
                if (!available) {
                    log.info("Failed to get Lock: {} {}", method.getName(), keys);
                } else {
                    log.info("Already unlock: {} {}", method.getName(), keys);
                }
            }
        }
    }
}
