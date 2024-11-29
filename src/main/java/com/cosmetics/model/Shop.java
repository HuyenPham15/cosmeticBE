package com.cosmetics.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shop")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int shopID;
    private String shopName;
    private String shopAddress;
    private String phone;
    private boolean defaultShop;
}
