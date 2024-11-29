package com.cosmetics.repository;

import com.cosmetics.dto.DailyRevenueDTO;
import com.cosmetics.dto.MonthlyRevenueDTO;
import com.cosmetics.dto.OrderDTO;
import com.cosmetics.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    // Thêm phương thức truy vấn tùy chỉnh nếu cần

//    Optional<Order> findById(String orderID);


//    // Đếm số đơn hàng hoàn thành
//    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'Hoàn thành'")
//    Long countCompletedOrders();

//    // Đếm số đơn hàng đang xử lý
//    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'Đang xử lý'")
//    Long countPendingOrders();

//    // Đếm số đơn hàng đang giao hàng
//    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'Đang giao hàng'")
//    Long countShippingOrders();
//
//    // Đếm số đơn hàng đã hủy
//    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'Đã hủy'")
//    Long countCanceledOrders();

    // Tính tổng doanh thu
    @Query("SELECT SUM(o.totalPrice) FROM Order o")
    Double calculateTotalRevenue();
    @Query("SELECT NEW com.cosmetics.dto.MonthlyRevenueDTO(MONTH(o.orderDate), SUM(o.totalPrice)) FROM Order o GROUP BY MONTH(o.orderDate)")
    List<MonthlyRevenueDTO> getMonthlyRevenue();
    @Query("SELECT new com.cosmetics.dto.DailyRevenueDTO(DAY(o.orderDate), SUM(o.totalPrice)) " +
            "FROM Order o GROUP BY DAY(o.orderDate)")
    List<DailyRevenueDTO> getDailyRevenue();

    Order findByOrderID(String orderID);

    List<Order> findByStatuss_statusId(int statusId);  // Tìm đơn hàng theo statusId

    List<Order> findByUsers_userID(int userID);  // Truy vấn qua thuộc tính 'user' và 'userId'

//    List<OrderDTO> findByStatus(String status);

//    @Query("SELECT o FROM Order o JOIN o.transactions t WHERE t.referenceCode = :transactionId")
//    Optional<Order> findByTransactionReference(@Param("transactionId") String transactionId);
//

}
