package com.leyou.cart.interceptor;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.threadLocals.UserHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor implements HandlerInterceptor {

    private static final String HEADER_NAME = "user_info";
    //前置拦截器
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取请求头
        String header = request.getHeader(HEADER_NAME);
        if (StringUtils.isBlank(header)) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        UserHolder.setUser(Long.valueOf(header));
        return true;
    }

}
