package com.imooc.controller;

import com.imooc.api.intercept.JWTCurrentUserInterceptor;
import com.imooc.base.BaseInfoProperties;
import com.imooc.enums.JobStatus;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.Job;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.EditJobBO;
import com.imooc.pojo.bo.SearchJobsBO;
import com.imooc.service.JobService;
import com.imooc.utils.GsonUtils;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("job")
public class JobController extends BaseInfoProperties {

    @Autowired
    private JobService jobService;

    /**
     * 新增或者编辑职位
     * @param editJobBO
     * @return
     */
    @PostMapping("modify")
    public GraceJSONResult modify(@RequestBody @Valid EditJobBO editJobBO) {

        // TODO EditJobBO 自行校验

        jobService.modifyJobDetail(editJobBO);

        return GraceJSONResult.ok();
    }

    /**
     * 分页查询职位列表
     * @param hrId
     * @param companyId
     * @param page
     * @param limit
     * @param status
     * @return
     */
    @PostMapping("hr/jobList")
    public GraceJSONResult jobListHR(String hrId,
                                  String companyId,
                                  Integer page,
                                  Integer limit,
                                  Integer status) {

        if (StringUtils.isBlank(hrId)) {
            return GraceJSONResult.errorMsg("hrId 不能为空");
        }

        if (page == null) page = 1;
        if (limit == null) limit = 10;

        PagedGridResult gridResult = jobService.queryJobList(hrId,
                                                            companyId,
                                                            page,
                                                            limit,
                                                            status);

        return GraceJSONResult.ok(gridResult);
    }

    /**
     * 获得历史职位列表
     * @param page
     * @param limit
     * @return
     */
    @PostMapping("jobList")
    public GraceJSONResult jobListCompany(Integer page, Integer limit) {

        if (page == null) page = 1;
        if (limit == null) limit = 10;

        Users users = JWTCurrentUserInterceptor.currentUser.get();
        String companyId = users.getHrInWhichCompanyId();

        PagedGridResult gridResult = jobService.queryJobList(null,
                companyId,
                page,
                limit,
                null);

        return GraceJSONResult.ok(gridResult);
    }

    /**
     * 查询职位详情
     * @param hrId
     * @param companyId
     * @param jobId
     * @return
     */
    @PostMapping("hr/jobDetail")
    public GraceJSONResult jobDetailHR(String hrId,
                                  String companyId,
                                  String jobId) {

        if (StringUtils.isBlank(hrId) || StringUtils.isBlank(companyId) || StringUtils.isBlank(jobId)) {
            return GraceJSONResult.error();
        }

        String jobDetailStr = redis.get(REDIS_JOB_DETAIL + ":" + companyId + ":" + hrId + ":" + jobId);
        Job job = null;
        if (StringUtils.isBlank(jobDetailStr)) {
            job = jobService.queryJobDetail(hrId, companyId, jobId);
        } else {
            job = GsonUtils.stringToBean(jobDetailStr, Job.class);
        }

        return GraceJSONResult.ok(job);
    }

    /**
     * 企业和admin查询职位详情
     * @param jobId
     * @return
     */
    @PostMapping("admin/jobDetail")
    public GraceJSONResult jobDetailAdminOrCompany(String jobId) {

        if (StringUtils.isBlank(jobId)) {
            return GraceJSONResult.error();
        }

        Job job = jobService.queryJobDetail(null, null, jobId);

        return GraceJSONResult.ok(job);
    }

    /**
     * 关闭和开启职位分为两个不同接口，为何不写在一起？
     * 因为如果写在同一个接口，无疑我们需要传入status状态作为参数
     * 只要接口存在参数，那就会增加风险，可能会被黑客攻击
     * 而且按照目前的接口解耦规范，我们也需要分开作为两个不同的接口
     * 另外，有判断就会有计算的损耗
     */

    /**
     * 关闭职位
     * @param hrId
     * @param companyId
     * @param jobId
     * @return
     */
    @PostMapping("close")
    public GraceJSONResult jobClose(String hrId, String companyId, String jobId) {

        if (StringUtils.isBlank(hrId) || StringUtils.isBlank(companyId) || StringUtils.isBlank(jobId)) {
            return GraceJSONResult.error();
        }

        jobService.modifyJobStatus(hrId, companyId, jobId, JobStatus.CLOSE);

        return GraceJSONResult.ok();
    }

    /**
     * 开放职位
     * @param hrId
     * @param companyId
     * @param jobId
     * @return
     */
    @PostMapping("open")
    public GraceJSONResult jobOpen(String hrId, String companyId, String jobId) {

        if (StringUtils.isBlank(hrId) || StringUtils.isBlank(companyId) || StringUtils.isBlank(jobId)) {
            return GraceJSONResult.error();
        }

        jobService.modifyJobStatus(hrId, companyId, jobId, JobStatus.OPEN);

        return GraceJSONResult.ok();
    }

    /**
     * 候选人搜索职位
     * @param searchJobsBO
     * @param page
     * @param limit
     * @return
     */
    @PostMapping("searchJobs")
    public GraceJSONResult searchJobs(@RequestBody SearchJobsBO searchJobsBO,
                                      Integer page,
                                      Integer limit) {

        if (page == null) page = 1;
        if (limit == null) limit = 10;

        PagedGridResult gridResult = jobService.searchJobs(searchJobsBO, page, limit);

        return GraceJSONResult.ok(gridResult);
    }
}
