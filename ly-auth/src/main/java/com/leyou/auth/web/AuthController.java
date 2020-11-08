package com.leyou.auth.web;

import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    /**
     * 登录授权
     * @param username
     * @param password
     * @param response
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestParam("username") String username, @RequestParam("password") String password,
                                      HttpServletResponse response) {
        authService.login(username, password, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 验证用户信息
     * @param response
     * @param request
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyUser(HttpServletResponse response, HttpServletRequest request) {
        return ResponseEntity.ok(authService.verifyUser(request, response));
    }

    /**
     * 登出服务
     * @param request
     * @param response
     * @return
     */
    @PostMapping("logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok().build();
    }

    /**
     * 微服务认证并申请令牌
     * @param id
     * @param secret
     * @return
     */
    @GetMapping("authentication")
    public ResponseEntity<String> authenticate(@RequestParam("id") Long id, @RequestParam("secret") String secret) {
        return ResponseEntity.ok(authService.authenticate(id, secret));

    }
}
