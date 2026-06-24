package com.travel.mapper;

import com.travel.entity.TravelNote;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TravelNoteMapper {
    TravelNote selectById(@Param("noteId") Integer noteId);
    List<TravelNote> selectListPage(@Param("keyword") String keyword,
                                    @Param("offset") int offset, @Param("limit") int limit);
    long countList(@Param("keyword") String keyword);
    List<TravelNote> selectByUserId(@Param("userId") Integer userId,
                                    @Param("offset") int offset, @Param("limit") int limit);
    long countByUserId(@Param("userId") Integer userId);
    int updateViewCount(@Param("noteId") Integer noteId);
    int updateLikeCount(@Param("noteId") Integer noteId, @Param("delta") int delta);
    int updateCommentCount(@Param("noteId") Integer noteId, @Param("delta") int delta);
    int insert(TravelNote note);
    int updateById(TravelNote note);
    int deleteById(@Param("noteId") Integer noteId);
    List<TravelNote> selectAll(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    long countAll(@Param("keyword") String keyword);
}
