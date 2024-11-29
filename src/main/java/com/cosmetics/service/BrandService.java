package com.cosmetics.service;

import com.cosmetics.dto.BrandDTO;
import com.cosmetics.model.Brand;
import com.cosmetics.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    // Phương thức lấy thương hiệu theo ID
    public BrandDTO getBrandById(int brandId) {
        Optional<Brand> brandOpt = brandRepository.findById(brandId);
        if (brandOpt.isPresent()) {
            Brand brand = brandOpt.get();
            return convertToDTO(brand);  // Chuyển đổi thực thể Brand thành BrandDTO
        } else {
            throw new RuntimeException("Không tìm thấy thương hiệu với ID: " + brandId);
        }
    }

    private BrandDTO convertToDTO(Brand brand) {
        BrandDTO dto = new BrandDTO();
        dto.setBrandId(brand.getBrandID());
        dto.setBrandName(brand.getBrandName());
        dto.setBrandImage(brand.getBrandImages());
        return dto;
    }
}