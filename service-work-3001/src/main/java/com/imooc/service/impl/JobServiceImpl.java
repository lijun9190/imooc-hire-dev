package com.imooc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.imooc.api.feign.CompanyMicroServiceFeign;
import com.imooc.api.feign.UserInfoMicroServiceFeign;
import com.imooc.base.BaseInfoProperties;
import com.imooc.enums.JobStatus;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.mapper.JobMapper;
import com.imooc.pojo.ChatMessage;
import com.imooc.pojo.Job;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.EditJobBO;
import com.imooc.pojo.bo.SearchBO;
import com.imooc.pojo.bo.SearchJobsBO;
import com.imooc.pojo.vo.CompanyInfoVO;
import com.imooc.pojo.vo.SearchJobsVO;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.JobService;
import com.imooc.utils.GsonUtils;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * HR发布的职位表 服务实现类
 * </p>
 *
 * @author 风间影月
 * @since 2022-09-04
 */
@Service
public class JobServiceImpl extends BaseInfoProperties implements JobService {

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private UserInfoMicroServiceFeign userInfoMicroServiceFeign;

    @Autowired
    private CompanyMicroServiceFeign companyMicroServiceFeign;

    @Transactional
    @Override
    public void modifyJobDetail(EditJobBO editJobBO) {

        Job job = new Job();
        BeanUtils.copyProperties(editJobBO, job);

        job.setUpdatedTime(LocalDateTime.now());

        if (StringUtils.isBlank(editJobBO.getId())) {
            // 新增
            job.setStatus(JobStatus.OPEN.type);
            job.setCreateTime(LocalDateTime.now());
            jobMapper.insert(job);
        } else {
            // 修改
            jobMapper.update(job, new QueryWrapper<Job>()
                    .eq("id", editJobBO.getId())
                    .eq("hr_id", editJobBO.getHrId())
                    .eq("company_id", editJobBO.getCompanyId())
            );
        }

        redis.del(REDIS_JOB_DETAIL +
                ":" + editJobBO.getCompanyId() +
                ":" + editJobBO.getHrId() +
                ":" + editJobBO.getId()
        );
    }

    @Override
    public PagedGridResult queryJobList(String hrId,
                                        String companyId,
                                        Integer page,
                                        Integer pageSize,
                                        Integer status) {
        PageHelper.startPage(page, pageSize);

        QueryWrapper queryWrapper = new QueryWrapper<Job>();
        if (StringUtils.isNotBlank(hrId)) {
            queryWrapper.eq("hr_id", hrId);
        }

        queryWrapper.eq("company_id", companyId);

        if (status != null) {
            if (status == JobStatus.OPEN.type ||
                status == JobStatus.CLOSE.type ||
                status == JobStatus.DELETE.type) {
                queryWrapper.eq("status", status);
            }
        }

        queryWrapper.orderByDesc("updated_time");

        List<Job> jobList = jobMapper.selectList(queryWrapper);
        return setterPagedGrid(jobList, page);
    }

    @Override
    public Job queryJobDetail(String hrId, String companyId, String jobId) {
        Integer[] status = new Integer[]{
                JobStatus.OPEN.type,
                JobStatus.CLOSE.type,
                JobStatus.DELETE.type
        };

        QueryWrapper queryWrapper = new QueryWrapper<Job>();
        queryWrapper.eq("id", jobId);

        // 为啥要增加判断？因为增加判断可以提高扩展性，更加灵活
        // 可以让admin/company来调用查询，只需要增加接口即可，提高service的公用性
        if (StringUtils.isNotBlank(hrId)) {
            queryWrapper.eq("hr_id", hrId);
        }
        if (StringUtils.isNotBlank(companyId)) {
            queryWrapper.eq("company_id", companyId);
        }

        queryWrapper.in("status", status);

        Job job = jobMapper.selectOne(queryWrapper);

        redis.set(REDIS_JOB_DETAIL +
                ":" + companyId +
                ":" + hrId +
                ":" + jobId, GsonUtils.object2String(job)
        );
        return job;
    }

    @Transactional
    @Override
    public void modifyJobStatus(String hrId,
                                String companyId,
                                String jobId,
                                JobStatus jobStatus) {

        Job job = new Job();
        job.setStatus(jobStatus.type);
        job.setUpdatedTime(LocalDateTime.now());

        // 修改
        jobMapper.update(job, new QueryWrapper<Job>()
                .eq("id", jobId)
                .eq("hr_id", hrId)
                .eq("company_id", companyId)
        );

        redis.del(REDIS_JOB_DETAIL +
                ":" + companyId +
                ":" + hrId +
                ":" + jobId
        );
    }

    @Override
    public PagedGridResult searchJobs(SearchJobsBO searchJobsBO,
                                      Integer page,
                                      Integer pageSize) {

        String jobName = searchJobsBO.getJobName();
        String jobType = searchJobsBO.getJobType();
        String city = searchJobsBO.getCity();
        Integer beginSalary = searchJobsBO.getBeginSalary();
        Integer endSalary = searchJobsBO.getEndSalary();

        PageHelper.startPage(page, pageSize);

        QueryWrapper<Job> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", JobStatus.OPEN.type);

        if (StringUtils.isNotBlank(jobName)) {
            queryWrapper.like("job_name", jobName);
        }

        if (StringUtils.isNotBlank(jobType)) {
            queryWrapper.like("job_type", jobType);
        }

        if (StringUtils.isNotBlank(city)) {
            queryWrapper.like("city", city);
        }

        if (beginSalary > 0 && endSalary > 0) {
//            queryWrapper.ge("end_salary", beginSalary);
            // 优化薪资区间的查询
            queryWrapper.and(
                    qw -> qw.or(
                            // 职位最低薪资begin <= 求职薪资begin <= 职位最高薪资end
                            subQW -> subQW.ge("end_salary", beginSalary)
                                          .le("begin_salary", beginSalary)
                    )
                    .or(
                            // 职位最低薪资begin <= 求职薪资end <= 职位最高薪资end
                            subQW -> subQW.ge("end_salary", endSalary)
                                          .le("begin_salary", endSalary)
                    )
                    .or(
                            // 求职薪资begin <= 职位最低薪资begin  求职薪资end >= 职位最高薪资end
                            subQW -> subQW.ge("begin_salary", beginSalary)
                                          .le("end_salary", endSalary)
                    )
            );
        }

        List<Job> jobList = jobMapper.selectList(queryWrapper);

        // 为空则不需要执行后续的数据查询拼接操作了
        if (jobList == null || jobList.isEmpty() || jobList.size() == 0) {
            return setterPagedGrid(jobList, page);
        }

        // 根据每个job中的企业id，去获得企业信息
        List<String> companyIds = new ArrayList<>();
        // 根据每个job中的hrid，去获得hr用户信息
        List<String> hrIds = new ArrayList<>();

        // 构建VO对象，用户返回给前端
        List<SearchJobsVO> jobsVOList = new ArrayList<>();
        for (Job j : jobList) {
            hrIds.add(j.getHrId());
            companyIds.add(j.getCompanyId());

            SearchJobsVO searchJobsVO = new SearchJobsVO();
            BeanUtils.copyProperties(j, searchJobsVO);
            jobsVOList.add(searchJobsVO);
        }

        // 远程调用查询并且拼接hr用户信息
        SearchBO searchBO = new SearchBO();
        searchBO.setUserIds(hrIds);
        GraceJSONResult userResult = userInfoMicroServiceFeign.getList(searchBO);
        String userListStr = (String)userResult.getData();
        List<UsersVO> hrUsersList = GsonUtils.stringToListAnother(userListStr, UsersVO.class);

        for (SearchJobsVO j : jobsVOList) {
            for (UsersVO u : hrUsersList) {
                if (j.getHrId().equals(u.getId())) {
                    j.setUsersVO(u);
                }
            }
        }

        // 远程调用查询并且拼接企业信息
        searchBO.setCompanyIds(companyIds);
        GraceJSONResult companyResult = companyMicroServiceFeign.getList(searchBO);
        String companyListStr = (String)companyResult.getData();
        List<CompanyInfoVO> companyInfoVOList = GsonUtils.stringToListAnother(companyListStr, CompanyInfoVO.class);

        for (SearchJobsVO j : jobsVOList) {
            for (CompanyInfoVO c : companyInfoVOList) {
                if (j.getCompanyId().equals(c.getCompanyId())) {
                    j.setCompanyInfoVO(c);
                }
            }
        }

        PagedGridResult gridResult = setterPagedGrid(jobList, page);
        gridResult.setRows(jobsVOList);

        return gridResult;
    }
}
