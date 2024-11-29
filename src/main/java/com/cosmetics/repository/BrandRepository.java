package com.cosmetics.repository;

import com.cosmetics.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {
    Brand findByBrandName(String brandName);
}
