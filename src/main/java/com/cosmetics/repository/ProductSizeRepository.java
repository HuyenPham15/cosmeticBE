package com.cosmetics.repository;

import com.cosmetics.model.Product;
import com.cosmetics.model.ProductSize;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, Integer> {
    List<ProductSize> findByProduct_ProductId(String productId);

    @Query("SELECT SUM(ps.quantity) FROM ProductSize ps")
    Long countTotalProductsSold();

    //hang ton
    @Query(value = "SELECT SUM(stock_summary.remaining_stock) AS total_remaining_stock " +
            "FROM (SELECT ps.product_size_id, ps.quantity AS stock_quantity, " +
            "COALESCE(SUM(od.quantity), 0) AS ordered_quantity, " +
            "(ps.quantity - COALESCE(SUM(od.quantity), 0)) AS remaining_stock " +
            "FROM ProductSize ps " +
            "LEFT JOIN order_details od ON ps.product_size_id = od.product_size_id " +
            "GROUP BY ps.product_size_id, ps.quantity) AS stock_summary",
            nativeQuery = true)
    Long calculateTotalRemainingStock();
    List<ProductSize> findByProduct(Product product);


    List<ProductSize> findByProductProductId(String productID);

    Optional<ProductSize> findByProductSizeID(int productSizeID);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductSize ps WHERE ps.product.productId = :productID")
    void deleteByProductId(@Param("productID") String productID);
}