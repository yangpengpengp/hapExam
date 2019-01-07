package com.hand.hap.hapExam.mapper;

import com.hand.hap.mybatis.common.Mapper;
import com.hand.hap.hapExam.dto.OrgCompanys;

import java.util.List;

public interface OrgCompanysMapper extends Mapper<OrgCompanys>{

    /**
     * 根据ID查询
     * @param companyId
     * @return OrgCompanys 实体
     */
    OrgCompanys selectCompanysById(long companyId);

    /**
     * 根据实体查询，公司LOV
     * @param orgCompanys
     * @return OrgCompanys 实体列表
     */
    List<OrgCompanys> selectLovCompanies(OrgCompanys orgCompanys);
}