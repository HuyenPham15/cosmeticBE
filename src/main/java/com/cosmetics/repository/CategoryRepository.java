package com.cosmetics.repository;

import com.cosmetics.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Category getCategoryByCategoryID(int categoryID);
}
