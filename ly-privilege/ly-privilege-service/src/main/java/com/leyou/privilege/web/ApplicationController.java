package com.leyou.privilege.web;

import com.leyou.privilege.dto.ApplicationDTO;
import com.leyou.privilege.entity.ApplicationInfo;
import com.leyou.privilege.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("app")
public class ApplicationController {
    @Autowired
    private ApplicationService applicationService;

    /**
     * 新增服务信息
     * @param applicationDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveApplicationInfo(ApplicationDTO applicationDTO) {
        applicationService.saveApplicationInfo(applicationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据id和密码查询服务信息
     * @param id
     * @param secret
     * @return
     */
    @GetMapping("query")
    public ResponseEntity<ApplicationDTO> queryByAppIdAndSecret(
            @RequestParam("id") Long id, @RequestParam("secret") String secret) {
        return ResponseEntity.ok(applicationService.queryByAppIdAndSecret(id, secret));

    }


}
