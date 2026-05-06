package com.imooc.canal;

import com.imooc.base.BaseInfoProperties;
import com.imooc.pojo.DataDictionary;
import com.imooc.pojo.co.DataDictionaryCO;
import com.imooc.utils.GsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import java.util.ArrayList;
import java.util.List;

@CanalTable("data_dictionary")      // 指定监听的表名
@Component
public class DataDictSyncHelper extends BaseInfoProperties
        implements EntryHandler<DataDictionaryCO> // 指定表关联的实体对象（javabean）
{

    private static final String DDKEY_PREFIX = DATA_DICTIONARY_LIST_TYPECODE + ":";

    @Override
    public void insert(DataDictionaryCO dataDictionary) {
//        System.out.println(dataDictionary);

        String ddkey = DDKEY_PREFIX + dataDictionary.getType_code();

        // 先查询redis中是否存在该数据字典list
        String ddListStr = redis.get(ddkey);
        List<DataDictionary> redisDDList = null;
        if (StringUtils.isBlank(ddListStr)) {
            // 如果不存在，则直接new一个list，添加并存入到redis中即可
            redisDDList = new ArrayList<>();
        } else {
            // 如果redis中存在该list，则直接在缓存的list中新增即可
            redisDDList = GsonUtils.stringToListAnother(ddListStr,
                                                        DataDictionary.class);
        }

        // 转换对象并且塞入list
        DataDictionary pendingDictionary = convertDD(dataDictionary);
        redisDDList.add(pendingDictionary);
        redis.set(ddkey, GsonUtils.object2String(redisDDList));
    }

    private DataDictionary convertDD(DataDictionaryCO dataDictionaryCO) {
        DataDictionary pendingDictionary = new DataDictionary();
        BeanUtils.copyProperties(dataDictionaryCO, pendingDictionary);
        pendingDictionary.setTypeCode(dataDictionaryCO.getType_code());
        pendingDictionary.setTypeName(dataDictionaryCO.getType_name());
        pendingDictionary.setItemKey(dataDictionaryCO.getItem_key());
        pendingDictionary.setItemValue(dataDictionaryCO.getItem_value());
        return pendingDictionary;
    }

    @Override
    public void update(DataDictionaryCO before, DataDictionaryCO after) {
        String ddkey = DDKEY_PREFIX + after.getType_code();

        // 先查询redis中是否存在该数据字典list
        String ddListStr = redis.get(ddkey);
        List<DataDictionary> redisDDList = null;
        if (StringUtils.isBlank(ddListStr)) {
            // 如果不存在，啥都不要干
        } else {
            // 如果redis中存在该list，则直接在缓存的list中修改对应的数据字典项就行，再重置缓存
            redisDDList = GsonUtils.stringToListAnother(ddListStr,
                                                        DataDictionary.class);
            for (DataDictionary dd : redisDDList) {
                if (dd.getId().equalsIgnoreCase(after.getId())) {
                    DataDictionary pendingDictionary = convertDD(after);
                    redisDDList.remove(dd);
                    redisDDList.add(pendingDictionary);
                    break;
                }
            }
            redis.set(ddkey, GsonUtils.object2String(redisDDList));
        }
    }

    @Override
    public void delete(DataDictionaryCO dataDictionary) {
        String ddkey = DDKEY_PREFIX + dataDictionary.getType_code();

        // 先查询redis中是否存在该数据字典list
        String ddListStr = redis.get(ddkey);
        List<DataDictionary> redisDDList = null;
        if (StringUtils.isBlank(ddListStr)) {
            // 如果不存在，啥都不要干
        } else {
            // 如果redis中存在该list，则直接在缓存的list中删除该数据字典项就行，再重置缓存
            redisDDList = GsonUtils.stringToListAnother(ddListStr,
                                                        DataDictionary.class);
            for (DataDictionary dd : redisDDList) {
                if (dd.getId().equalsIgnoreCase(dataDictionary.getId())) {
                    redisDDList.remove(dd);
                    break;
                }
            }
            redis.set(ddkey, GsonUtils.object2String(redisDDList));
        }
    }

}
