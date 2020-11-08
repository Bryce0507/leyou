package com.leyou.order.web;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.spring.annotation.MapperScan;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("pay")
public class PayController {

    @Autowired
    private OrderService orderService;


    /**
     * 支付成功后的回调
     * @param result
     * @return
     */
    @PostMapping(value = "/wx/notify", produces = "application/xml")
    public Map<String, String> handleNotify(@RequestBody Map<String, String> result) {
        //处理回调
        log.info("【支付回调】接收微信支付回调，结果{}", result);
        orderService.handleNotify(result);
        //返回成功
        Map<String, String> msg = new HashMap<>();
        msg.put("result_code", "SUCCESS");
        msg.put("result_msg", "OK");
        return msg;
    }

}
