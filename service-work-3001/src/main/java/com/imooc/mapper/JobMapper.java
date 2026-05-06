package com.imooc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imooc.pojo.Job;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * HR发布的职位表 Mapper 接口
 * </p>
 *
 * @author 风间影月
 * @since 2022-09-04
 */
@Repository
public interface JobMapper extends BaseMapper<Job> {

}
