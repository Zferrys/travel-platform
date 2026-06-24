package com.travel.controller;

import com.travel.dto.ApiResponse;
import com.travel.dto.PageRequest;
import com.travel.entity.Scenic;
import com.travel.exception.BusinessException;
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

    /** limit 参数安全上限 */
    private static final int MAX_LIMIT = 100;
    /** 经纬度有效范围 */
    private static final BigDecimal LNG_MIN = new BigDecimal("-180");
    private static final BigDecimal LNG_MAX = new BigDecimal("180");
    private static final BigDecimal LAT_MIN = new BigDecimal("-90");
    private static final BigDecimal LAT_MAX = new BigDecimal("90");

    private int safeLimit(int limit) {
        return Math.min(MAX_LIMIT, Math.max(1, limit));
    }

    private void validateCoord(BigDecimal lng, BigDecimal lat) {
        if (lng.compareTo(LNG_MIN) < 0 || lng.compareTo(LNG_MAX) > 0) {
            throw new BusinessException(400, "经度范围-180~180");
        }
        if (lat.compareTo(LAT_MIN) < 0 || lat.compareTo(LAT_MAX) > 0) {
            throw new BusinessException(400, "纬度范围-90~90");
        }
    }

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
        return ApiResponse.success(scenicService.listHot(city, safeLimit(limit)));
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
        return ApiResponse.success(scenicService.recommend(userId, city, safeLimit(limit)));
    }

    /** 附近景点 */
    @GetMapping("/nearby")
    public ApiResponse<List<Scenic>> nearby(@RequestParam BigDecimal lng,
                                             @RequestParam BigDecimal lat,
                                             @RequestParam(defaultValue = "8") int limit) {
        validateCoord(lng, lat);
        return ApiResponse.success(scenicService.nearby(lng, lat, safeLimit(limit)));
    }
}
