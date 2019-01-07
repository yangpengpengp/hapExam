package com.hand.hap.hapExam.mapper;

import com.hand.hap.mybatis.common.Mapper;
import com.hand.hap.hapExam.dto.InvInventoryItems;

import java.util.List;

public interface InvInventoryItemsMapper extends Mapper<InvInventoryItems> {
    /**
     * 根据ID查找物料
     * @param invInventoryItemId
     * @return InvInventoryItems实体
     */
    InvInventoryItems selectInvInventoryItemById(long invInventoryItemId);

    /**
     * 根据实体查询 物料LOV
     * @param inventoryItems
     * @return InvInventoryItems 实体列表
     */
    List<InvInventoryItems> selectLovInvInventoryItems(InvInventoryItems inventoryItems);

    /**
     * 满足ORDER_FLAG='Y' orderLine LOV
     * @param inventoryItems
     * @returnInvInventoryItems 实体列表
     */
    List<InvInventoryItems> selectLovInvInventoryItemsForOrderLines(InvInventoryItems inventoryItems);
}