package com.travel.service;

import com.travel.dto.ApiResponse;
import com.travel.dto.PageRequest;
import com.travel.entity.*;
import com.travel.exception.BusinessException;
import com.travel.mapper.*;
import com.travel.util.OrderIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private HotelRoomMapper hotelRoomMapper;
    @Autowired private TicketInventoryMapper ticketInventoryMapper;
    @Autowired private ScenicMapper scenicMapper;
    @Autowired private HotelMapper hotelMapper;

    private static final int MAX_NAME_LEN = 30;
    private static final int MAX_PHONE_LEN = 20;

    /** 本地防重标记 */
    private final Map<String, Boolean> seckillStatus = new ConcurrentHashMap<>();
    /** 共用延迟清理线程池 */
    private static final ScheduledExecutorService DEDUP_CLEANER =
            Executors.newScheduledThreadPool(2, r -> {
                Thread t = new Thread(r, "seckill-dedup-cleaner");
                t.setDaemon(true);
                return t;
            });

    /** 创建酒店订单 */
    @Transactional
    public String createHotelOrder(Integer userId, Integer roomId, Integer quantity,
                                    Date checkInDate, String contactName, String contactPhone) {
        if (contactName == null || contactName.trim().isEmpty() || contactName.length() > MAX_NAME_LEN) {
            throw new BusinessException(400, "联系人姓名不合法");
        }
        if (contactPhone == null || !contactPhone.matches("\\d{6,20}")) {
            throw new BusinessException(400, "联系电话不合法");
        }
        HotelRoom room = hotelRoomMapper.selectById(roomId);
        if (room == null || room.getStatus() == 0) {
            throw new BusinessException(400, "房型不可用");
        }
        int qty = quantity != null ? quantity : 1;
        if (room.getStock() == null || room.getStock() < qty) {
            throw new BusinessException(400, "库存不足");
        }
        int rows = hotelRoomMapper.decreaseStock(roomId, qty);
        if (rows == 0) {
            throw new BusinessException(400, "库存不足，请重试");
        }

        String orderId = OrderIdGenerator.generate();
        BigDecimal totalAmount = room.getPrice().multiply(BigDecimal.valueOf(qty));

        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setOrderType(1);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setOrderStatus(1);
        order.setContactName(contactName);
        order.setContactPhone(contactPhone);
        orderMapper.insert(order);

        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductType(1);
        item.setProductId(roomId);
        item.setProductName(room.getRoomType());
        item.setQuantity(qty);
        item.setPrice(room.getPrice());
        item.setUseDate(checkInDate);
        orderItemMapper.insert(item);

        log.info("酒店订单创建: {} user={} amount={}", orderId, userId, totalAmount);
        return orderId;
    }

    /** 门票秒杀下单 */
    @Transactional
    public String seckillTicket(Integer userId, Integer scenicId, Date useDate,
                                 String contactName, String contactPhone) {
        if (contactName == null || contactName.trim().isEmpty() || contactName.length() > MAX_NAME_LEN) {
            throw new BusinessException(400, "联系人姓名不合法");
        }
        if (contactPhone == null || !contactPhone.matches("\\d{6,20}")) {
            throw new BusinessException(400, "联系电话不合法");
        }
        // 截断时间部分，只保留日期（与DB的DATE类型对齐）
        if (useDate != null) {
            useDate = java.sql.Date.valueOf(new java.sql.Date(useDate.getTime()).toLocalDate());
        }
        String userKey = userId + ":" + scenicId + ":" + useDate;
        if (seckillStatus.putIfAbsent(userKey, Boolean.TRUE) != null) {
            throw new BusinessException(400, "请勿重复提交");
        }
        try {

            TicketInventory inv = ticketInventoryMapper.selectForUpdate(scenicId, useDate);
            if (inv == null || inv.getAvailableStock() <= 0) {
                throw new BusinessException(400, "库存不足");
            }
            int rows = ticketInventoryMapper.decreaseStockWithVersion(scenicId, useDate, inv.getVersion());
            if (rows == 0) {
                throw new BusinessException(400, "抢购失败，请重试");
            }

            Scenic scenic = scenicMapper.selectById(scenicId);
            String orderId = OrderIdGenerator.generate();

            Order order = new Order();
            order.setOrderId(orderId);
            order.setUserId(userId);
            order.setOrderType(2);
            order.setTotalAmount(scenic.getTicketPrice());
            order.setPayAmount(scenic.getTicketPrice());
            order.setOrderStatus(1);
            order.setContactName(contactName);
            order.setContactPhone(contactPhone);
            orderMapper.insert(order);

            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setProductType(2);
            item.setProductId(scenicId);
            item.setProductName(scenic.getScenicName());
            item.setQuantity(1);
            item.setPrice(scenic.getTicketPrice());
            item.setUseDate(useDate);
            orderItemMapper.insert(item);

            log.info("门票订单创建: {} user={}", orderId, userId);
            return orderId;
        } finally {
            DEDUP_CLEANER.schedule(() -> seckillStatus.remove(userKey), 5, TimeUnit.SECONDS);
        }
    }

    /** 订单详情 */
    public Order getOrder(String orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        return order;
    }

    /** 订单明细 */
    public List<OrderItem> getOrderItems(String orderId) {
        return orderItemMapper.selectByOrderId(orderId);
    }

    /** 我的订单（分页） */
    public ApiResponse.PageResult<Order> myOrders(Integer userId, Integer status, PageRequest page) {
        List<Order> list = orderMapper.selectByUserId(userId, status, page.getOffset(), page.getPageSize());
        long total = orderMapper.countByUserId(userId, status);
        return ApiResponse.page(list, total, page.getPage(), page.getPageSize());
    }

    /** 确认完成（已支付 → 已完成） */
    @Transactional
    public void completeOrder(String orderId, Integer userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException(403, "无权操作");
        if (order.getOrderStatus() != 2) throw new BusinessException(400, "仅已支付订单可确认完成");
        orderMapper.updateStatus(orderId, 4, null);
        log.info("订单已完成: {}", orderId);
    }

    /** 模拟支付 */
    @Transactional
    public void pay(String orderId, Integer userId, Integer payType) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException(403, "无权操作");
        if (order.getOrderStatus() != 1) throw new BusinessException(400, "订单状态不正确");
        orderMapper.updateStatus(orderId, 2, payType);
        log.info("订单支付成功: {}", orderId);
    }

    /** 取消订单（恢复库存） */
    @Transactional
    public void cancel(String orderId, Integer userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(400, "订单不存在");
        }
        if (order.getOrderStatus() != 1) {
            throw new BusinessException(400, "仅待支付订单可取消");
        }

        int rows = orderMapper.cancelOrder(orderId, userId);
        if (rows == 0) throw new BusinessException(400, "取消失败");

        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            if (item.getProductType() == 1 && item.getQuantity() != null) {
                hotelRoomMapper.decreaseStock(item.getProductId(), -item.getQuantity());
            } else if (item.getProductType() == 2) {
                TicketInventory inv = ticketInventoryMapper.selectForUpdate(item.getProductId(), item.getUseDate());
                if (inv != null) {
                    ticketInventoryMapper.restoreStock(item.getProductId(), item.getUseDate(), inv.getVersion());
                }
            }
        }
        log.info("订单取消，库存已恢复: {}", orderId);
    }
}
