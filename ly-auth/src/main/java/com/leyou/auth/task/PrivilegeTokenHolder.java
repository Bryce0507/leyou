package com.leyou.auth.task;

import com.leyou.auth.config.JwtProperties;
import com.leyou.common.auth.entity.AppInfo;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Slf4j
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class PrivilegeTokenHolder {

    @Autowired
    private JwtProperties jwtProp;

    private static final long TOKEN_REFRESH_INTERVAL = 86400000L;
    private String token;

    @Scheduled(fixedDelay = TOKEN_REFRESH_INTERVAL)
    private void loadToken() {
        //自己生成token
        try {
            //自己生成token
            AppInfo info = new AppInfo();
            info.setId(jwtProp.getPrivilege().getId());
            info.setServiceName(jwtProp.getPrivilege().getSecret());
            token = JwtUtils.generateTokenExpireInSeconds(info, jwtProp.getPrivateKey(), jwtProp.getPrivilege().getExpire());
            log.info("加载token成功，时间：{}", LocalTime.now().toString());
        } catch (Exception e) {
            throw new RuntimeException("加载token失败！", e);
        }
    }

    public String getToken() {
        return token;
    }
}
