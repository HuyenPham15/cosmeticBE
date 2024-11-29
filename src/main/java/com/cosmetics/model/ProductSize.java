package com.cosmetics.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_size")
public class ProductSize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_sizeid")
    private int productSizeID;
    int quantity;
    double price;
    private String variant;
    private String weight;
    @Column(nullable = true)
    Double discountPrice;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false)
    private Product product;


}
