package com.imooc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imooc.pojo.MqLocalMsgRecord;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 风间影月
 * @since 2022-11-09
 */
public interface MqLocalMsgRecordService extends IService<MqLocalMsgRecord> {

    /**
     * 批量根据id获得记录列表
     * @param msgIds
     * @return
     */
    public List<MqLocalMsgRecord> getBatchLocalMsgRecordList(List<String> msgIds);

}
