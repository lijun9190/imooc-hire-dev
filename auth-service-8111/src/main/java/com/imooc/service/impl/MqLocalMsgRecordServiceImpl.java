package com.imooc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imooc.mapper.MqLocalMsgRecordMapper;
import com.imooc.pojo.MqLocalMsgRecord;
import com.imooc.service.MqLocalMsgRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 风间影月
 * @since 2022-11-09
 */
@Service
public class MqLocalMsgRecordServiceImpl extends ServiceImpl<MqLocalMsgRecordMapper, MqLocalMsgRecord> implements MqLocalMsgRecordService {

    @Autowired
    private MqLocalMsgRecordMapper msgRecordMapper;

    @Override
    public List<MqLocalMsgRecord> getBatchLocalMsgRecordList(List<String> msgIds) {
        return msgRecordMapper.selectBatchIds(msgIds);
    }
}
