package com.cosmetics.repository;

import com.cosmetics.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Integer> {
    Shop findByDefaultShopTrue();
}
