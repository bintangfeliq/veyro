package com.example.veyro.config;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FotoProfilConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String lokasi = Path.of("uploads").toAbsolutePath().normalize().toUri().toString();
        CacheControl cacheSatuTahun = CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic();
        registry.addResourceHandler("/uploads/**").addResourceLocations(lokasi.endsWith("/") ? lokasi : lokasi + "/");
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/").setCacheControl(cacheSatuTahun);
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/").setCacheControl(cacheSatuTahun);
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/").setCacheControl(cacheSatuTahun);
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/").setCacheControl(cacheSatuTahun);
    }
}
