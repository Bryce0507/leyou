package com.leyou.auth.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {

    /**
     * 公钥地址
     */
    private String publicKeyPath;
    /**
     * 私钥地址
     */
    private String privateKeyPath;
    /**
     * 公钥
     */
    private PublicKey publicKey;
    /**
     * 私钥
     */
    private PrivateKey privateKey;


    /**
     * 用户token相关属性
     */
    private UserTokenProperties user = new UserTokenProperties();

    /**
     * 在加载完yml文件获取公钥和私钥的地址的后 才执行下面的方法   根据地址获取公私钥
     *
     * @throws Exception
     */

    private PrivilegeTokenProperties privilege = new PrivilegeTokenProperties();
    @Data
    public class UserTokenProperties {
        /**
         * token过期时长
         */
        private int expire;
        /**
         * cookie名字
         */
        private String cookieName;

        /**
         * 存放token的cookie的domain
         */
        private String cookieDomain;

        /**
         * cookie最小的刷新时间
         */
        private int minRefreshInterval;

    }

    @Data
    public class PrivilegeTokenProperties {
        /**
         * 服务id
         *
         */
        private Long id;
        /**
         * 服务密钥
         */
        private String secret;
        /**
         * 存放服务认证的请求头
         */
        private String headerName;

        /**
         * 服务有效时长
         */
        private int expire;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            this.publicKey = RsaUtils.getPublicKey(publicKeyPath);
            this.privateKey = RsaUtils.getPrivateKey(privateKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥和私钥失败",e);
            throw new RuntimeException(e);
        }
    }



}
