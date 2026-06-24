package com.travel.service;

import com.travel.dto.ApiResponse;
import com.travel.dto.PageRequest;
import com.travel.entity.TravelNote;
import com.travel.exception.BusinessException;
import com.travel.mapper.TravelNoteMapper;
import com.travel.util.XssFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TravelNoteService {

    @Autowired
    private TravelNoteMapper travelNoteMapper;

    /** 游记详情 */
    public TravelNote getById(Integer noteId) {
        TravelNote note = travelNoteMapper.selectById(noteId);
        if (note == null) throw new BusinessException(404, "游记不存在");
        travelNoteMapper.updateViewCount(noteId);
        return note;
    }

    /** 游记列表（分页） */
    public ApiResponse.PageResult<TravelNote> listPage(String keyword, PageRequest page) {
        List<TravelNote> list = travelNoteMapper.selectListPage(keyword, page.getOffset(), page.getPageSize());
        long total = travelNoteMapper.countList(keyword);
        return ApiResponse.page(list, total, page.getPage(), page.getPageSize());
    }

    /** 我的游记（分页） */
    public ApiResponse.PageResult<TravelNote> myNotesPage(Integer userId, PageRequest page) {
        List<TravelNote> list = travelNoteMapper.selectByUserId(userId, page.getOffset(), page.getPageSize());
        long total = travelNoteMapper.countByUserId(userId);
        return ApiResponse.page(list, total, page.getPage(), page.getPageSize());
    }

    /** 发布游记 */
    public void publish(TravelNote note) {
        note.setStatus(1);
        note.setContent(formatContent(note.getContent()));
        note.setTitle(XssFilter.cleanPlainText(note.getTitle()));
        travelNoteMapper.insert(note);
    }

    /** 更新游记 */
    public void update(TravelNote note, Integer userId) {
        TravelNote existing = travelNoteMapper.selectById(note.getNoteId());
        if (existing == null) throw new BusinessException(404, "游记不存在");
        if (!existing.getUserId().equals(userId)) throw new BusinessException(403, "只能修改自己的游记");
        if (note.getContent() != null) note.setContent(formatContent(note.getContent()));
        if (note.getTitle() != null) note.setTitle(XssFilter.cleanPlainText(note.getTitle()));
        // 空字符串视为不更新封面，避免误清空
        if (note.getCoverImage() != null && note.getCoverImage().trim().isEmpty()) {
            note.setCoverImage(null);
        }
        travelNoteMapper.updateById(note);
    }

    /** 内容格式化：换行→HTML段落，XSS过滤 */
    private String formatContent(String raw) {
        if (raw == null) return null;
        // 如果已经包含HTML标签，直接过滤
        if (raw.matches(".*<\\w+[^>]*>.*")) {
            return XssFilter.cleanNoteHtml(raw);
        }
        // 纯文本：双换行→段落，单换行→<br>
        StringBuilder sb = new StringBuilder();
        String[] paragraphs = raw.split("\n\n");
        for (String p : paragraphs) {
            String trimmed = p.trim();
            if (trimmed.isEmpty()) continue;
            sb.append("<p>").append(trimmed.replace("\n", "<br>")).append("</p>");
        }
        return XssFilter.cleanNoteHtml(sb.toString());
    }

    /** 删除游记 */
    public void delete(Integer noteId, Integer userId) {
        TravelNote existing = travelNoteMapper.selectById(noteId);
        if (existing == null) throw new BusinessException(404, "游记不存在");
        if (!existing.getUserId().equals(userId)) throw new BusinessException(403, "只能删除自己的游记");
        travelNoteMapper.deleteById(noteId);
    }

    /** 点赞 */
    public void like(Integer noteId) {
        travelNoteMapper.updateLikeCount(noteId, 1);
    }

    /** 取消点赞 */
    public void unlike(Integer noteId) {
        travelNoteMapper.updateLikeCount(noteId, -1);
    }
}
