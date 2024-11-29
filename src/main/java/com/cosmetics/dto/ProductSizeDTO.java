package com.cosmetics.dto;

import com.cosmetics.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSizeDTO {
    private int productSizeID;
    private String productId;
    private String variant;
    private double price;
    private Double discountPrice;
    private int quantity;

}