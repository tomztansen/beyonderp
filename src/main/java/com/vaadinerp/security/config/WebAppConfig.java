package com.vaadinerp.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class WebAppConfig implements WebMvcConfigurer {
    
    @Autowired
    private ReportSecurityInterceptor reportSecurityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(reportSecurityInterceptor)
                .addPathPatterns("/stimulsoft-java/**");
    }
}
