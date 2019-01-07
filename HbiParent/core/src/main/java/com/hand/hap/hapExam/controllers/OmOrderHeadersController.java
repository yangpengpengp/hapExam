package com.hand.hap.hapExam.controllers;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Controller;
import com.hand.hap.system.controllers.BaseController;
import com.hand.hap.core.IRequest;
import com.hand.hap.system.dto.ResponseData;
import com.hand.hap.hapExam.dto.OmOrderHeaders;
import com.hand.hap.hapExam.service.IOmOrderHeadersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindingResult;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class OmOrderHeadersController extends BaseController {
    private static final String ENC = "UTF-8";

    @Autowired
    private IOmOrderHeadersService service;

    @RequestMapping(value = "/hap/om/order/headers/exportOrderExcel")
    public void exportOrderExcel(OmOrderHeaders dto, @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                                 @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        IRequest requestContext = createRequestContext(httpServletRequest);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String curTime = df.format(new Date()).toString();
        String name = "SO_Order_" + curTime + ".xlsx";
        String userAgent = httpServletRequest.getHeader("User-Agent");
        if (userAgent.contains("Firefox")) {
            name = new String(name.getBytes("UTF-8"), "ISO8859-1");
        } else {
            name = URLEncoder.encode(name, ENC);
        }
        httpServletResponse.addHeader("Content-Disposition",
                "attachment; filename=\"" + name + "\"");
        httpServletResponse.setContentType("application/vnd.ms-excel" + ";charset=" + ENC);
        httpServletResponse.setHeader("Accept-Ranges", "bytes");
        SXSSFWorkbook excelFile = service.buildExportOrderExcel(requestContext, dto, page, pageSize);
        try (ServletOutputStream outputStream = httpServletResponse.getOutputStream();) {
            excelFile.write(outputStream);
        } finally {
            excelFile.close();
            excelFile.dispose();
        }
    }

    @RequestMapping(value = "/hap/om/order/headers/deleteOrder")
    @ResponseBody
    public ResponseData deleteOrder(@RequestBody List<OmOrderHeaders> dto, HttpServletRequest request) throws Exception {
        IRequest requestContext = createRequestContext(request);
        List<OmOrderHeaders> result = service.deleteOrder(requestContext, dto);
        return new ResponseData(result);
    }

    @RequestMapping(value = "/hap/om/order/headers/saveLinesAndHeader")
    @ResponseBody
    public ResponseData saveLinesAndHeader(@RequestBody List<OmOrderHeaders> dto, @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                                           @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request) throws Exception {
        IRequest requestContext = createRequestContext(request);
        List<OmOrderHeaders> result = service.saveLinesAndHeader(requestContext, dto, page, pageSize);
        return new ResponseData(result);
    }


    @RequestMapping(value = "/hap/om/order/headers/query")
    @ResponseBody
    public ResponseData query(OmOrderHeaders dto, @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                              @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize, HttpServletRequest request) {
        IRequest requestContext = createRequestContext(request);
        return new ResponseData(service.selectWithForgeinKey(requestContext, dto, page, pageSize));
    }

    @RequestMapping(value = "/hap/om/order/headers/submit")
    @ResponseBody
    public ResponseData update(@RequestBody List<OmOrderHeaders> dto, BindingResult result, HttpServletRequest request) {
        getValidator().validate(dto, result);
        if (result.hasErrors()) {
            ResponseData responseData = new ResponseData(false);
            responseData.setMessage(getErrorMessage(result, request));
            return responseData;
        }
        IRequest requestCtx = createRequestContext(request);
        return new ResponseData(service.batchUpdate(requestCtx, dto));
    }

    @RequestMapping(value = "/hap/om/order/headers/remove")
    @ResponseBody
    public ResponseData delete(HttpServletRequest request, @RequestBody List<OmOrderHeaders> dto) {
        service.batchDelete(dto);
        return new ResponseData();
    }
}