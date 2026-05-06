package com.imooc.api.feign;

import com.imooc.api.feign.fallback.UserInfoMicroServiceFeignFallback;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.SearchBO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "user-service", fallback = UserInfoMicroServiceFeignFallback.class)
public interface UserInfoMicroServiceFeign {

    @PostMapping("/userinfo/list/get")
    public GraceJSONResult getList(@RequestBody SearchBO searchBO);


    @PostMapping("/userinfo/getCountsByCompanyId")
    public GraceJSONResult getCountsByCompanyId(
            @RequestParam("companyId") String companyId);


    @PostMapping("/userinfo/bindingHRToCompany")
    public GraceJSONResult bindingHRToCompany(
            @RequestParam("hrUserId") String hrUserId,
            @RequestParam("realname") String realname,
            @RequestParam("companyId") String companyId);

    @PostMapping("/userinfo/get")
    public GraceJSONResult get(@RequestParam("userId") String userId);

    @PostMapping("/userinfo/changeUserToHR")
    public GraceJSONResult changeUserToHR(
            @RequestParam("hrUserId") String hrUserId);
}
