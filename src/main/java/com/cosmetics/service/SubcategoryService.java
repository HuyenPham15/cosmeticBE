package com.cosmetics.service;

import com.cosmetics.model.Subcategory;
import com.cosmetics.repository.SubcategoryRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubcategoryService {
    @Autowired
    SubcategoryRepository subcategoryRepository;
    public List<Subcategory> getSubcategoriesByCategoryId(int categoryID) {
        return subcategoryRepository.findByCategory_CategoryID(categoryID);
    }
    public Subcategory getSubcategoryByID(int subcategoryID) {
        return subcategoryRepository.getSubcategoryBySubcategoryID(subcategoryID);
    }



}
