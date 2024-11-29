package com.cosmetics.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "brand")
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brandid") // Tên cột trong cơ sở dữ liệu
    int brandID; // Đổi tên biến từ brandID thành brandId
    String brandName;
    @Column(name = "brand_images")
    private String brandImages; // Đảm bảo tên cột phải khớp với cơ sở dữ liệu

    @OneToMany(mappedBy = "brand")
    List<Product> products; // Đổi tên lớp từ product thành Product

}
