package com.imooc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imooc.pojo.SysParams;

/**
 * <p>
 * 系统参数配置表，本表仅有一条记录 服务类
 * </p>
 *
 * @author 风间影月
 * @since 2022-09-04
 */
public interface SysParamsService {

    /**
     * 获得系统参数
     * @return
     */
    public SysParams getSysParams();

    /**
     * 更新简历的最大刷新次数
     * @param maxCounts
     * @param version
     */
    public Integer updateMaxResumeRefreshCounts(Integer maxCounts, Integer version) throws Exception;

}
