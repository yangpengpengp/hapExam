package com.hand.hap.hapExam.mapper;

import com.hand.hap.mybatis.common.Mapper;
import com.hand.hap.hapExam.dto.OmOrderHeaders;

import java.util.List;

public interface OmOrderHeadersMapper extends Mapper<OmOrderHeaders>{

    /**
     * 根据实体查询
     * @param omOrderHeaders
     * @return OmOrderHeaders 实体列表
     */
    List<OmOrderHeaders> selectWithForgeinKey(OmOrderHeaders omOrderHeaders);
}