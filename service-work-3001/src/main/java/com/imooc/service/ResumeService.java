package com.imooc.service;

import com.imooc.pojo.ResumeEducation;
import com.imooc.pojo.ResumeExpect;
import com.imooc.pojo.ResumeProjectExp;
import com.imooc.pojo.ResumeWorkExp;
import com.imooc.pojo.bo.*;
import com.imooc.pojo.vo.ResumeVO;
import com.imooc.utils.PagedGridResult;

import java.util.List;

/**
 * 简历service
 */
public interface ResumeService {

    /**
     * 用户注册的时候初始化简历
     * @param userId
     */
    public void initResume(String userId);

    /**
     * 用户注册的时候初始化简历
     * @param userId
     * @param msgId
     */
    public void initResume(String userId, String msgId);

    /**
     * 更新简历
     * @param editResumeBO
     */
    public void modifyResume(EditResumeBO editResumeBO);

    /**
     * 查询用户简历信息
     * @param userId
     */
    public ResumeVO getResumeInfo(String userId);

    /**
     * 新增或者修改工作经验
     * @param workExpBO
     */
    public void editWorkExp(EditWorkExpBO workExpBO);

    /**
     * 查询工作经验详情
     * @param workExpId
     * @param userId
     * @return
     */
    public ResumeWorkExp getWorkExp(String workExpId, String userId);

    /**
     * 删除工作经验
     * @param workExpId
     * @param userId
     */
    public void deleteWorkExp(String workExpId, String userId);

    /**
     * 新增或者修改项目经验
     * @param projectExpBO
     */
    public void editProjectExp(EditProjectExpBO projectExpBO);

    /**
     * 查询项目经验
     * @param projectExpId
     * @param userId
     * @return
     */
    public ResumeProjectExp getProjectExp(String projectExpId, String userId);

    /**
     * 删除项目经验
     * @param projectExpId
     * @param userId
     */
    public void deleteProjectExp(String projectExpId, String userId);

    /**
     * 新增或修改我的学历
     * @param educationBO
     */
    public void editEducation(EditEducationBO educationBO);

    /**
     * 查询教育经历详情
     * @param eduId
     * @param userId
     * @return
     */
    public ResumeEducation getEducation(String eduId, String userId);

    /**
     * 删除教育经历
     * @param eduId
     * @param userId
     */
    public void deleteEducation(String eduId, String userId);

    /**
     * 新增或修改求职期望
     * @param expectBO
     */
    public void editJobExpect(EditResumeExpectBO expectBO);

    /**
     * 查询我的求职期望列表
     * @param resumeId
     * @param userId
     * @return
     */
    public List<ResumeExpect> getMyResumeExpectList(String resumeId, String userId);

    /**
     * 删除求职期望
     * @param resumeExpectId
     * @param userId
     */
    public void deleteResumeExpect(String resumeExpectId, String userId);

    /**
     * 刷新简历，更新刷新时间
     * @param resumeId
     * @param userId
     */
    public void refreshResume(String resumeId, String userId);

    /**
     * 搜索简历
     * @param searchResumesBO
     * @return
     */
    public PagedGridResult searchResumes(SearchResumesBO searchResumesBO,
                                         Integer page,
                                         Integer pageSize);

}
