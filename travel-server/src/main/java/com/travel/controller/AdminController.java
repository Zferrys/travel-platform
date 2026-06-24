package com.travel.controller;

import com.travel.dto.ApiResponse;
import com.travel.entity.*;
import com.travel.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 管理员接口（需要管理员权限，userType=3）
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

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
            throw new com.travel.exception.BusinessException(403, "需要管理员权限");
        }
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
        scenicMapper.insert(scenic);
        return ApiResponse.success("添加成功");
    }

    @PutMapping("/scenic/{id}")
    public ApiResponse<?> updateScenic(@PathVariable Integer id, @RequestBody Scenic scenic, HttpServletRequest req) {
        checkAdmin(req);
        scenic.setScenicId(id);
        scenicMapper.updateById(scenic);
        return ApiResponse.success("更新成功");
    }

    @DeleteMapping("/scenic/{id}")
    public ApiResponse<?> deleteScenic(@PathVariable Integer id, HttpServletRequest req) {
        checkAdmin(req);
        scenicMapper.deleteById(id);
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
        hotelMapper.insert(hotel);
        return ApiResponse.success("添加成功");
    }

    @PutMapping("/hotel/{id}")
    public ApiResponse<?> updateHotel(@PathVariable Integer id, @RequestBody Hotel hotel, HttpServletRequest req) {
        checkAdmin(req);
        hotel.setHotelId(id);
        hotelMapper.updateById(hotel);
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
        return ApiResponse.success(status == 1 ? "已启用" : "已禁用");
    }

    @DeleteMapping("/user/{id}")
    public ApiResponse<?> deleteUser(@PathVariable Integer id, HttpServletRequest req) {
        checkAdmin(req);
        User u = new User();
        u.setUserId(id);
        u.setStatus(0); // 软删除，不破坏外键引用
        userMapper.updateById(u);
        return ApiResponse.success("用户已禁用");
    }

    @PostMapping("/user")
    public ApiResponse<?> createUser(@RequestBody User user, HttpServletRequest req) {
        checkAdmin(req);
        user.setPassword(new com.travel.util.PasswordUtil().encode(user.getPassword()));
        user.setUserType(1);
        user.setStatus(1);
        userMapper.insert(user);
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
        return ApiResponse.success("已删除");
    }

    // ===== 订单概览 =====
    @GetMapping("/orders")
    public ApiResponse<List<Order>> allOrders(@RequestParam(required = false) Integer status, HttpServletRequest req) {
        checkAdmin(req);
        return ApiResponse.success(orderMapper.selectAll(status));
    }
}
