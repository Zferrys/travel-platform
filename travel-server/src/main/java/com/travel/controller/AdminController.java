package com.travel.controller;

import com.travel.dto.ApiResponse;
import com.travel.entity.*;
import com.travel.exception.BusinessException;
import com.travel.mapper.*;
import com.travel.util.XssFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * 管理员接口（需要管理员权限，userType=3）
 * 所有写操作均记录审计日志。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired private ScenicMapper scenicMapper;
    @Autowired private HotelMapper hotelMapper;
    @Autowired private HotelRoomMapper hotelRoomMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private TravelNoteMapper travelNoteMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private CommentMapper commentMapper;

    /** 校验管理员权限 */
    private void checkAdmin(HttpServletRequest request) {
        Integer userType = (Integer) request.getAttribute("userType");
        if (userType == null || userType != 3) {
            throw new BusinessException(403, "需要管理员权限");
        }
    }

    /** 获取操作用户名 */
    private String operator(HttpServletRequest req) {
        String username = (String) req.getAttribute("username");
        return username != null ? username : "unknown";
    }

    // ===== 景点管理 =====
    @GetMapping("/scenics")
    public ApiResponse<ApiResponse.PageResult<Scenic>> listScenics(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        checkAdmin(req);
        int offset = (page - 1) * pageSize;
        List<Scenic> list = scenicMapper.selectByConditionPage(null, keyword, null, null, offset, pageSize);
        long total = scenicMapper.countByCondition(null, keyword, null, null);
        return ApiResponse.success(ApiResponse.page(list, total, page, pageSize));
    }

    @PostMapping("/scenic")
    public ApiResponse<?> addScenic(@RequestBody Scenic scenic, HttpServletRequest req) {
        checkAdmin(req);
        // 字段基本校验 + XSS 清洗
        if (scenic.getScenicName() == null || scenic.getScenicName().trim().isEmpty()) {
            throw new BusinessException(400, "景点名称不能为空");
        }
        scenic.setScenicName(XssFilter.cleanPlainText(scenic.getScenicName()));
        if (scenic.getDescription() != null) {
            scenic.setDescription(XssFilter.cleanPlainText(scenic.getDescription()));
        }
        if (scenic.getTicketPrice() != null && scenic.getTicketPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "票价不能为负数");
        }
        scenicMapper.insert(scenic);
        log.warn("[AUDIT] operator={} action=ADD_SCENIC id={} name={}", operator(req), scenic.getScenicId(), scenic.getScenicName());
        return ApiResponse.success("添加成功");
    }

    @PutMapping("/scenic/{id}")
    public ApiResponse<?> updateScenic(@PathVariable Integer id, @RequestBody Scenic scenic, HttpServletRequest req) {
        checkAdmin(req);
        if (scenic.getScenicName() != null) scenic.setScenicName(XssFilter.cleanPlainText(scenic.getScenicName()));
        if (scenic.getDescription() != null) scenic.setDescription(XssFilter.cleanPlainText(scenic.getDescription()));
        if (scenic.getAddress() != null) scenic.setAddress(XssFilter.cleanPlainText(scenic.getAddress()));
        if (scenic.getTicketPrice() != null && scenic.getTicketPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "票价不能为负数");
        }
        scenic.setScenicId(id);
        scenicMapper.updateById(scenic);
        log.warn("[AUDIT] operator={} action=UPDATE_SCENIC id={}", operator(req), id);
        return ApiResponse.success("更新成功");
    }

    @DeleteMapping("/scenic/{id}")
    public ApiResponse<?> deleteScenic(@PathVariable Integer id, HttpServletRequest req) {
        checkAdmin(req);
        scenicMapper.deleteById(id);
        log.warn("[AUDIT] operator={} action=DELETE_SCENIC id={}", operator(req), id);
        return ApiResponse.success("已下架");
    }

    // ===== 酒店管理 =====
    @GetMapping("/hotels")
    public ApiResponse<ApiResponse.PageResult<Hotel>> listHotels(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        checkAdmin(req);
        int offset = (page - 1) * pageSize;
        List<Hotel> list = hotelMapper.selectByConditionPage(null, keyword, null, null, offset, pageSize);
        long total = hotelMapper.countByCondition(null, keyword, null, null);
        return ApiResponse.success(ApiResponse.page(list, total, page, pageSize));
    }

    @PostMapping("/hotel")
    public ApiResponse<?> addHotel(@RequestBody Hotel hotel, HttpServletRequest req) {
        checkAdmin(req);
        if (hotel.getHotelName() == null || hotel.getHotelName().trim().isEmpty()) {
            throw new BusinessException(400, "酒店名称不能为空");
        }
        hotel.setHotelName(XssFilter.cleanPlainText(hotel.getHotelName()));
        if (hotel.getDescription() != null) {
            hotel.setDescription(XssFilter.cleanPlainText(hotel.getDescription()));
        }
        hotelMapper.insert(hotel);
        log.warn("[AUDIT] operator={} action=ADD_HOTEL id={} name={}", operator(req), hotel.getHotelId(), hotel.getHotelName());
        return ApiResponse.success("添加成功");
    }

    @PutMapping("/hotel/{id}")
    public ApiResponse<?> updateHotel(@PathVariable Integer id, @RequestBody Hotel hotel, HttpServletRequest req) {
        checkAdmin(req);
        if (hotel.getHotelName() != null) hotel.setHotelName(XssFilter.cleanPlainText(hotel.getHotelName()));
        if (hotel.getDescription() != null) hotel.setDescription(XssFilter.cleanPlainText(hotel.getDescription()));
        if (hotel.getAddress() != null) hotel.setAddress(XssFilter.cleanPlainText(hotel.getAddress()));
        hotel.setHotelId(id);
        hotelMapper.updateById(hotel);
        log.warn("[AUDIT] operator={} action=UPDATE_HOTEL id={}", operator(req), id);
        return ApiResponse.success("更新成功");
    }

    // ===== 用户管理 =====
    @GetMapping("/users")
    public ApiResponse<ApiResponse.PageResult<User>> listUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        checkAdmin(req);
        int offset = (page - 1) * pageSize;
        List<User> list = userMapper.selectAllPaged(keyword, offset, pageSize);
        long total = userMapper.countAll(keyword);
        return ApiResponse.success(ApiResponse.page(list, total, page, pageSize));
    }

    @PutMapping("/user/{id}/status")
    public ApiResponse<?> toggleUserStatus(@PathVariable Integer id, @RequestParam Integer status, HttpServletRequest req) {
        checkAdmin(req);
        User u = new User();
        u.setUserId(id);
        u.setStatus(status);
        userMapper.updateById(u);
        log.warn("[AUDIT] operator={} action=TOGGLE_USER_STATUS id={} status={}", operator(req), id, status);
        return ApiResponse.success(status == 1 ? "已启用" : "已禁用");
    }

    @DeleteMapping("/user/{id}")
    public ApiResponse<?> deleteUser(@PathVariable Integer id, HttpServletRequest req) {
        checkAdmin(req);
        User u = new User();
        u.setUserId(id);
        u.setStatus(0); // 软删除，不破坏外键引用
        userMapper.updateById(u);
        log.warn("[AUDIT] operator={} action=DELETE_USER id={}", operator(req), id);
        return ApiResponse.success("用户已禁用");
    }

    @PostMapping("/user")
    public ApiResponse<?> createUser(@RequestBody User user, HttpServletRequest req) {
        checkAdmin(req);
        // 密码复杂度校验：至少8位，必须含字母和数字
        String rawPwd = user.getPassword();
        if (rawPwd == null || rawPwd.length() < 8
                || !rawPwd.matches(".*[a-zA-Z].*")
                || !rawPwd.matches(".*\\d.*")) {
            throw new BusinessException(400, "密码需至少8位且包含字母和数字");
        }
        user.setPassword(new com.travel.util.PasswordUtil().encode(rawPwd));
        user.setUsername(XssFilter.cleanPlainText(user.getUsername()));
        user.setUserType(1);
        user.setStatus(1);
        userMapper.insert(user);
        log.warn("[AUDIT] operator={} action=CREATE_USER username={}", operator(req), user.getUsername());
        return ApiResponse.success("用户创建成功");
    }

    // ===== 游记管理 =====
    @GetMapping("/notes")
    public ApiResponse<ApiResponse.PageResult<TravelNote>> listNotes(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        checkAdmin(req);
        int offset = (page - 1) * pageSize;
        List<TravelNote> list = travelNoteMapper.selectAll(keyword, offset, pageSize);
        long total = travelNoteMapper.countAll(keyword);
        return ApiResponse.success(ApiResponse.page(list, total, page, pageSize));
    }

    @DeleteMapping("/note/{id}")
    public ApiResponse<?> deleteNote(@PathVariable Integer id, HttpServletRequest req) {
        checkAdmin(req);
        travelNoteMapper.deleteById(id);
        log.warn("[AUDIT] operator={} action=DELETE_NOTE id={}", operator(req), id);
        return ApiResponse.success("已删除");
    }

    // ===== 评论管理 =====
    @GetMapping("/comments")
    public ApiResponse<ApiResponse.PageResult<Comment>> listComments(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest req) {
        checkAdmin(req);
        int offset = (page - 1) * pageSize;
        List<Comment> list = commentMapper.selectAll(keyword, offset, pageSize);
        long total = commentMapper.countAll(keyword);
        return ApiResponse.success(ApiResponse.page(list, total, page, pageSize));
    }

    @DeleteMapping("/comment/{id}")
    public ApiResponse<?> deleteComment(@PathVariable Integer id, HttpServletRequest req) {
        checkAdmin(req);
        commentMapper.forceDelete(id);
        log.warn("[AUDIT] operator={} action=DELETE_COMMENT id={}", operator(req), id);
        return ApiResponse.success("已删除");
    }

    // ===== 订单概览 =====
    @GetMapping("/orders")
    public ApiResponse<List<Order>> allOrders(@RequestParam(required = false) Integer status, HttpServletRequest req) {
        checkAdmin(req);
        return ApiResponse.success(orderMapper.selectAll(status));
    }
}
