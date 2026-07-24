package com.zzyl.admission.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.zzyl.admission.domain.ResidentCheckIn;

@Mapper
public interface ResidentCheckInMapper
{
    ResidentCheckIn selectResidentCheckInById(Long id);

    List<ResidentCheckIn> selectResidentCheckInList(ResidentCheckIn residentCheckIn);

    List<ResidentCheckIn> selectCheckedInList();

    int insertResidentCheckIn(ResidentCheckIn residentCheckIn);

    int updateResidentCheckIn(ResidentCheckIn residentCheckIn);

    int deleteResidentCheckInById(Long id);

    int deleteResidentCheckInByIds(Long[] ids);
}
