package com.imooc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imooc.pojo.Company;
import com.imooc.pojo.vo.CompanyInfoVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 企业表 Mapper 接口
 * </p>
 *
 * @author 风间影月
 * @since 2022-09-04
 */
@Repository
public interface CompanyMapperCustom {

    public List<CompanyInfoVO> queryCompanyList(
            @Param("paramMap") Map<String, Object> map);


    public CompanyInfoVO getCompanyInfo(
            @Param("paramMap") Map<String, Object> map);
}
