package com.cosmetics.restcontroller;


import com.cosmetics.dto.CartItemDTO;
import com.cosmetics.model.Cart;
import com.cosmetics.model.Product;
import com.cosmetics.model.ProductSize;
import com.cosmetics.repository.ProductRepository;
import com.cosmetics.repository.ProductSizeRepository;
import com.cosmetics.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;

    public CartController(CartService cartService, ProductRepository productRepository, ProductSizeRepository productSizeRepository) {
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.productSizeRepository = productSizeRepository;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItemDTO cartItem) {
        try {
            // Validate the cartItem (optional, but recommended)
            if (cartItem == null || cartItem.getProductId() == null || cartItem.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body("Invalid cart item.");
            }

            // Call the service to add the cart item
            CartItemDTO updatedCartItem = cartService.addToCart(cartItem);

            // Return the updated cart item
            return ResponseEntity.ok(updatedCartItem); // Return the updated cart item
        } catch (Exception e) {
            // Log the error
            logger.error("Error adding to cart", e); // Using logger to log the error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding to cart: " + e.getMessage());
        }
    }

    @GetMapping("/items")
    public ResponseEntity<List<CartItemDTO>> getCartItems(@RequestParam int userId) {
        try {
            List<CartItemDTO> cartItems = cartService.getCartItems(userId);

            // Kiểm tra và cập nhật giá giỏ hàng
            for (CartItemDTO item : cartItems) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                ProductSize productSize = productSizeRepository.findById(item.getProductSizeId())
                        .orElseThrow(() -> new RuntimeException("Product size not found"));

                double price;
                // Kiểm tra nếu sản phẩm có giá giảm
                if (productSize.getDiscountPrice() != null && productSize.getDiscountPrice() > 0) {
                    price = productSize.getDiscountPrice();  // Sử dụng giá giảm nếu có
                } else {
                    // Nếu không có giá giảm, sử dụng giá gốc
                    price = cartService.getDisplayPrice(product, item.getProductSizeId());
                }

                // Cập nhật lại giá trong CartItemDTO
                item.setPrice(price);
                item.setTotalPrice(price * item.getQuantity());  // Cập nhật lại tổng tiền của sản phẩm
            }

            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            logger.error("Error retrieving cart items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/unique-count/{userId}")
    public int getUniqueProductCount(@PathVariable int userId) {
        return cartService.getUniqueProductCount(userId);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<List<Cart>> removeItem(@PathVariable Integer id) {
        logger.info("Received request to remove item with ID: " + id);
        try {
            List<Cart> updatedCart = cartService.removeItem(id);
            return ResponseEntity.ok(updatedCart); // Trả về danh sách giỏ hàng đã cập nhật
        } catch (Exception e) {
            logger.error("Error removing item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") String productId) {
        Optional<Product> product = cartService.getProductById(productId);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}