package com.cosmetics.service;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class AdminWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketHandlerImpl webSocketHandler;

    public AdminWebSocketHandler(WebSocketHandlerImpl orderWebSocketHandler) {
        this.webSocketHandler = orderWebSocketHandler;
    }

    // Lưu session của admin khi họ kết nối
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Giả sử chỉ có một admin kết nối
        webSocketHandler.setAdminSession(session);
    }

    // Xử lý nhận thông tin đơn hàng từ client (admin không cần gửi phản hồi gì ở đây)
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Xử lý hoặc gửi phản hồi nếu cần
    }
}
