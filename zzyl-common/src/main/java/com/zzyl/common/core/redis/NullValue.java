package com.zzyl.common.core.redis;

import java.io.Serializable;

/**
 * 空值标记占位：用于缓存"DB 确认不存在"的 ID，防穿透。
 * TTL 45s 自动过期，使用方式：redisCache.setCacheObject(key, NullValue.INSTANCE, Duration.ofSeconds(45));
 * 读取时用 cached instanceof NullValue 识别。
 *
 * @author oh-my-opencode
 */
public final class NullValue implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final NullValue INSTANCE = new NullValue();

    private NullValue() {}
}
