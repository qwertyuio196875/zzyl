package com.zzyl.system.service.impl;

import java.time.Duration;
import java.util.List;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zzyl.common.constant.CacheConstants;
import com.zzyl.common.constant.UserConstants;
import com.zzyl.common.core.redis.CacheTtlUtils;
import com.zzyl.common.core.redis.NullValue;
import com.zzyl.common.core.redis.RedisCache;
import com.zzyl.common.core.text.Convert;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.common.utils.StringUtils;
import com.zzyl.system.domain.SysConfig;
import com.zzyl.system.mapper.SysConfigMapper;
import com.zzyl.system.service.ISysConfigService;

/**
 * 参数配置 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class SysConfigServiceImpl implements ISysConfigService
{
    private static final Logger log = LoggerFactory.getLogger(SysConfigServiceImpl.class);

    @Autowired
    private SysConfigMapper configMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 项目启动时，初始化参数到缓存
     */
    @PostConstruct
    public void init()
    {
        loadingConfigCache();
    }

    /**
     * 查询参数配置信息
     * 
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    @Override
    public SysConfig selectConfigById(Long configId)
    {
        SysConfig config = new SysConfig();
        config.setConfigId(configId);
        return configMapper.selectConfig(config);
    }

    /**
     * 根据键名查询参数配置信息
     * 
     * @param configKey 参数key
     * @return 参数键值
     */
    @Override
    public String selectConfigByKey(String configKey)
    {
        String configValue = null;
        try
        {
            Object cached = redisCache.getCacheObject(getCacheKey(configKey));
            if (cached instanceof NullValue)
            {
                // 命中空标记 → 模拟"DB 不存在"，返回空字符串
                return StringUtils.EMPTY;
            }
            configValue = Convert.toStr(cached);
        }
        catch (Exception e)
        {
            log.warn("sys_config 缓存读取失败，降级到 DB key={}", configKey, e);
        }
        if (StringUtils.isNotEmpty(configValue))
        {
            return configValue;
        }

        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        SysConfig retConfig = configMapper.selectConfig(config);

        try
        {
            if (StringUtils.isNotNull(retConfig))
            {
                redisCache.setCacheObject(getCacheKey(configKey), retConfig.getConfigValue(),
                        CacheTtlUtils.resolveSysConfigTtl());
                return retConfig.getConfigValue();
            }
            else
            {
                redisCache.setCacheObject(getCacheKey(configKey), NullValue.INSTANCE, Duration.ofSeconds(45));
            }
        }
        catch (Exception e)
        {
            log.warn("sys_config 缓存写入失败 key={}", configKey, e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取验证码开关
     * 
     * @return true开启，false关闭
     */
    @Override
    public boolean selectCaptchaEnabled()
    {
        String captchaEnabled = selectConfigByKey("sys.account.captchaEnabled");
        if (StringUtils.isEmpty(captchaEnabled))
        {
            return true;
        }
        return Convert.toBool(captchaEnabled);
    }

    /**
     * 查询参数配置列表
     * 
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    @Override
    public List<SysConfig> selectConfigList(SysConfig config)
    {
        return configMapper.selectConfigList(config);
    }

    /**
     * 新增参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int insertConfig(SysConfig config)
    {
        int row = configMapper.insertConfig(config);
        if (row > 0)
        {
            // 写流程统一为「先 DB 后删缓存」，下一次读自动回源并写入带 TTL 的新值
            safeDeleteCache(config.getConfigKey());
        }
        return row;
    }

    /**
     * 修改参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int updateConfig(SysConfig config)
    {
        SysConfig temp = configMapper.selectConfigById(config.getConfigId());
        if (!StringUtils.equals(temp.getConfigKey(), config.getConfigKey()))
        {
            safeDeleteCache(temp.getConfigKey());
        }

        int row = configMapper.updateConfig(config);
        if (row > 0)
        {
            // 写流程统一为「先 DB 后删缓存」，下一次读自动回源并写入带 TTL 的新值
            safeDeleteCache(config.getConfigKey());
        }
        return row;
    }

    /**
     * 批量删除参数信息
     * 
     * @param configIds 需要删除的参数ID
     */
    @Override
    public void deleteConfigByIds(Long[] configIds)
    {
        for (Long configId : configIds)
        {
            SysConfig config = selectConfigById(configId);
            if (StringUtils.equals(UserConstants.YES, config.getConfigType()))
            {
                throw new ServiceException(String.format("内置参数【%1$s】不能删除 ", config.getConfigKey()));
            }
            configMapper.deleteConfigById(configId);
            safeDeleteCache(config.getConfigKey());
        }
    }

    /**
     * 加载参数缓存数据
     */
    @Override
    public void loadingConfigCache()
    {
        List<SysConfig> configsList = configMapper.selectConfigList(new SysConfig());
        for (SysConfig config : configsList)
        {
            redisCache.setCacheObject(
                getCacheKey(config.getConfigKey()),
                config.getConfigValue(),
                CacheTtlUtils.resolveSysConfigTtl()
            );
        }
    }

    /**
     * 清空参数缓存数据
     */
    @Override
    public void clearConfigCache()
    {
        try
        {
            List<String> keys = redisCache.scan(CacheConstants.SYS_CONFIG_KEY + "*", 1000L);
            if (keys != null && !keys.isEmpty())
            {
                redisCache.deleteObjects(keys);
            }
        }
        catch (Exception e)
        {
            log.error("清空参数缓存失败：{}", e.getMessage());
        }
    }

    /**
     * 重置参数缓存数据
     */
    @Override
    public void resetConfigCache()
    {
        clearConfigCache();
        loadingConfigCache();
    }

    /**
     * 校验参数键名是否唯一
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public boolean checkConfigKeyUnique(SysConfig config)
    {
        Long configId = StringUtils.isNull(config.getConfigId()) ? -1L : config.getConfigId();
        SysConfig info = configMapper.checkConfigKeyUnique(config.getConfigKey());
        if (StringUtils.isNotNull(info) && info.getConfigId().longValue() != configId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 设置cache key
     *
     * @param configKey 参数键
     * @return 缓存键key
     */
    private String getCacheKey(String configKey)
    {
        return CacheConstants.SYS_CONFIG_KEY + configKey;
    }

    /**
     * 安全的缓存失效：Redis 异常不影响 DB 写流程的返回值，依赖 TTL 兜底。
     *
     * @param configKey 参数键
     */
    private void safeDeleteCache(String configKey)
    {
        try
        {
            redisCache.deleteObject(getCacheKey(configKey));
        }
        catch (Exception e)
        {
            log.warn("sys_config 缓存失效失败 key={}，等 TTL 兜底", configKey, e);
        }
    }
}
