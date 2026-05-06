package com.imooc.controller;

import com.google.gson.Gson;
import com.imooc.api.feign.UserInfoMicroServiceFeign;
import com.imooc.api.intercept.JWTCurrentUserInterceptor;
import com.imooc.base.BaseInfoProperties;
import com.imooc.enums.CompanyReviewStatus;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Company;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.*;
import com.imooc.pojo.vo.CompanyInfoVO;
import com.imooc.pojo.vo.CompanySimpleVO;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.CompanyService;
import com.imooc.utils.GsonUtils;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.PagedGridResult;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("company")
public class CompanyController extends BaseInfoProperties {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserInfoMicroServiceFeign userInfoMicroServiceFeign;

    /**
     * 根据全称查询企业信息
     * @param fullName
     * @return
     */
    @PostMapping("getByFullName")
    public GraceJSONResult getByFullName(String fullName) {

        if (StringUtils.isBlank(fullName)) {
            return GraceJSONResult.error();
        }

        Company company = companyService.getByFullName(fullName);
        if (company == null) return GraceJSONResult.ok(null);

        CompanySimpleVO companySimpleVO = new CompanySimpleVO();
        BeanUtils.copyProperties(company, companySimpleVO);

        return GraceJSONResult.ok(companySimpleVO);
    }

    /**
     * 创建待审核的企业或者重新发起审核
     * @param createCompanyBO
     * @return
     */
    @PostMapping("createNewCompany")
    public GraceJSONResult createNewCompany(
            @RequestBody @Valid CreateCompanyBO createCompanyBO) {

        // TODO 课后自行校验 CreateCompanyBO

        String companyId = createCompanyBO.getCompanyId();
        String doCompanyId = "";
        if (StringUtils.isBlank(companyId)) {
            // 如果为空，则创建公司
            doCompanyId = companyService.createNewCompany(createCompanyBO);
        } else {
            // 否则不为空，则在原有的公司信息基础上做修改
            doCompanyId = companyService.resetNewCompany(createCompanyBO);
        }

        return GraceJSONResult.ok(doCompanyId);
    }

    /**
     * 获得企业信息
     * @param companyId
     * @param withHRCounts
     * @return
     */
    @PostMapping("getInfo")
    public GraceJSONResult getInfo(String companyId, boolean withHRCounts) {

        CompanySimpleVO companySimpleVO = getCompany(companyId);
        // 根据companyId获得旗下有多少个hr绑定，微服务的远程调用
        if (withHRCounts && companySimpleVO != null) {
            GraceJSONResult graceJSONResult =
                    userInfoMicroServiceFeign.getCountsByCompanyId(companyId);
            Object data = graceJSONResult.getData();
            Long hrCounts = Long.valueOf(data.toString());
            companySimpleVO.setHrCounts(hrCounts);
        }

        return GraceJSONResult.ok(companySimpleVO);
    }


    private CompanySimpleVO getCompany(String companyId) {
        if (StringUtils.isBlank(companyId)) return null;

        String companyJson = redis.get(REDIS_COMPANY_BASE_INFO + ":" + companyId);
        if (StringUtils.isBlank(companyJson)) {
            // 查询数据库
            Company company = companyService.getById(companyId);
            if (company == null) {
                return null;
            }

            CompanySimpleVO simpleVO = new CompanySimpleVO();
            BeanUtils.copyProperties(company, simpleVO);

            redis.set(REDIS_COMPANY_BASE_INFO + ":" + companyId,
                    new Gson().toJson(simpleVO),
                    1 * 60);
            return simpleVO;
        } else {
            // 不为空，直接转换对象
           return new Gson().fromJson(companyJson, CompanySimpleVO.class);
        }
    }

    /**
     * 提交企业的审核信息
     * @param reviewCompanyBO
     * @return
     */
    @PostMapping("goReviewCompany")
    @GlobalTransactional
    public GraceJSONResult goReviewCompany(
            @RequestBody @Valid ReviewCompanyBO reviewCompanyBO) {

        // 1. 微服务调用，绑定HR企业id
        GraceJSONResult result = userInfoMicroServiceFeign.bindingHRToCompany(
                                                    reviewCompanyBO.getHrUserId(),
                                                    reviewCompanyBO.getRealname(),
                                                    reviewCompanyBO.getCompanyId());
        String hrMobile = result.getData().toString();
//        System.out.println(hrMobile);

        // 2. 保存审核信息，修改状态为[3：审核中（等待审核）]
        reviewCompanyBO.setHrMobile(hrMobile);
        companyService.commitReviewCompanyInfo(reviewCompanyBO);

        return GraceJSONResult.ok();
    }

    /**
     * 根据hr的用户id查询最新的企业信息
     * @param hrUserId
     * @return
     */
    @PostMapping("information")
    public GraceJSONResult information(String hrUserId) {

        UsersVO hrUser = getHRInfoVO(hrUserId);

        CompanySimpleVO company = getCompany(hrUser.getHrInWhichCompanyId());

        return GraceJSONResult.ok(company);
    }

    private UsersVO getHRInfoVO(String hrUserId) {
        GraceJSONResult jsonResult = userInfoMicroServiceFeign.get(hrUserId);
        Object data = jsonResult.getData();

        String json = JsonUtils.objectToJson(data);
        UsersVO hrUser = JsonUtils.jsonToPojo(json, UsersVO.class);
        return hrUser;
    }

    /**
     * saas获得企业基础信息
     * @return
     */
    @PostMapping("info")
    public GraceJSONResult info() {

        Users currentUser = JWTCurrentUserInterceptor.currentUser.get();

        CompanySimpleVO companyInfo = getCompany(currentUser.getHrInWhichCompanyId());

        return GraceJSONResult.ok(companyInfo);
    }

    /**
     * saas获得查询企业详情
     * @return
     */
    @PostMapping("saas/moreInfo")
    public GraceJSONResult saasMoreInfo() {

        Users currentUser = JWTCurrentUserInterceptor.currentUser.get();

        CompanyInfoVO companyInfo = getCompanyMoreInfo(
                currentUser.getHrInWhichCompanyId());

        return GraceJSONResult.ok(companyInfo);
    }

    /**
     * app用户端获得查询企业详情
     * @return
     */
    @PostMapping("moreInfo")
    public GraceJSONResult moreInfo(String companyId) {
        CompanyInfoVO companyInfo = getCompanyMoreInfo(companyId);
        return GraceJSONResult.ok(companyInfo);
    }

    private CompanyInfoVO getCompanyMoreInfo(String companyId) {
        if (StringUtils.isBlank(companyId)) return null;

        String companyJson = redis.get(REDIS_COMPANY_MORE_INFO + ":" + companyId);
        if (StringUtils.isBlank(companyJson)) {
            // 查询数据库
            Company company = companyService.getById(companyId);
            if (company == null) {
                return null;
            }

            CompanyInfoVO infoVO = new CompanyInfoVO();
            BeanUtils.copyProperties(company, infoVO);

            redis.set(REDIS_COMPANY_MORE_INFO + ":" + companyId,
                    new Gson().toJson(infoVO),
                    1 * 60);
            return infoVO;
        } else {
            // 不为空，直接转换对象
            return new Gson().fromJson(companyJson, CompanyInfoVO.class);
        }
    }

    /**
     * 维护企业信息
     * @param companyInfoBO
     * @return
     */
    @PostMapping("modify")
    public GraceJSONResult modify(
            @RequestBody ModifyCompanyInfoBO companyInfoBO,
            Integer num) throws Exception {

//        if (num!=null && num>1) {
//            Thread.sleep(5000);
//        }

        // 判断当前用户绑定的企业，是否和修改的企业一致，如果不一致，则异常
        checkUser(companyInfoBO.getCurrentUserId(), companyInfoBO.getCompanyId());

        // 修改企业信息
        companyService.modifyCompanyInfo(companyInfoBO, num);

        // 企业相册信息的保存
        if (StringUtils.isNotBlank(companyInfoBO.getPhotos())) {
            companyService.savePhotos(companyInfoBO);
        }

        return GraceJSONResult.ok();
    }

    @GetMapping("fairLock")
    public GraceJSONResult fairLock(Integer num) throws Exception {
        companyService.modifyCompanyInfo(null, num);
        return GraceJSONResult.ok();
    }

    @GetMapping("readLock")
    public GraceJSONResult readLock() throws Exception {
        companyService.testReadLock();
        return GraceJSONResult.ok();
    }

    @GetMapping("writeLock")
    public GraceJSONResult writeLock() throws Exception {
        companyService.testWriteLock();
        return GraceJSONResult.ok();
    }

    @GetMapping("semaphore/lock")
    public GraceJSONResult semaphoreLock(Integer num) throws Exception {
        companyService.testSemaphoreLock(num);
        return GraceJSONResult.ok();
    }

    @GetMapping("semaphore/release")
    public GraceJSONResult semaphoreRelease(Integer num) throws Exception {
        companyService.testSemaphoreRelease(num);
        return GraceJSONResult.ok();
    }

    @GetMapping("release/car")
    @ResponseBody
    public GraceJSONResult releaseCar() throws Exception {
        companyService.testCountDownLatch();
        return GraceJSONResult.ok("资源全部就绪，【硫酸】发车完毕。。。");
    }

    @GetMapping("doneStep/car")
    @ResponseBody
    public GraceJSONResult doneStepCar(String name) throws Exception {
        companyService.testDoneStep();
        return GraceJSONResult.ok("资源【" + name + "】准备就绪。。。");
    }

    /**
     * 获得企业相册内容
     * @param companyId
     * @return
     */
    @PostMapping("getPhotos")
    public GraceJSONResult getPhotos(String companyId) {
        return GraceJSONResult.ok(companyService.getPhotos(companyId));
    }

    /**
     * 获得企业相册内容
     * @return
     */
    @PostMapping("saas/getPhotos")
    public GraceJSONResult getPhotosSaas() {
        String companyId = JWTCurrentUserInterceptor.currentUser.get()
                                                                .getHrInWhichCompanyId();
        return GraceJSONResult.ok(companyService.getPhotos(companyId));
    }

    /**
     * 校验企业下的HR是否OK
     * @param currentUserId
     * @param companyId
     */
    private void checkUser(String currentUserId, String companyId) {

        if (StringUtils.isBlank(currentUserId)) {
            GraceException.display(ResponseStatusEnum.COMPANY_INFO_UPDATED_ERROR);
        }

        UsersVO hrUser = getHRInfoVO(currentUserId);
        if (hrUser != null && !hrUser.getHrInWhichCompanyId().equalsIgnoreCase(companyId)) {
            GraceException.display(ResponseStatusEnum.COMPANY_INFO_UPDATED_NO_AUTH_ERROR);
        }
    }

    // **************************** 以上为用户端所使用 ****************************

    // **************************** 以下为运营平台所使用 ****************************


    @PostMapping("admin/getCompanyList")
    public GraceJSONResult adminGetCompanyList(
                            @RequestBody @Valid QueryCompanyBO companyBO,
                            Integer page,
                            Integer limit) {

        if (page == null) page = 1;
        if (limit == null) limit = 10;

        PagedGridResult gridResult = companyService.queryCompanyListPaged(
                                                            companyBO,
                                                            page,
                                                            limit);
        return GraceJSONResult.ok(gridResult);
    }

    /**
     * 根据企业id获得最新企业数据
     * @param companyId
     * @return
     */
    @PostMapping("admin/getCompanyInfo")
    public GraceJSONResult getCompanyInfo(String companyId) {

        CompanyInfoVO companyInfo = companyService.getCompanyInfo(companyId);

        return GraceJSONResult.ok(companyInfo);
    }

    /**
     * 企业审核通过，用户成为HR角色
     * @param reviewCompanyBO
     * @return
     */
    @PostMapping("admin/doReview")
    public GraceJSONResult getCompanyInfo(
            @RequestBody @Valid ReviewCompanyBO reviewCompanyBO) {

        // 1. 审核企业
        companyService.updateReviewInfo(reviewCompanyBO);

        // 2. 如果审核成功，则更新用户角色成为HR
        if (reviewCompanyBO.getReviewStatus() == CompanyReviewStatus.SUCCESSFUL.type) {
            userInfoMicroServiceFeign.changeUserToHR(reviewCompanyBO.getHrUserId());
        }

        // 3. 清除用户端的企业缓存
        redis.del(REDIS_COMPANY_BASE_INFO + ":" + reviewCompanyBO.getCompanyId());

        return GraceJSONResult.ok();
    }

    /**
     * 根据企业id获得企业列表
     * @param searchBO
     * @return
     */
    @PostMapping("list/get")
    public GraceJSONResult getList(@RequestBody SearchBO searchBO) {

        List<Company> companyList = companyService.getByIds(searchBO.getCompanyIds());

        List<CompanyInfoVO> companyVOList = new ArrayList<>();
        for (Company c : companyList) {
            CompanyInfoVO companyInfoVO = new CompanyInfoVO();
            BeanUtils.copyProperties(c, companyInfoVO);
            companyInfoVO.setCompanyId(c.getId());
            companyVOList.add(companyInfoVO);
        }

        String companyListStr = GsonUtils.object2String(companyVOList);

        return GraceJSONResult.ok(companyListStr);
    }

    @PostMapping("isVip")
    public GraceJSONResult isVip(String companyId) {

        if (StringUtils.isBlank(companyId)) {
            return GraceJSONResult.ok(false);
        }

        return GraceJSONResult.ok(companyService.getIsVip(companyId));
    }

    @PostMapping("saas/isVip")
    public GraceJSONResult isVipSaas() {

        Users user = JWTCurrentUserInterceptor.currentUser.get();
        String companyId = user.getHrInWhichCompanyId();

        if (StringUtils.isBlank(companyId)) {
            return GraceJSONResult.ok(false);
        }

        return GraceJSONResult.ok(companyService.getIsVip(companyId));
    }
}
