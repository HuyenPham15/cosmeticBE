package com.cosmetics.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int reviewID;
    String rating;
    String comment;
    @Temporal(TemporalType.DATE)
    @Column(name = "create_at")
    Date create_at = new Date();
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "orderID")
    Order order;
}
