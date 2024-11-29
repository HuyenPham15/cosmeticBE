package com.cosmetics.repository;

import com.cosmetics.model.DiscountProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountProductRepository extends JpaRepository<DiscountProduct, String> {
}
