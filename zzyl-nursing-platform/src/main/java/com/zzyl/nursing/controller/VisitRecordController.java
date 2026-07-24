package com.zzyl.nursing.controller;

import java.util.List;

import com.zzyl.common.annotation.Log;
import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.enums.BusinessType;
import com.zzyl.nursing.domain.VisitRecord;
import com.zzyl.nursing.service.IVisitRecordService;
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

import com.zzyl.common.core.page.TableDataInfo;

/**
 * 来访记录Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/nursing/visit")
public class VisitRecordController extends BaseController
{
    @Autowired
    private IVisitRecordService visitRecordService;

    /**
     * 查询来访记录列表
     */
    @PreAuthorize("@ss.hasPermi('nursing:visit:list')")
    @GetMapping("/list")
    public TableDataInfo list(VisitRecord visitRecord)
    {
        startPage();
        List<VisitRecord> list = visitRecordService.selectVisitRecordList(visitRecord);
        return getDataTable(list);
    }

    /**
     * 导出来访记录列表
     */
    @PreAuthorize("@ss.hasPermi('nursing:visit:export')")
    @Log(title = "来访记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(VisitRecord visitRecord)
    {
        List<VisitRecord> list = visitRecordService.selectVisitRecordList(visitRecord);
        //ExcelUtil<VisitRecord> util = new ExcelUtil<VisitRecord>(VisitRecord.class);
        //util.exportExcel(response, list, "来访记录数据");
    }

    /**
     * 获取来访记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('nursing:visit:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(visitRecordService.selectVisitRecordById(id));
    }

    /**
     * 新增来访记录
     */
    @PreAuthorize("@ss.hasPermi('nursing:visit:add')")
    @Log(title = "来访记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody VisitRecord visitRecord)
    {
        return toAjax(visitRecordService.insertVisitRecord(visitRecord));
    }

    /**
     * 修改来访记录
     */
    @PreAuthorize("@ss.hasPermi('nursing:visit:edit')")
    @Log(title = "来访记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VisitRecord visitRecord)
    {
        return toAjax(visitRecordService.updateVisitRecord(visitRecord));
    }

    /**
     * 删除来访记录
     */
    @PreAuthorize("@ss.hasPermi('nursing:visit:remove')")
    @Log(title = "来访记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(visitRecordService.deleteVisitRecordByIds(ids));
    }

    /**
     * 审核来访记录
     */
    @PreAuthorize("@ss.hasPermi('nursing:visit:approve')")
    @Log(title = "来访记录", businessType = BusinessType.UPDATE)
    @PutMapping("/approve/{id}")
    public AjaxResult approve(@PathVariable Long id)
    {
        return toAjax(visitRecordService.approveVisitRecord(id));
    }

    /**
     * 取消来访记录
     */
    @PreAuthorize("@ss.hasPermi('nursing:visit:cancel')")
    @Log(title = "来访记录", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{id}")
    public AjaxResult cancel(@PathVariable Long id)
    {
        return toAjax(visitRecordService.cancelVisitRecord(id));
    }

    /**
     * 签到来访记录
     */
    @PreAuthorize("@ss.hasPermi('nursing:visit:signIn')")
    @Log(title = "来访记录", businessType = BusinessType.UPDATE)
    @PutMapping("/signIn/{id}")
    public AjaxResult signIn(@PathVariable Long id)
    {
        return toAjax(visitRecordService.signInVisitRecord(id));
    }

    /**
     * 离开登记
     */
    @PreAuthorize("@ss.hasPermi('nursing:visit:leave')")
    @Log(title = "来访记录", businessType = BusinessType.UPDATE)
    @PutMapping("/leave/{id}")
    public AjaxResult leave(@PathVariable Long id)
    {
        return toAjax(visitRecordService.leaveVisitRecord(id));
    }
}
