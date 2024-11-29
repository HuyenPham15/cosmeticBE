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
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int cartID;
    int quantity;
    double price;
    double totalPrice;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "product_size_id")
    ProductSize productSize;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "userid")
    Users users;
}
