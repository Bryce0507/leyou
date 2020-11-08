package com.leyou.order.interceptor;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.threadLocals.UserHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor  implements HandlerInterceptor {
    private static final String HEADER_NAME = "user_info";
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取用户头信息
        String userId = request.getHeader(HEADER_NAME);
        if (StringUtils.isBlank(userId)) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        //将userId存入到threadLocal
        UserHolder.setUser(Long.valueOf(userId));
        return true;
    }
}
