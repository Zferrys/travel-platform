package com.travel.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * XSS 过滤器 — 使用 Jsoup 白名单机制过滤 HTML
 */
public class XssFilter {

    /** 游记内容的宽松白名单（允许常见排版标签） */
    private static final Safelist NOTE_SAFELIST = Safelist.relaxed()
            .addTags("h1", "h2", "h3", "h4", "h5", "h6", "div", "span",
                     "img", "figure", "figcaption", "video", "audio", "source",
                     "table", "thead", "tbody", "tr", "th", "td", "hr", "br",
                     "pre", "code", "blockquote", "font")
            .addAttributes("img", "src", "alt", "title", "width", "height")
            .addAttributes("video", "src", "controls", "width", "height")
            .addAttributes("font", "size", "color")
            .addAttributes("table", "border", "cellpadding", "cellspacing")
            .addAttributes(":all", "class")
            .addProtocols("img", "src", "http", "https")
            .addProtocols("a", "href", "http", "https", "mailto");

    /** 纯文本（评论等），剥离所有 HTML */
    private static final Safelist TEXT_ONLY = Safelist.none();

    /**
     * 过滤游记 HTML 内容（允许排版标签）
     */
    public static String cleanNoteHtml(String html) {
        if (html == null) return null;
        return Jsoup.clean(html, NOTE_SAFELIST);
    }

    /**
     * 过滤纯文本内容（剥离所有 HTML）
     */
    public static String cleanPlainText(String text) {
        if (text == null) return null;
        return Jsoup.clean(text, TEXT_ONLY);
    }
}
