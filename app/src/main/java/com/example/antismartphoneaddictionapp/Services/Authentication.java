package com.example.antismartphoneaddictionapp.Services;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Authentication {

    private static byte[] SecretKey = {0x0f};
    static String key = "1234567890123456";

    //Encrypt
    private static byte[] encrypt(String password, String key) throws Exception {
        byte[] textBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }

    //Decrypt
    private static byte[] decrypt(String password, String key) throws Exception {
        byte[] textBytes = Base64.decode(password, Base64.DEFAULT);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }

    public static String EncryptMessage(String message) {
        try {
            if (message != null) {
                if (!message.trim().equals("")) {
                    byte[] txt0 = encrypt(message, key);
                    return Base64.encodeToString(txt0, Base64.DEFAULT);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    public static String DecryptMessage(String message) {

        try {
            if (message != null) {
                if (!message.trim().equals("")) {
                    byte[] txt1 = decrypt(message, key);
                    return new String(txt1, StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

}
