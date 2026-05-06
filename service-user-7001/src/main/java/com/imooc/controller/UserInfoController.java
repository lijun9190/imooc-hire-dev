package com.imooc.controller;

import com.google.gson.Gson;
import com.imooc.api.intercept.JWTCurrentUserInterceptor;
import com.imooc.base.BaseInfoProperties;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.Company;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.ModifyUserBO;
import com.imooc.pojo.bo.SearchBO;
import com.imooc.pojo.vo.CompanySimpleVO;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.UserService;
import com.imooc.utils.GsonUtils;
import com.imooc.utils.JWTUtils;
import com.imooc.utils.PagedGridResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.beans.Beans;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("userinfo")
@Slf4j
public class UserInfoController extends BaseInfoProperties {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtils jwtUtils;

    @PostMapping("modify")
    public GraceJSONResult modify(@RequestBody ModifyUserBO userBO) throws Exception {

        // 修改用户信息
        userService.modifyUserInfo(userBO);

        // 返回最新用户信息
        UsersVO usersVO = getUserInfo(userBO.getUserId(), true);

        return GraceJSONResult.ok(usersVO);
    }

    private UsersVO getUserInfo(String userId, boolean needJWT) {
        // 查询获得用户的最新信息
        Users latestUser = userService.getById(userId);

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(latestUser, usersVO);

        if (needJWT) {
            // 重新生成并且覆盖原来的token
            String uToken = jwtUtils.createJWTWithPrefix(new Gson().toJson(latestUser),
                    TOKEN_USER_PREFIX);
            usersVO.setUserToken(uToken);
        }

        return usersVO;
    }

    /**
     * 根据企业id，查询绑定的hr数量有多少
     * @param companyId
     * @return
     */
    @PostMapping("getCountsByCompanyId")
    public GraceJSONResult getCountsByCompanyId(
            @RequestParam("companyId") String companyId) {

        String hrCountsStr = redis.get(REDIS_COMPANY_HR_COUNTS + ":" + companyId);
        Long hrCounts = 0l;
        if (StringUtils.isBlank(hrCountsStr)) {

            hrCounts = userService.getCountsByCompanyId(companyId);
            redis.set(REDIS_COMPANY_HR_COUNTS + ":" + companyId,
                    hrCounts + "",
                    1 * 60);
            // FIXME: 此处有缓存击穿的风险，思考结合业务，怎么处理更好？
        } else {
            hrCounts = Long.valueOf(hrCountsStr);
        }

        return GraceJSONResult.ok(hrCounts);
    }

    /**
     * 绑定企业和hr用户的关系
     * @param hrUserId
     * @param realname
     * @param companyId
     * @return
     */
    @PostMapping("bindingHRToCompany")
    public GraceJSONResult bindingHRToCompany(
            @RequestParam("hrUserId") String hrUserId,
            @RequestParam("realname") String realname,
            @RequestParam("companyId") String companyId) {

        userService.updateUserCompanyId(hrUserId,
                                        realname,
                                        companyId);

        Users hrUser = userService.getById(hrUserId);

        return GraceJSONResult.ok(hrUser.getMobile());
    }

    /**
     * 刷新用户信息，传递最新的用户信息以及刷新token给前端
     * @param userId
     * @return
     */
    @PostMapping("freshUserInfo")
    public GraceJSONResult freshUserInfo(@RequestParam("userId") String userId) {
        UsersVO usersVO = getUserInfo(userId, true);
        return GraceJSONResult.ok(usersVO);
    }

    /**
     * 获得用户信息
     * @param userId
     * @return
     */
    @PostMapping("get")
    public GraceJSONResult get(@RequestParam("userId") String userId) {
        UsersVO usersVO = getUserInfo(userId, false);
        return GraceJSONResult.ok(usersVO);
    }

    /**
     * 转换身份成为hr
     * @param hrUserId
     * @return
     */
    @PostMapping("changeUserToHR")
    public GraceJSONResult changeUserToHR(@RequestParam("hrUserId") String hrUserId) {
        userService.updateUserToHR(hrUserId);
        return GraceJSONResult.ok();
    }

    /**
     * 用户离职公司，修改用户角色为普通用户
     * @param hrUserId
     * @return
     */
    @PostMapping("changeUserToCand")
    public GraceJSONResult changeUserToCand(@RequestParam("hrUserId") String hrUserId) {
        userService.updateUserToCand(hrUserId);
        return GraceJSONResult.ok();
    }

    /**
     * 查询当前企业下的hr列表
     * @param page
     * @param limit
     * @return
     */
    @PostMapping("saas/hrList")
    public GraceJSONResult changeUserToHR(Integer page, Integer limit) {

        Users user = JWTCurrentUserInterceptor.currentUser.get();
        String companyId = user.getHrInWhichCompanyId();

        PagedGridResult gridResult = userService.getHRList(companyId, page, limit);

        return GraceJSONResult.ok(gridResult);
    }

    /**
     * 根据用户id获得用户列表
     * @param searchBO
     * @return
     */
    @PostMapping("list/get")
    public GraceJSONResult getList(@RequestBody SearchBO searchBO) {

        List<Users> userList = userService.getByIds(searchBO.getUserIds());

        List<UsersVO> userVOList = new ArrayList<>();
        for (Users u : userList) {
            UsersVO usersVO = new UsersVO();
            BeanUtils.copyProperties(u, usersVO);
            userVOList.add(usersVO);
        }

        String userListStr = GsonUtils.object2String(userVOList);

        return GraceJSONResult.ok(userListStr);
    }

}
