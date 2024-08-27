package com.eventbasedarchitechture.event_based.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AccessTokenValidator implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception{

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED); // 405 Method Not Allowed
//            response.getWriter().write("This endpoint only supports PO requests.");
            return false;
        }

        String payload = request.getParameter("payload");
//        String status = request.getParameter("status");

        if (payload!=null && !payload.isEmpty()){
//            response.getWriter().write("Events: " + payload + "\n");
//            response.getWriter().write("This payload is returned.");
            return true;
        }else{
            response.setStatus(403);
//            response.getWriter().write("Payload is missing or empty.");
            return false;
        }
    }

}