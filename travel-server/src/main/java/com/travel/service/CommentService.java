package com.travel.service;

import com.travel.dto.ApiResponse;
import com.travel.dto.PageRequest;
import com.travel.entity.Comment;
import com.travel.exception.BusinessException;
import com.travel.mapper.CommentMapper;
import com.travel.mapper.TravelNoteMapper;
import com.travel.util.XssFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private static final int TARGET_SCENIC = 1;
    private static final int TARGET_HOTEL = 2;
    private static final int TARGET_NOTE = 3;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private TravelNoteMapper travelNoteMapper;

    /** 评论列表（分页） */
    public ApiResponse.PageResult<Comment> listPage(Integer targetType, Integer targetId, PageRequest page) {
        List<Comment> list = commentMapper.selectByTarget(targetType, targetId, page.getOffset(), page.getPageSize());
        long total = commentMapper.countByTarget(targetType, targetId);
        return ApiResponse.page(list, total, page.getPage(), page.getPageSize());
    }

    /** 发表评论 */
    @Transactional
    public void publish(Comment comment) {
        // 校验 targetType 合法性，防止脏数据
        Integer tt = comment.getTargetType();
        if (tt == null || (tt != TARGET_SCENIC && tt != TARGET_HOTEL && tt != TARGET_NOTE)) {
            throw new BusinessException(400, "不支持的评论目标类型");
        }
        String trimmed = comment.getContent() == null ? "" : comment.getContent().trim();
        if (trimmed.length() < 2) {
            throw new BusinessException(400, "评论内容至少2个字符");
        }
        if (trimmed.length() > 2000) {
            throw new BusinessException(400, "评论内容过长（最多2000字）");
        }
        comment.setContent(XssFilter.cleanPlainText(trimmed));
        commentMapper.insert(comment);
        // 游记评论数+1
        if (tt == TARGET_NOTE) {
            travelNoteMapper.updateCommentCount(comment.getTargetId(), 1);
        }
    }

    /** 删除评论（恢复游记评论数） */
    @Transactional
    public void delete(Integer commentId, Integer userId) {
        // 先查出评论，确认存在并获取 targetType/targetId
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException(404, "评论不存在");
        int rows = commentMapper.deleteById(commentId, userId);
        if (rows == 0) throw new BusinessException(400, "删除失败");
        // 游记评论数-1
        if (comment.getTargetType() == TARGET_NOTE) {
            travelNoteMapper.updateCommentCount(comment.getTargetId(), -1);
        }
    }
}
