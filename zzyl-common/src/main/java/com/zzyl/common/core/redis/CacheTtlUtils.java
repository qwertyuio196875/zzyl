package com.zzyl.common.core.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 缓存 TTL 计算工具：基准 + 随机抖动。
 * 提供静态调用入口便于在 Service / Util 中直接使用。
 * 缓存已托管至 CacheTtlProperties。
 */
@Component
public class CacheTtlUtils {

    private static CacheTtlProperties PROPS;

    @Autowired
    public void init(CacheTtlProperties props) {
        CacheTtlUtils.PROPS = props;
    }

    /** 系统参数基准 TTL + 抖动 */
    public static Duration resolveSysConfigTtl() {
        return withJitter(PROPS != null ? PROPS.getSysConfig() : Duration.ofHours(24));
    }

    /** 字典基准 TTL + 抖动 */
    public static Duration resolveSysDictTtl() {
        return withJitter(PROPS != null ? PROPS.getSysDict() : Duration.ofHours(24));
    }

    /** 关 TTL 时返回 false（调用方据此决定是否传 Duration） */
    public static boolean isEnabled() {
        return PROPS == null || PROPS.isEnabled();
    }

    private static Duration withJitter(Duration base) {
        if (base == null) {
            return Duration.ofHours(24);
        }
        Duration jitter = PROPS != null ? PROPS.getJitter() : Duration.ofMinutes(30);
        if (jitter == null || jitter.isZero() || jitter.isNegative()) {
            return base;
        }
        long jitterSeconds = ThreadLocalRandom.current().nextLong(jitter.toSeconds() + 1);
        return base.plusSeconds(jitterSeconds);
    }
}