package com.zzyl.admission.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.zzyl.admission.domain.ResidentCheckOut;

@Mapper
public interface ResidentCheckOutMapper
{
    ResidentCheckOut selectResidentCheckOutById(Long id);

    List<ResidentCheckOut> selectResidentCheckOutList(ResidentCheckOut residentCheckOut);

    int insertResidentCheckOut(ResidentCheckOut residentCheckOut);

    int updateResidentCheckOut(ResidentCheckOut residentCheckOut);

    int deleteResidentCheckOutById(Long id);

    int deleteResidentCheckOutByIds(Long[] ids);
}
