package com.eventbasedarchitechture.event_based.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Register implements WebMvcConfigurer {

    @Autowired
    private CheckNullValueValidator payloadCheckValidator;


    @Override
    public void addInterceptors(InterceptorRegistry registry){
//        registry.addInterceptor(checkFailure).addPathPatterns("/events/*");
        registry.addInterceptor(payloadCheckValidator).addPathPatterns("/events");
    }
}
