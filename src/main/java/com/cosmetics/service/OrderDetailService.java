//package com.cosmetics.service;
//
//import com.cosmetics.Dao.OrderDAO;
//import com.cosmetics.Dao.OrderdetailDAO;
//import com.cosmetics.dto.OrderDetailDTO;
//import com.cosmetics.entity.order_details;
//import com.cosmetics.model.OrderDetail;
//import com.cosmetics.repository.OrderDetailRepository;
//import com.cosmetics.repository.OrderRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@Transactional
//public class OrderDetailService {
//
//    @Autowired
//    private OrderDetailRepository orderDetailRepository;
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    public OrderDetailDTO getOrderDetailById(Integer orderDetailsID) {
//        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailsID) // Sử dụng orderdetailDAO
//                .orElse(null); // Trả về null nếu không tìm thấy
//
//        if (orderDetail != null) {
//            OrderDetailDTO dto = new OrderDetailDTO();
//            dto.setOrderID(Integer.valueOf(String.valueOf(orderDetail.getOrderDetailsID())));
//            dto.setQuantity(orderDetail.getQuantity());
//            dto.setPrice(orderDetail.getPrice());
//            dto.setTotalPrice(orderDetail.getTotalMoney());
//
//            // Lấy thông tin sản phẩm
//            if (orderDetail.getProductSize() != null) {
//                dto.setProductName(orderDetail.getProductSize().getProductName());
//                dto.setProductImage(orderDetail.getProductSize().getImages());
//
//
//            }
//            return dto;
//        }
//        return null; // Trả về null nếu không tìm thấy
//    }
//}