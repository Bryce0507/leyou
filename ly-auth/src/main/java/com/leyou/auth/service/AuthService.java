package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.common.auth.entity.AppInfo;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.privilege.client.ApplicationClient;
import com.leyou.privilege.dto.ApplicationDTO;
import com.leyou.user.UserDTO;
import com.leyou.user.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.concurrent.TimeUnit;
@Slf4j
@Service
public class AuthService {

    private static final String USER_ROLE = "guest";
    @Autowired
    private JwtProperties prop;

    @Autowired
    private UserClient userClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ApplicationClient applicationClient;

    /**
     * 登录授权
     * @param username
     * @param password
     * @param response
     */
    public void login(String username, String password, HttpServletResponse response) {
        //查询用户
        try {
            UserDTO userDTO = userClient.queryUserByUsernameAndPassword(username, password);
            //生成userInfo，没写权限功能，暂时都用guest
            UserInfo userInfo = new UserInfo(userDTO.getId(), userDTO.getUsername(), USER_ROLE);

            //生成token
            String token = JwtUtils.generateTokenExpireInMinutes(userInfo, prop.getPrivateKey(), prop.getUser().getExpire());

            //写入cookie中
            CookieUtils.newCookieBuilder()
                    .response(response)   //response,用于写cookie
                    .httpOnly(true)     //保证安全 防止xxs 攻击 ，允许js操作cookie
                    .domain(prop.getUser().getCookieDomain())  //设置domain
                    .name(prop.getUser().getCookieName())
                    .value(token)  //设置cookie名称和值
                    .build();  //写cookie
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }


    }

    /**
     * 验证用户信息
     * @param request
     * @param response
     * @return
     */
    public UserInfo verifyUser(HttpServletRequest request, HttpServletResponse response) {
        try {
            //读取cookie
            String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
            //解析token的信息
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);
            //获取token的id
            String id = payload.getId();
            //判断redis 中是否存在这个id
            Boolean bool = redisTemplate.hasKey(id);
            if (bool != null && bool) {
                //抛出异常,证明token无效，直接返货401
                throw new LyException(ExceptionEnum.UNAUTHORIZED);
            }
            //获取过期时间
            Date expiration = payload.getExpiration();
//            System.out.println(new SimpleDateFormat().format(expiration));
            //获取刷新时间
            DateTime refreshTime = new DateTime(expiration.getTime()).minusMinutes(prop.getUser().getMinRefreshInterval());
//            System.out.println(new SimpleDateFormat().format(refreshTime.getMillis()));
            //判断是否已经过了刷新时间
            if (refreshTime.isBefore(System.currentTimeMillis())) {
                //如果过了刷新时间，则重新生成一个token
                token = JwtUtils.generateTokenExpireInMinutes(payload.getUserInfo(), prop.getPrivateKey(), prop.getUser().getExpire());
                //写入cookie
                CookieUtils.newCookieBuilder()
                        //response 用于写cookie
                        .response(response)
                        //不允许js操作cookie 防止xxs攻击
                        .httpOnly(true)
                        //设置cook的name
                        .name(prop.getUser().getCookieName())
                        //设置cookie的value
                        .value(token)
                        //设置domain
                        .domain(prop.getUser().getCookieDomain())
                        .build();
            }
            //获取荷载中的 userInfo数据
            return payload.getUserInfo();
        } catch (Exception e) {
            log.error("用户信息证明失败！", e);
            //抛出异常证明token无效，直接返回401
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }

    }

    /**
     * 用户的登出
     * @param request
     * @param response
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        //获取token
        String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
        //解析token
        Payload<Object> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
        //获取荷载id
        String id = payload.getId();
        //剩下的有效时长
        Long time = payload.getExpiration().getTime() - System.currentTimeMillis();
        //将id 和剩余时长存入到redis中
        if (time > 5000) {  //如果时间剩下5秒以内就不存入redis中
            redisTemplate.opsForValue().set(id, "", time, TimeUnit.MILLISECONDS);
        }
        //删除cookie
        CookieUtils.deleteCookie(prop.getUser().getCookieName(), prop.getUser().getCookieDomain(), response);

    }

    /**
     * 微服务认证并申请令牌
     * @param id
     * @param secret
     * @return
     */
    public String authenticate(Long id, String secret) {
        //验证id和secret
        ApplicationDTO applicationDTO = applicationClient.queryByAppIdAndSecret(id, secret);
        //生成token
        AppInfo info = new AppInfo();
        info.setId(applicationDTO.getId());
        info.setServiceName(applicationDTO.getServiceName());
        //生成token并返回
        return JwtUtils.generateTokenExpireInSeconds(info, prop.getPrivateKey(), prop.getPrivilege().getExpire());

    }
}
