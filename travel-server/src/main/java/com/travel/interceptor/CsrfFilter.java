package com.travel.interceptor;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * CSRF 防护过滤器
 * GET/HEAD/OPTIONS 请求生成 Token，POST/PUT/DELETE 校验 Token
 */
public class CsrfFilter implements Filter {

    private static final String CSRF_TOKEN_ATTR = "CSRF_TOKEN";
    private static final String CSRF_HEADER = "X-CSRF-TOKEN";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String method = httpRequest.getMethod();

        // 安全方法：生成/刷新 CSRF Token 存入 Session
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method)) {
            ensureCsrfToken(httpRequest, httpResponse);
            chain.doFilter(request, response);
            return;
        }

        // API 路径使用 JWT 认证，跳过 CSRF（JWT 本身防 CSRF）
        if (httpRequest.getRequestURI().startsWith(httpRequest.getContextPath() + "/api/")) {
            chain.doFilter(request, response);
            return;
        }

        // 非安全方法：校验 CSRF Token
        String headerToken = httpRequest.getHeader(CSRF_HEADER);
        String sessionToken = (String) httpRequest.getSession().getAttribute(CSRF_TOKEN_ATTR);

        if (headerToken == null || sessionToken == null || !headerToken.equals(sessionToken)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"code\":403,\"message\":\"CSRF校验失败\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private void ensureCsrfToken(HttpServletRequest request, HttpServletResponse response) {
        Object token = request.getSession().getAttribute(CSRF_TOKEN_ATTR);
        if (token == null) {
            String newToken = UUID.randomUUID().toString();
            request.getSession().setAttribute(CSRF_TOKEN_ATTR, newToken);
        }
        // 暴露给前端通过 JS 读取
        if (response.getHeader(CSRF_HEADER) == null) {
            String sessionToken = (String) request.getSession().getAttribute(CSRF_TOKEN_ATTR);
            response.setHeader(CSRF_HEADER, sessionToken);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
