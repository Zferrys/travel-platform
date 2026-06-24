package com.travel.service;

import com.travel.dto.LoginRequest;
import com.travel.dto.RegisterRequest;
import com.travel.dto.UserVO;
import com.travel.entity.User;
import com.travel.exception.BusinessException;
import com.travel.interceptor.LoginRateLimiter;
import com.travel.mapper.UserMapper;
import com.travel.util.DataMaskingUtil;
import com.travel.util.JwtTokenUtil;
import com.travel.util.PasswordUtil;
import com.travel.util.XssFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private LoginRateLimiter loginRateLimiter;

    /**
     * 用户注册
     */
    @Transactional
    public void register(RegisterRequest request) {
        // 参数校验
        if (!StringUtils.hasText(request.getUsername()) || request.getUsername().length() < 3) {
            throw new BusinessException(400, "用户名至少3个字符");
        }
        if (!StringUtils.hasText(request.getPassword()) || request.getPassword().length() < 6) {
            throw new BusinessException(400, "密码至少6个字符");
        }

        // 用户名唯一性检查
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 手机号唯一性检查
        if (StringUtils.hasText(request.getPhone())) {
            if (userMapper.countByPhone(request.getPhone()) > 0) {
                throw new BusinessException(400, "手机号已被注册");
            }
        }

        // 创建用户（输入过滤）
        User user = new User();
        user.setUsername(XssFilter.cleanPlainText(request.getUsername()));
        user.setPassword(passwordUtil.encode(request.getPassword()));
        String cleanNick = XssFilter.cleanPlainText(request.getNickname());
        user.setNickname(StringUtils.hasText(cleanNick) ? cleanNick : user.getUsername());
        user.setPhone(request.getPhone());
        user.setAvatar("/images/default-avatar.svg");
        user.setGender(0);

        userMapper.insert(user);
        log.info("新用户注册: {}", user.getUsername());
    }

    /**
     * 用户登录（含验证码 + IP限流）
     */
    public Map<String, Object> login(LoginRequest request, String ip) {
        if (!StringUtils.hasText(request.getUsername())) {
            throw new BusinessException(400, "请输入用户名");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BusinessException(400, "请输入密码");
        }

        // 1. 检查 IP/用户名是否被锁定
        String lockMsg = loginRateLimiter.checkLocked(ip, request.getUsername());
        if (lockMsg != null) {
            throw new BusinessException(429, lockMsg);
        }

        // 2. 校验验证码
        if (!captchaService.verify(request.getCaptchaKey(), request.getCaptchaCode())) {
            loginRateLimiter.recordFail(ip, request.getUsername());
            throw new BusinessException(400, "验证码错误");
        }

        // 3. 查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            loginRateLimiter.recordFail(ip, request.getUsername());
            throw new BusinessException(400, "用户名或密码错误");
        }

        // 4. 检查状态
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 5. 验证密码
        if (!passwordUtil.matches(request.getPassword(), user.getPassword())) {
            loginRateLimiter.recordFail(ip, request.getUsername());
            throw new BusinessException(400, "用户名或密码错误");
        }

        // 6. 登录成功，清除失败记录
        loginRateLimiter.clearFail(ip, request.getUsername());

        // 生成 Token
        String token = jwtTokenUtil.generateToken(user.getUserId(),
                user.getUsername(), user.getUserType());

        // 返回用户信息和 Token
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", convertToVO(user));
        return result;
    }

    /**
     * 获取当前用户信息
     */
    public UserVO getUserInfo(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return convertToVO(user);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public void updateUserInfo(Integer userId, User updateData) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        updateData.setUserId(userId);
        userMapper.updateById(updateData);
    }

    /**
     * Entity → VO（脱敏）
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(DataMaskingUtil.maskPhone(user.getPhone()));
        vo.setEmail(DataMaskingUtil.maskEmail(user.getEmail()));
        vo.setAvatar(user.getAvatar());
        vo.setGender(user.getGender());
        if (user.getBirthday() != null) {
            vo.setBirthday(new SimpleDateFormat("yyyy-MM-dd").format(user.getBirthday()));
        }
        vo.setUserType(user.getUserType());
        if (user.getCreateTime() != null) {
            vo.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(user.getCreateTime()));
        }
        return vo;
    }
}
