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
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new BusinessException(400, "评论内容不能为空");
        }
        if (comment.getContent().length() > 2000) {
            throw new BusinessException(400, "评论内容过长（最多2000字）");
        }
        comment.setContent(XssFilter.cleanPlainText(comment.getContent()));
        commentMapper.insert(comment);
        // 游记评论数+1
        if (comment.getTargetType() == 3) {
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
        if (comment.getTargetType() == 3) {
            travelNoteMapper.updateCommentCount(comment.getTargetId(), -1);
        }
    }
}
