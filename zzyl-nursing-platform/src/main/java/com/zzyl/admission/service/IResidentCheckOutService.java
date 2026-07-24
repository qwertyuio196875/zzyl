package com.zzyl.admission.service;

import java.util.List;
import com.zzyl.admission.domain.CheckoutApproveDto;
import com.zzyl.admission.domain.CheckoutCompleteDto;
import com.zzyl.admission.domain.ResidentCheckOut;

public interface IResidentCheckOutService
{
    ResidentCheckOut selectResidentCheckOutById(Long id);

    List<ResidentCheckOut> selectResidentCheckOutList(ResidentCheckOut residentCheckOut);

    int insertResidentCheckOut(ResidentCheckOut residentCheckOut);

    int updateResidentCheckOut(ResidentCheckOut residentCheckOut);

    int deleteResidentCheckOutByIds(Long[] ids);

    int approveCheckout(CheckoutApproveDto dto);

    int completeCheckout(CheckoutCompleteDto dto);
}
