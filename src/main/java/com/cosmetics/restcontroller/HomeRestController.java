package com.cosmetics.restcontroller;

import com.cosmetics.dto.BrandDTO;
import com.cosmetics.dto.ProductDTO;
import com.cosmetics.dto.ProductSizeDTO;
import com.cosmetics.model.Category;
import com.cosmetics.model.Product;
import com.cosmetics.model.ProductSize;
import com.cosmetics.repository.CategoryRepository;
import com.cosmetics.repository.ProductRepository;
import com.cosmetics.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/home/product")
public class HomeRestController {
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public HomeRestController(ProductRepository productRepository, ProductService productService, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> productDTOs = productService.getAllProducts();
        if (productDTOs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(productDTOs);
    }
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO> getProductByProductId(@PathVariable String productId) {
        ProductDTO productDTO = productService.getProductByProductId(productId);

        if (productDTO != null) {
            return ResponseEntity.ok(productDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @GetMapping("/brand")
    public ResponseEntity<List<BrandDTO>> getTop5BrandsWithMostProducts() {
        List<Object[]> results = productRepository.findTop5BrandsWithMostProducts();
        List<BrandDTO> brandDTOs = new ArrayList<>();

        // Ánh xạ kết quả từ Object[] sang BrandDTO
        for (Object[] result : results) {
            BrandDTO brandDTO = new BrandDTO();
            brandDTO.setBrandId((Integer) result[0]);
            brandDTO.setBrandName((String) result[1]);
            brandDTO.setBrandImage((String) result[2]); // Ảnh thương hiệu
            brandDTOs.add(brandDTO);
        }
        return ResponseEntity.ok(brandDTOs);
    }
    @GetMapping("/category")
    public ResponseEntity<List<Category>> getAllCategory(Model model) {
        return ResponseEntity.ok(categoryRepository.findAll());
    }
    @GetMapping("/category/{categoryID}")
    public List<Product> getProductsByCategory(@PathVariable int categoryID) {
        return productService.getProductsByCategory(categoryID);
    }


}
