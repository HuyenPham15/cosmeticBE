package com.cosmetics.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subcategory")
public class Subcategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int subcategoryID;
    String subcategoryName;
    @ManyToOne
    @JoinColumn(name = "categoryID")
    Category category;
    @OneToMany(mappedBy = "subcategory", fetch = FetchType.EAGER)
    List<Product> products;
}
