package com.travel.mapper;

import com.travel.entity.Scenic;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScenicMapper {
    Scenic selectById(@Param("scenicId") Integer scenicId);
    List<Scenic> selectByCity(@Param("city") String city);
    List<Scenic> selectHotScenics(@Param("city") String city, @Param("limit") int limit);
    List<Scenic> selectByTags(@Param("tags") List<String> tags, @Param("limit") int limit);
    List<Scenic> selectNearby(@Param("minLng") java.math.BigDecimal minLng,
                              @Param("maxLng") java.math.BigDecimal maxLng,
                              @Param("minLat") java.math.BigDecimal minLat,
                              @Param("maxLat") java.math.BigDecimal maxLat,
                              @Param("limit") int limit);
    // 分页版
    List<Scenic> selectByConditionPage(@Param("city") String city, @Param("keyword") String keyword,
                                       @Param("tags") String tags, @Param("minLevel") Integer minLevel,
                                       @Param("offset") int offset, @Param("limit") int limit);
    long countByCondition(@Param("city") String city, @Param("keyword") String keyword,
                          @Param("tags") String tags, @Param("minLevel") Integer minLevel);
    int updateViewCount(@Param("scenicId") Integer scenicId);
    int insert(Scenic scenic);
    int updateById(Scenic scenic);
    int deleteById(@Param("scenicId") Integer scenicId);
}
