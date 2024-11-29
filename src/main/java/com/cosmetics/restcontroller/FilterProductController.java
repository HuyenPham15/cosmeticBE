package com.cosmetics.restcontroller;


import com.cosmetics.dto.ProductDTO;
import com.cosmetics.model.Category;
import com.cosmetics.model.Product;
import com.cosmetics.model.ProductSize;
import com.cosmetics.model.Subcategory;
import com.cosmetics.repository.ProductRepository;
import com.cosmetics.service.ProductService;
import com.cosmetics.service.SubcategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
@CrossOrigin(origins = "*")
public class FilterProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productDAO;
    @Autowired
    private SubcategoryService subcategoryService;

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam("keyword") String keyword) {
        List<Product> products = productService.searchProducts(keyword);

        List<ProductDTO> productDTOs = products.stream().map(product -> {
            ProductDTO dto = new ProductDTO();
            dto.setProductId(product.getProductId());
            dto.setProductName(product.getProductName());
            dto.setDescription(product.getDescription());
            dto.setIngredients(product.getIngredients());
            dto.setSpecifications(product.getSpecifications());
            dto.setBenefits(product.getBenefits());
            dto.setUsage(product.getUsage());
            dto.setReview(product.getReview());

            String imagesJson = product.getImages(); // Nhận dữ liệu hình ảnh
            System.out.println("Dữ liệu hình ảnh nhận được: " + imagesJson);
            if (imagesJson != null && !imagesJson.trim().isEmpty()) {
                // Chia chuỗi hình ảnh thành mảng
                String[] imageArray = imagesJson.split(","); // Phân tách chuỗi theo dấu phẩy
                // Kiểm tra xem mảng hình ảnh có chứa hình ảnh hợp lệ không
                if (imageArray.length > 0) {
                    // Xử lý mảng hình ảnh hợp lệ
                    dto.setImages(Arrays.asList(imageArray));
                } else {
                    dto.setImages(Collections.singletonList("default-image.png")); // Gán hình ảnh mặc định nếu không có hình ảnh
                }
            } else {
                dto.setImages(Collections.singletonList("default-image.png")); // Gán hình ảnh mặc định nếu không có dữ liệu
            }

            ProductSize size=new ProductSize();
            dto.setPrice(size.getPrice());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(productDTOs);
    }


    @GetMapping("/category/{categoryID}")
    public List<Product> getProductsByCategory(@PathVariable int categoryID) {
        return productService.getProductsByCategory(categoryID);
    }


    @GetMapping("/subcategory/products/{subcategoryID}")
    public List<Product> getProductsBySubcategoryID(@PathVariable int subcategoryID) {
        return productService.getProductsBySubcategoryID(subcategoryID);
    }
    @GetMapping("/category/{categoryId}/subcategory")
    public List<Subcategory> getSubcategoriesByCategory(@PathVariable int categoryId) {
        return subcategoryService.getSubcategoriesByCategoryId(categoryId);
    }
    @GetMapping("/{categoryID}/subcategory/{subcategoryID}/products")
    public List<Product> getProductsBySubcategory(@PathVariable int categoryID, @PathVariable int subcategoryID) {
        return productService.getProductsBySubcategoryCategory(categoryID, subcategoryID);
    }
    @GetMapping("/{categoryID}")
    public Category getCategoryById(@PathVariable int categoryID) {
        return productService.getCategoryByID(categoryID);
    }

    @GetMapping("/subcategory/{subcategoryID}")
    public  Subcategory getSubcategoryById(@PathVariable int subcategoryID) {
        return subcategoryService.getSubcategoryByID(subcategoryID);
    }
}