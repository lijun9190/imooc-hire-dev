package com.imooc.service.impl;

import com.github.pagehelper.PageInfo;
import com.imooc.base.BaseInfoProperties;
import com.imooc.enums.DealStatus;
import com.imooc.enums.JobStatus;
import com.imooc.mapper.JobMapper;
import com.imooc.pojo.Job;
import com.imooc.pojo.bo.SearchReportJobBO;
import com.imooc.pojo.mo.ReportMO;
import com.imooc.repository.ReportJobRepository;
import com.imooc.service.ReportService;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ReportServiceImpl extends BaseInfoProperties implements ReportService {

    @Autowired
    private ReportJobRepository reportJobRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JobMapper jobMapper;

    @Override
    public void saveReportRecord(ReportMO reportMO) {

        reportMO.setDealStatus(DealStatus.WAITING.type);
        reportMO.setCreatedTime(LocalDateTime.now());
        reportMO.setUpdatedTime(LocalDateTime.now());

        reportJobRepository.save(reportMO);
    }

    @Override
    public boolean isReportRecordExist(String reportUserId, String jobId) {

        ReportMO record = reportJobRepository.findByReportUserIdAndJobId(reportUserId,
                                                                         jobId);
        return record == null ? false : true;
    }

    @Override
    public PagedGridResult pagedReportRecordList(SearchReportJobBO reportJobBO,
                                                 Integer page,
                                                 Integer pageSize) {
        String jobName = reportJobBO.getJobName();
        String companyName = reportJobBO.getCompanyName();
        String reportUserName = reportJobBO.getReportUserName();
        Integer dealStatus = reportJobBO.getDealStatus();
        LocalDateTime beginDate = reportJobBO.getBeginDateTime();
        LocalDateTime endDate = reportJobBO.getEndDateTime();

        // 1. 创建查询对象
        Query query = new Query();

        // 2. 创建条件对象
        //Criteria criteria = new Criteria();

        // 3. 设置查询条件参数
        if (StringUtils.isNotBlank(jobName)) {
            query = addLikeByValue(query, "job_name", jobName);
        }
        if (StringUtils.isNotBlank(companyName)) {
            query = addLikeByValue(query, "company_name", companyName);
        }
        if (StringUtils.isNotBlank(reportUserName)) {
            query = addLikeByValue(query, "report_user_name", reportUserName);
        }

        if (dealStatus != null) {
            query.addCriteria(Criteria.where("deal_status").is(dealStatus));
        }

        if (beginDate != null && endDate == null) {
            query.addCriteria(Criteria.where("created_time").gte(beginDate));
        } else if (beginDate == null && endDate != null) {
            query.addCriteria(Criteria.where("created_time").lte(endDate));
        } else if (beginDate != null && endDate != null) {
            query.addCriteria(Criteria.where("created_time").gte(beginDate).lte(endDate));
        }

        // 4. 查询记录总数，必须在分页前查询，否则总数不对
        long counts = mongoTemplate.count(query, ReportMO.class);

        // 5. 设置分页
        Pageable pageable = PageRequest.of(page,
                                            pageSize,
                                            Sort.Direction.DESC,
                                            "created_time");
        query.with(pageable);

        // 6. 执行查询
        List<ReportMO> list = mongoTemplate.find(query, ReportMO.class);

        // 7. 封装分页grid信息数据
        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(list);
        gridResult.setPage(page);
        gridResult.setRecords(counts);

        return gridResult;
    }

    private Query addLikeByValue(Query query, String key, String value) {
        // 拼接 正则表达式和查询参数
        Pattern pattern = Pattern.compile("^.*" + value + ".*$");
        // 指定要查询的属性
        query.addCriteria(Criteria.where(key).regex(pattern));
        return query;
    }

    @Transactional
    @Override
    public void updateReportRecordStatus(String reportId, DealStatus status) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(reportId));

        Update update = new Update();
        update.set("deal_status", status.type);
        update.set("updated_time", LocalDateTime.now());

        if (status == DealStatus.DONE) {
            ReportMO tmp = reportJobRepository.findById(reportId).get();

            String jobId = tmp.getJobId();
            Job pending = new Job();
            pending.setId(jobId);
            pending.setStatus(JobStatus.DELETE.type);
            pending.setViolateReason(tmp.getReportReason());
            pending.setUpdatedTime(LocalDateTime.now());
            jobMapper.updateById(pending);

            Job job = jobMapper.selectById(jobId);
            redis.del(REDIS_JOB_DETAIL +
                    ":" + job.getCompanyId() +
                    ":" + job.getHrId() +
                    ":" + jobId);
        }

        mongoTemplate.updateFirst(query, update, ReportMO.class);
    }
}
