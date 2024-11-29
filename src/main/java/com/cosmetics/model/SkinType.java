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
@Entity
@Table(name = "skin_type")
public class SkinType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer skinID;
    String skinName;
    @JsonManagedReference
    @OneToMany(mappedBy = "skinType", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<Product> product;
}
