package cn.sbx0.upload.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.MultipartConfigElement;

@Configuration
public class UploadFileConfig implements WebMvcConfigurer {

    @Value("${file.uploadFolder}")
    private String uploadFolder;
    @Value("${file.staticAccessPath}")
    private String staticAccessPath;

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation(uploadFolder);
        factory.setMaxFileSize("100Mb"); // 文件最大
        factory.setMaxRequestSize("100Mb");// 设置总上传数据总大小
        return factory.createMultipartConfig();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(staticAccessPath).addResourceLocations("file:" + uploadFolder);
    }
}