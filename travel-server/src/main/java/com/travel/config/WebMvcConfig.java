package com.travel.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * 配置 /uploads/** URL 映射到文件上传目录，使前端可以访问上传的图片。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = UploadDirResolver.resolve();
        String location = "file:" + uploadDir.toString().replace("\\", "/") + "/";
        log.info("Mapping /uploads/** → {}", location);
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
