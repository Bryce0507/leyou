package com.leyou.common;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.RsaUtils;
import org.junit.Test;

import javax.sound.sampled.DataLine;
import java.security.PrivateKey;
import java.security.PublicKey;

public class AuthTest {
    private String privateFilePath = "D:\\progarm\\ssh\\id_rsa";
    private String publicFilePath = "D:\\progarm\\ssh\\id_rsa.pub";


    @Test
    public void TestAuth() throws Exception {
        //生成密钥对
        RsaUtils.generateKey(publicFilePath, privateFilePath, "hello", 2048);

        //获取私密
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateFilePath);
        //获取公钥
        PublicKey publicKey = RsaUtils.getPublicKey(publicFilePath);
        
    }

    @Test
    public void testJWT() throws Exception {
        //获取私钥
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateFilePath);
        //生成token
        String token = JwtUtils.generateTokenExpireInMinutes(new UserInfo(1l, "lisi", "guest"),
                privateKey, 5);
        System.out.println("token = " + token);

        //获取公钥
        PublicKey publicKey = RsaUtils.getPublicKey(publicFilePath);
        //解析token
        Payload<UserInfo> info = JwtUtils.getInfoFromToken(token, publicKey, UserInfo.class);

        System.out.println("info.getId() = " + info.getId());
        System.out.println("info.getUserInfo() = " + info.getUserInfo());
        System.out.println("info.getExpiration() = " + info.getExpiration());


    }
}
