package com.cosmetics.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Date;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "discount_sp")
public class DiscountProduct {
    @Id
    @Column(name = "discountsp_id", length = 8, nullable = false, unique = true)
    private String discountsp_id; // Khóa chính gồm 8 chữ số ngẫu nhiên


    @Column(name = "discount_sp")
    private float discount_sp;

    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date start_date;

    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date end_date;

    @Column(name = "status")
    private Boolean status;

    // Sử dụng tên duy nhất cho JsonManagedReference
    @OneToMany(mappedBy = "discountProduct", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    private List<Product> products;
    @PrePersist
    public void prePersist() {
        if (this.discountsp_id == null || this.discountsp_id.isEmpty()) {
            this.discountsp_id = generateRandomId();
        }
    }

    public String generateRandomId() {
        return RandomStringUtils.randomNumeric(8);
    }
}
