package com.hand.hap.hapExam.service.impl;

import com.github.pagehelper.PageHelper;
import com.hand.hap.code.rule.exception.CodeRuleException;
import com.hand.hap.code.rule.service.ISysCodeRuleProcessService;
import com.hand.hap.core.IRequest;
import com.hand.hap.hapExam.dto.OmOrderLines;
import com.hand.hap.hapExam.mapper.OmOrderHeadersMapper;
import com.hand.hap.hapExam.service.*;
import com.hand.hap.system.service.impl.BaseServiceImpl;
import com.hand.hap.task.exception.TaskExecuteException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hand.hap.hapExam.dto.OmOrderHeaders;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OmOrderHeadersServiceImpl extends BaseServiceImpl<OmOrderHeaders> implements IOmOrderHeadersService {
    public static final String LINE_HEADER_ID = "LINE_HEADER_ID";
    public static final String LINE_ID = "LINE_ID";

    @Autowired
    ISysCodeRuleProcessService codeRuleProcessService;

    @Autowired
    IOmOrderLinesService orderLinesService;

    @Autowired
    IOrgCompanysService companysService;

    @Autowired
    IArCustomersService customersService;

    @Autowired
    IInvInventoryItemsService iInvInventoryItemsService;

    @Autowired
    OmOrderHeadersMapper headersMapper;

    // Excel的标题栏
    private static String[] titles = new String[]{"销售订单号", "公司名称", "客户名称", " 订单日期", "订单状态", "物料编码", "物料描述", "数量", "销售单价", "金额"};

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<OmOrderHeaders> selectWithForgeinKey(IRequest request, OmOrderHeaders condition, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return headersMapper.selectWithForgeinKey(condition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OmOrderHeaders> saveLinesAndHeader(IRequest requestContext, List<OmOrderHeaders> dto, int page, int pageSize) throws TaskExecuteException {
        ArrayList<OmOrderHeaders> resuleList = new ArrayList<>();
        try {
            for (OmOrderHeaders orderHeader : dto) {
                if (orderHeader.getHeaderId() == null || orderHeader.getHeaderId() == 0) {
                    //新建头行逻辑
                    String headerCode = codeRuleProcessService.getRuleCode(LINE_HEADER_ID);

                    //因为Id不自增,所以设置ID和编号相同
                    orderHeader.setHeaderId(Long.valueOf(headerCode));
                    orderHeader.setOrderNumber(headerCode);
                    //先插入订单头,获取到ID
                    headersMapper.insert(orderHeader);
                    List<OmOrderLines> orderLines = orderHeader.getOrderLines();
                    if (orderLines != null) {
                        for (OmOrderLines orderLine : orderLines) {
                            orderLine.setHeaderId(orderHeader.getHeaderId());
                            String lineCode = codeRuleProcessService.getRuleCode(LINE_ID);

                            orderLine.setLineId(Long.valueOf(lineCode));
                            orderLine.setLineNumber(Long.valueOf(lineCode));

                            //表格中的字段没有COMPANY_ID 添加头部中的COMPANY_ID
                            orderLine.setCompanyId(orderHeader.getCompanyId());

                            orderLinesService.insert(requestContext, orderLine);
                        }
                    }
                } else {
                    //保存头行逻辑
                    headersMapper.updateByPrimaryKeySelective(orderHeader);
                    List<OmOrderLines> orderLines = orderHeader.getOrderLines();
                    if (orderLines != null) {
                        for (OmOrderLines orderLine : orderLines) {
                            if (orderLine.getLineId() != null) {
                                orderLinesService.updateByPrimaryKeySelective(requestContext, orderLine);
                            } else {
                                orderLine.setHeaderId(orderHeader.getHeaderId());
                                String lineCode = codeRuleProcessService.getRuleCode(LINE_ID);

                                orderLine.setLineId(Long.valueOf(lineCode));
                                orderLine.setLineNumber(Long.valueOf(lineCode));

                                //表格中的字段没有COMPANY_ID 添加头部中的COMPANY_ID
                                orderLine.setCompanyId(orderHeader.getCompanyId());

                                orderLinesService.insert(requestContext, orderLine);
                            }
                        }
                    }
                }
                resuleList.add(orderHeader);
            }
        } catch (CodeRuleException e) {
            e.printStackTrace();
            throw new TaskExecuteException(TaskExecuteException.CODE_EXECUTE_ERROR, TaskExecuteException.MSG_SERVER_BUSY);
        }

        return resuleList;
    }

    @Override
    public SXSSFWorkbook buildExportOrderExcel(IRequest requestContext, OmOrderHeaders omOrderHeaders, int page, int pageSize) {

        if ("null".equals(omOrderHeaders.getOrderNumber())) {
            omOrderHeaders.setOrderNumber(null);
        }
        if ("null".equals(omOrderHeaders.getOrderStatus())) {
            omOrderHeaders.setOrderStatus(null);
        }
        List<OmOrderHeaders> orders = selectWithForgeinKey(requestContext, omOrderHeaders, page, pageSize);
        for (OmOrderHeaders order : orders) {
            OmOrderLines temOrderLines = new OmOrderLines();
            temOrderLines.setHeaderId(order.getHeaderId() == null ? 0 : order.getHeaderId());
            order.setOrderLines(orderLinesService.selectWithForgeinKey(requestContext, temOrderLines, page, pageSize));
        }
        SXSSFWorkbook workbook = new SXSSFWorkbook(50);
        SXSSFSheet sheet = workbook.createSheet("Order Page");
        //先用titles创建Excel的头部
        createOrderInfoExcelTitle(workbook, sheet);

        // row计数器
        final AtomicInteger count = new AtomicInteger(1);
        // sheet页row索引
        final AtomicInteger rowIndex = new AtomicInteger(1);

        createOrderInfoExcelContent(count, rowIndex, orders, workbook, sheet);

        return workbook;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OmOrderHeaders> deleteOrder(IRequest requestContext, List<OmOrderHeaders> dto) {
        if (dto != null) {
            for (OmOrderHeaders orderHeader : dto) {
                if (orderHeader.getOrderLines() != null) {
                    for (OmOrderLines orderLine : orderHeader.getOrderLines()) {
                        orderLinesService.deleteByPrimaryKey(orderLine);
                    }
                }
                OmOrderHeaders temOrderHeader = new OmOrderHeaders();
                temOrderHeader.setHeaderId(orderHeader.getHeaderId());
                mapper.delete(temOrderHeader);
            }
        }
        return dto;
    }

    /*
     * @Description 创建导出Excel的主体部分,通过调用 createRow 来创建内容
     * 如果一个订单只存在头部,只生成一行代表这个订单的Excel行
     * 如果订单存在订单行,生成数量等于行数量的记录
     * @Param [count, rowIndex, orders, workbook, sheet]
     * @return void
     **/
    private void createOrderInfoExcelContent(AtomicInteger count, AtomicInteger rowIndex, List<OmOrderHeaders> orders, SXSSFWorkbook workbook, SXSSFSheet sheet) {
        if (orders != null) {
            CellStyle dateFormat = workbook.createCellStyle();
            dateFormat.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-DD HH:mm:ss"));
            for (OmOrderHeaders order : orders) {
                if (order.getOrderLines() != null && order.getOrderLines().size() > 0) {
                    for (OmOrderLines orderLine : order.getOrderLines()) {
                        createRow(sheet.createRow(rowIndex.getAndIncrement()), order, orderLine, dateFormat);
                    }
                } else {
                    createRow(sheet.createRow(rowIndex.getAndIncrement()), order, null, dateFormat);
                }
            }
        }
    }

    /*
     * @Description 循环订单头部与订单行
     * @Param [row, order, orderLine, dateFormat]
     * @return void
     **/
    private void createRow(SXSSFRow row, OmOrderHeaders order,OmOrderLines orderLine, CellStyle dateFormat) {
        SXSSFCell cell0 = row.createCell(0);
        cell0.setCellType(CellType.NUMERIC);
        cell0.setCellValue(order.getOrderNumber());

        SXSSFCell cell1 = row.createCell(1);
        cell1.setCellType(CellType.STRING);
        cell1.setCellValue(order.getCompanyName());

        SXSSFCell cell2 = row.createCell(2);
        cell2.setCellType(CellType.STRING);
        cell2.setCellValue(order.getCustomerName());

        SXSSFCell cell3 = row.createCell(3);
        cell3.setCellStyle(dateFormat);
        cell3.setCellValue(order.getOrderDate());

        SXSSFCell cell4 = row.createCell(4);
        cell4.setCellType(CellType.STRING);
        cell4.setCellValue(order.getOrderStatus());

        if (orderLine == null) {
            return;
        }
        SXSSFCell cell5 = row.createCell(5);
        cell5.setCellType(CellType.STRING);
        cell5.setCellValue(orderLine.getItemCode());

        SXSSFCell cell6 = row.createCell(6);
        cell6.setCellType(CellType.STRING);
        cell6.setCellValue(orderLine.getItemDescription());

        SXSSFCell cell7 = row.createCell(7);
        cell7.setCellType(CellType.NUMERIC);
        cell7.setCellValue(orderLine.getOrderdQuantity());

        SXSSFCell cell8 = row.createCell(8);
        cell8.setCellType(CellType.NUMERIC);
        cell8.setCellValue(orderLine.getUnitSellingPrice());

        SXSSFCell cell9 = row.createCell(9);
        cell9.setCellType(CellType.NUMERIC);
        cell9.setCellValue(orderLine.getUnitSellingPrice()*orderLine.getOrderdQuantity());
        //    private static String[] titles = new String[]{"销售订单号", "公司名称", "客户名称", "订单日期	","订单状态", "物料编码", "物料描述", "数量", "销售单价", "金额"};

    }

    /**
     * @Description 创建Excel文件的头部
     * @Param [wb, sheet]
     * @return void
     **/
    private void createOrderInfoExcelTitle(SXSSFWorkbook wb, SXSSFSheet sheet) {
        SXSSFRow titleRow = sheet.createRow(0);
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        for (int i = 0; i < titles.length; i++) {
            SXSSFCell firstCell = titleRow.createCell(i);
            firstCell.setCellValue(titles[i]);
            // 设置列宽度
            sheet.setColumnWidth(i, titles[i].length() * 1020);
            firstCell.setCellStyle(cellStyle);
        }

    }

}