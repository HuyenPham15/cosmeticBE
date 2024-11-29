package com.cosmetics.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {

    private final String uploadDir = "D://DATN//cosmeticstore//cosmeticstore-app//src//assets//image"; // Đường dẫn đến thư mục lưu trữ ảnh

    public String saveAvatar(MultipartFile file) throws IOException {
        // Tạo tên file duy nhất
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Lưu file vào thư mục
        Path filePath = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về đường dẫn URL của file
        return fileName;
    }
}
