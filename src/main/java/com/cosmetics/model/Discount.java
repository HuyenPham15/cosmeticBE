package com.cosmetics.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table (name = "discount")
public class Discount {
    @Id
    String discountID;
    String discountCode;
    String description;
    String discountType;
    Double amount;
    @Temporal(TemporalType.DATE)
    @Column(name = "startDate")
    Date startDate = new Date();
    @Temporal(TemporalType.DATE)
    @Column(name = "endDate")
    Date endDate = new Date();
    boolean isActive;
    int maxUsage;
    int usageCount;
    @OneToMany(mappedBy = "discount", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Order> order = new ArrayList<>();
}
