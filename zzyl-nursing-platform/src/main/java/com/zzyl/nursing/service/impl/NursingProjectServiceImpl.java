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
import com.zzyl.nursing.domain.NursingProjectVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.mapper.NursingProjectMapper;
import com.zzyl.nursing.domain.NursingProject;
import com.zzyl.nursing.service.INursingProjectService;

/**
 * 护理项目Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-22
 */
@Service
public class NursingProjectServiceImpl implements INursingProjectService 
{
    private static final Logger log = LoggerFactory.getLogger(NursingProjectServiceImpl.class);

    private static final Duration MAIN_TTL_BASE = Duration.ofHours(24);
    private static final int MAIN_TTL_JITTER_MAX_MIN = 30;
    private static final Duration NULL_TTL = Duration.ofSeconds(45);

    @Autowired
    private NursingProjectMapper nursingProjectMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 查询护理项目
     * 
     * @param id 护理项目主键
     * @return 护理项目
     */
    @Override
    public NursingProject selectNursingProjectById(Long id)
    {
        String key = CacheKeyConstants.nursingProjectById(id);
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
            log.warn("护理项目缓存读取失败，降级到 DB key={}", key, e);
        }
        if (cached != null)
        {
            return (NursingProject) cached;
        }

        NursingProject db = nursingProjectMapper.selectNursingProjectById(id);

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
            log.warn("护理项目缓存写入失败 key={}", key, e);
        }
        return db;
    }

    /**
     * 查询护理项目列表
     * 
     * @param nursingProject 护理项目
     * @return 护理项目
     */
    @Override
    public List<NursingProject> selectNursingProjectList(NursingProject nursingProject)
    {
        return nursingProjectMapper.selectNursingProjectList(nursingProject);
    }

    /**
     * 新增护理项目
     * 
     * @param nursingProject 护理项目
     * @return 结果
     */
    @Override
    public int insertNursingProject(NursingProject nursingProject)
    {
        nursingProject.setCreateTime(DateUtils.getNowDate());
        return nursingProjectMapper.insertNursingProject(nursingProject);
    }

    /**
     * 修改护理项目
     * 
     * @param nursingProject 护理项目
     * @return 结果
     */
    @Override
    public int updateNursingProject(NursingProject nursingProject)
    {
        nursingProject.setUpdateTime(DateUtils.getNowDate());
        int rows = nursingProjectMapper.updateNursingProject(nursingProject);
        invalidateCache(nursingProject.getId());
        return rows;
    }

    /**
     * 批量删除护理项目
     * 
     * @param ids 需要删除的护理项目主键
     * @return 结果
     */
    @Override
    public int deleteNursingProjectByIds(Long[] ids)
    {
        int rows = nursingProjectMapper.deleteNursingProjectByIds(ids);
        if (ids != null && ids.length > 0)
        {
            List<String> keys = Arrays.stream(ids)
                    .map(CacheKeyConstants::nursingProjectById)
                    .collect(Collectors.toList());
            try
            {
                redisCache.deleteObjects(keys);
            }
            catch (Exception e)
            {
                log.warn("批量护理项目缓存失效失败 size={}", keys.size(), e);
            }
        }
        return rows;
    }

    /**
     * 删除护理项目信息
     * 
     * @param id 护理项目主键
     * @return 结果
     */
    @Override
    public int deleteNursingProjectById(Long id)
    {
        int rows = nursingProjectMapper.deleteNursingProjectById(id);
        invalidateCache(id);
        return rows;
    }

    /**
     * 查询所有护理项目
     *
     * @return 护理项目
     */
    @Override
    public List<NursingProjectVo> getAll() {
        return nursingProjectMapper.getAll();
    }

    private Duration mainTtl()
    {
        return MAIN_TTL_BASE.plusMinutes(ThreadLocalRandom.current().nextInt(MAIN_TTL_JITTER_MAX_MIN));
    }

    private void invalidateCache(Long id)
    {
        if (id == null) return;
        String key = CacheKeyConstants.nursingProjectById(id);
        try
        {
            redisCache.deleteObject(key);
        }
        catch (Exception e)
        {
            log.warn("护理项目缓存失效失败 key={}，等 TTL 兜底", key, e);
        }
    }
}
