package com.zzyl.admission.util;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class AdmissionNoUtilsTest
{
    @Test
    void shouldGenerateDistinctBusinessNumbers()
    {
        String assessmentNo = AdmissionNoUtils.assessmentNo();
        String checkInNo = AdmissionNoUtils.checkInNo();
        String checkOutNo = AdmissionNoUtils.checkOutNo();

        assertTrue(assessmentNo.startsWith("HA"));
        assertTrue(checkInNo.startsWith("CI"));
        assertTrue(checkOutNo.startsWith("CO"));
        assertNotEquals(assessmentNo, checkInNo);
    }
}
