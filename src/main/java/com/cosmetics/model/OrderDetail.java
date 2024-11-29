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
@Table(name = "order_details")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer orderDetailsID;
    Integer quantity;
    double price;
    double totalMoney;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "orderID")
    Order order;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "productSizeID")
    ProductSize productSize;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "discountID")
    Discount discount;
}
