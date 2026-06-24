package com.travel.mapper;

import com.travel.entity.Comment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentMapper {
    Comment selectById(@Param("commentId") Integer commentId);
    List<Comment> selectByTarget(@Param("targetType") Integer targetType, @Param("targetId") Integer targetId,
                                 @Param("offset") int offset, @Param("limit") int limit);
    long countByTarget(@Param("targetType") Integer targetType, @Param("targetId") Integer targetId);
    int insert(Comment comment);
    int deleteById(@Param("commentId") Integer commentId, @Param("userId") Integer userId);
    int forceDelete(@Param("commentId") Integer commentId);
    List<Comment> selectAll(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    long countAll(@Param("keyword") String keyword);
}
