package com.zzyl.admission.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 入退管理业务编号生成
 */
public final class AdmissionNoUtils
{
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private AdmissionNoUtils() {}

    public static String assessmentNo()
    {
        return "HA" + nextSuffix();
    }

    public static String checkInNo()
    {
        return "CI" + nextSuffix();
    }

    public static String checkOutNo()
    {
        return "CO" + nextSuffix();
    }

    private static String nextSuffix()
    {
        String time = LocalDateTime.now().format(FORMATTER);
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return time + random;
    }
}
