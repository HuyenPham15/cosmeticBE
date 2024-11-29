package com.cosmetics.restcontroller;

import com.cosmetics.dto.*;
import com.cosmetics.model.*;
import com.cosmetics.repository.*;
import com.cosmetics.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.cosmetics.service.FileService;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/ad")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private OrderDetailRepository orderdetailDAO;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryDAO;

    @Autowired
    private SubcategoryRepository subcategoryDAO;

    @Autowired
    private BrandRepository brandDAO;

    @Autowired
    private SkintypeRepository skintypeDAO;

    @Autowired
    private TransactionRepository transactionDAO;

    @Autowired
    private ProductSizeRepository productsizeDAO;

    @Autowired
    private OrderRepository orderDAO;

    @Autowired
    private ProductSizeService productSizeService;


    @Autowired
    private ProductService productService;

    @Autowired
    private FileService fileService;


    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    @Autowired
    private ProductSizeRepository productSizeRepository;


    @GetMapping("/admin")
    public ResponseEntity<List<ProductDTO>> getAllProductDetails() {
        try {
            List<Product> products = productRepository.findAll();
            List<ProductDTO> productDTOs = products.stream().map(product -> {
                ProductDTO dto = new ProductDTO();
                dto.setProductId(product.getProductId());
                dto.setProductName(product.getProductName());
                dto.setImages(Collections.singletonList(product.getImages()));
                dto.setDescription(product.getDescription());
                dto.setSpecifications(product.getSpecifications());
                dto.setIngredients(product.getIngredients());
                dto.setBenefits(product.getBenefits());
                dto.setUsage(product.getUsage());
                dto.setReview(product.getReview());
// Xử lý discount
                if (product.getDiscountProduct() != null) {
                    dto.setDiscountsp_id(product.getDiscountProduct().getDiscountsp_id());
                } else {
                    dto.setDiscountsp_id("N/A");
                }
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

                // Lấy thông tin từ các entity liên quan với kiểm tra null
                if (product.getSkinType() != null) {
                    dto.setSkinName(product.getSkinType().getSkinName());
                } else {
                    dto.setSkinName("N/A"); // Hoặc để trống
                }

                if (product.getBrand() != null) {
                    dto.setBrandName(product.getBrand().getBrandName());
                } else {
                    dto.setBrandName("N/A"); // Hoặc để trống
                }

                if (product.getSubcategory() != null) {
                    dto.setSubcategoryName(product.getSubcategory().getSubcategoryName());

                    // Từ Subcategory lấy Category
                    if (product.getSubcategory().getCategory() != null) {
                        dto.setCategoryName(product.getSubcategory().getCategory().getCategoryName());
                    } else {
                        dto.setCategoryName("N/A"); // Hoặc để trống
                    }
                } else {
                    dto.setSubcategoryName("N/A"); // Hoặc để trống
                    dto.setCategoryName("N/A"); // Hoặc để trống
                }

                // Lấy thông tin kích thước, giá tiền, số lượng và variant
                if (product.getProductSizes() != null && !product.getProductSizes().isEmpty()) {
                    // Giả sử bạn muốn lấy kích thước đầu tiên trong danh sách kích thước
                    ProductSize firstSize = product.getProductSizes().getFirst();
                    dto.setProductSizeID(firstSize.getProductSizeID());
                    dto.setPrice(firstSize.getPrice()); // Gán giá tiền từ kích thước đầu tiên
                    dto.setQuantity(firstSize.getQuantity()); // Gán số lượng từ kích thước đầu tiên
                    dto.setVariant(firstSize.getVariant());


                } else {
                    dto.setProductSizeID(0); // Nếu là kiểu int, bạn có thể để 0 hoặc giá trị mặc định khác
                    dto.setPrice(0.0); // Giá mặc định nếu không có kích thước
                    dto.setQuantity(0); // Số lượng mặc định nếu không có kích thước
                    dto.setVariant("");

                }

                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(productDTOs);
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console để dễ dàng gỡ lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    //xóa sản phẩm
    @Transactional
    @DeleteMapping("/admin/{productID}")
    public ResponseEntity<String> deleteProduct(@PathVariable String productID) {
        try {
            productSizeRepository.deleteByProductId(productID);
            productRepository.deleteById(productID);

            return ResponseEntity.ok("Xóa sản phẩm và dữ liệu liên quan thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa sản phẩm.");
        }
    }



    @PostMapping(value = "/admin", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO,
                                                    @RequestParam(value = "images", required = false) List<MultipartFile> images) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(); // Khởi tạo ObjectMapper để chuyển đổi Map thành JSON

        String randomProductID = productService.generateNextProductID();

        // Kiểm tra xem Product ID đã tồn tại hay chưa
        if (productRepository.existsByProductId(randomProductID)) {
            return ResponseEntity.badRequest().body(null); // Trả về null nếu ID đã tồn tại
        }

        // Tạo đối tượng product mới
        Product newProduct = new Product();
        newProduct.setProductId(randomProductID);
        newProduct.setProductName(productDTO.getProductName());

        List<String> imageUrls = productDTO.getImages(); // Lấy danh sách URL hình ảnh từ productDTO

        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                // Xử lý URL hình ảnh
                System.out.println("Processing image URL: " + imageUrl);
                // Nếu cần, bạn có thể tải hình ảnh từ URL và lưu vào hệ thống
            }
        } else {
            System.out.println("No images provided.");
        }
        newProduct.setImages(String.join(",", imageUrls));
        System.out.println("Images set for newProduct: " + newProduct.getImages());


        // Thiết lập các thuộc tính khác cho sản phẩm
        newProduct.setDescription(productDTO.getDescription());
        newProduct.setSpecifications(productDTO.getSpecifications());
        newProduct.setIngredients(productDTO.getIngredients());
        newProduct.setBenefits(productDTO.getBenefits());
        newProduct.setUsage(productDTO.getUsage());
        newProduct.setReview(productDTO.getReview());


        // Tìm SkinType theo skinID
        if (productDTO.getSkinID() != null) {
            SkinType skinType = skintypeDAO.findById(productDTO.getSkinID()).orElse(null);
            if (skinType == null) {
                return ResponseEntity.badRequest().body(null); // Trả về null nếu Skin ID không tồn tại
            }
            newProduct.setSkinType(skinType);
        }

        // Tìm Brand theo brandID
        if (productDTO.getBrandID() != null) {
            Brand brand = brandDAO.findById(productDTO.getBrandID()).orElse(null);
            if (brand == null) {
                return ResponseEntity.badRequest().body(null); // Trả về null nếu Brand ID không tồn tại
            }
            newProduct.setBrand(brand);
        }

        // Tìm Subcategory theo subcategoryID
        if (productDTO.getSubcategoryID() != null) {
            Subcategory subCategory = subcategoryDAO.findById(productDTO.getSubcategoryID()).orElse(null);
            if (subCategory == null) {
                return ResponseEntity.badRequest().body(null); // Trả về null nếu Subcategory ID không tồn tại
            }
            newProduct.setSubcategory(subCategory);
        }

        // Lưu đối tượng product
        Product savedProduct = productRepository.save(newProduct);

        // Tạo product_sizes cho sản phẩm
        List<ProductSizeDTO> productSizes = productDTO.getSizes();
        if (productSizes != null && !productSizes.isEmpty()) {
            for (ProductSizeDTO sizeDTO : productSizes) {
                // Tạo đối tượng product_size mới
                ProductSize newSize = new ProductSize();
                newSize.setQuantity(sizeDTO.getQuantity());
                newSize.setPrice(sizeDTO.getPrice());
                newSize.setVariant(sizeDTO.getVariant()); // Lưu variant

                // Liên kết với sản phẩm
                newSize.setProduct(savedProduct);

                // Lưu product_size
                productsizeDAO.save(newSize); // Hibernate sẽ tự động sinh ID cho newSize
            }
        }

        // Chuyển đổi đối tượng savedProduct thành ProductDTO để trả về
        ProductDTO responseProductDTO = new ProductDTO();
        responseProductDTO.setProductId(savedProduct.getProductId());
        responseProductDTO.setProductName(savedProduct.getProductName());
        responseProductDTO.setDescription(savedProduct.getDescription());
        responseProductDTO.setSpecifications(savedProduct.getSpecifications());
        responseProductDTO.setIngredients(savedProduct.getIngredients());
        responseProductDTO.setBenefits(savedProduct.getBenefits());
        responseProductDTO.setUsage(savedProduct.getUsage());
        responseProductDTO.setReview(savedProduct.getReview());

        // Thiết lập các thuộc tính khác cho ProductDTO nếu cần
        if (savedProduct.getSkinType() != null) {
            responseProductDTO.setSkinID(savedProduct.getSkinType().getSkinID());
        }
        if (savedProduct.getBrand() != null) {
            responseProductDTO.setBrandID(savedProduct.getBrand().getBrandID());
        }
        if (savedProduct.getSubcategory() != null) {
            responseProductDTO.setSubcategoryID(savedProduct.getSubcategory().getSubcategoryID());
        }

        // Lấy danh sách product_sizes và thêm vào ProductDTO
        List<ProductSizeDTO> sizeDTOs = new ArrayList<>();
        List<ProductSize> savedSizes = productsizeDAO.findByProduct(savedProduct);
        for (ProductSize size : savedSizes) {
            ProductSizeDTO sizeDTO = new ProductSizeDTO();
            sizeDTO.setProductSizeID(size.getProductSizeID());
            sizeDTO.setQuantity(size.getQuantity());
            sizeDTO.setPrice(size.getPrice());
            sizeDTO.setVariant(size.getVariant());
            sizeDTOs.add(sizeDTO);
        }
        responseProductDTO.setSizes(sizeDTOs);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseProductDTO);
    }

    @GetMapping("/admin/{productID}")
    public ResponseEntity<ProductDTO> getProductID(@PathVariable String productID) {
        try {
            // Tìm sản phẩm theo productID
            Optional<Product> productOpt = productRepository.findById(productID);

            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                ProductDTO dto = new ProductDTO();

                // Chuyển đổi product entity sang DTO
                dto.setProductId(product.getProductId());
                dto.setProductName(product.getProductName());
                dto.setImages(Collections.singletonList(product.getImages()));
                dto.setDescription(product.getDescription());
                dto.setSpecifications(product.getSpecifications());
                dto.setIngredients(product.getIngredients());
                dto.setBenefits(product.getBenefits());
                dto.setUsage(product.getUsage());
                dto.setReview(product.getReview());

                // Kiểm tra và gán giá trị loại da, thương hiệu, danh mục
                dto.setSkinName(product.getSkinType() != null ? product.getSkinType().getSkinName() : "N/A");
                dto.setBrandName(product.getBrand() != null ? product.getBrand().getBrandName() : "N/A");
                if (product.getSubcategory() != null) {
                    dto.setSubcategoryName(product.getSubcategory().getSubcategoryName());
                    dto.setCategoryName(product.getSubcategory().getCategory() != null ?
                            product.getSubcategory().getCategory().getCategoryName() : "N/A");
                } else {
                    dto.setSubcategoryName("N/A");
                    dto.setCategoryName("N/A");
                }

                List<ProductSizeDTO> productSizeDTOs = new ArrayList<>();
                if (product.getProductSizes() != null && !product.getProductSizes().isEmpty()) {
                    for (ProductSize size : product.getProductSizes()) {
                        ProductSizeDTO sizeDTO = new ProductSizeDTO();
                        sizeDTO.setProductSizeID(size.getProductSizeID());
                        sizeDTO.setPrice(size.getPrice());
                        sizeDTO.setQuantity(size.getQuantity());
                        sizeDTO.setVariant(size.getVariant());

                        productSizeDTOs.add(sizeDTO); // Thêm kích thước vào danh sách
                    }
                }

// Gán danh sách kích thước cho sản phẩm
                dto.setSizes(productSizeDTOs);


                return ResponseEntity.ok(dto);
            } else {
                // Nếu không tìm thấy sản phẩm, trả về NOT FOUND
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console để dễ dàng gỡ lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    @PutMapping("/admin/{productID}")
    public ResponseEntity<?> updateProduct(@PathVariable String productID, @RequestBody ProductDTO productDTO) {
        try {
            // Kiểm tra sản phẩm có tồn tại hay không
            Optional<Product> existingProductOpt = productRepository.findById(productID);
            if (existingProductOpt.isPresent()) {
                Product existingProduct = existingProductOpt.get();

                // Cập nhật thông tin sản phẩm
                existingProduct.setProductName(productDTO.getProductName());
                existingProduct.setDescription(productDTO.getDescription());
                existingProduct.setSpecifications(productDTO.getSpecifications());
                existingProduct.setIngredients(productDTO.getIngredients());
                existingProduct.setBenefits(productDTO.getBenefits());
                existingProduct.setUsage(productDTO.getUsage());
                existingProduct.setReview(productDTO.getReview());


                // 3. Cập nhật danh sách hình ảnh
                List<String> newImageUrls = productDTO.getImages();
                if (newImageUrls != null && !newImageUrls.isEmpty()) {
                    // Lấy danh sách hình ảnh hiện tại từ sản phẩm
                    List<String> existingImages = new ArrayList<>(Arrays.asList(existingProduct.getImages().split(",")));

                    // Tạo danh sách hình ảnh sau khi cập nhật (loại bỏ ảnh không có trong danh sách mới)
                    existingImages.retainAll(newImageUrls);

                    // Thêm ảnh mới chưa có trong danh sách hiện tại
                    for (String newImage : newImageUrls) {
                        if (!existingImages.contains(newImage)) {
                            existingImages.add(newImage);
                        }
                    }

                    // Chuyển đổi danh sách hình ảnh đã cập nhật thành chuỗi và gán lại vào sản phẩm
                    existingProduct.setImages(String.join(",", existingImages));
                } else {
                    // Nếu danh sách mới là null hoặc rỗng, xóa hết hình ảnh hiện tại
                    existingProduct.setImages("");
                }



                // Cập nhật loại da (SkinType)
                if (productDTO.getSkinID() != null) {
                    SkinType skinType = skintypeDAO.findById(productDTO.getSkinID())
                            .orElseThrow(() -> new IllegalArgumentException("Skin ID không tồn tại"));
                    existingProduct.setSkinType(skinType);
                }

                // Cập nhật thương hiệu (Brand)
                if (productDTO.getBrandID() != null) {
                    Brand brand = brandDAO.findById(productDTO.getBrandID())
                            .orElseThrow(() -> new IllegalArgumentException("Brand ID không tồn tại"));
                    existingProduct.setBrand(brand);
                }

                // Cập nhật danh mục con (Subcategory)
                if (productDTO.getSubcategoryID() != null) {
                    Subcategory subCategory = subcategoryDAO.findById(productDTO.getSubcategoryID())
                            .orElseThrow(() -> new IllegalArgumentException("Subcategory ID không tồn tại"));
                    existingProduct.setSubcategory(subCategory);
                }


                // Cập nhật danh sách kích thước sản phẩm
                List<ProductSizeDTO> productSizesDTO = productDTO.getSizes();
                List<ProductSize> existingSizes = productsizeDAO.findByProduct(existingProduct);

// Kiểm tra xem danh sách kích thước có khác null và không rỗng
                if (productSizesDTO != null && !productSizesDTO.isEmpty()) {
                    // Tạo một Map để lưu trữ các ID kích thước và đối tượng mới
                    Map<Integer, ProductSizeDTO> newSizeMap = productSizesDTO.stream()
                            .collect(Collectors.toMap(ProductSizeDTO::getProductSizeID, sizeDTO -> sizeDTO));

                    // Cập nhật thông tin cho product_size hiện có và xóa nếu không còn trong danh sách mới
                    Iterator<ProductSize> iterator = existingSizes.iterator();
                    while (iterator.hasNext()) {
                        ProductSize existingSize = iterator.next();
                        ProductSizeDTO sizeDTO = newSizeMap.get(existingSize.getProductSizeID());

                        if (sizeDTO != null) {
                            // Cập nhật nếu product_size đã tồn tại
                            existingSize.setQuantity(sizeDTO.getQuantity());
                            existingSize.setPrice(sizeDTO.getPrice());
                            existingSize.setVariant(sizeDTO.getVariant());
                            productsizeDAO.save(existingSize); // Lưu cập nhật
                            newSizeMap.remove(existingSize.getProductSizeID());
                        } else {
                            // Xóa kích thước không còn tồn tại
                            productsizeDAO.delete(existingSize);
                            iterator.remove();
                        }
                    }

                    // Tạo mới nếu product_size không tồn tại
                    for (ProductSizeDTO sizeDTO : newSizeMap.values()) {
                        ProductSize newSize = new ProductSize();
                        newSize.setQuantity(sizeDTO.getQuantity());
                        newSize.setPrice(sizeDTO.getPrice());
                        newSize.setVariant(sizeDTO.getVariant());
                        newSize.setProduct(existingProduct); // Liên kết với sản phẩm hiện tại
                        productsizeDAO.save(newSize); // Lưu kích thước mới
                        existingSizes.add(newSize); // Thêm vào danh sách hiện có
                    }
                }

                // Cập nhật danh sách kích thước sản phẩm trong đối tượng sản phẩm
                existingProduct.setProductSizes(existingSizes);


                // Lưu lại thông tin sản phẩm đã cập nhật
                Product updatedProduct = productRepository.save(existingProduct);

                // Chuyển đổi sản phẩm đã cập nhật thành ProductDTO để trả về
                ProductDTO updatedProductDTO = new ProductDTO();
                updatedProductDTO.setProductId(updatedProduct.getProductId());
                updatedProductDTO.setProductName(updatedProduct.getProductName());
                updatedProductDTO.setDescription(updatedProduct.getDescription());
                updatedProductDTO.setSpecifications(updatedProduct.getSpecifications());
                updatedProductDTO.setIngredients(updatedProduct.getIngredients());
                updatedProductDTO.setBenefits(updatedProduct.getBenefits());
                updatedProductDTO.setUsage(updatedProduct.getUsage());
                updatedProductDTO.setReview(updatedProduct.getReview());
                updatedProductDTO.setSkinID(updatedProduct.getSkinType() != null ? updatedProduct.getSkinType().getSkinID() : null);
                updatedProductDTO.setBrandID(updatedProduct.getBrand() != null ? updatedProduct.getBrand().getBrandID() : null);
                updatedProductDTO.setSubcategoryID(updatedProduct.getSubcategory() != null ? updatedProduct.getSubcategory().getSubcategoryID() : null);



                String imagesString = updatedProduct.getImages();
                List<String> imagesList = (imagesString != null && !imagesString.isEmpty())
                        ? Arrays.asList(imagesString.split(","))
                        : new ArrayList<>();
                updatedProductDTO.setImages(imagesList);


                // Cập nhật danh sách kích thước sản phẩm cho DTO
                List<ProductSizeDTO> updatedSizesDTO = existingSizes.stream()
                        .map(existingSize -> {
                            ProductSizeDTO sizeDTO = new ProductSizeDTO();
                            sizeDTO.setProductSizeID(existingSize.getProductSizeID());
                            sizeDTO.setQuantity(existingSize.getQuantity());
                            sizeDTO.setPrice(existingSize.getPrice());
                            sizeDTO.setVariant(existingSize.getVariant());
                            return sizeDTO;
                        })
                        .collect(Collectors.toList());
                updatedProductDTO.setSizes(updatedSizesDTO);

                return ResponseEntity.ok(updatedProductDTO); // Trả về đối tượng ProductDTO đã cập nhật
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sản phẩm không tồn tại");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        }

    }

    @GetMapping("/order_detail")
    public ResponseEntity<List<OrderDetail>> getAllOrderDetails(Model model) {
        return ResponseEntity.ok(orderdetailDAO.findAll());
    }

    @Autowired
    private BrandService brandService;

    // API để lấy thương hiệu theo ID
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<BrandDTO> getBrandById(@PathVariable int brandId) {
        BrandDTO brand = brandService.getBrandById(brandId);
        return new ResponseEntity<>(brand, HttpStatus.OK);
    }


    @GetMapping("/brand")
    public ResponseEntity<List<Brand>> getAllBrand(Model model) {
        return ResponseEntity.ok(brandDAO.findAll());
    }



    @GetMapping("/category")
    public ResponseEntity<List<Category>> getAllCategory(Model model) {
        return ResponseEntity.ok(categoryDAO.findAll());
    }

    @GetMapping("/product_size")
    public ResponseEntity<List<ProductSize>> getAllproductsize(Model model) {
        return ResponseEntity.ok(productsizeDAO.findAll());
    }

//    @GetMapping("/admin/product_sizes/{productID}")
//    public ResponseEntity<List<ProductSizeDTO>> getProductSizesByProductId(@PathVariable String productID) {
//        // Lấy danh sách kích thước sản phẩm từ service
//        List<ProductSizeDTO> productSizes = productSizeService.getProductSizesByProductId(productID);
//
//        // Kiểm tra xem danh sách có rỗng hay không
//        if (productSizes == null || productSizes.isEmpty()) {
//            return ResponseEntity.noContent().build(); // Trả về 204 No Content nếu không có dữ liệu
//        }
//
//        // Trả về danh sách kích thước sản phẩm với mã phản hồi 200 OK
//        return ResponseEntity.ok(productSizes);
//    }

    @GetMapping("/admin/byBrand/{brandId}")
    public ResponseEntity<List<Product>> getProductsByBrandId(@PathVariable("brandId") Integer brandId) {
        List<Product> products = productService.getProductsByBrandId(brandId);
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/skin_type")
    public ResponseEntity<List<SkinType>> getAllSkintype(Model model) {
        return ResponseEntity.ok(skintypeDAO.findAll());
    }


    @GetMapping("/subcategory")
    public ResponseEntity<List<Subcategory>> getAllSubcategory(Model model) {
        return ResponseEntity.ok(subcategoryDAO.findAll());
    }



    @GetMapping("/transaction")
    public ResponseEntity<List<Transactions>> getAlltransaction(Model model) {
        return ResponseEntity.ok(transactionDAO.findAll());
    }




    @GetMapping("/order")
    public ResponseEntity<List<Order>> getAllorder(Model model) {
        return ResponseEntity.ok(orderDAO.findAll());
    }


    @Autowired
    DiscountRepository discountRepository;

    @GetMapping("/discount")
    public ResponseEntity<List<Discount>> getAlldiscount(Model model) {
        return ResponseEntity.ok(discountRepository.findAll());
    }


    // Lấy thông tin chi tiết của một discount theo ID
    @GetMapping("/discount/{id}")
    public ResponseEntity<Discount> getDiscountById(@PathVariable String id) {
        Optional<Discount> discount = discountService.getDiscountById(id);
        if (discount.isPresent()) {
            return ResponseEntity.ok(discount.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Thêm mới một discount
    @PostMapping("/discount")
    public ResponseEntity<?> addDiscount(@RequestBody Discount discount) {
        try {
            // Gọi hàm trong service để thêm discount mới
            Discount newDiscount = discountService.addDiscount(discount);
            return ResponseEntity.ok(newDiscount);
        } catch (Exception e) {
            // Xử lý lỗi và trả về phản hồi thích hợp
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred while creating the discount: " + e.getMessage());
        }
    }


    @Autowired
    private DiscountService discountService; // Inject service

    // Cập nhật thông tin của một discount theo ID
    @PutMapping("/discount/{id}")
    public ResponseEntity<Discount> updateDiscount(@PathVariable String id, @RequestBody Discount discount) {
        Optional<Discount> existingDiscount = discountService.getDiscountById(id);
        if (existingDiscount.isPresent()) {
            Discount updatedDiscount = discountService.updateDiscount(id, discount);
            return ResponseEntity.ok(updatedDiscount);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



    // Lấy danh sách top 5 thương hiệu với nhiều sản phẩm nhất, bao gồm hình ảnh
    @GetMapping("/top-brands")
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

    @Autowired
    DiscountProductRepository   discountProductRepository;

    // Create a new discount
    @PostMapping("/discount_sp")
    public ResponseEntity<DiscountProduct> createDiscount(@RequestBody DiscountProduct discountDetails) {
        // Kiểm tra nếu ID đã được thiết lập, nếu chưa thì tạo ID mới
        if (discountDetails.getDiscountsp_id() == null || discountDetails.getDiscountsp_id().isEmpty()) {
            discountDetails.setDiscountsp_id(discountDetails.generateRandomId());
        }

        DiscountProduct newDiscount = discountProductRepository.save(discountDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDiscount);
    }



    @GetMapping("/discount_sp/{id}")
    public ResponseEntity<DiscountProduct> getDiscountspById(@PathVariable String id) {
        Optional<DiscountProduct> optionalDiscount = discountProductRepository.findById(id);

        if (optionalDiscount.isPresent()) {
            return ResponseEntity.ok(optionalDiscount.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Get all discounts
    @GetMapping("/discount_sp/getAllDiscount")
    public ResponseEntity<List<DiscountProduct>> getAllDiscounts() {
        List<DiscountProduct> discounts = discountProductRepository.findAll();
        if (discounts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(discounts);
    }


    // Update an existing discount
    @PutMapping("/discount_sp/{id}")
    public ResponseEntity<DiscountProduct> updateDiscount(@PathVariable String id, @RequestBody DiscountProduct discountDetails) {
        Optional<DiscountProduct> optionalDiscount = discountProductRepository.findById(id);

        if (optionalDiscount.isPresent()) {
            DiscountProduct existingDiscount = optionalDiscount.get();
            // Cập nhật các giá trị mới từ discountDetails
            existingDiscount.setDiscount_sp(discountDetails.getDiscount_sp());
            existingDiscount.setStart_date(discountDetails.getStart_date());
            existingDiscount.setEnd_date(discountDetails.getEnd_date());
            existingDiscount.setStatus(discountDetails.getStatus()); // Cập nhật status từ dữ liệu client

            DiscountProduct updatedDiscount = discountProductRepository.save(existingDiscount);
            return ResponseEntity.ok(updatedDiscount);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    @DeleteMapping("/discount_sp/{id}")
    public ResponseEntity<String> deleteDiscount(@PathVariable String id) {
        Optional<DiscountProduct> optionalDiscount = discountProductRepository.findById(id);

        if (optionalDiscount.isPresent()) {
            DiscountProduct discount = optionalDiscount.get();
            if (Boolean.TRUE.equals(discount.getStatus())) {
                // Mã giảm giá đang hoạt động, không thể xóa
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Mã giảm giá đang hoạt động, không thể xóa.");
            } else {
                // Xóa mã giảm giá ngừng hoạt động
                discountProductRepository.delete(discount);
                return ResponseEntity.noContent().build();
            }
        } else {
            // Mã giảm giá không tồn tại
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mã giảm giá không tồn tại.");
        }
    }


    @PutMapping("admin/{productID}/discount")
    public ResponseEntity<String> updateProductDiscount(
            @PathVariable String productID,
            @RequestBody Map<String, String> payload) {

        String discountsp_id = payload.get("discountsp_id"); // Lấy discountsp_id từ body

        Optional<Product> productOpt = productRepository.findById(productID);
        if (productOpt.isPresent()) {
            Product existingProduct = productOpt.get();

            Optional<DiscountProduct> discountOpt = discountProductRepository.findById(discountsp_id);
            if (discountOpt.isPresent()) {
                existingProduct.setDiscountProduct(discountOpt.get());
                productRepository.save(existingProduct);
                return ResponseEntity.ok("Cập nhật Discount thành công!");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Discount không tồn tại!");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sản phẩm không tồn tại!");
        }
    }



    @PutMapping("/admin/{productID}/remove-discount")
    public ResponseEntity<String> removeProductDiscount(@PathVariable String productID) {
        try {
            // Tìm sản phẩm theo ID
            Optional<Product> productOpt = productRepository.findById(productID);
            if (productOpt.isPresent()) {
                Product existingProduct = productOpt.get();

                // Kiểm tra nếu sản phẩm không có discount
                if (existingProduct.getDiscountProduct() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sản phẩm không có Discount để xóa.");
                }

                // Xóa discountsp_id
                existingProduct.setDiscountProduct(null);
                productRepository.save(existingProduct);

                return ResponseEntity.ok("Xóa Discount trong sản phẩm thành công!");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sản phẩm không tồn tại!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa Discount trong sản phẩm.");
        }
    }



}
