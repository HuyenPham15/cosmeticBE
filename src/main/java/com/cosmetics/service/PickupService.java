package com.cosmetics.service;

import com.cosmetics.dto.Shift;
import com.cosmetics.dto.ShiftResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PickupService {
    // URL của API GHN để lấy danh sách ca lấy hàng
    private static final String SHIFT_API_URL = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shift/date";

    private static final String TOKEN = "18da3387-9f92-11ef-8e53-0a00184fe694";
    public List<Shift> getShifts(long date) {
        // Tạo đối tượng RestTemplate để gửi yêu cầu HTTP
        RestTemplate restTemplate = new RestTemplate();

        // Tạo URL với tham số date (Unix timestamp)
        String url = SHIFT_API_URL + "?date=" + date;

        // Tạo header chứa token
        HttpHeaders headers = new HttpHeaders();
        headers.set("token", TOKEN);

        // Tạo yêu cầu với header và URL
        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, java.net.URI.create(url));

        // Gửi yêu cầu GET và nhận dữ liệu trả về
        ResponseEntity<ShiftResponse> response = restTemplate.exchange(requestEntity, ShiftResponse.class);

        // Kiểm tra xem response có hợp lệ không
        if (response != null && response.getBody() != null && response.getBody().getData() != null) {
            // Trả về danh sách các ca lấy hàng
            return response.getBody().getData();
        }

        // Nếu không có ca lấy hàng, trả về null
        return null;
    }
}
