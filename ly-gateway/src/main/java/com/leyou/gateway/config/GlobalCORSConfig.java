package com.leyou.gateway.config;


import com.leyou.gateway.properties.CORSProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableConfigurationProperties(CORSProperties.class)
public class GlobalCORSConfig {

    @Bean
    public CorsFilter corsFilter(CORSProperties prop) {
        //1.添加CORS配置信息
        CorsConfiguration config = new CorsConfiguration();
        //允许的域
        prop.getAllowedOrigins().forEach(config::addAllowedOrigin);
        //是否发送cookie信息
        config.setAllowCredentials(prop.getAllowCredentials());
        //允许的请求方式
  /*      config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");*/

        prop.getAllowedMethods().forEach(config::addAllowedMethod);
        //允许头的信息
//        config.addAllowedHeader("*");

        prop.getAllowedHeaders().forEach(config::addAllowedHeader);

        //添加映射路径，我们拦截一切请求
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration(prop.getFilterPath(),config);

        //返回新的CORSFilter
        return new CorsFilter(configSource);
    }
}
