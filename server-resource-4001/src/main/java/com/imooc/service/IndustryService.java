package com.imooc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imooc.pojo.Industry;
import com.imooc.pojo.vo.TopIndustryWithThirdListVO;

import java.util.List;

/**
 * <p>
 * 行业表 服务类
 * </p>
 *
 * @author 风间影月
 * @since 2022-09-04
 */
public interface IndustryService extends IService<Industry> {

    /**
     * 根据名称判断是否存在
     * @param nodeName
     * @return
     */
    public boolean getIndustryIsExistByName(String nodeName);

    /**
     * 创建行业分类
     * @param industry
     */
    public void createIndustry(Industry industry);

    /**
     * 获得所有顶级（一级）分类列表
     * @return
     */
    public List<Industry> getTopIndustryList();

    /**
     * 根据顶级分类id获得所有三级分类列表
     * @param topIndustryId
     * @return
     */
    public List<Industry> getThirdListByTop(String topIndustryId);

    /**
     * 根据id获得子分类列表
     * @param industryId
     * @return
     */
    public List<Industry> getChildrenIndustryList(String industryId);

    /**
     * 创建行业分类
     * @param industry
     */
    public void updateIndustry(Industry industry);

    /**
     * 获得节点下子节点的数量
     * @return
     */
    public Long getChildrenIndustryCounts(String industryId);

    /**
     * 根据三级节点的ID，获得top顶级分类节点的ID
     * @param thirdIndustryId
     * @return
     */
    public String getTopIndustryId(String thirdIndustryId);

    /**
     * 查询三级分类列表
     * @return
     */
    public List<TopIndustryWithThirdListVO> getAllThirdIndustryList();

}
