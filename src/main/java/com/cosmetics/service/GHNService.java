package com.cosmetics.service;

import com.cosmetics.dto.GHNCreateOrderRequest;
import com.cosmetics.dto.GHNItem;
import com.cosmetics.model.Order;
import com.cosmetics.model.Shop;
import com.cosmetics.repository.OrderRepository;
import com.cosmetics.repository.ShopRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GHNService {
    private final ShopRepository shopRepository;
    private final RestTemplate restTemplate;
    private final OrderRepository orderRepository;
    @Value("${ghn.api.token}")
    private String ghnApiToken;
    private static final String GHN_API_URL_PROVINCE = "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/province";
    private static final String GHN_API_URL_DISTRICT = "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/district";
    private static final String GHN_API_URL_WARD = "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/ward?district_id=";


    public GHNService(ShopRepository shopRepository, RestTemplate restTemplate, OrderRepository orderRepository) {
        this.shopRepository = shopRepository;
        this.restTemplate = restTemplate;
        this.orderRepository = orderRepository;
    }
    public JsonNode createGHNOrder(GHNCreateOrderRequest request, String orderId) {
        // Lấy thông tin shop mặc định
        Shop shop = shopRepository.findByDefaultShopTrue();
        if (shop == null) {
            throw new RuntimeException("Không tìm thấy shop mặc định");
        }
        Order order = orderRepository.findByOrderID(orderId);
        if (order == null) {
            throw new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId);
        }
        // Tách địa chỉ từ shop
        Map<String, String> addressComponents = parseAddress(shop.getShopAddress());
        Map<String, String> addressIds = parseAndGetAddressIds(order.getShippingAddress());
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnApiToken); // Đảm bảo token GHN hợp lệ

        // Tạo body yêu cầu từ `GHNCreateOrderRequest`
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("payment_type_id", request.getPayment_type_id());
        requestBody.put("note", request.getNote());
        if (request.getRequired_note() == null || request.getRequired_note().trim().isEmpty()) {
            requestBody.put("required_note", "Không có ghi chú");
        } else {
            requestBody.put("required_note", request.getRequired_note());
        }

        requestBody.put("from_name", shop.getShopName());
        requestBody.put("from_phone", shop.getPhone());
        requestBody.put("from_address", addressComponents.get("address"));
        requestBody.put("from_ward_name", addressComponents.get("ward"));
        requestBody.put("from_district_name", addressComponents.get("district"));
        requestBody.put("from_province_name", addressComponents.get("province"));

        // Thông tin người nhận
        requestBody.put("to_name", order.getUsers().getFirstName());
        requestBody.put("to_phone", order.getUsers().getPhoneNumber());
        requestBody.put("to_address", order.getShippingAddress());  // Địa chỉ đầy đủ của người nhận
        requestBody.put("to_ward_code", addressIds.get("wardId")); // Mã xã/phường
        requestBody.put("to_district_id", addressIds.get("provinceId")); // Mã quận/huyện

        if (request.getWeight() <= 0) {
            requestBody.put("weight", 1);  // Đặt giá trị mặc định là 1 nếu không có trọng lượng hợp lệ
        } else {
            requestBody.put("weight", request.getWeight());
        }

        requestBody.put("cod_amount", (int) request.getCod_amount()); // Ensure int type for cod_amount
        requestBody.put("content", request.getContent());
        requestBody.put("length", request.getLength());
        requestBody.put("width", request.getWidth());
        requestBody.put("height", request.getHeight());
        requestBody.put("insurance_value", request.getInsurance_value());
        requestBody.put("service_id", request.getService_id());
        requestBody.put("service_type_id", request.getService_type_id());

        // Loại bỏ trường coupon nếu có
        requestBody.remove("coupon");

        // Thêm thông tin điểm pick-up và điểm giao hàng
        requestBody.put("pick_station_id", request.getPick_station_id());
        requestBody.put("deliver_station_id", request.getDeliver_station_id());

        // Kiểm tra và khởi tạo danh sách items nếu nó là null
        if (request.getItems() == null) {
            request.setItems(new ArrayList<>()); // Khởi tạo một danh sách trống
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (GHNItem item : request.getItems()) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", item.getName());
            itemMap.put("code", String.valueOf(item.getCode()));  // Đảm bảo mã là chuỗi
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("price", item.getPrice());
            itemMap.put("length", item.getLength());
            itemMap.put("width", item.getWidth());
            itemMap.put("height", item.getHeight());
            itemMap.put("weight", item.getWeight());
            items.add(itemMap);
        }
        requestBody.put("items", items);

        requestBody.put("pick_shift", request.getPick_shift());

        HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, httpRequest, JsonNode.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Error creating GHN order: " + response.getBody().toString());
            }
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tạo đơn hàng GHN: " + e.getMessage());
        }
    }

    public JsonNode getGHNOrderDetails(String orderCode) {
        // Use GET method to fetch order details from GHN API
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/detail?order_code=" + orderCode;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnApiToken);  // Ensure that the API token is correctly set

        // No request body is needed for GET, so we create an empty HttpEntity
        HttpEntity<?> httpRequest = new HttpEntity<>(headers);

        try {
            // Send GET request to GHN API
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, httpRequest, JsonNode.class);

            // Check if the response is successful
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Error getting GHN order details: " + response.getBody().toString());
            }

            // Return the response body (order details)
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy thông tin đơn hàng GHN: " + e.getMessage());
        }
    }

    public Map<String, String> parseAddress(String fullAddress) {
        Map<String, String> addressComponents = new HashMap<>();

        if (fullAddress != null && !fullAddress.isEmpty()) {
            String[] parts = fullAddress.split(",\\s*");

            // Kiểm tra và thêm các thành phần vào map nếu có
            if (parts.length > 0) {
                addressComponents.put("address", parts[0].trim()); // Số nhà, Đường
            }
            if (parts.length > 1) {
                addressComponents.put("ward", parts[parts.length - 3].trim()); // Phường
            }
            if (parts.length > 2) {
                addressComponents.put("district", parts[parts.length - 2].trim()); // Quận
            }
            if (parts.length > 3) {
                addressComponents.put("province", parts[parts.length - 1].trim()); // Thành phố
            }
        }
        return addressComponents;
    }
    public JsonNode getProvinces() {
        String url = GHN_API_URL_PROVINCE;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnApiToken); // Đảm bảo token GHN hợp lệ

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi gọi API lấy danh sách tỉnh: " + e.getMessage());
        }
    }

    public JsonNode getDistricts(String provinceId) {
        String url = GHN_API_URL_DISTRICT + "?province_id=" + provinceId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnApiToken); // Đảm bảo token GHN hợp lệ

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi gọi API lấy danh sách quận/huyện: " + e.getMessage());
        }
    }

    public JsonNode getWards(String districtId) {
        String url = GHN_API_URL_WARD + districtId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnApiToken); // Đảm bảo token GHN hợp lệ

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi gọi API lấy danh sách xã/phường: " + e.getMessage());
        }
    }

    public Map<String, String> parseAndGetAddressIds(String fullAddress) {
        // Tách địa chỉ thành các phần
        Map<String, String> addressComponents = parseAddress(fullAddress);

        String provinceName = addressComponents.get("province");
        String districtName = addressComponents.get("district");
        String wardName = addressComponents.get("ward");

        // Lấy ID của tỉnh
        JsonNode provinces = getProvinces();
        String provinceId = getProvinceId(provinces, provinceName);

        // Lấy ID của quận
        JsonNode districts = getDistricts(provinceId);
        String districtId = getDistrictId(districts, districtName);

        // Lấy ID của phường
        JsonNode wards = getWards(districtId);
        String wardId = getWardId(wards, wardName);

        // Gắn các ID vào request (Ví dụ: vào một object request)
        Map<String, String> requestData = new HashMap<>();
        requestData.put("provinceId", provinceId);
        requestData.put("districtId", districtId);
        requestData.put("wardId", wardId);

        // Output để kiểm tra kết quả
        System.out.println("Request Data: " + requestData);
        return addressComponents;
    }

    private String getProvinceId(JsonNode provinces, String provinceName) {
        for (JsonNode province : provinces) {
            if (province.get("name").asText().equalsIgnoreCase(provinceName)) {
                return province.get("province_id").asText();
            }
        }
        throw new RuntimeException("Không tìm thấy tỉnh: " + provinceName);
    }

    // Tìm ID của quận/huyện từ danh sách quận/huyện
    private String getDistrictId(JsonNode districts, String districtName) {
        for (JsonNode district : districts) {
            if (district.get("name").asText().equalsIgnoreCase(districtName)) {
                return district.get("district_id").asText();
            }
        }
        throw new RuntimeException("Không tìm thấy quận/huyện: " + districtName);
    }

    // Tìm ID của xã/phường từ danh sách xã/phường
    private String getWardId(JsonNode wards, String wardName) {
        for (JsonNode ward : wards) {
            if (ward.get("name").asText().equalsIgnoreCase(wardName)) {
                return ward.get("ward_id").asText();
            }
        }
        throw new RuntimeException("Không tìm thấy xã/phường: " + wardName);
    }
}
