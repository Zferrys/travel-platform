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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
        if (note.getTitle() != null) {
            String title = note.getTitle().trim();
            if (title.isEmpty() || title.length() > 100) {
                throw new BusinessException(400, "游记标题长度需在 1-100 之间");
            }
            note.setTitle(XssFilter.cleanPlainText(title));
        }
        // 禁止通过更新接口修改 status，防止绕过审核流程
        note.setStatus(null);
        // 空字符串视为不更新封面，避免误清空
        if (note.getCoverImage() != null && note.getCoverImage().trim().isEmpty()) {
            note.setCoverImage(null);
        }
        travelNoteMapper.updateById(note);
    }

    /**
     * 内容格式化：纯文本换行→HTML段落，已有HTML标签则直接过滤。
     * 统一经过 Jsoup 白名单过滤，不依赖正则判断是否含 HTML。
     */
    private String formatContent(String raw) {
        if (raw == null) return null;
        // 包含 HTML 标签特征 → 直接走 Jsoup 白名单过滤
        if (raw.indexOf('<') >= 0 && raw.indexOf('>') >= 0) {
            return XssFilter.cleanNoteHtml(raw);
        }
        // 纯文本：双换行→段落，单换行→<br>
        StringBuilder sb = new StringBuilder(raw.length() + 128);
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

    /** 点赞（防重：同一用户对同一游记只能点赞一次） */
    public void like(Integer noteId, Integer userId) {
        if (userId == null) throw new BusinessException(401, "请先登录");
        TravelNote note = travelNoteMapper.selectById(noteId);
        if (note == null) throw new BusinessException(404, "游记不存在");
        if (!LikeDedup.recordAndCheck(noteId, userId)) {
            throw new BusinessException(400, "已经点过赞了");
        }
        travelNoteMapper.updateLikeCount(noteId, 1);
    }

    /** 取消点赞 */
    public void unlike(Integer noteId, Integer userId) {
        if (userId == null) throw new BusinessException(401, "请先登录");
        TravelNote note = travelNoteMapper.selectById(noteId);
        if (note == null) throw new BusinessException(404, "游记不存在");
        if (!LikeDedup.isLiked(noteId, userId)) {
            throw new BusinessException(400, "还没有点过赞");
        }
        LikeDedup.remove(noteId, userId);
        travelNoteMapper.updateLikeCount(noteId, -1);
    }

    /**
     * 点赞防重器 — 基于 ConcurrentHashMap 的内存记录。
     * key = "noteId:userId"，服务重启后清空（可接受，不要求持久化）。
     */
    private static class LikeDedup {
        private static final ConcurrentHashMap<String, Boolean> LIKES = new ConcurrentHashMap<>();

        static boolean isLiked(Integer noteId, Integer userId) {
            return Boolean.TRUE.equals(LIKES.get(key(noteId, userId)));
        }

        /** 记录点赞，返回 false 表示已点过 */
        static boolean recordAndCheck(Integer noteId, Integer userId) {
            return LIKES.putIfAbsent(key(noteId, userId), Boolean.TRUE) == null;
        }

        static void remove(Integer noteId, Integer userId) {
            LIKES.remove(key(noteId, userId));
        }

        private static String key(Integer noteId, Integer userId) {
            return noteId + ":" + userId;
        }
    }
}
