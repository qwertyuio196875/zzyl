package com.zzyl.admission.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.zzyl.common.constant.CacheKeyConstants;
import com.zzyl.common.core.redis.RedisCache;
import com.zzyl.common.utils.spring.SpringUtils;

/**
 * 入退管理业务编号生成
 *
 * O7 改造：原实现 timestamp(yyyyMMddHHmmss) + ThreadLocalRandom.nextInt(100,1000)
 *   - 同一秒内并发 > 900 时碰撞概率 ~1/1000；
 *   - 凌晨跨秒边界（HH:MM:SS 同时变化）窗口更短但仍可观测碰撞。
 *
 * 改为 Redis INCR 原子序列号，Key 格式 zzyl:{env}:admission:seq:checkIn:{yyyyMMdd}。
 *   - INCR 是 Redis 单线程原子操作，全集群唯一；
 *   - 首次 INCR 返回 1 时设 2 天 TTL，覆盖业务高峰 + 容错窗口；
 *   - Redis 不可用时降级到 timestamp 末 4 位（保留单实例可用性，不保证全局唯一）。
 *
 * 单号格式变化：17 位 (yyyyMMddHHmmss + 3dig) → 12 位 (yyyyMMdd + 4dig)。
 *   - DB 列为 varchar(32)，无长度风险；
 *   - 业务对外展示（打印 / 导出 / 二维码）通常按字符串处理，无固定长度校验；
 *   - 若下游消费方有强长度校验，需自行适配。
 */
public final class AdmissionNoUtils
{
    private static final DateTimeFormatter SEQ_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private AdmissionNoUtils() {}

    public static String assessmentNo()
    {
        return "HA" + nextSeqSuffix();
    }

    public static String checkInNo()
    {
        return "CI" + nextSeqSuffix();
    }

    public static String checkOutNo()
    {
        return "CO" + nextSeqSuffix();
    }

    private static String nextSeqSuffix()
    {
        String dateStr = LocalDate.now().format(SEQ_DATE_FMT);
        try
        {
            RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
            String key = CacheKeyConstants.admissionCheckInSeq(dateStr);
            Long seq = redisCache.increment(key);
            if (seq != null && seq == 1L)
            {
                redisCache.expire(key, Duration.ofDays(2));
            }
            return dateStr + String.format("%04d", seq == null ? 0L : seq);
        }
        catch (Exception e)
        {
            // Redis 不可用：降级到 timestamp 末 4 位（不保证全局唯一，但保证单实例内可用）
            return dateStr + String.format("%04d", (int) (System.currentTimeMillis() % 10000L));
        }
    }
}
