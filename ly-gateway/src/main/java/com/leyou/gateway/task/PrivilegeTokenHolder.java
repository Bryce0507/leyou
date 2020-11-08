package com.leyou.gateway.task;

import com.leyou.gateway.client.AuthClient;
import com.leyou.gateway.config.JwtProperties;
import jdk.nashorn.internal.parser.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Slf4j
@Component
//@EnableConfigurationProperties(JwtProperties.class)
public class PrivilegeTokenHolder {

    @Autowired
    private JwtProperties prop;

    private String token;

    /**
     * token 的刷新时间
     */
    private static final long TOKEN_REFRESH_INTERVAL = 86400000L;

    /**
     * token获取失败后重试的时间
     */
    private static final long TOKEN_RETRY_INTERVAL = 10000L;

    @Autowired
    private AuthClient authClient;

    @Scheduled(fixedDelay = TOKEN_REFRESH_INTERVAL)
    private void loadToken() throws InterruptedException {
        //发起请求获取token
        while (true) {
            try {
                token = authClient.authenticate(prop.getPrivilege().getId(), prop.getPrivilege().getSecret());
                log.info("加载token成功，时间：{}", LocalTime.now().toString());
                break;
            } catch (Exception e) {
                log.error("加载token失败",e);
            }
            //休眠10秒后重试
            Thread.sleep(TOKEN_RETRY_INTERVAL);
        }
    }

    public String getToken() {
        return token;
    }

}
