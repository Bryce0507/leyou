package com.leyou.gateway.filters;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProp;

    @Autowired
    private FilterProperties filterProp;


    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取请求
        HttpServletRequest request = ctx.getRequest();
        //获取路径
        String requestURI = request.getRequestURI();
        //判断白名单
        return !isAllowPath(requestURI);
    }

    private boolean isAllowPath(String requestURI) {
        //定义一个标记
        boolean flag = false;
        //遍历可访问的路径
        for (String allowPath : this.filterProp.getAllowPaths()) {
            if (requestURI.startsWith(allowPath)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    @Override
    public Object run() throws ZuulException {
        //获取上下文
        RequestContext currentContext = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = currentContext.getRequest();
        //获取token
        String token = CookieUtils.getCookieValue(request, jwtProp.getUser().getCookieName());
        //获取荷载
        try {
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, jwtProp.getPublicKey(), UserInfo.class);
            //解析没问题获取用户信息
            UserInfo userInfo = payload.getUserInfo();
            //获取角色
            String role = userInfo.getRole();
            //获取当前的资源路径
            String path = request.getRequestURI();
            String method = request.getMethod();
            //TODO 判断权限，此处暂时空置，等待权限服务完成后补充
            log.info("【网关】用户{}，角色{}。访问服务{}：{}", userInfo.getUsername(), userInfo.getRole(), path, method);
            //将用户id放入到请求头中 在购物车的微服务中会用到
            currentContext.addZuulRequestHeader(jwtProp.getUser().getHeaderName(),userInfo.getId().toString());
        } catch (Exception e) {
            //校验异常，返回403
            currentContext.setSendZuulResponse(false);
            currentContext.setResponseStatusCode(403);
            log.error("非法访问，未登录，地址{}",request.getRemoteUser(),e);
        }
        return null;

    }
}
