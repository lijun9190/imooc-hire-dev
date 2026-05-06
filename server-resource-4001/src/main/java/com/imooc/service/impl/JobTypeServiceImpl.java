package com.imooc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.imooc.base.BaseInfoProperties;
import com.imooc.mapper.JobTypeMapper;
import com.imooc.mapper.JobTypeMapperCustom;
import com.imooc.pojo.JobType;
import com.imooc.pojo.vo.JobTypeSecondAndThirdVO;
import com.imooc.service.JobTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 职位类别 服务实现类
 * </p>
 *
 * @author 风间影月
 * @since 2022-07-12
 */
@Service
public class JobTypeServiceImpl extends BaseInfoProperties implements JobTypeService {

    @Autowired
    private JobTypeMapper jobTypeMapper;

    @Autowired
    private JobTypeMapperCustom jobTypeMapperCustom;

    @Override
    public List<JobType> getTopJobTypeList() {
        List<JobType> topJobTypeList = jobTypeMapper.selectList(
                new QueryWrapper<JobType>()
                        .eq("father_id", 0)
                        .orderByAsc("sort")
        );

        return topJobTypeList;
    }

    @Override
    public List<JobType> getThirdListByTop(String topJobTypeId) {
        Map<String, Object> map = new HashMap<>();
        map.put("topJobTypeId", topJobTypeId);
        List<JobType> jobTypeList = jobTypeMapperCustom.getThirdJobTypeByTop(map);

        return jobTypeList;
    }

    @Override
    public List<JobTypeSecondAndThirdVO> getSecondAndThirdListByTop(String topJobTypeId) {
        Map<String, Object> map = new HashMap<>();
        map.put("topJobTypeId", topJobTypeId);
        List<JobTypeSecondAndThirdVO> list = jobTypeMapperCustom.getSecondAndThirdListByTop(map);

        return list;
    }

    @Override
    public List<JobType> getChildrenJobTypeList(String jobTypeId) {
        List<JobType> topJobTypeList = jobTypeMapper.selectList(
                new QueryWrapper<JobType>()
                        .eq("father_id", jobTypeId)
                        .orderByAsc("sort")
        );

        return topJobTypeList;
    }

    @Override
    public Long getChildrenJobTypeCounts(String jobTypeId) {
        Long counts = jobTypeMapper.selectCount(
                new QueryWrapper<JobType>()
                        .eq("father_id", jobTypeId)
        );
        return counts;
    }

    @Override
    public boolean getJobTypeIsExistByName(String name) {
        JobType jobType = jobTypeMapper.selectOne(new QueryWrapper<JobType>()
                .eq("name", name));

        return jobType != null ? true : false;
    }

    @Override
    public JobType getJobTypeById(String id) {
        return jobTypeMapper.selectById(id);
    }

    @Override
    public void createJobType(JobType jobType) {
        jobTypeMapper.insert(jobType);
    }

    @Override
    public void updateJobType(JobType jobType) {
        jobTypeMapper.updateById(jobType);
    }

    @Override
    public void deleteJobType(String id) {
        jobTypeMapper.deleteById(id);
    }
}
