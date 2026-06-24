package com.travel.controller;

import com.travel.dto.ApiResponse;
import com.travel.entity.Favorite;
import com.travel.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /** 添加收藏 */
    @PostMapping("/add")
    public ApiResponse<?> add(@RequestBody Favorite favorite, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        favorite.setUserId(userId);
        favoriteService.add(favorite);
        return ApiResponse.success("收藏成功");
    }

    /** 取消收藏 */
    @DeleteMapping("/remove")
    public ApiResponse<?> remove(@RequestParam Integer targetType, @RequestParam Integer targetId,
                                  HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        favoriteService.remove(userId, targetType, targetId);
        return ApiResponse.success("已取消收藏");
    }

    /** 是否已收藏 */
    @GetMapping("/check")
    public ApiResponse<Boolean> check(@RequestParam Integer targetType, @RequestParam Integer targetId,
                                       HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return ApiResponse.success(favoriteService.isFavorited(userId, targetType, targetId));
    }

    /** 我的收藏 */
    @GetMapping("/list")
    public ApiResponse<List<Favorite>> myFavorites(@RequestParam(required = false) Integer targetType,
                                                    HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return ApiResponse.success(favoriteService.myFavorites(userId, targetType));
    }
}
