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
import com.zzyl.admission.domain.HealthAssessment;
import com.zzyl.admission.service.IHealthAssessmentService;
import com.zzyl.common.annotation.Log;
import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.common.enums.BusinessType;
import com.zzyl.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/admission/assessment")
public class HealthAssessmentController extends BaseController
{
    @Autowired
    private IHealthAssessmentService healthAssessmentService;

    @PreAuthorize("@ss.hasPermi('admission:assessment:list')")
    @GetMapping("/list")
    public TableDataInfo list(HealthAssessment healthAssessment)
    {
        startPage();
        List<HealthAssessment> list = healthAssessmentService.selectHealthAssessmentList(healthAssessment);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('admission:assessment:export')")
    @Log(title = "健康评估", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HealthAssessment healthAssessment)
    {
        List<HealthAssessment> list = healthAssessmentService.selectHealthAssessmentList(healthAssessment);
        ExcelUtil<HealthAssessment> util = new ExcelUtil<>(HealthAssessment.class);
        util.exportExcel(response, list, "健康评估数据");
    }

    @PreAuthorize("@ss.hasPermi('admission:assessment:query')")
    @GetMapping("/{id:\\d+}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(healthAssessmentService.selectHealthAssessmentById(id));
    }

    @PreAuthorize("@ss.hasPermi('admission:assessment:add')")
    @Log(title = "健康评估", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HealthAssessment healthAssessment)
    {
        return toAjax(healthAssessmentService.insertHealthAssessment(healthAssessment));
    }

    @PreAuthorize("@ss.hasPermi('admission:assessment:edit')")
    @Log(title = "健康评估", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HealthAssessment healthAssessment)
    {
        return toAjax(healthAssessmentService.updateHealthAssessment(healthAssessment));
    }

    @PreAuthorize("@ss.hasPermi('admission:assessment:remove')")
    @Log(title = "健康评估", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(healthAssessmentService.deleteHealthAssessmentByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('admission:assessment:edit')")
    @Log(title = "健康评估", businessType = BusinessType.UPDATE)
    @PutMapping("/complete/{id:\\d+}")
    public AjaxResult complete(@PathVariable Long id)
    {
        return toAjax(healthAssessmentService.completeAssessment(id));
    }
}
