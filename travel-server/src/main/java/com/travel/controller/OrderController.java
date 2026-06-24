package com.travel.controller;

import com.travel.dto.ApiResponse;
import com.travel.dto.PageRequest;
import com.travel.entity.Order;
import com.travel.entity.OrderItem;
import com.travel.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /** 创建酒店订单 */
    @PostMapping("/hotel")
    public ApiResponse<Map<String, String>> createHotelOrder(@RequestBody Map<String, Object> params,
                                                              HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Integer roomId = (Integer) params.get("roomId");
        Integer quantity = params.containsKey("quantity") ? (Integer) params.get("quantity") : 1;
        String contactName = (String) params.get("contactName");
        String contactPhone = (String) params.get("contactPhone");
        Date checkInDate = params.containsKey("checkInDate")
                ? new Date(((Number) params.get("checkInDate")).longValue()) : new Date();

        String orderId = orderService.createHotelOrder(userId, roomId, quantity,
                checkInDate, contactName, contactPhone);
        Map<String, String> data = new HashMap<>();
        data.put("orderId", orderId);
        return ApiResponse.success("下单成功", data);
    }

    /** 门票秒杀 */
    @PostMapping("/seckill")
    public ApiResponse<Map<String, String>> seckill(@RequestBody Map<String, Object> params,
                                                     HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Integer scenicId = (Integer) params.get("scenicId");
        String contactName = (String) params.get("contactName");
        String contactPhone = (String) params.get("contactPhone");
        Date useDate = params.containsKey("useDate")
                ? new Date(((Number) params.get("useDate")).longValue()) : new Date();

        String orderId = orderService.seckillTicket(userId, scenicId, useDate, contactName, contactPhone);
        Map<String, String> data = new HashMap<>();
        data.put("orderId", orderId);
        return ApiResponse.success("抢购成功", data);
    }

    /** 订单详情（仅允许查看自己的订单） */
    @GetMapping("/detail/{orderId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String orderId,
                                                    HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Order order = orderService.getOrder(orderId);
        // 校验订单归属，防止越权查看他人订单
        if (!order.getUserId().equals(userId)) {
            return ApiResponse.error(403, "无权查看此订单");
        }
        List<OrderItem> items = orderService.getOrderItems(orderId);
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", items);
        return ApiResponse.success(data);
    }

    /** 我的订单（分页） */
    @GetMapping("/list")
    public ApiResponse<ApiResponse.PageResult<Order>> myOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return ApiResponse.success(orderService.myOrders(userId, status, new PageRequest(page, pageSize)));
    }

    /** 模拟支付 */
    @PostMapping("/pay/{orderId}")
    public ApiResponse<?> pay(@PathVariable String orderId,
                               @RequestParam(defaultValue = "1") Integer payType,
                               HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        orderService.pay(orderId, userId, payType);
        return ApiResponse.success("支付成功");
    }

    /** 取消订单 */
    @PostMapping("/cancel/{orderId}")
    public ApiResponse<?> cancel(@PathVariable String orderId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        orderService.cancel(orderId, userId);
        return ApiResponse.success("已取消");
    }

    /** 确认完成（已支付 → 已完成） */
    @PostMapping("/complete/{orderId}")
    public ApiResponse<?> complete(@PathVariable String orderId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        orderService.completeOrder(orderId, userId);
        return ApiResponse.success("已完成");
    }
}
