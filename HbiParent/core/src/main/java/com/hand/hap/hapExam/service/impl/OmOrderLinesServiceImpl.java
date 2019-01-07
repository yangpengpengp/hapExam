package com.hand.hap.hapExam.service.impl;

import com.github.pagehelper.PageHelper;
import com.hand.hap.core.IRequest;
import com.hand.hap.hapExam.mapper.OmOrderLinesMapper;
import com.hand.hap.system.service.impl.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hand.hap.hapExam.dto.OmOrderLines;
import com.hand.hap.hapExam.service.IOmOrderLinesService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class OmOrderLinesServiceImpl extends BaseServiceImpl<OmOrderLines> implements IOmOrderLinesService{

    @Autowired
    OmOrderLinesMapper mapper;

    @Override
    public List<OmOrderLines> selectWithForgeinKey(IRequest requestContext, OmOrderLines dto, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        return mapper.selectWithForgeinKey(dto);
    }
}