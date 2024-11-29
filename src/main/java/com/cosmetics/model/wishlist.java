package com.cosmetics.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wishlist")
public class wishlist {
    @EmbeddedId
    private WishlistId id;

    @ManyToOne
    @MapsId("userID")
    @JoinColumn(name = "userID")
    private Users users;

    @ManyToOne
    @MapsId("productID")
    @JoinColumn(name = "productID")
    private Product product;
}
