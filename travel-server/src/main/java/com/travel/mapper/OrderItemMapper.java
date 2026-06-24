package com.travel.mapper;

import com.travel.entity.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    List<OrderItem> selectByOrderId(@Param("orderId") String orderId);
    int insert(OrderItem item);
    int updateRefundStatus(@Param("itemId") Integer itemId, @Param("refundStatus") Integer refundStatus);
}
