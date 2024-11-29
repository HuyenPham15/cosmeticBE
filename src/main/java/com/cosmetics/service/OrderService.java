package com.cosmetics.service;

import com.cosmetics.dto.OrderDTO;
import com.cosmetics.dto.OrderDetailDTO;
import com.cosmetics.model.*;
import com.cosmetics.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.*;

@Service

public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private UserService userService;
    private static final String DIGITS = "0123456789";
    private static final int ORDER_ID_LENGTH = 10;
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    private ProductSizeRepository productSizeRepository;
    @Autowired
    private VNPAYService vnpayService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private DiscountProductRepository discountProductRepository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private CartRepository cartRepository;

    public double getProductPriceAtOrderTime(String productId, Integer productSizeId, String discountsp_id) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));
        ProductSize productSize = productSizeRepository.findById(productSizeId)
                .orElseThrow(() -> new NoSuchElementException("Product size not found"));
        if (discountsp_id != null && !discountsp_id.isEmpty()) {
            DiscountProduct discountProduct = discountProductRepository.findById(discountsp_id)
                    .orElseThrow(() -> new NoSuchElementException("Discount not found"));

            // Tính giá sau khi áp dụng giảm giá
            return productSize.getPrice() * (1 - discountProduct.getDiscount_sp() / 100.0);
        } else {
            // Trả về giá gốc nếu không có giảm giá
            return productSize.getPrice();
        }
    }

    public Map<String, Object> createOrder(OrderDTO orderDTO, HttpServletRequest request) {
        List<Order> savedOrders = new ArrayList<>();
        Map<String, Object> responseMap = new HashMap<>();

        Users user = userService.findByUserId(orderDTO.getUserId());
        if (user == null) {
            throw new NoSuchElementException("User not found");
        }

        if (orderDTO.getOrderDetails() != null && !orderDTO.getOrderDetails().isEmpty()) {
            for (OrderDetailDTO detailDTO : orderDTO.getOrderDetails()) {
                String randomOrderID = generateRandomOrderID();
                Order order = new Order();
                order.setOrderID(randomOrderID);
                order.setPaymentMethod(orderDTO.getPaymentMethod());
                Status defaultStatus = statusRepository.findById(1)
                        .orElseThrow(() -> new NoSuchElementException("Status with ID 0 not found"));
                order.setStatuss(defaultStatus);
                order.setShippingAddress(orderDTO.getShippingAddress());
                order.setShippingFee(orderDTO.getShippingFee());

                double discountAmount = 0.0;
                if (orderDTO.getDiscountId() != null) {
                    Discount discount = discountRepository.findByDiscountCode(orderDTO.getDiscountId())
                            .orElseThrow(() -> new NoSuchElementException("Discount not found"));

                    if ("FREESHIP".equalsIgnoreCase(discount.getDiscountCode())) {
                        order.setShippingFee((float) 0); // Trừ phí vận chuyển
                    } else {
                        discountAmount = discount.getAmount();
                    }
                }

                // Sử dụng giá sản phẩm được gửi từ frontend (detailDTO.getPrice)
                double productPriceAtOrderTime = detailDTO.getPrice();
                double totalPrice = productPriceAtOrderTime * detailDTO.getQuantity();
                totalPrice -= discountAmount; // Áp dụng giảm giá

                // Đảm bảo tổng tiền không âm
                if (totalPrice < 0) {
                    totalPrice = 0;
                }
                order.setTotalPrice(totalPrice + order.getShippingFee());
                order.setUsers(user);
                int pointsEarned = calculatePoints(order);
                order.setUserPoint(pointsEarned);

                // Lưu thông tin đơn hàng vào database
                Order savedOrder = orderRepository.save(order);
                if (savedOrder != null) {
                    savedOrders.add(savedOrder);

                    // Lưu thông tin chi tiết đơn hàng
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(savedOrder);
                    orderDetail.setQuantity(detailDTO.getQuantity());
                    orderDetail.setPrice(productPriceAtOrderTime);  // Lưu giá được gửi từ frontend
                    orderDetail.setTotalMoney(order.getTotalPrice());

                    // Lấy thông tin ProductSize
                    ProductSize productSize = productSizeRepository.findById(detailDTO.getProductSizeID())
                            .orElseThrow(() -> new NoSuchElementException("Product size not found"));
                    orderDetail.setProductSize(productSize);
                    orderDetailRepository.save(orderDetail);

                    int updatedStock = productSize.getQuantity() - detailDTO.getQuantity();  // Giảm số lượng theo số lượng trong chi tiết đơn hàng
                    if (updatedStock < 0) {
                        throw new IllegalArgumentException("Không đủ hàng trong kho");
                    }

                    productSize.setQuantity(updatedStock);
                    productSizeRepository.save(productSize);  // Lưu thay đổi vào cơ sở dữ liệu
                    if (orderDTO.getPaymentMethod() == 1) {
                        Transactions transaction = new Transactions();
                        transaction.setTransactionID(generateTransactionID());
                        transaction.setAmount(order.getTotalPrice());
                        transaction.setTransaction_date(new Date());
                        transaction.setStatus("Chưa thanh toán");
                        transaction.setOrder(savedOrder); // Liên kết với đơn hàng
                        transactionRepository.save(transaction);
                    } else if (orderDTO.getPaymentMethod()==2) {
                        String paymentUrl = vnpayService.createOrder(request, (int) totalPrice, savedOrder.getOrderID());
                        responseMap.put("paymentUrl", paymentUrl);
                    } else {
                        throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ");
                    }

                    // Lưu điểm thưởng vào userPoints
                    UserPoint userPoints = new UserPoint();
                    userPoints.setUsers(user);
                    userPoints.setPoint(pointsEarned);
                    userPoints.setOrder(savedOrder); // Liên kết với đơn hàng
                    userPointRepository.save(userPoints);
                }
            }
        } else {
            throw new IllegalArgumentException("Order details cannot be null or empty");
        }
        responseMap.put("orders", savedOrders);
        return responseMap;
    }

    private int calculatePoints(Order order) {
        return (int) (order.getTotalPrice() / 100);
    }

    public String generateRandomOrderID() {
        StringBuilder sb = new StringBuilder();

        // Thêm 3 chữ cái cố định "CMT"
        sb.append("CMT");

        for (int i = 0; i < 7; i++) {
            int index = random.nextInt(DIGITS.length());
            sb.append(DIGITS.charAt(index));
        }

        return sb.toString();
    }

    public String generateTransactionID() {
        Random random = new Random();
        int randomID = 100000 + random.nextInt(900000); // Số ngẫu nhiên từ 100000 đến 999999
        return String.valueOf(randomID);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByStatus(int statusId) {
        return orderRepository.findByStatuss_statusId(statusId);
    }
    public void updateOrderStatus(String orderID, int statusId) {
        if (statusId <= 0) {
            throw new RuntimeException("Trạng thái không hợp lệ");
        }

        Order order = orderRepository.findByOrderID(orderID);
        if (order == null) {
            throw new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderID);
        }
        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạng thái với ID: " + statusId));
        order.setStatuss(status);
        orderRepository.save(order);
    }

    public List<Order> findOrdersByUserId(int userID) {
        return orderRepository.findByUsers_userID(userID);  // Dùng phương thức repository để lấy đơn hàng
    }
//    public List<OrderDTO> getPendingOrders() {
//        return orderRepository.findByStatus("Chờ thanh toán"); // Lấy đơn hàng có trạng thái chờ xử lý
//    }

//    public void updateOrderStatus(String orderId, String status) {
//        Order order = orderRepository.findById(orderId).orElseThrow();
//        order.setStatus(status);
//        orderRepository.save(order);
//    }
}
