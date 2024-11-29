package com.cosmetics.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_point")
public class UserPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int userPointID;
    int point;
    @Temporal(TemporalType.DATE)
    @Column(name = "date")
    Date date = new Date();
    String status;
    // Quan hệ với Users
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference("user-point-users")
    @JoinColumn(name = "userID")
    private Users users;

    // Quan hệ với Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "orderID")
    private Order order;

}
