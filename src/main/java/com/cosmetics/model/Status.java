package com.cosmetics.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "status")
@Entity
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int statusId;
    private String statusName;
    private String statusDescription;
    @OneToMany(mappedBy = "statuss")
    @JsonManagedReference
    List<Order> order;
}
