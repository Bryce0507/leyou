package com.leyou.privilege.client;


import com.leyou.privilege.dto.ApplicationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("privilege-service")
public interface ApplicationClient {

    /**
     * 根据id和密码查询服务信息
     *
     * @param id
     * @param secret
     * @return
     */
    @GetMapping("app/query")
    ApplicationDTO queryByAppIdAndSecret(
            @RequestParam("id") Long id, @RequestParam("secret") String secret);





}
