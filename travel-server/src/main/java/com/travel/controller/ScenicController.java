package com.travel.controller;

import com.travel.dto.ApiResponse;
import com.travel.dto.PageRequest;
import com.travel.entity.Scenic;
import com.travel.service.ScenicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/scenic")
public class ScenicController {

    @Autowired
    private ScenicService scenicService;

    /** 景点详情 */
    @GetMapping("/detail/{id}")
    public ApiResponse<Scenic> detail(@PathVariable("id") Integer id) {
        return ApiResponse.success(scenicService.getById(id));
    }

    /** 景点列表（按城市） */
    @GetMapping("/list")
    public ApiResponse<List<Scenic>> list(@RequestParam(required = false) String city) {
        return ApiResponse.success(scenicService.listByCity(city));
    }

    /** 热门景点 */
    @GetMapping("/hot")
    public ApiResponse<List<Scenic>> hot(@RequestParam(required = false) String city,
                                          @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(scenicService.listHot(city, limit));
    }

    /** 搜索（分页） */
    @GetMapping("/search")
    public ApiResponse<ApiResponse.PageResult<Scenic>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Integer minLevel,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(scenicService.searchPage(city, keyword, tags, minLevel,
                new PageRequest(page, pageSize)));
    }

    /** 推荐 */
    @GetMapping("/recommend")
    public ApiResponse<List<Scenic>> recommend(@RequestParam(required = false) String city,
                                                @RequestParam(defaultValue = "10") int limit,
                                                javax.servlet.http.HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        // 未登录或 userId 为 null 时，回退到热门推荐
        return ApiResponse.success(scenicService.recommend(userId, city, limit));
    }

    /** 附近景点 */
    @GetMapping("/nearby")
    public ApiResponse<List<Scenic>> nearby(@RequestParam BigDecimal lng,
                                             @RequestParam BigDecimal lat,
                                             @RequestParam(defaultValue = "8") int limit) {
        return ApiResponse.success(scenicService.nearby(lng, lat, limit));
    }
}
