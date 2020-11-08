package com.leyou.common.auth.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RsaUtils {


    private static final int DEFAULT_KEY_SIZE = 2048;

    /**
     * 从文件中读取公钥
     * @param fileName  公钥保存路径
     * @return 公钥对象
     */
    public static PublicKey getPublicKey(String fileName) throws Exception {
        byte[] bytes = readFile(fileName);
        return getPublicKey(bytes);
    }

    /**
     * 从文件中读取密钥
     *
     * @param fileName 密钥保存路径
     * @return 密钥对象
     */
    public static PrivateKey getPrivateKey(String fileName) throws Exception {
        byte[] bytes = readFile(fileName);
        return getPrivateKey(bytes);
    }


    /**
     * 获取公钥
     * @param bytes  公钥的字节形式
     * @return
     * @throws Exception
     */
    private static PublicKey getPublicKey(byte[] bytes) throws Exception {
        bytes = Base64.getDecoder().decode(bytes);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    /**
     * 获取私钥
     * @param bytes  私钥的字节形式
     * @return   私钥对象
     * @throws Exception
     */
    private static PrivateKey getPrivateKey(byte[] bytes) throws Exception {
        bytes = Base64.getDecoder().decode(bytes);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }

    /**
     * 文件名转换成字节形式
     * @param fileName  文件名
     * @return
     * @throws IOException
     */
    private static byte[] readFile(String fileName) throws IOException {
        byte[] bytes = Files.readAllBytes(new File(fileName).toPath());
        return bytes;
    }

    /**
     * 根据密文，生成rsa公钥和私钥，并写入指定文件
     * @param publicKeyFileName  公钥文件路径
     * @param privateKeyFileName  私钥文件路径
     * @param secret  生成的密文
     * @param keySize
     * @throws Exception
     */
    public static void generateKey(String publicKeyFileName, String privateKeyFileName,
                                   String secret, int keySize
    ) throws Exception {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = new SecureRandom(secret.getBytes());
        keyPairGenerator.initialize(Math.max(keySize,DEFAULT_KEY_SIZE),secureRandom);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        //获取公钥并写出
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        publicKeyBytes = Base64.getEncoder().encode(publicKeyBytes);
        writeFile(publicKeyFileName, publicKeyBytes);

        //获取私钥并写出
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        privateKeyBytes = Base64.getEncoder().encode(privateKeyBytes);
        writeFile(privateKeyFileName, privateKeyBytes);
    }

    /**
     * 写出文件
     * @param destPath
     * @param bytes
     * @throws IOException
     */
    private static void writeFile(String destPath, byte[] bytes) throws IOException {
        File dest = new File(destPath);
        if (!dest.exists()) {
            dest.createNewFile();
        }
        Files.write(dest.toPath(), bytes);
    }
}
