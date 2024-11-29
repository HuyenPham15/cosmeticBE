package com.cosmetics.model;

import com.cosmetics.dto.OrderDetailDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    String orderID;
    @Temporal(TemporalType.DATE)
    @Column(name = "orderDate")
    Date orderDate = new Date();
    int paymentMethod;
    String shippingAddress;
    Float shippingFee;
    @Temporal(TemporalType.DATE)
    @Column(name = "create_at")
    Date create_at = new Date();
    @Temporal(TemporalType.DATE)
    @Column(name = "update_at")
    Date update_at = new Date();
    int userPoint;
    double totalPrice;

    @Temporal(TemporalType.DATE)
    @Column(name = "expiraDate")
    Date expireDate;
    String note;
    String required_note;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "userID")
    Users users;
    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER) // Thêm cascade nếu cần
    List<OrderDetail> orderDetails; // Đổi tên lớp thành OrderDetails
    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<UserPoint> userPoints = new ArrayList<>();
    @JsonManagedReference
    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
    List<Transactions> transactions;
    @ManyToOne
    @JoinColumn(name = "discountID") // Phải khớp với tên cột trong cơ sở dữ liệu
    @JsonBackReference
    private Discount discount;
    @ManyToOne
    @JoinColumn(name = "statusId")
    @JsonBackReference
    private Status statuss;
    @Override
    public int hashCode() {
        return Objects.hash(orderID, totalPrice, shippingAddress, statuss);
    }
    @Override
    public String toString() {
        return "Order{" +
                "orderID=" + orderID +
                ", user=" + users +  // This can cause recursion if User's toString() calls Order's toString()
                '}';
    }
}
