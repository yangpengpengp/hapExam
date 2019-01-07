package com.hand.hap.hapExam.service;

import com.hand.hap.core.IRequest;
import com.hand.hap.core.ProxySelf;
import com.hand.hap.system.service.IBaseService;
import com.hand.hap.hapExam.dto.OmOrderHeaders;
import com.hand.hap.task.exception.TaskExecuteException;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IOmOrderHeadersService extends IBaseService<OmOrderHeaders>, ProxySelf<IOmOrderHeadersService>{
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<OmOrderHeaders> selectWithForgeinKey(IRequest request, OmOrderHeaders omOrderHeaders, int pageNum, int pageSize);

    @Transactional(propagation = Propagation.SUPPORTS)
    public List<OmOrderHeaders> saveLinesAndHeader(IRequest requestContext, List<OmOrderHeaders> dto, int page, int pageSize) throws TaskExecuteException;

    SXSSFWorkbook buildExportOrderExcel(IRequest requestContext, OmOrderHeaders dto, int page, int pageSize);

    List<OmOrderHeaders> deleteOrder(IRequest requestContext, List<OmOrderHeaders> dto);

}