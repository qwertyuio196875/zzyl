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
import com.zzyl.admission.domain.CheckoutApproveDto;
import com.zzyl.admission.domain.CheckoutCompleteDto;
import com.zzyl.admission.domain.ResidentCheckOut;
import com.zzyl.admission.service.IResidentCheckOutService;
import com.zzyl.common.annotation.Log;
import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.common.enums.BusinessType;
import com.zzyl.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/admission/checkout")
public class ResidentCheckOutController extends BaseController
{
    @Autowired
    private IResidentCheckOutService residentCheckOutService;

    @PreAuthorize("@ss.hasPermi('admission:checkout:list')")
    @GetMapping("/list")
    public TableDataInfo list(ResidentCheckOut residentCheckOut)
    {
        startPage();
        List<ResidentCheckOut> list = residentCheckOutService.selectResidentCheckOutList(residentCheckOut);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('admission:checkout:export')")
    @Log(title = "退住管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ResidentCheckOut residentCheckOut)
    {
        List<ResidentCheckOut> list = residentCheckOutService.selectResidentCheckOutList(residentCheckOut);
        ExcelUtil<ResidentCheckOut> util = new ExcelUtil<>(ResidentCheckOut.class);
        util.exportExcel(response, list, "退住管理数据");
    }

    @PreAuthorize("@ss.hasPermi('admission:checkout:query')")
    @GetMapping("/{id:\\d+}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(residentCheckOutService.selectResidentCheckOutById(id));
    }

    @PreAuthorize("@ss.hasPermi('admission:checkout:add')")
    @Log(title = "退住管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ResidentCheckOut residentCheckOut)
    {
        return toAjax(residentCheckOutService.insertResidentCheckOut(residentCheckOut));
    }

    @PreAuthorize("@ss.hasPermi('admission:checkout:edit')")
    @Log(title = "退住管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ResidentCheckOut residentCheckOut)
    {
        return toAjax(residentCheckOutService.updateResidentCheckOut(residentCheckOut));
    }

    @PreAuthorize("@ss.hasPermi('admission:checkout:remove')")
    @Log(title = "退住管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(residentCheckOutService.deleteResidentCheckOutByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('admission:checkout:approve')")
    @Log(title = "退住管理", businessType = BusinessType.UPDATE)
    @PutMapping("/approve")
    public AjaxResult approve(@RequestBody CheckoutApproveDto dto)
    {
        return toAjax(residentCheckOutService.approveCheckout(dto));
    }

    @PreAuthorize("@ss.hasPermi('admission:checkout:complete')")
    @Log(title = "退住管理", businessType = BusinessType.UPDATE)
    @PutMapping("/complete")
    public AjaxResult complete(@RequestBody CheckoutCompleteDto dto)
    {
        return toAjax(residentCheckOutService.completeCheckout(dto));
    }
}
