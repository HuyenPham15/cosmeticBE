package com.cosmetics.service;

import com.cosmetics.dto.ProductSizeDTO;
import com.cosmetics.model.ProductSize;
import com.cosmetics.repository.ProductSizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductSizeService {

    @Autowired
    private ProductSizeRepository productSizeRepository;

//
//    public List<ProductSizeDTO> getProductSizesByProductId(String productID) {
//        List<ProductSize> productSizes = productSizeRepository.findByProductProductId(productID);
//        return productSizes.stream()
//                .map(size -> new ProductSizeDTO(
//                        size.getProductSizeID(),
//                        size.getProduct().getProductId(), // Gán productId từ Product
//                        size.getVariant(),
//                        size.getPrice(),
//                        size.getQuantity(),
//                        size.getDiscountPrice()
//                ))
//                .collect(Collectors.toList());
//    }


    // Phương thức chuyển đổi từ entity sang DTO
    // Phương thức chuyển đổi từ entity sang DTO
    private ProductSizeDTO convertToDTO(ProductSize size) {
        ProductSizeDTO dto = new ProductSizeDTO();
        dto.setProductSizeID(size.getProductSizeID());
        dto.setQuantity(size.getQuantity());
        dto.setPrice(size.getPrice());



        return dto;
    }


}