package com.cosmetics.service;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketHandlerImpl extends TextWebSocketHandler {
    private WebSocketSession adminSession;

    // Lưu kết nối của admin (Có thể lưu trong một map nếu có nhiều admin)
    public void setAdminSession(WebSocketSession session) {
        this.adminSession = session;
    }

    // Xử lý khi nhận được thông tin đơn hàng từ client
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Lấy thông tin đơn hàng từ client
        String orderData = message.getPayload();
        System.out.println("Đơn hàng nhận được từ client: " + orderData);

        // Kiểm tra nếu admin đã kết nối thì gửi đơn hàng cho admin
        if (adminSession != null && adminSession.isOpen()) {
            try {
                adminSession.sendMessage(new TextMessage(orderData)); // Gửi thông tin đơn hàng cho admin
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}