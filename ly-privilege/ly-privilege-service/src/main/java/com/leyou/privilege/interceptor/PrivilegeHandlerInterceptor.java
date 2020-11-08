package com.leyou.privilege.interceptor;

import com.leyou.common.auth.entity.AppInfo;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.privilege.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class PrivilegeHandlerInterceptor implements HandlerInterceptor {

    private JwtProperties jwtProp;

    public PrivilegeHandlerInterceptor(JwtProperties jwtProp) {
        this.jwtProp = jwtProp;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            //获取请求头
            String token = request.getHeader(jwtProp.getPrivilege().getHeaderName());
            //校验
            Payload<AppInfo> payload = JwtUtils.getInfoFromToken(token, jwtProp.getPublicKey(), AppInfo.class);
            //TODO 获取token中的服务信息，做细粒度认证
            AppInfo info = payload.getUserInfo();
            log.info("服务{}正在请求资源：{}", info.getServiceName(), request.getRequestURI());
            return true; //拦截器放行
        } catch (Exception e) {
            log.error("服务访问拒绝，token认证失败！",e);
            return false; //拦截
        }
    }
}
