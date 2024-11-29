package com.cosmetics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private String orderID;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderDate;
    int paymentMethod;
    String shippingAddress;
    Float shippingFee;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createAt;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateAt;
    private int userPoint;
    private double totalPrice;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expiraDate;
    private Integer userId;
    private String userFirstName; // First name của người dùng
    private String userLastName;  // Last name của người dùng
    private Integer payment_type_id;
    private String note;
    private String weight;
    private String required_note;
    private List<OrderDetailDTO> orderDetails = new ArrayList<>();
    private List<UserPointDTO> userPoints = new ArrayList<>();
    private List<TransactionDTO> Transaction;
    private String discountId;
    private int statusId;
}
