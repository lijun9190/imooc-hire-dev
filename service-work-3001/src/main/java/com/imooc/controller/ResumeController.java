package com.imooc.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.github.benmanes.caffeine.cache.Cache;
import com.imooc.base.BaseInfoProperties;
import com.imooc.enums.ActiveTime;
import com.imooc.enums.EduEnum;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.ResumeEducation;
import com.imooc.pojo.ResumeExpect;
import com.imooc.pojo.ResumeProjectExp;
import com.imooc.pojo.ResumeWorkExp;
import com.imooc.pojo.bo.*;
import com.imooc.pojo.vo.ResumeVO;
import com.imooc.service.ResumeService;
import com.imooc.utils.GsonUtils;
import com.imooc.utils.LocalDateUtils;
import com.imooc.utils.PagedGridResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("resume")
public class ResumeController extends BaseInfoProperties {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private Cache<String, Integer> resumeRefreshCountsCache;

    /**
     * 初始化用户简历
     * @param userId
     * @return
     */
    @PostMapping("init")
    public GraceJSONResult init(@RequestParam("userId") String userId) {
        resumeService.initResume(userId);
        return GraceJSONResult.ok();
    }

    /**
     * 编辑简历信息
     * @param editResumeBO
     * @return
     */
    @PostMapping("modify")
    public GraceJSONResult modify(@RequestBody @Valid EditResumeBO editResumeBO) {

        // TODO EditResumeBO 自行校验

        resumeService.modifyResume(editResumeBO);

        return GraceJSONResult.ok();
    }

    /**
     * 查询我的简历
     * @param userId
     * @return
     */
    @PostMapping("queryMyResume")
    public GraceJSONResult queryMyResume(String userId) {

        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.error();
        }

        /**
         * 由于自己的简历在修改完毕以后，一般不会去做太多的修改，
         * 而且每次查询又是会涉及到多张表查询，内容信息比较多也比较大，
         * 所以可以采用redis进行信息存储，来提升查询的速度与性能。
         */

        String resumeJson = redis.get(REDIS_RESUME_INFO + ":" + userId);
        ResumeVO resumeVO = null;
        if (StringUtils.isBlank(resumeJson)) {
            resumeVO = resumeService.getResumeInfo(userId);
            redis.set(REDIS_RESUME_INFO + ":" + userId, GsonUtils.object2String(resumeVO));
        } else {
            resumeVO = GsonUtils.stringToBean(resumeJson, ResumeVO.class);
        }

        return GraceJSONResult.ok(resumeVO);
    }

    /**
     * 新增/编辑工作经验
     * @param editResumeBO
     * @return
     */
    @PostMapping("editWorkExp")
    public GraceJSONResult editWorkExp(@RequestBody @Valid EditWorkExpBO editResumeBO) {

        // TODO EditWorkExpBO 自行校验

        resumeService.editWorkExp(editResumeBO);

        return GraceJSONResult.ok();
    }

    /**
     * 获得工作经验的详情
     * @param workExpId
     * @param userId
     * @return
     */
    @PostMapping("getWorkExp")
    public GraceJSONResult getWorkExp(String workExpId, String userId) {

        if (StringUtils.isBlank(workExpId) || StringUtils.isBlank(userId)) {
            return GraceJSONResult.error();
        }

        ResumeWorkExp exp = resumeService.getWorkExp(workExpId, userId);

        return GraceJSONResult.ok(exp);
    }

    /**
     * 删除工作经验
     * @param workExpId
     * @param userId
     * @return
     */
    @PostMapping("deleteWorkExp")
    public GraceJSONResult deleteWorkExp(String workExpId, String userId) {

        if (StringUtils.isBlank(workExpId) || StringUtils.isBlank(userId)) {
            return GraceJSONResult.error();
        }

        resumeService.deleteWorkExp(workExpId, userId);

        return GraceJSONResult.ok();
    }

    /**
     * 编辑项目经验
     * @param editResumeBO
     * @return
     */
    @PostMapping("editProjectExp")
    public GraceJSONResult editProjectExp(@RequestBody @Valid EditProjectExpBO editResumeBO) {

        // TODO EditProjectExpBO 自行校验

        resumeService.editProjectExp(editResumeBO);

        return GraceJSONResult.ok();
    }

    /**
     * 查询项目经验的详情信息
     * @param projectExpId
     * @param userId
     * @return
     */
    @PostMapping("getProjectExp")
    public GraceJSONResult getProjectExp(String projectExpId, String userId) {

        if (StringUtils.isBlank(projectExpId) || StringUtils.isBlank(userId)) {
            return GraceJSONResult.error();
        }

        ResumeProjectExp exp = resumeService.getProjectExp(projectExpId, userId);

        return GraceJSONResult.ok(exp);
    }

    @PostMapping("deleteProjectExp")
    public GraceJSONResult deleteProjectExp(String projectExpId, String userId) {

        if (StringUtils.isBlank(projectExpId) || StringUtils.isBlank(userId)) {
            return GraceJSONResult.error();
        }

        resumeService.deleteProjectExp(projectExpId, userId);

        return GraceJSONResult.ok();
    }


    /**
     * 新增或修改我的学历
     * @param educationBO
     * @return
     */
    @PostMapping("editEducation")
    public GraceJSONResult editEducation(@RequestBody @Valid EditEducationBO educationBO) {

        // TODO EditEducationBO 自行校验

        resumeService.editEducation(educationBO);

        return GraceJSONResult.ok();
    }

    /**
     * 获得教育经历详情
     * @param eduId
     * @param userId
     * @return
     */
    @PostMapping("getEducation")
    public GraceJSONResult getEducation(String eduId, String userId) {

        if (StringUtils.isBlank(eduId) || StringUtils.isBlank(userId)) {
            return GraceJSONResult.error();
        }

        ResumeEducation education = resumeService.getEducation(eduId, userId);

        return GraceJSONResult.ok(education);
    }

    /**
     * 删除教育经历
     * @param eduId
     * @param userId
     * @return
     */
    @PostMapping("deleteEducation")
    public GraceJSONResult deleteEducation(String eduId, String userId) {

        if (StringUtils.isBlank(eduId) || StringUtils.isBlank(userId)) {
            return GraceJSONResult.error();
        }

        resumeService.deleteEducation(eduId, userId);

        return GraceJSONResult.ok();
    }

    /**
     * 新增或者修改求职期望
     * @param expectBO
     * @return
     */
    @PostMapping("editJobExpect")
    public GraceJSONResult editJobExpect(@RequestBody @Valid EditResumeExpectBO expectBO) {

        // TODO EditResumeExpectBO 自行校验

        resumeService.editJobExpect(expectBO);

        return GraceJSONResult.ok();
    }

    /**
     * 查询求职期望列表
     * @param resumeId
     * @param userId
     * @return
     */
    @PostMapping("getMyResumeExpectList")
    public GraceJSONResult getMyResumeExpectList(String resumeId, String userId) {

        if (StringUtils.isBlank(resumeId) || StringUtils.isBlank(userId)) {
            return GraceJSONResult.error();
        }

        String myResumeExpectListJson = redis.get(REDIS_RESUME_EXPECT + ":" + userId);
        List<ResumeExpect> expectList = null;
        if (StringUtils.isBlank(myResumeExpectListJson)) {
            expectList = resumeService.getMyResumeExpectList(resumeId, userId);
        } else {
            expectList = GsonUtils.stringToList(myResumeExpectListJson, ResumeExpect.class);
        }

        return GraceJSONResult.ok(expectList);
    }

    /**
     * 删除求职期望
     * @param resumeExpectId
     * @param userId
     * @return
     */
    @PostMapping("deleteMyResumeExpect")
    public GraceJSONResult deleteMyResumeExpect(String resumeExpectId, String userId) {

        if (StringUtils.isBlank(resumeExpectId) || StringUtils.isBlank(userId)) {
            return GraceJSONResult.error();
        }

        resumeService.deleteResumeExpect(resumeExpectId, userId);

        return GraceJSONResult.ok();
    }

    // blockHandler 函数，原方法调用被限流/降级/系统保护的时候调用
    public GraceJSONResult blockForResumeRefresh(String resumeId,
                                                 String userId,
                                                 BlockException ex) {
        return GraceJSONResult.errorMsg("刷新失败~，请稍后再试~!");
    }

    // 降级的兜底
    public GraceJSONResult fallbackForResumeRefresh(String resumeId,
                                                    String userId) {
        String today = LocalDateUtils.getLocalDateStr();
        redis.increment(USER_ALREADY_REFRESHED_COUNTS + ":" + today + ":" + userId, 1);
        return GraceJSONResult.ok("刷新成功~~");
    }

    /**
     * 刷新简历
     * @param resumeId
     * @param userId
     * @return
     */
    @SentinelResource(value = "test/refresh",
            blockHandler = "blockForResumeRefresh",
            fallback = "fallbackForResumeRefresh"
    )
    @PostMapping("refresh")
    public GraceJSONResult refresh(String resumeId, String userId) {

        System.out.println("userId = " + userId);
        System.out.println("resumeId = " + resumeId);

        //int a = 1 / 0;

        if (StringUtils.isBlank(resumeId) || StringUtils.isBlank(userId)) {
            return GraceJSONResult.error();
        }

        // 查询最大允许刷新的参数（写死为3，后续会改为其他的中间件）
        //int maxResumeRefreshCounts = 3;
        //String maxCountsStr = redis.get(REDIS_MAX_RESUME_REFRESH_COUNTS);
        //int maxResumeRefreshCounts = Integer.valueOf(maxCountsStr);


        // 从本地缓存中获得最大刷新次数，如果没有，则从redis中获得
        Integer maxResumeRefreshCounts = resumeRefreshCountsCache.get(CACHE_MAX_RESUME_REFRESH_COUNTS, s -> {
            System.out.println("本地缓存没有命中，从redis中查询...");
            String maxCountsStr = redis.get(REDIS_MAX_RESUME_REFRESH_COUNTS);

            // TODO: 课后作业：redis中没有，再从zk中获得

            return Integer.valueOf(maxCountsStr);
            // 此处由于我们使用了缓存预热，所以可以直接从redis中获得
            // 如果没有缓存预热，可以写死一个固定值
            // 若业务不同，可以会存在动态数据存取（比如每个用户权限，不同商品的属性信息），那么此处则需要查询数据库
        });



        // 从redis中获得用户在当天的已刷新次数，如果 <= 该系统参数，
        // 则刷新，否则返回错误提示：本日刷新字数已达上限
        String today = LocalDateUtils.getLocalDateStr();
        int userAlreadyRefreshedCounts = 0;
        String userAlreadyRefreshedCountsStr = redis.get(USER_ALREADY_REFRESHED_COUNTS + ":" + today + ":" + userId);
        if (StringUtils.isBlank(userAlreadyRefreshedCountsStr)) {
            // 如果为空，表示今天用户没有刷新过，则设置为0，这个缓存只有当前才会被查询，所以可以设置为24小时失效
            redis.set(USER_ALREADY_REFRESHED_COUNTS + ":" + today + ":" + userId, userAlreadyRefreshedCounts+"", 24*60*60);
        } else {
            // 不为空，直接转换为int再判断
            userAlreadyRefreshedCounts = Integer.valueOf(userAlreadyRefreshedCountsStr);
        }

        if (userAlreadyRefreshedCounts < maxResumeRefreshCounts) {
            // 用户刷新简历并且保存到数据库
            resumeService.refreshResume(resumeId, userId);

            // 修改当天的刷新次数到redis中
            redis.increment(USER_ALREADY_REFRESHED_COUNTS + ":" + today + ":" + userId, 1);
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.RESUME_MAX_LIMIT_ERROR);
        }

        return GraceJSONResult.ok();
    }

    /**
     * 搜索简历
     * TODO 后续整合es优化为检索
     * @param searchResumesBO
     * @param page
     * @param limit
     * @return
     */
    @PostMapping("searchResumes")
    public GraceJSONResult deleteMyResumeExpect(@RequestBody SearchResumesBO searchResumesBO,
                                                Integer page,
                                                Integer limit) {

        String activeTime = searchResumesBO.getActiveTime();
        Integer activeTimes = ActiveTime.getActiveTimes(activeTime);
        searchResumesBO.setActiveTimes(activeTimes);

        String edu = searchResumesBO.getEdu();
        Integer eduIndex = EduEnum.getEduIndex(edu);
        List<String> eduList = EduEnum.getEduList(eduIndex);
        searchResumesBO.setEduList(eduList);

        PagedGridResult gridResult = resumeService.searchResumes(searchResumesBO,
                                                                page,
                                                                limit);

        return GraceJSONResult.ok(gridResult);
    }
}
