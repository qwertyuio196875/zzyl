package com.zzyl.common.core.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 缓存 TTL 配置（仅外部化用于系统参数/字典等基础数据）。
 * 抖动通过 {@link CacheTtlUtils} 在写入时叠加，避免集中过期。
 * 缓存已托管至 CacheTtlProperties。
 */
@Component
@ConfigurationProperties(prefix = "zzyl.cache.ttl")
public class CacheTtlProperties {

    /** 系统参数 sys_config:* 基准 TTL，默认 24h */
    private Duration sysConfig = Duration.ofHours(24);

    /** 字典 sys_dict:* 基准 TTL，默认 24h */
    private Duration sysDict = Duration.ofHours(24);

    /** 随机抖动上限，默认 1800 秒（30 分钟，0 表示关闭） */
    private Duration jitter = Duration.ofMinutes(30);

    /** 是否启用 TTL（默认 true；调试时关闭可回退旧行为） */
    private boolean enabled = true;

    public Duration getSysConfig() { return sysConfig; }
    public void setSysConfig(Duration sysConfig) { this.sysConfig = sysConfig; }

    public Duration getSysDict() { return sysDict; }
    public void setSysDict(Duration sysDict) { this.sysDict = sysDict; }

    public Duration getJitter() { return jitter; }
    public void setJitter(Duration jitter) { this.jitter = jitter; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}