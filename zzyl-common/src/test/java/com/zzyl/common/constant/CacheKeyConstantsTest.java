package com.zzyl.common.constant;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CacheKeyConstantsTest
{
    @Test
    void env_defaults_to_local()
    {
        assertEquals("local", CacheKeyConstants.env());
    }

    @Test
    void nursingLevelById_returns_correct_key()
    {
        assertEquals("zzyl:local:nursing:level:byId:1", CacheKeyConstants.nursingLevelById(1L));
    }

    @Test
    void nursingProjectById_returns_correct_key()
    {
        assertEquals("zzyl:local:nursing:project:byId:42", CacheKeyConstants.nursingProjectById(42L));
    }

    @Test
    void sysConfigByKey_returns_correct_key()
    {
        assertEquals("zzyl:local:system:config:captcha", CacheKeyConstants.sysConfigByKey("captcha"));
    }

    @Test
    void sysDictByType_returns_correct_key()
    {
        assertEquals("zzyl:local:system:dict:sex", CacheKeyConstants.sysDictByType("sex"));
    }

    @Test
    void admissionCheckInSeq_returns_correct_key()
    {
        assertEquals("zzyl:local:admission:seq:checkIn:20260801", CacheKeyConstants.admissionCheckInSeq("20260801"));
    }
}
