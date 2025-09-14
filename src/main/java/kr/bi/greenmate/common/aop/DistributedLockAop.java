package kr.bi.greenmate.common.aop;

import kr.bi.greenmate.common.annotation.DistributedLock;
import kr.bi.greenmate.common.exception.ApplicationException;
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
import java.util.Arrays;

import static kr.bi.greenmate.common.exception.CommonErrorCode.LOCK_ACQUISITION_FAILED;
import static kr.bi.greenmate.common.exception.CommonErrorCode.LOCK_INTERRUPTED;
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
        log.info("Start DistributedLock");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String[] keys = getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.keys());
        String prefix = distributedLock.prefix();

        RLock[] locks = new RLock[keys.length];
        for (int i = 0; i < locks.length; i++) {
            String lockKey = REDISSON_LOCK_PREFIX
                + (prefix.isEmpty() ? "" : prefix + ":")
                + keys[i];
            locks[i] = redissonClient.getLock(lockKey);
        }

        RedissonMultiLock multiLock = new RedissonMultiLock(locks);

        boolean available = false;
        try {
            available = multiLock.tryLock(
                distributedLock.waitTime(),
                distributedLock.leaseTime(),
                distributedLock.timeUnit()
            );
            if (!available) {
                throw new ApplicationException(LOCK_ACQUISITION_FAILED);
            }
            log.debug("Success lock: {}", Arrays.toString(keys));
            return aopForTransaction.proceed(joinPoint);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApplicationException(LOCK_INTERRUPTED);
        } finally {
            try {
                multiLock.unlock();
                log.debug("Unlock: {}", Arrays.toString(keys));
            } catch (IllegalMonitorStateException e) {
                if (!available) {
                    log.debug("Failed to get Lock: {} {}", method.getName(), keys);
                } else {
                    log.debug("Already unlock: {} {}", method.getName(), keys);
                }
            }
        }
    }
}
