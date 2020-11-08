package com.leyou.gateway.config;

import com.leyou.common.auth.utils.RsaUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {

    /**
     * 公钥地址
     */
    private PublicKey publicKey;

    /**
     * 公钥地址
     */
    private String publicKeyPath;


    private UserTokenProperties user = new UserTokenProperties();

    private PrivilegeTokenProperties privilege = new PrivilegeTokenProperties();


    @Data
    public class PrivilegeTokenProperties {
        /**
         * 服务id
         */
        private Long id;

        /**
         * 服务的密钥
         */
        private String secret;

        /**
         * 存放服务认证token 的请求头
         */
        private String headerName;
    }


    @Data
    public class UserTokenProperties {
        /**
         * 存房token的cookie名字
         */
        private String cookieName;

        /**
         * 存放用户的请求头名称
         */
        private String headerName;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        //获取公钥
        try {
            this.publicKey = RsaUtils.getPublicKey(publicKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥失败！",e);
            throw new RuntimeException(e);
        }

    }
}
