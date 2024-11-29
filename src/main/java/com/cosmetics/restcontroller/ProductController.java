package com.cosmetics.restcontroller;


import com.cosmetics.dto.BrandDTO;
import com.cosmetics.dto.ProductDTO;
import com.cosmetics.model.Product;
import com.cosmetics.repository.ProductRepository;
import com.cosmetics.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/getAll")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> productDTOs = productService.getAllProducts();
        if (productDTOs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(productDTOs);
    }


    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable(required = false) String productId) {
        if (productId == null || productId.isEmpty()) {
            return ResponseEntity.badRequest().body(null); // Hoặc xử lý theo cách bạn muốn
        }

        ProductDTO product = productService.getProductByProductId(productId);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<ProductDTO> getProduct(@PathVariable String id) {
//        Optional<Product> productOpt = productRepository.findById(id);
//        if (productOpt.isPresent()) {
//            Product product = productOpt.get();
//            ProductDTO productDTO = new ProductDTO();
//
//            // Tính toán giá hiển thị
//            double displayPrice = productService.getDisplayPrice(product, pr);
//
//            // Thiết lập các thông tin khác của sản phẩm vào DTO
//            productDTO.setProductName(product.getProductName());
//            productDTO.setDescription(product.getDescription());
//            productDTO.setPrice(displayPrice);
//
//            return ResponseEntity.ok(productDTO);
//        }
//        return ResponseEntity.notFound().build();
//    }


}