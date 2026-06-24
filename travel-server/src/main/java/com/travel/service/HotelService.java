package com.travel.service;

import com.travel.dto.ApiResponse;
import com.travel.dto.PageRequest;
import com.travel.entity.Hotel;
import com.travel.entity.HotelRoom;
import com.travel.exception.BusinessException;
import com.travel.mapper.HotelMapper;
import com.travel.mapper.HotelRoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class HotelService {

    @Autowired
    private HotelMapper hotelMapper;

    @Autowired
    private HotelRoomMapper hotelRoomMapper;

    /** 酒店详情（含房型列表） */
    public Hotel getById(Integer hotelId) {
        Hotel hotel = hotelMapper.selectById(hotelId);
        if (hotel == null) {
            throw new BusinessException(404, "酒店不存在");
        }
        return hotel;
    }

    /** 酒店房型列表 */
    public List<HotelRoom> getRooms(Integer hotelId) {
        return hotelRoomMapper.selectByHotelId(hotelId);
    }

    /** 按城市查询 */
    public List<Hotel> listByCity(String city) {
        return hotelMapper.selectByCity(city);
    }

    /** 搜索（分页） */
    public ApiResponse.PageResult<Hotel> searchPage(String city, String keyword, Integer minStar,
                                                     Integer maxStar, PageRequest page) {
        List<Hotel> list = hotelMapper.selectByConditionPage(city, keyword, minStar, maxStar,
                page.getOffset(), page.getPageSize());
        long total = hotelMapper.countByCondition(city, keyword, minStar, maxStar);
        return ApiResponse.page(list, total, page.getPage(), page.getPageSize());
    }

    /** 附近酒店 */
    public List<Hotel> nearby(BigDecimal lng, BigDecimal lat) {
        BigDecimal offset = new BigDecimal("0.1");
        return hotelMapper.selectNearby(
                lng.subtract(offset), lng.add(offset),
                lat.subtract(offset), lat.add(offset));
    }
}
