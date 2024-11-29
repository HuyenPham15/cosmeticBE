package com.cosmetics.restcontroller;

import com.cosmetics.dto.GHNCreateOrderRequest;
import com.cosmetics.dto.GHNItem;
import com.cosmetics.dto.OrderDTO;
import com.cosmetics.model.*;
import com.cosmetics.repository.*;
import com.cosmetics.service.*;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private VNPAYService vnpayService;
    @Autowired
    private GHNService ghnService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductSizeRepository productSizeRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private UserPointRepository userPointRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private UserService userService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }
    @PostMapping("/order")
    public ResponseEntity<?> createOrder(@RequestBody OrderDTO orderDTO, HttpServletRequest request) {
        try {
            System.out.println("Received OrderDTO: " + orderDTO);
            Map<String, Object> response = orderService.createOrder(orderDTO, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // In chi tiết lỗi
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi tạo đơn hàng: " + e.getMessage());
        }
    }

//    @PostMapping("/create-order/{orderId}")
//    public ResponseEntity<JsonNode> createOrder(@PathVariable("orderId") String orderId,
//                                                @RequestBody GHNCreateOrderRequest request) {
//        try {
//            JsonNode response = ghnService.createGHNOrder(request, orderId);
//            return new ResponseEntity<>(response, HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @GetMapping("/statuses")
    public ResponseEntity<List<Status>> getAllStatuses() {
        List<Status> statuses = statusRepository.findAll();
        return ResponseEntity.ok(statuses);
    }
    @GetMapping("/status")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@RequestParam("statusId") int statusId) {
        List<Order> orders = orderService.getOrdersByStatus(statusId);
        System.out.println("Orders: " + orders);  // Debugging
        List<OrderDTO> orderDTOs = new ArrayList<>();
        for (Order order : orders) {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setOrderID(order.getOrderID());
            orderDTO.setOrderDate(order.getOrderDate());
            orderDTO.setPaymentMethod(order.getPaymentMethod());
            orderDTO.setShippingAddress(order.getShippingAddress());
            orderDTO.setShippingFee(order.getShippingFee());
            orderDTO.setCreateAt(order.getCreate_at());
            orderDTO.setUpdateAt(order.getUpdate_at());
            orderDTO.setUserPoint(order.getUserPoint());
            orderDTO.setTotalPrice(order.getTotalPrice());
            orderDTO.setExpiraDate(order.getExpireDate());
            orderDTO.setNote(order.getNote());
            orderDTO.setRequired_note(order.getRequired_note());
            orderDTO.setStatusId(order.getStatuss().getStatusId());

            Users user = userService.findByUserId(order.getUsers().getUserID());
            if (user != null) {
                orderDTO.setUserId(user.getUserID());
                orderDTO.setUserFirstName(user.getFirstName());
                orderDTO.setUserLastName(user.getLastName());
            }
            orderDTOs.add(orderDTO);
        }
        return ResponseEntity.ok(orderDTOs);
    }

    @PutMapping("/{orderId}/update-status")
    public ResponseEntity<String> updateOrderStatus(@PathVariable String orderId, @RequestBody Map<String, Integer> body) {
        Integer statusId = body.get("status");
        if (statusId == null || orderId == null || orderId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Đơn hàng không hợp lệ");
        }
        try {
            orderService.updateOrderStatus(orderId, statusId); // Gọi service để cập nhật trạng thái
            return ResponseEntity.ok("Cập nhật trạng thái thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật trạng thái đơn hàng.");
        }
    }
}