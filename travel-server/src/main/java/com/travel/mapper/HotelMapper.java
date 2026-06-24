package com.travel.mapper;

import com.travel.entity.Hotel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HotelMapper {
    Hotel selectById(@Param("hotelId") Integer hotelId);
    List<Hotel> selectByCity(@Param("city") String city);
    List<Hotel> selectByCondition(@Param("city") String city, @Param("keyword") String keyword,
                                  @Param("minStar") Integer minStar, @Param("maxStar") Integer maxStar);
    List<Hotel> selectByConditionPage(@Param("city") String city, @Param("keyword") String keyword,
                                      @Param("minStar") Integer minStar, @Param("maxStar") Integer maxStar,
                                      @Param("offset") int offset, @Param("limit") int limit);
    long countByCondition(@Param("city") String city, @Param("keyword") String keyword,
                          @Param("minStar") Integer minStar, @Param("maxStar") Integer maxStar);
    int insert(Hotel hotel);
    int updateById(Hotel hotel);
    List<Hotel> selectNearby(@Param("minLng") java.math.BigDecimal minLng, @Param("maxLng") java.math.BigDecimal maxLng,
                             @Param("minLat") java.math.BigDecimal minLat, @Param("maxLat") java.math.BigDecimal maxLat);
}
