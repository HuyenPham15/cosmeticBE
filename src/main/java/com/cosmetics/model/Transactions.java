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
@Table(name = "transactions")
public class Transactions {
    @Id
    String transactionID;
    double amount;
    @Temporal(TemporalType.DATE)
    @Column(name = "transaction_date")
    Date transaction_date = new Date();
    String status;
    @Temporal(TemporalType.DATE)
    @Column(name = "created_at")
    Date created_at = new Date();
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "orderID")
    Order order;
    @Override
    public int hashCode() {
        return Objects.hash(transactionID, status, amount, order);  // Chỉ sử dụng các thuộc tính quan trọng
    }

}
