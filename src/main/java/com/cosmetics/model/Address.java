package com.cosmetics.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "address")
public class Address {
    @Id
    int addressID;
    String specific_address;
    boolean is_default;
    private String phoneNumber;
    private String first_name;
    private String last_name;
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "userID")
    Users users;
}
