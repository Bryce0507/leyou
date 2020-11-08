package com.leyou.user.client;

import com.leyou.user.AddressDTO;
import com.leyou.user.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("user-service")
public interface UserClient {


    /**
     * 根据用户名和密码查询用户
     *
     * @return
     */
    @GetMapping("query")
    UserDTO queryUserByUsernameAndPassword(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    );

    /**
     * 根据用户id 和 地址id 查询 地址
     * @param userId
     * @param id
     * @return
     */
    @GetMapping("address")
    AddressDTO queryAddressById(@RequestParam("userId") Long userId, @RequestParam("id") Long id);
}
