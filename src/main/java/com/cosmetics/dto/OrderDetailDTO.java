package com.cosmetics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO {
    private Integer orderDetailsID;
    private Integer quantity;
    private double price;
    private double totalMoney;
    private String orderID;
    private Integer productSizeID;
    private List<ProductSizeDTO> sizes;
    private List<TransactionDTO> transactions;

}