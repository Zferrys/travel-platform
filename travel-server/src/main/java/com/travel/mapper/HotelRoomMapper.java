package com.travel.mapper;

import com.travel.entity.HotelRoom;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HotelRoomMapper {
    List<HotelRoom> selectByHotelId(@Param("hotelId") Integer hotelId);
    HotelRoom selectById(@Param("roomId") Integer roomId);
    int decreaseStock(@Param("roomId") Integer roomId, @Param("quantity") int quantity);
    int insert(HotelRoom room);
    int updateById(HotelRoom room);
}
