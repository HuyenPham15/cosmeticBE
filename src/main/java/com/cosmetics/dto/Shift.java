package com.cosmetics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Shift {
    private int id;
    private String title;
    private long fromTime;  // Thời gian bắt đầu ca lấy hàng (Unix timestamp)
    private long toTime;
}
