package com.cosmetics.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class CartItemDTO {
    private int cartId;              // ID of the cart item (Auto-generated in database, don't use @Id here)
    private String productId;        // Product identifier
    private int quantity;            // Quantity of the product in the cart
    private double price;            // Price of the product (could be discounted or regular price)
    private double totalPrice;       // Total price for the quantity
    private String productName;      // Name of the product
    private String productImage;     // Image URL or path of the product
    private int productSizeId;       // Size ID for the product (linked to product sizes)
    private String variant;          // Variant of the product (like size or color)
    private Integer userId;          // User ID (nullable in case no user is provided)
    private String weight;
}