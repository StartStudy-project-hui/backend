package com.study.studyproject.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class CorsConfig  {

    public static final String REFRESH_TOKEN = "Refresh_Token";
    public static final String ACCESS_TOKEN = "Access_Token";
    public static final String ALL = "*";

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Arrays.asList("http://localhost:5173", "https://www.startstudy.store/"
                ,"http://localhost:8080"));
        config.addAllowedHeader(ALL);
        config.addAllowedMethod(ALL);
        config.setExposedHeaders(Arrays.asList(REFRESH_TOKEN, ACCESS_TOKEN));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}
