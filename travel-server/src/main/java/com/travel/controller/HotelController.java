package com.travel.controller;

import com.travel.dto.ApiResponse;
import com.travel.dto.PageRequest;
import com.travel.entity.Hotel;
import com.travel.entity.HotelRoom;
import com.travel.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotel")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    /** 酒店详情（含房型） */
    @GetMapping("/detail/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable("id") Integer id) {
        Hotel hotel = hotelService.getById(id);
        List<HotelRoom> rooms = hotelService.getRooms(id);
        Map<String, Object> data = new HashMap<>();
        data.put("hotel", hotel);
        data.put("rooms", rooms);
        return ApiResponse.success(data);
    }

    /** 酒店列表 */
    @GetMapping("/list")
    public ApiResponse<List<Hotel>> list(@RequestParam(required = false) String city) {
        return ApiResponse.success(hotelService.listByCity(city));
    }

    /** 搜索（分页） */
    @GetMapping("/search")
    public ApiResponse<ApiResponse.PageResult<Hotel>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minStar,
            @RequestParam(required = false) Integer maxStar,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(hotelService.searchPage(city, keyword, minStar, maxStar,
                new PageRequest(page, pageSize)));
    }

    /** 房型列表 */
    @GetMapping("/rooms/{hotelId}")
    public ApiResponse<List<HotelRoom>> rooms(@PathVariable Integer hotelId) {
        return ApiResponse.success(hotelService.getRooms(hotelId));
    }

    /** 附近酒店 */
    @GetMapping("/nearby")
    public ApiResponse<List<Hotel>> nearby(@RequestParam BigDecimal lng,
                                           @RequestParam BigDecimal lat) {
        if (lng.compareTo(new BigDecimal("-180")) < 0 || lng.compareTo(new BigDecimal("180")) > 0) {
            throw new com.travel.exception.BusinessException(400, "经度范围-180~180");
        }
        if (lat.compareTo(new BigDecimal("-90")) < 0 || lat.compareTo(new BigDecimal("90")) > 0) {
            throw new com.travel.exception.BusinessException(400, "纬度范围-90~90");
        }
        return ApiResponse.success(hotelService.nearby(lng, lat));
    }
}
