package com.cosmetics.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class AESUtil {
    public static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        System.out.println("Encrypted Data (Base64): " + Base64.getEncoder().encodeToString(encryptedData));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        System.out.println("Decoded Data Length: " + decodedData.length);  // Kiểm tra độ dài dữ liệu
        byte[] decryptedData = cipher.doFinal(decodedData);
        System.out.println("Decrypted Data: " + new String(decryptedData));
        return new String(decryptedData);
    }

    // Tạo khóa AES
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // Độ dài khóa có thể là 128, 192 hoặc 256 bit
        return keyGen.generateKey();
    }
    // Tạo SecretKey từ chuỗi base64 (dùng cho trường hợp lưu khóa AES vào DB)
    public static SecretKey getKeyFromBase64String(String keyString) {
        if (keyString == null || keyString.isEmpty()) {
            throw new IllegalArgumentException("Khóa AES không được để trống.");
        }

        try {
            byte[] decodedKey = Base64.getDecoder().decode(keyString);  // Giải mã chuỗi Base64
            return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");  // Tạo SecretKey từ byte array
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Chuỗi Base64 không hợp lệ: " + e.getMessage());
        }
    }

    // Lấy chuỗi base64 của SecretKey để lưu vào database (nếu cần)
    public static String getBase64StringFromKey(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
