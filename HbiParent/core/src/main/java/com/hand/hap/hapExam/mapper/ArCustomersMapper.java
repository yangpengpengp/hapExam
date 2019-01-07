package com.hand.hap.hapExam.mapper;

import com.hand.hap.mybatis.common.Mapper;
import com.hand.hap.hapExam.dto.ArCustomers;

import java.util.List;

public interface ArCustomersMapper extends Mapper<ArCustomers>{

    /**
     * 根据客户ID查询客户
     * @param customerId
     * @return
     */
    ArCustomers selectCustomerWithOutRequestId(long customerId);

    /**
     * 根据实体查询 客户LOV
     * @param arCustomers
     * @return
     */
    List<ArCustomers> selectLovCustomers(ArCustomers arCustomers);
}