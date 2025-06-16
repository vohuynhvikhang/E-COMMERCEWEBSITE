package com.poly.asm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.poly.asm.entitys.User;

import java.util.Arrays;
import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final List<String> USER_REQUIRED_URLS = Arrays.asList(
            "/account/edit", "/account/chgpwd", "/order/"
    );
    private static final List<String> ADMIN_REQUIRED_URLS = Arrays.asList(
            "/admin/"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        Object user = request.getSession().getAttribute("user");

        if (USER_REQUIRED_URLS.stream().anyMatch(uri::startsWith)) {
            if (user == null) {
                response.sendRedirect("/account/login?error=Vui lòng đăng nhập");
                return false;
            }
        }

        if (ADMIN_REQUIRED_URLS.stream().anyMatch(uri::startsWith) && !uri.equals("/admin/home/index")) {
            if (user == null || !isAdmin(user)) {
                response.sendRedirect("/account/login?error=Bạn không có quyền truy cập");
                return false;
            }
        }

        return true;
    }

    private boolean isAdmin(Object user) {
        return user instanceof User && "ADMIN".equals(((User) user).getRole());
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {}

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {}
}