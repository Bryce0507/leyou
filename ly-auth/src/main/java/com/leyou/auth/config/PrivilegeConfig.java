package com.leyou.auth.config;

import com.leyou.auth.task.PrivilegeTokenHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PrivilegeConfig  {

    @Bean
    public RequestInterceptor requestInterceptor(JwtProperties prop, PrivilegeTokenHolder privilegeTokenHolder) {
        return new PrivilegeFeignInterceptor(prop, privilegeTokenHolder);
    }

    //自定义feign拦截器
    @Slf4j
    static class PrivilegeFeignInterceptor implements RequestInterceptor {
        private JwtProperties jwtProperties;
        private PrivilegeTokenHolder privilegeTokenHolder;

        public PrivilegeFeignInterceptor(JwtProperties jwtProperties, PrivilegeTokenHolder privilegeTokenHolder) {
            this.jwtProperties = jwtProperties;
            this.privilegeTokenHolder = privilegeTokenHolder;
        }

        @Override
        public void apply(RequestTemplate requestTemplate) {
            log.info("添加请求验证token！");
            JwtProperties.PrivilegeTokenProperties privilege = jwtProperties.getPrivilege();
            requestTemplate.header(privilege.getHeaderName(), privilegeTokenHolder.getToken());

        }
    }
}
