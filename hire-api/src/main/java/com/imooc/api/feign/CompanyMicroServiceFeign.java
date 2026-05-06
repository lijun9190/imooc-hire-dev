package com.imooc.api.feign;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.SearchBO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("company-service")
public interface CompanyMicroServiceFeign {

    @PostMapping("/company/list/get")
    public GraceJSONResult getList(@RequestBody SearchBO searchBO);

}
