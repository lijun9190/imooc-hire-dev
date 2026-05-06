package com.imooc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imooc.pojo.Resume;
import com.imooc.pojo.vo.SearchResumesVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 简历表 Mapper 接口
 * </p>
 *
 * @author 风间影月
 * @since 2022-09-04
 */
@Repository
public interface ResumeMapperCustom extends BaseMapper<Resume> {

    public List<SearchResumesVO> searchResumesList(
            @Param("paramMap") Map<String, Object> map);

}
