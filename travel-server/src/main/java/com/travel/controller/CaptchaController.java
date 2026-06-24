package com.travel.controller;

import com.travel.dto.ApiResponse;
import com.travel.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 验证码接口
 */
@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    /**
     * 获取验证码
     * GET /api/captcha/generate
     * 返回 { key, image: "data:image/png;base64,..." }
     */
    @GetMapping("/generate")
    public ApiResponse<Map<String, String>> generate() {
        return ApiResponse.success(captchaService.generate());
    }
}
