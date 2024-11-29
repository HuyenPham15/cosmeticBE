package com.cosmetics.service;

import com.cosmetics.configuration.VNPAYConfig;
import com.cosmetics.model.Order;
import com.cosmetics.model.Status;
import com.cosmetics.model.Transactions;
import com.cosmetics.repository.OrderRepository;
import com.cosmetics.repository.StatusRepository;
import com.cosmetics.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPAYService {
    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final StatusRepository statusRepository;

    public VNPAYService(TransactionRepository transactionRepository, OrderRepository orderRepository, StatusRepository statusRepository) {
        this.transactionRepository = transactionRepository;
        this.orderRepository = orderRepository;
        this.statusRepository = statusRepository;
    }

    public String createOrder(HttpServletRequest request, int amount, String orderID) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = VNPAYConfig.getRandomNumber(8);
        String vnp_IpAddr = VNPAYConfig.getIpAddress(request);
        String vnp_TmnCode = VNPAYConfig.vnp_TmnCode;
        String orderType = "order-type";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderID);
        vnp_Params.put("vnp_OrderType", orderType);

        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPAYConfig.vnp_Returnurl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 1);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        saveTransaction(orderID, vnp_TxnRef, amount, "Đang thanh toán");

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String salt = VNPAYConfig.vnp_HashSecret;
        String vnp_SecureHash = VNPAYConfig.hmacSHA512(salt, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPAYConfig.vnp_PayUrl + "?" + queryUrl;
        return paymentUrl;
    }

    public int orderReturn(HttpServletRequest request) {
        // Thu thập tất cả các tham số từ request
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        String signValue = VNPAYConfig.hashAllFields(fields);
        if (signValue.equals(vnp_SecureHash)) {
            String transactionStatus = request.getParameter("vnp_TransactionStatus");
            String orderID = request.getParameter("vnp_OrderInfo");
            String transactionID = request.getParameter("vnp_TxnRef");
            int amount = Integer.parseInt(request.getParameter("vnp_Amount")) / 100;

            long paymentTimestamp = Long.parseLong(request.getParameter("vnp_PayDate")); // Thời gian thanh toán từ VNPay
            long currentTime = System.currentTimeMillis();

            long timeLimit = 15 * 60 * 1000; // 15 phút = 15 * 60 * 1000 ms
            if ((currentTime - paymentTimestamp) > timeLimit) {
                saveTransaction(orderID, transactionID, amount, "Đã hủy");
                updateOrderStatus(orderID, "Hủy đơn hàng");
                return 0;
            }
            switch (transactionStatus) {
                case "00": // Thành công
                    saveTransaction(orderID, transactionID, amount, "Đã thanh toán");
                    return 1;
                case "01": // Thất bại
                    saveTransaction(orderID, transactionID, amount, "Thanh toán thất bại");
                    return 2; // Thanh toán thất bại
                case "02": // Hủy
                    saveTransaction(orderID, transactionID, amount, "Đã hủy");
                    return 0; // Đơn hàng bị hủy
                default:
                    return -2; // Trạng thái không xác định
            }
        } else {
            return -1; // Xác minh chữ ký không thành công
        }
    }
    private void saveTransaction(String orderID, String transactionID, int amount, String status) {
        Transactions existingTransaction = transactionRepository.findByTransactionID(transactionID);

        if (existingTransaction == null) {
            // Nếu giao dịch chưa tồn tại, tạo mới giao dịch
            Transactions existingOrderTransaction = transactionRepository.findByOrderOrderID(orderID);

            if (existingOrderTransaction == null) {
                // Kiểm tra đơn hàng và tạo giao dịch mới
                Order order = orderRepository.findById(orderID).orElse(null);
                if (order != null) {
                    Transactions transaction = new Transactions();
                    transaction.setOrder(order);
                    transaction.setAmount(amount);
                    transaction.setTransaction_date(new Date());
                    transaction.setStatus(status);
                    transaction.setCreated_at(new Date());
                    transaction.setTransactionID(transactionID);
                    transactionRepository.save(transaction);
                    if ("Đã hủy".equalsIgnoreCase(status)) {
                        updateOrderStatus(orderID, "Đã hủy");  // Cập nhật trạng thái đơn hàng
                    }
                    System.out.println("Cập nhật giao dịch cho đơn hàng: " + orderID);
                } else {
                    System.out.println("Không tìm thấy đơn hàng với ID: " + orderID);
                }
            } else {
                // Cập nhật giao dịch cho đơn hàng đã tồn tại
                existingOrderTransaction.setStatus(status);
                existingOrderTransaction.setAmount(amount);
                existingOrderTransaction.setTransaction_date(new Date());
                transactionRepository.save(existingOrderTransaction);
                if ("Đã hủy".equalsIgnoreCase(status)) {
                    updateOrderStatus(orderID, "Đã hủy");  // Cập nhật trạng thái đơn hàng
                }
                System.out.println("Cập nhật giao dịch cho đơn hàng: " + orderID);
            }
        } else {
            // Nếu giao dịch đã tồn tại, kiểm tra trạng thái
            if (!existingTransaction.getStatus().equals(status)) {
                // Cập nhật trạng thái nếu có thay đổi
                existingTransaction.setStatus(status);
                existingTransaction.setAmount(amount); // Cập nhật số tiền nếu cần
                existingTransaction.setTransaction_date(new Date()); // Cập nhật ngày giao dịch
                transactionRepository.save(existingTransaction); // Lưu lại giao dịch đã cập nhật
                System.out.println("Cập nhật trạng thái giao dịch cho transactionID: " + transactionID);
            } else {
                System.out.println("Giao dịch với ID " + transactionID + " đã tồn tại và không có thay đổi.");
            }
        }
    }

    private void updateOrderStatus(String orderID, String statusName) {
        System.out.println("Đang cố gắng cập nhật trạng thái đơn hàng với ID: " + orderID + " thành trạng thái: " + statusName);
        Optional<Order> optionalOrder = orderRepository.findById(orderID);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            Status status = statusRepository.findByStatusName(statusName)
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy trạng thái với tên: " + statusName));
            order.setStatuss(status);
            orderRepository.save(order);
            System.out.println("Cập nhật trạng thái đơn hàng " + orderID + " thành '" + status.getStatusName() + "'");
        } else {
            System.out.println("Không tìm thấy đơn hàng với ID: " + orderID);
        }
    }


}
