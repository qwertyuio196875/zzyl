package com.zzyl.admission.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.zzyl.admission.domain.CheckoutApproveDto;
import com.zzyl.admission.domain.CheckoutCompleteDto;
import com.zzyl.admission.domain.ResidentCheckIn;
import com.zzyl.admission.domain.ResidentCheckOut;
import com.zzyl.admission.mapper.ResidentCheckInMapper;
import com.zzyl.admission.mapper.ResidentCheckOutMapper;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.common.utils.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class ResidentCheckOutServiceImplTest
{
    @Mock
    private ResidentCheckOutMapper residentCheckOutMapper;

    @Mock
    private ResidentCheckInMapper residentCheckInMapper;

    @InjectMocks
    private ResidentCheckOutServiceImpl residentCheckOutService;

    @Test
    void insertShouldRejectNonCheckedInResident()
    {
        ResidentCheckIn checkIn = new ResidentCheckIn();
        checkIn.setId(1L);
        checkIn.setStatus("0");
        when(residentCheckInMapper.selectResidentCheckInById(1L)).thenReturn(checkIn);

        ResidentCheckOut input = new ResidentCheckOut();
        input.setCheckInId(1L);

        assertThrows(ServiceException.class, () -> residentCheckOutService.insertResidentCheckOut(input));
    }

    @Test
    void insertShouldFillResidentInfo()
    {
        ResidentCheckIn checkIn = new ResidentCheckIn();
        checkIn.setId(1L);
        checkIn.setStatus("2");
        checkIn.setElderName("王五");
        checkIn.setIdCard("110101199001011234");
        checkIn.setBedNo("A101");
        when(residentCheckInMapper.selectResidentCheckInById(1L)).thenReturn(checkIn);
        when(residentCheckOutMapper.selectResidentCheckOutList(any())).thenReturn(Collections.emptyList());
        when(residentCheckOutMapper.insertResidentCheckOut(any())).thenReturn(1);

        ResidentCheckOut input = new ResidentCheckOut();
        input.setCheckInId(1L);
        input.setReason("1");

        int rows = residentCheckOutService.insertResidentCheckOut(input);

        assertEquals(1, rows);
        assertNotNull(input.getCheckOutNo());
        assertEquals("王五", input.getElderName());
    }

    @Test
    void approveShouldUpdateStatus()
    {
        ResidentCheckOut existing = new ResidentCheckOut();
        existing.setId(1L);
        existing.setStatus("0");
        when(residentCheckOutMapper.selectResidentCheckOutById(1L)).thenReturn(existing);
        when(residentCheckOutMapper.updateResidentCheckOut(any())).thenReturn(1);

        CheckoutApproveDto dto = new CheckoutApproveDto();
        dto.setId(1L);
        dto.setApproved(true);
        dto.setApproveRemark("同意");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class))
        {
            securityUtils.when(SecurityUtils::getUsername).thenReturn("admin");
            int rows = residentCheckOutService.approveCheckout(dto);
            assertEquals(1, rows);
        }
    }

    @Test
    void completeShouldUpdateCheckInStatus()
    {
        ResidentCheckOut existing = new ResidentCheckOut();
        existing.setId(1L);
        existing.setCheckInId(2L);
        existing.setStatus("1");
        when(residentCheckOutMapper.selectResidentCheckOutById(1L)).thenReturn(existing);
        when(residentCheckOutMapper.updateResidentCheckOut(any())).thenReturn(1);
        when(residentCheckInMapper.updateResidentCheckIn(any())).thenReturn(1);

        CheckoutCompleteDto dto = new CheckoutCompleteDto();
        dto.setId(1L);

        int rows = residentCheckOutService.completeCheckout(dto);

        assertEquals(1, rows);
        verify(residentCheckInMapper).updateResidentCheckIn(any());
    }
}
