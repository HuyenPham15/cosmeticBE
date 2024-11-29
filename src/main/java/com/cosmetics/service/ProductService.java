package com.cosmetics.service;


import com.cosmetics.dto.ProductDTO;
import com.cosmetics.dto.ProductSizeDTO;
import com.cosmetics.model.Category;
import com.cosmetics.model.Product;
import com.cosmetics.model.ProductSize;
import com.cosmetics.repository.CategoryRepository;
import com.cosmetics.repository.ProductRepository;
import com.cosmetics.repository.ProductSizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository; // Thêm ProductRepository
    @Autowired
    private ProductSizeRepository productSizeRepository;
    @Autowired
    private CategoryRepository categoryRepository;

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
        ProductSize productSize = product.getProductSizes().stream()
                .filter(size -> size.getProductSizeID() == productSizeID) // So sánh productSizeId với int
                .findFirst()
                .orElse(null);

        if (productSize != null) {
            return Math.round(productSize.getPrice() * 10.0) / 10.0;
        }
        return 0;
    }

    private double getOriginalPriceFromSizes(String productId) {
        List<ProductSize> productSizes = productSizeRepository.findByProduct_ProductId(productId);
        return productSizes.stream()
                .mapToDouble(ProductSize::getPrice)
                .min()
                .orElse(0.0);
    }

    private List<ProductSizeDTO> mapProductSizeToDTO(List<ProductSize> productSizes) {
        return productSizes.stream()
                .map(size -> {
                    ProductSizeDTO sizeDTO = new ProductSizeDTO();
                    sizeDTO.setProductSizeID(size.getProductSizeID());
                    sizeDTO.setProductId(size.getProduct().getProductId()); // Lấy productId từ Product (sử dụng getProduct() của ProductSize)
                    sizeDTO.setVariant(size.getVariant());
                    sizeDTO.setPrice(size.getPrice());
                    sizeDTO.setDiscountPrice(getDisplayPrice(size.getProduct(), size.getProductSizeID())); // Sử dụng getDisplayPrice với Product của ProductSize
                    return sizeDTO;
                })
                .collect(Collectors.toList());
    }

    private List<String> mapImages(String images) {
        if (images != null && !images.isEmpty()) {
            List<String> imageList = Arrays.asList(images.split(","));
            return imageList.isEmpty() ? Collections.singletonList("default-image.png") : imageList;
        }
        return Collections.singletonList("default-image.png");
    }

    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();

        return products.stream().map(product -> {
            ProductDTO productDTO = new ProductDTO();

            // Lấy giá sản phẩm sau giảm (nếu có giảm giá)
            ProductSize firstProductSize = product.getProductSizes().stream().findFirst().orElse(null);  // Lấy kích thước đầu tiên
            double displayPrice = 0.0;

            if (firstProductSize != null) {
                displayPrice = getDisplayPrice(product, firstProductSize.getProductSizeID());  // Sử dụng productSizeId
            }

            double originalPrice = getOriginalPriceFromSizes(product.getProductId());  // Hàm lấy giá gốc từ các size

            // Lấy thông tin các thuộc tính của sản phẩm
            productDTO.setProductId(product.getProductId());
            productDTO.setProductName(product.getProductName());
            productDTO.setDescription(product.getDescription());
            productDTO.setPrice(originalPrice);

            // Các thông tin khác của sản phẩm
            productDTO.setBenefits(product.getBenefits());
            productDTO.setSpecifications(product.getSpecifications());
            productDTO.setIngredients(product.getIngredients());
            productDTO.setUsage(product.getUsage());
            productDTO.setReview(product.getReview());
            productDTO.setSkinName(product.getSkinType() != null ? product.getSkinType().getSkinName() : "N/A");
            productDTO.setBrandName(product.getBrand() != null ? product.getBrand().getBrandName() : "N/A");
            productDTO.setSubcategoryName(
                    product.getSubcategory() != null ? product.getSubcategory().getSubcategoryName() : "N/A"
            );
            productDTO.setImages(mapImages(product.getImages()));

            // Lấy danh sách kích thước của sản phẩm
            List<ProductSize> productSizes = productSizeRepository.findByProduct_ProductId(product.getProductId());
            productDTO.setSizes(mapProductSizeToDTO(productSizes));

            // Nếu sản phẩm có mã giảm giá, kiểm tra và cập nhật lại giá hiển thị (nếu mã giảm giá hết hạn)
            if (product.getDiscountProduct() != null) {
                Date today = new Date();
                if (today.after(product.getDiscountProduct().getEnd_date())) {
                    // Mã giảm giá đã hết hạn, ẩn hoặc thay đổi giá hiển thị
                    productDTO.setDiscountPrice(0.0);  // Hoặc thay giá giảm bằng giá gốc
                } else {
                    productDTO.setDiscountPrice(displayPrice);  // Set giá giảm
                }
            }

            return productDTO;
        }).collect(Collectors.toList());
    }


    public ProductDTO getProductByProductId(String productId) {
        return productRepository.findByProductId(productId).map(product -> {
            ProductDTO productDTO = new ProductDTO();

            ProductSize firstProductSize = product.getProductSizes().stream().findFirst().orElse(null);  // Lấy kích thước đầu tiên
            double displayPrice = 0.0;

            if (firstProductSize != null) {
                displayPrice = getDisplayPrice(product, firstProductSize.getProductSizeID());  // Sử dụng productSizeId
            }

            double originalPrice = getOriginalPriceFromSizes(product.getProductId());  // Hàm lấy giá gốc từ các size

            // Thiết lập các thông tin cơ bản của sản phẩm
            productDTO.setProductId(product.getProductId());
            productDTO.setProductName(product.getProductName());
            productDTO.setDescription(product.getDescription());
            productDTO.setPrice(originalPrice);  // Giá gốc

            // Các thông tin khác của sản phẩm
            productDTO.setBenefits(product.getBenefits());
            productDTO.setSpecifications(product.getSpecifications());
            productDTO.setIngredients(product.getIngredients());
            productDTO.setUsage(product.getUsage());
            productDTO.setReview(product.getReview());
            productDTO.setSkinName(product.getSkinType() != null ? product.getSkinType().getSkinName() : "N/A");
            productDTO.setBrandName(product.getBrand() != null ? product.getBrand().getBrandName() : "N/A");
            productDTO.setSubcategoryName(
                    product.getSubcategory() != null ? product.getSubcategory().getSubcategoryName() : "N/A"
            );

            // Xử lý hình ảnh của sản phẩm
            String firstImage = null;
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                List<String> imageList = Arrays.asList(product.getImages().split(","));
                if (!imageList.isEmpty()) {
                    firstImage = imageList.get(0); // Lấy hình ảnh đầu tiên
                }
            }
            productDTO.setImages(mapImages(product.getImages())); // Giả sử mapImages xử lý chuỗi hình ảnh

            // Lấy danh sách kích thước của sản phẩm
            List<ProductSize> productSizes = productSizeRepository.findByProduct_ProductId(product.getProductId());
            productDTO.setSizes(mapProductSizeToDTO(productSizes));

            // Nếu sản phẩm có mã giảm giá, kiểm tra và cập nhật lại giá hiển thị
            if (product.getDiscountProduct() != null) {
                Date today = new Date();
                if (today.after(product.getDiscountProduct().getEnd_date())) {
                    // Mã giảm giá đã hết hạn, ẩn hoặc thay đổi giá hiển thị
                    productDTO.setDiscountPrice(0.0);  // Hoặc thay giá giảm bằng giá gốc
                } else {
                    productDTO.setDiscountPrice(displayPrice);  // Giá giảm còn lại sau khi có mã giảm giá
                }
            } else {
                productDTO.setDiscountPrice(displayPrice);  // Nếu không có giảm giá, vẫn trả giá gốc
            }

            return productDTO;
        }).orElse(null);
    }

    public String deleteProductById(String productID) {
        Optional<Product> productOpt = productRepository.findById(productID);
        if (productOpt.isPresent()) {
            productRepository.deleteById(productID);
            return "Xóa sản phẩm thành công!";
        } else {
            throw new RuntimeException("Không tìm thấy sản phẩm với ID: " + productID);
        }
    }

    public List<ProductSize> getProductSizesByProductId(String productID) {
        return productSizeRepository.findByProduct_ProductId(productID);
    }

    public Long countTotalProductsSold() {
        return productSizeRepository.countTotalProductsSold();
    }

    public Long getTotalRemainingStock() {
        return productSizeRepository.calculateTotalRemainingStock();
    }

    public String generateNextProductID() {
        // Giả sử bạn đã có một phương thức để lấy mã sản phẩm cuối cùng từ cơ sở dữ liệu
        String lastProductID = productRepository.findLastProductID(); // Lấy mã sản phẩm cuối cùng, ví dụ "SP015"

        if (lastProductID == null || lastProductID.isEmpty()) {
            // Nếu không có sản phẩm nào, bắt đầu từ mã đầu tiên
            return "SP001";
        }

        // Tách phần số từ mã sản phẩm (bỏ qua "SP")
        String numberPart = lastProductID.substring(2);

        // Chuyển phần số sang kiểu int
        int productNumber = Integer.parseInt(numberPart);

        // Tăng giá trị số lên 1
        productNumber++;

        // Định dạng lại phần số để đảm bảo có đủ 3 chữ số
        String nextProductID = String.format("P%03d", productNumber);

        return nextProductID;
    }

    public List<Product> getProductsByBrandId(Integer brandId) {
        return productRepository.findByBrandId(brandId);
    }


    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProductsByKeyword(keyword);
    }


    public List<Product> getProductsByCategory(int categoryID) {
        List<Product> products = productRepository.findBySubcategory_Category_CategoryID(categoryID);
        return products.stream().map(product -> {
            ProductSize firstProductSize = product.getProductSizes().stream().findFirst().orElse(null);
            double displayPrice = 0.0;

            if (firstProductSize != null) {
                displayPrice = getDisplayPrice(product, firstProductSize.getProductSizeID());
            }
            double originalPrice = getOriginalPriceFromSizes(product.getProductId());
            if (product.getProductSizes() != null) {
                product.getProductSizes().forEach(size -> {
                    size.setPrice(originalPrice);

                    // Xử lý giá giảm (nếu có mã giảm giá hợp lệ)
                    if (product.getDiscountProduct() != null) {
                        Date today = new Date();
                        if (today.after(product.getDiscountProduct().getEnd_date())) {
                            // Nếu mã giảm giá hết hạn, đặt giá giảm về 0
                            size.setDiscountPrice(0.0);
                        } else {
                            // Nếu mã giảm giá hợp lệ, cập nhật giá giảm
                            size.setDiscountPrice(getDisplayPrice(product, size.getProductSizeID()));
                        }
                    } else {
                        // Không có mã giảm giá
                        size.setDiscountPrice(0.0);
                    }
                });
            }

            // Cập nhật danh sách kích thước
            List<ProductSize> productSizes = productSizeRepository.findByProduct_ProductId(product.getProductId());
            product.setProductSizes(productSizes);

            return product;
        }).collect(Collectors.toList());
    }


    public List<Object[]> getTop5BrandsWithMostProducts() {
        return productRepository.findTop5BrandsWithMostProducts();
    }

    public List<Product> getProductsBySubcategoryID(int subcategoryID) {
        return productRepository.findBySubcategory_SubcategoryID(subcategoryID);
    }
    public  List<Product>getProductsBySubcategoryCategory(int categoryID, int subcategoryID) {
        return productRepository.findBySubcategory_SubcategoryIDAndSubcategory_Category_CategoryID(categoryID, subcategoryID);

    }

    public Category getCategoryByID(int categoryID) {
        return categoryRepository.getCategoryByCategoryID(categoryID);
    }

}