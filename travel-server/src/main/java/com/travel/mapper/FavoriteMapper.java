package com.travel.mapper;

import com.travel.entity.Favorite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FavoriteMapper {
    int insert(Favorite favorite);
    /** 原子插入，重复则忽略（防 TOCTOU 竞态） */
    int insertIgnore(Favorite favorite);
    int delete(@Param("userId") Integer userId, @Param("targetType") Integer targetType,
               @Param("targetId") Integer targetId);
    boolean exists(@Param("userId") Integer userId, @Param("targetType") Integer targetType,
                   @Param("targetId") Integer targetId);
    List<Integer> selectByUserAndType(@Param("userId") Integer userId, @Param("targetType") Integer targetType);
    List<Favorite> selectByUserId(@Param("userId") Integer userId, @Param("targetType") Integer targetType);
}
