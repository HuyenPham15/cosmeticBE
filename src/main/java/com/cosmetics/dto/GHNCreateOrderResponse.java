package com.cosmetics.dto;

import lombok.Data;

@Data
public class GHNCreateOrderResponse {
    private String order_code;
    private String message;
    private int status;
    private Data data;
}