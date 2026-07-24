package com.zzyl.admission.service;

import java.util.List;
import com.zzyl.admission.domain.ResidentCheckIn;

public interface IResidentCheckInService
{
    ResidentCheckIn selectResidentCheckInById(Long id);

    List<ResidentCheckIn> selectResidentCheckInList(ResidentCheckIn residentCheckIn);

    List<ResidentCheckIn> selectCheckedInList();

    int insertResidentCheckIn(ResidentCheckIn residentCheckIn);

    int updateResidentCheckIn(ResidentCheckIn residentCheckIn);

    int deleteResidentCheckInByIds(Long[] ids);

    int confirmCheckIn(Long id);

    int cancelCheckIn(Long id);
}
