package com.imooc.canal;

import com.github.benmanes.caffeine.cache.Cache;
import com.imooc.base.BaseInfoProperties;
import com.imooc.pojo.DataDictionary;
import com.imooc.pojo.co.DataDictionaryCO;
import com.imooc.pojo.co.SysParamsCO;
import com.imooc.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
//@CanalTable("sys_params")      // 指定监听的表名
//@Component
public class SysParamsSyncHelper extends BaseInfoProperties
        implements EntryHandler<SysParamsCO> // 指定表关联的实体对象（javabean）
{

    @Autowired
    private Cache<String, Integer> resumeRefreshCountsCache;

    @Override
    public void insert(SysParamsCO co) {
    }

    @Override
    public void update(SysParamsCO before, SysParamsCO after) {
        //System.out.println(before);
        //System.out.println(after);

        Integer maxCounts = after.getMax_resume_refresh_counts();
        // 各个微服务节点监听到变动，则更新各自的本地缓存
        resumeRefreshCountsCache.put(CACHE_MAX_RESUME_REFRESH_COUNTS, maxCounts);

        log.info("简历微服务节点本地缓存已更新...更新后的[最大刷新阈值]为：{}", maxCounts);

        // 更新到缓存redis中
        redis.set(REDIS_MAX_RESUME_REFRESH_COUNTS, maxCounts + "");
    }

    @Override
    public void delete(SysParamsCO co) {
    }

}
