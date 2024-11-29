package com.cosmetics.restcontroller;


import com.cosmetics.dto.OrderDTO;
import com.cosmetics.model.Order;
import com.cosmetics.model.Transactions;
import com.cosmetics.repository.OrderRepository;
import com.cosmetics.repository.TransactionRepository;
import com.cosmetics.service.OrderService;
import com.cosmetics.service.TransactionService;
import com.cosmetics.service.VNPAYService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/vnpay")
public class VNPayController {
    @Autowired
    private VNPAYService vnPayService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/vnpay-payment-return")
    public ModelAndView paymentCompleted(HttpServletRequest request) {
        int paymentStatus = vnPayService.orderReturn(request); // Call the service to process payment status

        ModelAndView modelAndView = new ModelAndView();

        String vnpTxnRef = request.getParameter("vnp_TxnRef"); // VNPay Transaction ID
        String orderID = request.getParameter("vnp_OrderInfo"); // Order ID

        switch (paymentStatus) {
            case 1: // Success
                modelAndView.setViewName("redirect:http://localhost:4200/pay"); // Success page URL
                break;
            case 0: // Failure
                modelAndView.setViewName("redirect:http://localhost:4200/error"); // Failure page URL
                break;
            case -1: // Signature verification failed
                modelAndView.setViewName("redirect:http://localhost:4200/error"); // Error page URL for failed signature verification
                break;
            case -2: // Unknown status
                modelAndView.setViewName("redirect:http://localhost:4200/error"); // Error page URL for unknown status
                break;
            default:
                modelAndView.setViewName("redirect:http://localhost:4200/error"); // Handle any unexpected cases
                break;
        }

        return modelAndView;
    }

}