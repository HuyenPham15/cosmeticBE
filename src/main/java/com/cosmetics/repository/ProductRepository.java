package com.cosmetics.repository;

import com.cosmetics.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByProductId(String productId); // Phương thức tìm sản phẩm theo ID

    // Thêm phương thức truy vấn tùy chỉnh nếu cần
    // Tùy chọn: truy vấn với join fetch để nạp các mối quan hệ nếu cần thiết
    @Query("SELECT p FROM Product p " +
            "JOIN FETCH p.subcategory " +
            "JOIN FETCH p.brand " +
            "JOIN FETCH p.skinType " +
            "JOIN FETCH p.productSizes")
    List<Product> findAllWithDetails();

    boolean existsByProductId(String productID);

    @Query("SELECT p.productId FROM Product p ORDER BY p.productId DESC LIMIT 1")
    String findLastProductID();

    @Query(value = "SELECT TOP 5 p.product_id, p.product_name, SUM(od.quantity) AS total_sold " +
            "FROM product p " +
            "JOIN product_size ps ON p.product_id = ps.product_id " +
            "JOIN order_details od ON ps.product_size_id = od.product_size_id " +
            "GROUP BY p.product_id, p.product_name " +
            "ORDER BY total_sold DESC",
            nativeQuery = true)
    List<Object[]> findTop5BestSellingProducts();

    // Tìm kiếm tất cả sản phẩm dựa theo brandId
    @Query("SELECT p FROM Product p WHERE p.brand.brandID = :brandId")
    List<Product> findByBrandId(@Param("brandId") Integer brandId);


    @Query("SELECT p FROM Product p WHERE p.productName LIKE %:keyword% OR p.description LIKE %:keyword%")
    List<Product> searchProductsByKeyword(@Param("keyword") String keyword);

    //brand
    @Query(value = "SELECT TOP 5 b.brandid, b.brand_name, b.brand_images, COUNT(p.product_id) AS total_products " +
            "FROM product p " +
            "JOIN brand b ON p.brandid = b.brandid " +
            "GROUP BY b.brandid, b.brand_name, b.brand_images " +
            "ORDER BY total_products DESC",
            nativeQuery = true)
    List<Object[]> findTop5BrandsWithMostProducts();
    List<Product> findBySubcategory_Category_CategoryID(int categoryId);
    List<Product> findBySubcategory_SubcategoryID(int subcategoryID);
    List<Product> findBySubcategory_SubcategoryIDAndSubcategory_Category_CategoryID(int subcategoryID, int categoryID);

    Optional<Product> findByProductIdAndProductSizesProductSizeID(String productId, int productSizeId);

}