package com.cosmetics.repository;

import com.cosmetics.model.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, Integer> {
    Subcategory findBySubcategoryName(String subcategoryName);
    List<Subcategory> findByCategory_CategoryID(int categoryID);
    Subcategory getSubcategoryBySubcategoryID(int subcategoryID);
}
