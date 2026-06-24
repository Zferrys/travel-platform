package com.travel.controller;

import com.travel.dto.ApiResponse;
import com.travel.dto.PageRequest;
import com.travel.entity.Comment;
import com.travel.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /** 评论列表（分页） */
    @GetMapping("/list/{targetType}/{targetId}")
    public ApiResponse<ApiResponse.PageResult<Comment>> list(
            @PathVariable Integer targetType, @PathVariable Integer targetId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(commentService.listPage(targetType, targetId,
                new PageRequest(page, pageSize)));
    }

    /** 发表评论 */
    @PostMapping("/publish")
    public ApiResponse<?> publish(@RequestBody Comment comment, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        comment.setUserId(userId);
        commentService.publish(comment);
        return ApiResponse.success("评论成功");
    }

    /** 删除评论 */
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable("id") Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        commentService.delete(id, userId);
        return ApiResponse.success("已删除");
    }
}
