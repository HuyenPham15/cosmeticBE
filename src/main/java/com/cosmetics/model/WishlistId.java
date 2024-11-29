package com.cosmetics.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistId implements Serializable {

    @Column(name = "userID")
    private Integer userID;

    @Column(name = "productID")
    private String productID;

}