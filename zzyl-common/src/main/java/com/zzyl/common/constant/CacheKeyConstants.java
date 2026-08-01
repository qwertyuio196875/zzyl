package com.zzyl.common.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Redis Key 统一命名规范：zzyl:{env}:{module}:{biz}:{id}
 * 业务代码不直接拼字符串，必须走本类静态方法。
 *
 * @author oh-my-opencode
 */
@Component
public final class CacheKeyConstants
{
    private static String env = "local";

    /** 给其他模块读取 env 用（如 AdmissionNoUtils 拼 Key） */
    public static String env()
    {
        return env;
    }

    @Value("${zzyl.cache.key-prefix:local}")
    public void setEnv(String e)
    {
        CacheKeyConstants.env = e;
    }

    private CacheKeyConstants() {}

    // —— 护理（PR4 使用） ——
    public static String nursingLevelById(Long id)
    {
        return "zzyl:" + env + ":nursing:level:byId:" + id;
    }

    public static String nursingProjectById(Long id)
    {
        return "zzyl:" + env + ":nursing:project:byId:" + id;
    }

    // —— 系统（PR2 使用，统一替换散落的 sys_config: / sys_dict:） ——
    public static String sysConfigByKey(String key)
    {
        return "zzyl:" + env + ":system:config:" + key;
    }

    public static String sysDictByType(String type)
    {
        return "zzyl:" + env + ":system:dict:" + type;
    }

    // —— 入住（PR5 使用） ——
    public static String admissionCheckInSeq(String dateStr)
    {
        return "zzyl:" + env + ":admission:seq:checkIn:" + dateStr;
    }
}
