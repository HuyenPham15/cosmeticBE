package com.cosmetics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // Tạo constructor với tất cả các tham số
@NoArgsConstructor  // Tạo constructor không tham số
public class VerificationRequestDTO {
    private String email; // Thêm thuộc tính email
    private String code;
    private long expirationTime;
}
