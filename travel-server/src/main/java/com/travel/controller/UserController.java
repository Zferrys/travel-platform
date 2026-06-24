package com.travel.controller;

import com.travel.dto.ApiResponse;
import com.travel.dto.LoginRequest;
import com.travel.dto.RegisterRequest;
import com.travel.dto.UserVO;
import com.travel.entity.User;
import com.travel.interceptor.LoginRateLimiter;
import com.travel.service.FileService;
import com.travel.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    /**
     * 用户注册
     * POST /api/user/register
     */
    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return ApiResponse.success("注册成功");
    }

    /**
     * 用户登录
     * POST /api/user/login
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request,
                                                   HttpServletRequest httpRequest) {
        String ip = LoginRateLimiter.getClientIp(httpRequest);
        log.info("用户登录: {} (IP: {})", request.getUsername(), ip);
        Map<String, Object> result = userService.login(request, ip);
        return ApiResponse.success("登录成功", result);
    }

    /**
     * 获取当前用户信息
     * GET /api/user/info
     */
    @GetMapping("/info")
    public ApiResponse<UserVO> getUserInfo(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        UserVO userVO = userService.getUserInfo(userId);
        return ApiResponse.success(userVO);
    }

    /**
     * 更新用户信息
     * PUT /api/user/info
     */
    @PutMapping("/info")
    public ApiResponse<?> updateUserInfo(HttpServletRequest request,
                                          @RequestBody User updateData) {
        Integer userId = (Integer) request.getAttribute("userId");
        userService.updateUserInfo(userId, updateData);
        return ApiResponse.success("更新成功");
    }

    /** 上传头像 */
    @PostMapping("/avatar")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                             HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String url = fileService.uploadImage(file);
        User update = new User();
        update.setUserId(userId);
        update.setAvatar(url);
        userService.updateUserInfo(userId, update);
        return ApiResponse.success("头像更新成功", url);
    }
}
