package com.test.config;

import com.test.dto.MemberDto;
import io.jsonwebtoken.Claims;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JwtInterceptor implements HandlerInterceptor {

    private JwtUtil jwtUtil;

    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);
        if (token == null) {
            return true;
        }

        try {
            Claims claims = jwtUtil.parse(token);
            MemberDto member = new MemberDto();
            member.setId(Long.parseLong(claims.getSubject()));
            member.setEmail(claims.get("email", String.class));
            member.setName(claims.get("name", String.class));
            request.setAttribute("loginMember", member);
        } catch (Exception e) {
            // 만료되거나 유효하지 않은 토큰 → 쿠키 삭제 후 통과
            Cookie expired = new Cookie("jwt", "");
            expired.setMaxAge(0);
            expired.setPath("/");
            response.addCookie(expired);
        }

        return true;
    }

    private String extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if ("jwt".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
