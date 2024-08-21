package com.eventbasedarchitechture.event_based.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthorizeValidator implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception{

        String status = request.getParameter("status");

        if(status!=null && status.equalsIgnoreCase("PENDING")){
            return true;
        }else{
            response.setStatus(403);
            return false;
        }
    }
}
