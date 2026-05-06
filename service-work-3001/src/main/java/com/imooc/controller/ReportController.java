package com.imooc.controller;

import com.imooc.base.BaseInfoProperties;
import com.imooc.enums.DealStatus;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.bo.SearchReportJobBO;
import com.imooc.pojo.mo.ReportMO;
import com.imooc.service.ReportService;
import com.imooc.utils.LocalDateUtils;
import com.imooc.utils.PagedGridResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("report")
public class ReportController extends BaseInfoProperties {

    @Autowired
    private ReportService reportService;

    /**
     * 新增举报记录
     * @param reportMO
     * @return
     */
    @PostMapping("create")
    public GraceJSONResult create(@RequestBody @Valid ReportMO reportMO) {

        // TODO ReportMO 自行校验

        boolean isExist = reportService.isReportRecordExist(reportMO.getReportUserId(),
                                                            reportMO.getJobId());
        if (!isExist) {
            reportService.saveReportRecord(reportMO);
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.REPORT_RECORD_EXIST_ERROR);
        }

        return GraceJSONResult.ok();
    }

    /**
     * 用于在运管端查询举报记录的列表
     * @param reportJobBO
     * @param page
     * @param pageSize
     * @return
     */
    @PostMapping("pagedReportRecordList")
    public GraceJSONResult pagedReportRecordList(@RequestBody SearchReportJobBO reportJobBO,
                                                 Integer page,
                                                 Integer pageSize) {

        if (page == null) page = COMMON_START_PAGE_ZERO;
        if (pageSize == null) pageSize = COMMON_PAGE_SIZE;

        LocalDate beginDate = reportJobBO.getBeginDate();
        LocalDate endDate = reportJobBO.getEndDate();

        if (beginDate != null) {
            // 开始日期加上时间: 00:00:00
            String beginDateTimeStr = LocalDateUtils.format(beginDate,
                                            LocalDateUtils.DATE_PATTERN) + " 00:00:00";

            LocalDateTime beginDateTime = LocalDateUtils.parseLocalDateTime(beginDateTimeStr,
                                                            LocalDateUtils.DATETIME_PATTERN);
            reportJobBO.setBeginDateTime(beginDateTime);
        }

        if (endDate != null) {
            // 结束日期加上时间: 23:59:59
            String endDateTimeStr = LocalDateUtils.format(endDate,
                    LocalDateUtils.DATE_PATTERN) + " 23:59:59";

            LocalDateTime endDateTime = LocalDateUtils.parseLocalDateTime(endDateTimeStr,
                    LocalDateUtils.DATETIME_PATTERN);
            reportJobBO.setEndDateTime(endDateTime);
        }

        System.out.println(reportJobBO.toString());

        PagedGridResult gridResult = reportService.pagedReportRecordList(reportJobBO, page, pageSize);

        return GraceJSONResult.ok(gridResult);
    }

    /**
     * 删除职位
     * @param reportId
     * @return
     */
    @PostMapping("deal/delete")
    public GraceJSONResult delete(String reportId) {
        reportService.updateReportRecordStatus(reportId, DealStatus.DONE);
        return GraceJSONResult.ok();
    }

    /**
     * 忽略职位
     * @param reportId
     * @return
     */
    @PostMapping("deal/ignore")
    public GraceJSONResult ignore(String reportId) {
        reportService.updateReportRecordStatus(reportId, DealStatus.IGNORE);
        return GraceJSONResult.ok();
    }
}
