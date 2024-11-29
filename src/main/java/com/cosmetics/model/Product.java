package com.cosmetics.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product {

    @Id
    @Column(name = "product_id")
    private String productId; // ID sản phẩm
    private String productName; // Tên sản phẩm
    private String images;
    private String description; // Mô tả sản phẩm
    private String specifications; // Thông số sản phẩm
    private String ingredients; // Thành phần
    private String benefits; // Công dụng
    private String usage; // Cách dùng
    private String review; // Đánh giá
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "subcategoryID")
    Subcategory subcategory;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "skinID")
    SkinType skinType;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "brandid", referencedColumnName = "brandid") // Kiểm tra tên cột
    Brand brand;
    @JsonManagedReference
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSize> productSizes = new ArrayList<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER) // Thêm cascade nếu cần
    List<wishlist> wishlists;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "discountsp_id")
    DiscountProduct discountProduct;
}