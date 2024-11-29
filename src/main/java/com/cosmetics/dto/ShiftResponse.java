package com.cosmetics.dto;

import lombok.Data;

import java.util.List;

@Data
public class ShiftResponse {
    private int code;
    private List<Shift> data;
}
