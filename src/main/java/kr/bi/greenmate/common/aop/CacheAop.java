package kr.bi.greenmate.common.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.bi.greenmate.common.annotation.CacheableWithTTL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheAop {

    private static final String CACHE_KEY_SEPARATOR = "::";
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Around("@annotation(kr.bi.greenmate.common.annotation.CacheableWithTTL)")
    public Object cacheProcess(final ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("CacheAop Start");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CacheableWithTTL cacheableWithTTL = method.getAnnotation(CacheableWithTTL.class);

        String cacheName = cacheableWithTTL.cacheName();
        long ttl = cacheableWithTTL.ttl();
        TimeUnit unit = cacheableWithTTL.unit();

        String key = createCacheKey(cacheName, joinPoint.getArgs());

        Object cachedValue = redisTemplate.opsForValue().get(key);

        if (cachedValue != null) {
            return cachedValue;
        }
        Object result = joinPoint.proceed();
        if (result != null) {
            log.debug("Create new cache key: {}, TTL: {}", key, ttl);
            redisTemplate.opsForValue().set(key, result, ttl, unit);
        }
        return result;
    }

    private String createCacheKey(String cacheName, Object[] args) {
        StringBuilder keyBuilder = new StringBuilder(cacheName);
        if (args == null || args.length == 0) {
            keyBuilder.append(CACHE_KEY_SEPARATOR).append("[]");
            return keyBuilder.toString();
        }
        try{
            String argsAsJson = objectMapper.writeValueAsString(args);
            keyBuilder.append(CACHE_KEY_SEPARATOR).append(argsAsJson);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize cache key args to JSON: {}", args);
            throw new RuntimeException(e);
        }
        return keyBuilder.toString();
    }
}
