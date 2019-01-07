package com.hand.hap.hapExam.service;

import com.hand.hap.core.IRequest;
import com.hand.hap.core.ProxySelf;
import com.hand.hap.system.service.IBaseService;
import com.hand.hap.hapExam.dto.OmOrderLines;

import java.util.List;

public interface IOmOrderLinesService extends IBaseService<OmOrderLines>, ProxySelf<IOmOrderLinesService>{
    List<OmOrderLines> selectWithForgeinKey(IRequest requestContext, OmOrderLines dto, int page, int pageSize);
}