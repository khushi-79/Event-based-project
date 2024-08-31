package com.eventbasedarchitechture.event_based.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CheckNullValueValidator implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return true; // Allow GET requests
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String payload = request.getParameter("payload");
            String url = request.getParameter("openUrl");

            if (payload != null && !payload.isEmpty() && url != null && !url.isEmpty()) {
                return true;
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Both 'payload' and 'openUrl' should be provided as parameters.");
                return false;
            }
        }

        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED); // 405 Method Not Allowed for other methods
        return false;
    }



}