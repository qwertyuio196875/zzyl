package com.zzyl.nursing.service.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import com.zzyl.common.constant.CacheKeyConstants;
import com.zzyl.common.core.redis.NullValue;
import com.zzyl.common.core.redis.RedisCache;
import com.zzyl.common.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.mapper.NursingLevelMapper;
import com.zzyl.nursing.domain.NursingLevel;
import com.zzyl.nursing.service.INursingLevelService;

/**
 * 护理等级Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-23
 */
@Service
public class NursingLevelServiceImpl implements INursingLevelService 
{
    private static final Logger log = LoggerFactory.getLogger(NursingLevelServiceImpl.class);

    private static final Duration MAIN_TTL_BASE = Duration.ofHours(24);
    private static final int MAIN_TTL_JITTER_MAX_MIN = 30;
    private static final Duration NULL_TTL = Duration.ofSeconds(45);

    @Autowired
    private NursingLevelMapper nursingLevelMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 查询护理等级
     * 
     * @param id 护理等级主键
     * @return 护理等级
     */
    @Override
    public NursingLevel selectNursingLevelById(Long id)
    {
        String key = CacheKeyConstants.nursingLevelById(id);
        Object cached = null;
        try
        {
            cached = redisCache.getCacheObject(key);
            if (cached instanceof NullValue)
            {
                return null;
            }
        }
        catch (Exception e)
        {
            log.warn("护理等级缓存读取失败，降级到 DB key={}", key, e);
        }
        if (cached != null)
        {
            return (NursingLevel) cached;
        }

        NursingLevel db = nursingLevelMapper.selectNursingLevelById(id);

        try
        {
            if (db != null)
            {
                redisCache.setCacheObject(key, db, mainTtl());
            }
            else
            {
                redisCache.setCacheObject(key, NullValue.INSTANCE, NULL_TTL);
            }
        }
        catch (Exception e)
        {
            log.warn("护理等级缓存写入失败 key={}", key, e);
        }
        return db;
    }

    /**
     * 查询护理等级列表
     * 
     * @param nursingLevel 护理等级
     * @return 护理等级
     */
    @Override
    public List<NursingLevel> selectNursingLevelList(NursingLevel nursingLevel)
    {
        return nursingLevelMapper.selectNursingLevelList(nursingLevel);
    }

    /**
     * 新增护理等级
     * 
     * @param nursingLevel 护理等级
     * @return 结果
     */
    @Override
    public int insertNursingLevel(NursingLevel nursingLevel)
    {
        nursingLevel.setCreateTime(DateUtils.getNowDate());
        return nursingLevelMapper.insertNursingLevel(nursingLevel);
    }

    /**
     * 修改护理等级
     * 
     * @param nursingLevel 护理等级
     * @return 结果
     */
    @Override
    public int updateNursingLevel(NursingLevel nursingLevel)
    {
        nursingLevel.setUpdateTime(DateUtils.getNowDate());
        int rows = nursingLevelMapper.updateNursingLevel(nursingLevel);
        invalidateCache(nursingLevel.getId());
        return rows;
    }

    /**
     * 批量删除护理等级
     * 
     * @param ids 需要删除的护理等级主键
     * @return 结果
     */
    @Override
    public int deleteNursingLevelByIds(Long[] ids)
    {
        int rows = nursingLevelMapper.deleteNursingLevelByIds(ids);
        if (ids != null && ids.length > 0)
        {
            List<String> keys = Arrays.stream(ids)
                    .map(CacheKeyConstants::nursingLevelById)
                    .collect(Collectors.toList());
            try
            {
                redisCache.deleteObjects(keys);
            }
            catch (Exception e)
            {
                log.warn("批量护理等级缓存失效失败 size={}", keys.size(), e);
            }
        }
        return rows;
    }

    /**
     * 删除护理等级信息
     * 
     * @param id 护理等级主键
     * @return 结果
     */
    @Override
    public int deleteNursingLevelById(Long id)
    {
        int rows = nursingLevelMapper.deleteNursingLevelById(id);
        invalidateCache(id);
        return rows;
    }

    private Duration mainTtl()
    {
        return MAIN_TTL_BASE.plusMinutes(ThreadLocalRandom.current().nextInt(MAIN_TTL_JITTER_MAX_MIN));
    }

    private void invalidateCache(Long id)
    {
        if (id == null) return;
        String key = CacheKeyConstants.nursingLevelById(id);
        try
        {
            redisCache.deleteObject(key);
        }
        catch (Exception e)
        {
            log.warn("护理等级缓存失效失败 key={}，等 TTL 兜底", key, e);
        }
    }
}
