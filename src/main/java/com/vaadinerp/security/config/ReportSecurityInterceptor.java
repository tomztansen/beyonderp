package com.vaadinerp.security.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ReportSecurityInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object user = request.getSession().getAttribute("SPRING_MVC_USER");
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access. Please login via the main application first.");
            return false;
        }
        return true;
    }
}
