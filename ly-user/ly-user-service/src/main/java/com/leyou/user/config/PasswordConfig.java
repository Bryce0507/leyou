package com.leyou.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;

@Configuration
@ConfigurationProperties(prefix = "ly.encoder.crypt")
@Data
public class PasswordConfig {

    private int strength;
    private String secret;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        //利用密钥生成随机的安全码
        SecureRandom secureRandom = new SecureRandom(secret.getBytes());
        //初始化BCryptPasswordEncoder
        return new BCryptPasswordEncoder(strength, secureRandom);


    }
}
