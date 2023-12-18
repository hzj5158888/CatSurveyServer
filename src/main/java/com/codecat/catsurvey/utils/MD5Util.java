package com.codecat.catsurvey.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    private final static String  salt = "MyJavaWebHashSalt"; //盐值

    public static String getMD5(String password) {
        try {
            // 创建 MessageDigest 实例并指定算法为 MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 将密码和盐值连接后进行摘要计算
            md.update((password + salt).getBytes());

            // 获取摘要结果
            byte[] bytes = md.digest();
            StringBuilder hashedPassword = new StringBuilder();
            for (byte b : bytes) {
                hashedPassword.append(String.format("%02x", b));
            }
            return hashedPassword.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}
