package com.travel.mapper;

import com.travel.entity.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    Order selectById(@Param("orderId") String orderId);
    List<Order> selectByUserId(@Param("userId") Integer userId, @Param("status") Integer status,
                               @Param("offset") int offset, @Param("limit") int limit);
    long countByUserId(@Param("userId") Integer userId, @Param("status") Integer status);
    int insert(Order order);
    int updateStatus(@Param("orderId") String orderId, @Param("orderStatus") Integer orderStatus,
                     @Param("payType") Integer payType);
    int cancelOrder(@Param("orderId") String orderId, @Param("userId") Integer userId);
    List<Order> selectAll(@Param("status") Integer status);
}
