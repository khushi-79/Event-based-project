package com.eventbasedarchitechture.event_based.interceptor;

import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class AuthorizeValidator implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception{

        String status = request.getParameter("status");

        // Check if status is "PROCESSED"
        if (status != null && status.equalsIgnoreCase("PROCESSED")) {
            return true;
        } else {
           response.setStatus(403);  // Forbidden
            return false;
        }
    }
}
