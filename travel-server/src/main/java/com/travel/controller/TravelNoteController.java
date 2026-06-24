package com.travel.controller;

import com.travel.dto.ApiResponse;
import com.travel.dto.PageRequest;
import com.travel.entity.TravelNote;
import com.travel.service.TravelNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/travel-note")
public class TravelNoteController {

    @Autowired
    private TravelNoteService travelNoteService;

    /** 游记详情 */
    @GetMapping("/detail/{id}")
    public ApiResponse<TravelNote> detail(@PathVariable("id") Integer id) {
        return ApiResponse.success(travelNoteService.getById(id));
    }

    /** 游记列表（分页） */
    @GetMapping("/list")
    public ApiResponse<ApiResponse.PageResult<TravelNote>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(travelNoteService.listPage(keyword, new PageRequest(page, pageSize)));
    }

    /** 我的游记（分页） */
    @GetMapping("/my")
    public ApiResponse<ApiResponse.PageResult<TravelNote>> myNotes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return ApiResponse.success(travelNoteService.myNotesPage(userId, new PageRequest(page, pageSize)));
    }

    /** 发布游记 */
    @PostMapping("/publish")
    public ApiResponse<?> publish(@RequestBody TravelNote note, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        note.setUserId(userId);
        travelNoteService.publish(note);
        return ApiResponse.success("发布成功");
    }

    /** 更新游记 */
    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable("id") Integer id, @RequestBody TravelNote note,
                                 HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        note.setNoteId(id);
        travelNoteService.update(note, userId);
        return ApiResponse.success("更新成功");
    }

    /** 删除游记 */
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable("id") Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        travelNoteService.delete(id, userId);
        return ApiResponse.success("已删除");
    }

    /** 点赞 */
    @PostMapping("/like/{id}")
    public ApiResponse<?> like(@PathVariable("id") Integer id) {
        travelNoteService.like(id);
        return ApiResponse.success();
    }

    /** 取消点赞 */
    @PostMapping("/unlike/{id}")
    public ApiResponse<?> unlike(@PathVariable("id") Integer id) {
        travelNoteService.unlike(id);
        return ApiResponse.success();
    }
}
