package com.ali.antelaka;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // أي رابط يبدأ بـ /uploads/ يروح للمجلد اللي على VPS
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/home/ali/uploads/");
    }
}

