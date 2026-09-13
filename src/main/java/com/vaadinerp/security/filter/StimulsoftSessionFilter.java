package com.vaadinerp.security.filter;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet mentah Stimulsoft (/stimulsoft_webviewer_action, /stimulsoft_webdesigner_action,
 * /stimulsoft_web_resource) didaftarkan lewat ServletRegistrationBean, jadi tidak lewat
 * DispatcherServlet -> ReportSecurityInterceptor tidak berlaku, dan vaadin.excludeUrls
 * melepasnya dari Vaadin. Tanpa filter ini aksi designer/viewer bisa dipanggil tanpa sesi.
 * Aturan: wajib ada user di sesi (atribut yang sama dengan interceptor); selain itu 401.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 11)
public class StimulsoftSessionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/stimulsoft_")) {
            Object user = request.getSession(false) != null ? request.getSession(false).getAttribute("SPRING_MVC_USER") : null;
            if (user == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login required.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
