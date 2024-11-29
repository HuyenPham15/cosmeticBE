package com.cosmetics.repository;

import com.cosmetics.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Cart findByProductSize_productSizeIDAndProductSize_product_productId(Integer productSizeId, String productId);

    List<Cart> findByUsers_UserID(int userId); // 'UserID' là tên của thuộc tính trong lớp Users

    Cart findByProductSize_productSizeIDAndProductSize_product_productIdAndUsers_UserID(
            int productSize_productSizeID, String productSize_product_productId, int users_userID);
}