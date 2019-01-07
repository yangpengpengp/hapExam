package com.hand.hap.hapExam.mapper;

import com.hand.hap.mybatis.common.Mapper;
import com.hand.hap.hapExam.dto.OmOrderLines;

import java.util.List;

public interface OmOrderLinesMapper extends Mapper<OmOrderLines>{

    /**
     * 根据实体查询
     * @param omOrderLines
     * @return OmOrderLines 实体列表
     */
    List<OmOrderLines> selectWithForgeinKey(OmOrderLines omOrderLines);
}