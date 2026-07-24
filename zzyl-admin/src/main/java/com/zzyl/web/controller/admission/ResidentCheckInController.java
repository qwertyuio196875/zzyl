package com.zzyl.web.controller.admission;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zzyl.admission.domain.ResidentCheckIn;
import com.zzyl.admission.service.IResidentCheckInService;
import com.zzyl.common.annotation.Log;
import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.common.enums.BusinessType;
import com.zzyl.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/admission/checkin")
public class ResidentCheckInController extends BaseController
{
    @Autowired
    private IResidentCheckInService residentCheckInService;

    @PreAuthorize("@ss.hasPermi('admission:checkin:list')")
    @GetMapping("/list")
    public TableDataInfo list(ResidentCheckIn residentCheckIn)
    {
        startPage();
        List<ResidentCheckIn> list = residentCheckInService.selectResidentCheckInList(residentCheckIn);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('admission:checkin:query')")
    @GetMapping("/checkedInList")
    public AjaxResult checkedInList()
    {
        return success(residentCheckInService.selectCheckedInList());
    }

    @PreAuthorize("@ss.hasPermi('admission:checkin:export')")
    @Log(title = "入住办理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ResidentCheckIn residentCheckIn)
    {
        List<ResidentCheckIn> list = residentCheckInService.selectResidentCheckInList(residentCheckIn);
        ExcelUtil<ResidentCheckIn> util = new ExcelUtil<>(ResidentCheckIn.class);
        util.exportExcel(response, list, "入住办理数据");
    }

    @PreAuthorize("@ss.hasPermi('admission:checkin:query')")
    @GetMapping("/{id:\\d+}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(residentCheckInService.selectResidentCheckInById(id));
    }

    @PreAuthorize("@ss.hasPermi('admission:checkin:add')")
    @Log(title = "入住办理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ResidentCheckIn residentCheckIn)
    {
        return toAjax(residentCheckInService.insertResidentCheckIn(residentCheckIn));
    }

    @PreAuthorize("@ss.hasPermi('admission:checkin:edit')")
    @Log(title = "入住办理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ResidentCheckIn residentCheckIn)
    {
        return toAjax(residentCheckInService.updateResidentCheckIn(residentCheckIn));
    }

    @PreAuthorize("@ss.hasPermi('admission:checkin:remove')")
    @Log(title = "入住办理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(residentCheckInService.deleteResidentCheckInByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('admission:checkin:confirm')")
    @Log(title = "入住办理", businessType = BusinessType.UPDATE)
    @PutMapping("/confirm/{id:\\d+}")
    public AjaxResult confirm(@PathVariable Long id)
    {
        return toAjax(residentCheckInService.confirmCheckIn(id));
    }

    @PreAuthorize("@ss.hasPermi('admission:checkin:edit')")
    @Log(title = "入住办理", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{id:\\d+}")
    public AjaxResult cancel(@PathVariable Long id)
    {
        return toAjax(residentCheckInService.cancelCheckIn(id));
    }
}
