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

        String payload = request.getParameter("payload");
        String status = request.getParameter("status");

        if (payload!=null && !payload.isEmpty() && status!=null){
            return true;
        }else{
            response.setStatus(403);
            return false;
        }
    }

}