package com.cosmetics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandDTO {
    private int brandId;      // ID của thương hiệu
    private String brandName; // Tên của thương hiệu
    private String brandImage; // URL của ảnh thương hiệu
}
