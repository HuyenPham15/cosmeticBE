package com.cosmetics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public  class GHNItem {
    private String name;
    private String code;
    private int quantity;
    private int price;
    private int length;
    private int width;
    private int height;
    private int weight;
    private GHNCategory category;
}
