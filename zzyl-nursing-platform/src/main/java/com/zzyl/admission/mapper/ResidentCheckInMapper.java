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

    /**
     * 确认入住（O6：带 status 条件 update，返回 0 表示状态已变更或记录不存在）
     * WHERE 条件 status IN ('0','1') —— 状态机保证仅"待办理"或"办理中"可推进到"已入住"。
     * 并发场景下只有一个事务会返回 1，其余返回 0，由 Service 层抛 ServiceException。
     */
    int confirmCheckIn(ResidentCheckIn residentCheckIn);

    /**
     * 取消入住（O6：带 status 条件 update，返回 0 表示状态已变更或记录不存在）
     * WHERE 条件 status IN ('0','1') —— 防止在已入住/已退住状态下被重复取消。
     */
    int cancelCheckIn(ResidentCheckIn residentCheckIn);

    int deleteResidentCheckInById(Long id);

    int deleteResidentCheckInByIds(Long[] ids);
}
