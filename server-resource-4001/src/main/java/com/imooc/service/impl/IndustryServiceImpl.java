package com.imooc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imooc.mapper.IndustryMapper;
import com.imooc.mapper.IndustryMapperCustom;
import com.imooc.pojo.Industry;
import com.imooc.pojo.vo.TopIndustryWithThirdListVO;
import com.imooc.service.IndustryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 行业表 服务实现类
 * </p>
 *
 * @author 风间影月
 * @since 2022-09-04
 */
@Service
public class IndustryServiceImpl extends ServiceImpl<IndustryMapper, Industry> implements IndustryService {

    @Autowired
    private IndustryMapperCustom industryMapperCustom;

    @Override
    public boolean getIndustryIsExistByName(String nodeName) {

        Industry Industry = baseMapper.selectOne(new QueryWrapper<Industry>()
                                            .eq("name", nodeName));

        return Industry != null ? true : false;
    }

    @Transactional
    @Override
    public void createIndustry(Industry industry) {
        baseMapper.insert(industry);
    }

    @Override
    public List<Industry> getTopIndustryList() {

//        List<Industry> list = baseMapper.selectList(new QueryWrapper<Industry>()
//                .eq("father_id", 0)
//                .orderByAsc("sort")
//        );

        return getChildrenIndustryList("0");
    }

    @Override
    public List<Industry> getChildrenIndustryList(String industryId) {

        List<Industry> list = baseMapper.selectList(new QueryWrapper<Industry>()
                .eq("father_id", industryId)
                .orderByAsc("sort")
        );

        return list;
    }

    @Transactional
    @Override
    public void updateIndustry(Industry industry) {
        baseMapper.updateById(industry);
    }

    @Override
    public Long getChildrenIndustryCounts(String industryId) {

        Long counts = baseMapper.selectCount(new QueryWrapper<Industry>()
                .eq("father_id", industryId)
        );

        return counts;
    }

    @Override
    public List<Industry> getThirdListByTop(String topIndustryId) {

        /**
         * 两个方案：
         *  1. 先根据topIndustryId查询二级，在拼接二级的id，然后再去查询三级
         *  2. 自连接查询（两张表，三张表）
         *  思考：高并发下哪种方案更好？
         */

        Map<String, Object> map = new HashMap<>();
        map.put("topIndustryId", topIndustryId);

        List<Industry> list = industryMapperCustom.getThirdIndustryByTop(map);
        return list;
    }

    @Override
    public String getTopIndustryId(String thirdIndustryId) {
        Map<String, Object> map = new HashMap<>();
        map.put("thirdIndustryId", thirdIndustryId);

        return industryMapperCustom.getTopIndustryId(map);
    }

    @Override
    public List<TopIndustryWithThirdListVO> getAllThirdIndustryList() {
        return industryMapperCustom.getAllThirdIndustryList();
    }
}
