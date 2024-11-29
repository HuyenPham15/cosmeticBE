package com.cosmetics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private String productId;
    private String description;
    private List<ProductSizeDTO> sizes;
    private String categoryName;
    private String productName; // Tên sản phẩm
    private List<String> images; // Hình ảnh sản phẩm
    private String specifications; // Mô tả sản phẩm
    private String ingredients; // Mô tả sản phẩm
    private String benefits; // Mô tả sản phẩm
    private String usage; // Mô tả sản phẩm
    private String review; // Mô tả sản phẩm
    private Integer skinID;
    private String skinName; // Tên loại da
    private Integer brandID;
    private String brandName; // Tên thương hiệu
    private Integer subcategoryID;
    private String subcategoryName; // Tên danh mục con
    private Double price; // Giá tiền
    private Double discountPrice;
    private String discountsp_id;
    private String discount_sp;
    private Integer productSizeID;
    private Integer quantity; // Số lượng
    private String variant; // ID của variant
}
