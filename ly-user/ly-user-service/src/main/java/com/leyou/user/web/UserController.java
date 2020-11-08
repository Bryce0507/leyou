package com.leyou.user.web;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.user.AddressDTO;
import com.leyou.user.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 数据验证功能
     * @param param
     * @param type
     * @return
     */
    @GetMapping("/check/{param}/{type}")
    @ApiOperation(value = "校验用户名数据是否可用，如果不存在则可用")
    @ApiResponses({
            @ApiResponse(code=200,message = "校验结果有效，true或false代表可用或不可用"),
            @ApiResponse(code=400,message = "请求参数有误，比如type不是指定的值")
    })
    public ResponseEntity<Boolean> checkUserData(
            @ApiParam(value = "要校验的数据",example = "lisi") @PathVariable("param") String param,
            @ApiParam(value = "数据类型，1：用户名，2：手机号", example = "1")@PathVariable("type") Integer type) {
        return ResponseEntity.ok(userService.checkUserData(param, type));
    }

    /**
     * 发送验证码
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone) {
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 用户注册
     * @param user
     * @param code
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, BindingResult result, @RequestParam("code") String code) {
        if (result.hasErrors()) {
            String message = result.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("|"));
            throw new LyException(400, message);
        }
        userService.register(user, code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名和密码查询用户
     *
     * @return
     */
    @GetMapping("query")
    public ResponseEntity<UserDTO> queryUserByUsernameAndPassword(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    ) {
        return ResponseEntity.ok(userService.queryUserByUsernameAndPassword(username, password));

    }


    /**
     * 根据用户id 和 地址id 查询 地址
     * @param userId
     * @param id
     * @return
     */
    @GetMapping("address")
    public ResponseEntity<AddressDTO> queryAddressById(@RequestParam("userId") Long userId, @RequestParam("id") Long id) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setId(1L);
        addressDTO.setStreet("航头镇航头路18号传智播客 3号楼");
        addressDTO.setCity("上海");
        addressDTO.setDistrict("浦东新区");
        addressDTO.setAddressee("虎哥");
        addressDTO.setPhone("15800000000");
        addressDTO.setProvince("上海");
        addressDTO.setZipCode("210000");
        addressDTO.setIsDefault(true);
        return ResponseEntity.ok(addressDTO);
    }


}
