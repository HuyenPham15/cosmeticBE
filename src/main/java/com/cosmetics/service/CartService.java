package com.cosmetics.service;


import com.cosmetics.dto.CartItemDTO;
import com.cosmetics.dto.ProductSizeDTO;
import com.cosmetics.model.Cart;
import com.cosmetics.model.Product;
import com.cosmetics.model.ProductSize;
import com.cosmetics.model.Users;
import com.cosmetics.repository.CartRepository;
import com.cosmetics.repository.ProductRepository;
import com.cosmetics.repository.ProductSizeRepository;
import com.cosmetics.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductSizeRepository productSizeRepository;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }
    public double getDisplayPrice(Product product, int productSizeID) {
        Date today = new Date();

        // Kiểm tra xem có mã giảm giá không và mã giảm giá còn hiệu lực không
        if (product.getDiscountProduct() != null
                && product.getDiscountProduct().getDiscount_sp() > 0
                && product.getDiscountProduct().getStart_date() != null
                && product.getDiscountProduct().getEnd_date() != null
                && today.after(product.getDiscountProduct().getStart_date())
                && today.before(product.getDiscountProduct().getEnd_date())) {

            // Tính giá giảm nếu mã giảm giá còn hiệu lực
            double discountedPrice = product.getProductSizes().stream()
                    .mapToDouble(size -> size.getPrice() * (1 - product.getDiscountProduct().getDiscount_sp() / 100.0))
                    .average()
                    .orElse(0);

            return Math.round(discountedPrice * 10.0) / 10.0;
        }

        // Nếu không có giá giảm, trả về giá gốc của productSize
        ProductSize productSize = product.getProductSizes().stream()
                .filter(size -> size.getProductSizeID() == productSizeID) // So sánh productSizeId với int
                .findFirst()
                .orElse(null);

        if (productSize != null) {
            return Math.round(productSize.getPrice() * 10.0) / 10.0;
        }
        return 0;
    }

    public CartItemDTO addToCart(CartItemDTO cartItemDTO) {
        Product product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductSize productSize = productSizeRepository.findById(cartItemDTO.getProductSizeId())
                .orElseThrow(() -> new RuntimeException("Product size not found"));

        // Xác định giá
        double price;
        if (productSize.getDiscountPrice() != null && productSize.getDiscountPrice() > 0) {
            price = productSize.getDiscountPrice();
        } else {
            price = getDisplayPrice(product, cartItemDTO.getProductSizeId());
        }

        // Kiểm tra số lượng yêu cầu có hợp lệ không
        if (cartItemDTO.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        // Kiểm tra số lượng sản phẩm còn lại trong kho
        int availableStock = productSize.getQuantity();  // Giả sử bạn có thuộc tính availableStock trong ProductSize
        if (cartItemDTO.getQuantity() > availableStock) {
            throw new RuntimeException("Not enough stock available. Only " + availableStock + " left.");
        }

        Integer userId = cartItemDTO.getUserId();
        if (userId == null || userId <= 0) {
            throw new RuntimeException("User ID is null");
        }

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Cart existingCartItem = cartRepository.findByProductSize_productSizeIDAndProductSize_product_productId(
                cartItemDTO.getProductSizeId(), cartItemDTO.getProductId()
        );

        double totalPrice;
        Cart savedCartItem;

        if (existingCartItem != null) {
            // Kiểm tra nếu giỏ hàng đã có sản phẩm này, cộng thêm số lượng
            int newQuantity = existingCartItem.getQuantity() + cartItemDTO.getQuantity();
            if (newQuantity > availableStock) {
                throw new RuntimeException("Not enough stock available. Only " + availableStock + " left.");
            }

            existingCartItem.setQuantity(newQuantity);
            totalPrice = existingCartItem.getQuantity() * price;  // Tính lại giá trị tổng
            existingCartItem.setTotalPrice(totalPrice);
            existingCartItem.setUsers(user); // Đảm bảo cập nhật userId
            savedCartItem = cartRepository.save(existingCartItem); // Lưu giỏ hàng đã cập nhật
        } else {
            // Nếu sản phẩm chưa có, tạo mới
            if (cartItemDTO.getQuantity() > availableStock) {
                throw new RuntimeException("Not enough stock available. Only " + availableStock + " left.");
            }

            totalPrice = price * cartItemDTO.getQuantity();
            Cart newCartItem = new Cart();

            newCartItem.setQuantity(cartItemDTO.getQuantity());
            newCartItem.setPrice(price);
            newCartItem.setTotalPrice(totalPrice);
            newCartItem.setProductSize(productSize);
            newCartItem.setUsers(user); // Đảm bảo userId được set
            savedCartItem = cartRepository.save(newCartItem); // Cơ sở dữ liệu tự động tạo cartId
        }

        // Chuyển đổi dữ liệu từ Cart thành CartItemDTO để trả về
        CartItemDTO responseDTO = new CartItemDTO();
        responseDTO.setCartId(savedCartItem.getCartID());
        responseDTO.setProductId(product.getProductId());
        responseDTO.setProductSizeId(productSize.getProductSizeID());
        responseDTO.setQuantity(cartItemDTO.getQuantity());
        responseDTO.setTotalPrice(totalPrice);
        responseDTO.setPrice(price);
        responseDTO.setVariant(productSize.getVariant());
        responseDTO.setWeight(productSize.getWeight());
        responseDTO.setProductName(product.getProductName());
        String[] images = product.getImages().split(",");
        String firstImage = images.length > 0 ? images[0].trim() : "default_image.jpg";
        responseDTO.setProductImage(firstImage);

        responseDTO.setUserId(cartItemDTO.getUserId());
        return responseDTO;
    }


    public List<CartItemDTO> getCartItems(int userId) {
        List<Cart> cartItems = cartRepository.findByUsers_UserID(userId);
        return cartItems.stream().map(cart -> {
            CartItemDTO dto = new CartItemDTO();
            dto.setCartId(cart.getCartID());
            dto.setProductId(cart.getProductSize().getProduct().getProductId());
            dto.setProductSizeId(cart.getProductSize().getProductSizeID());
            dto.setQuantity(cart.getQuantity());
            dto.setPrice(cart.getPrice());  // Giá ở đây có thể là giá gốc hoặc giá giảm nếu đã cập nhật ở controller
            dto.setTotalPrice(cart.getTotalPrice());
            dto.setProductName(cart.getProductSize().getProduct().getProductName());
            String[] images = cart.getProductSize().getProduct().getImages().split(",");
            String firstImage = images.length > 0 ? images[0].trim() : "";
            dto.setProductImage(firstImage);
            dto.setUserId(cart.getUsers().getUserID());
            dto.setVariant(cart.getProductSize().getVariant());
            dto.setWeight(cart.getProductSize().getWeight());
            return dto;
        }).collect(Collectors.toList());
    }



    public int getUniqueProductCount(int userId) {
        // Lấy tất cả các mục giỏ hàng của người dùng theo userID
        List<Cart> items = cartRepository.findByUsers_UserID(userId);

        Set<String> uniqueProductIds = new HashSet<>();
        for (Cart item : items) {
            uniqueProductIds.add(item.getProductSize().getProduct().getProductId());
        }
        return uniqueProductIds.size();
    }

    public List<Cart> removeItem(Integer id) {
        Optional<Cart> item = cartRepository.findById(id);
        if (item.isPresent()) {
            cartRepository.deleteById(id);
        }
        // Trả về danh sách giỏ hàng sau khi xóa
        return cartRepository.findAll();
    }

    public Optional<Product> getProductById(String productId) {
        return productRepository.findById(productId);
    }
}