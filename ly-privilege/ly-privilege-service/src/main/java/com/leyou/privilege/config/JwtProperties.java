package com.leyou.privilege.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.PublicKey;

@Slf4j
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {
    /**
     * 公钥的地址
     */
    private String publicKeyPath;

    /**
     * 服务认证token相关属性
     */
    private PrivilegeTokenProperties privilege = new PrivilegeTokenProperties();

    /**
     * 公钥
     */
    private PublicKey publicKey;

    @Data
    public class PrivilegeTokenProperties {

        /**
         * 服务id
         */
        private Long id;

        /**
         * 服务密钥
         */
        private String secret;
        /**
         * 存放服务认证token 的请求头
         */
        private String headerName;
    }





    //实现InitializingBean
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            //给公钥赋值
            this.publicKey = RsaUtils.getPublicKey(publicKeyPath);
        } catch (Exception e) {
            log.error("公钥初始化失败！", e);
            throw new RuntimeException(e);
        }

    }
}
