package com.leyou.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "ly.oss")
@Component
public class OSSProperties {
    private String accessKeyId;
    private String accessKeyService;
    private String host;
    private String endpoint;
    private String dir;
    private Long expireTime;
    private Long maxFileSize;
}
