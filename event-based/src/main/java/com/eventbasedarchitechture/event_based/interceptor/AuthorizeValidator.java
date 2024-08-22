package com.eventbasedarchitechture.event_based.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class AuthorizeValidator implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizeValidator.class);

    @Override
    public boolean preHandle(
            HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception{

        String status = request.getParameter("status");
        logger.info("Status received: {}", status);

        // Check if status is "PROCESSED"
        if (status != null && status.equalsIgnoreCase("PROCESSED")) {
            logger.info("Status is PROCESSED, proceeding with request");
            return true;  // Continue processing the request
        } else {
            logger.warn("Status is NOT PROCESSED, blocking request");
            response.setStatus(403);  // Forbidden
            return false;  // Reject the request
        }
    }
}
