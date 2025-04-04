package poly.asm.Services;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import poly.asm.Models.Order;

@Service
public class MomoPaymentService {

    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @Value("${momo.api-endpoint}")
    private String apiEndpoint;

    @Value("${momo.return-url}")
    private String returnUrl;

    @Value("${momo.notify-url}")
    private String notifyUrl;

    /**
     * Tạo yêu cầu thanh toán với tổng tiền của đơn hàng
     */
    public Map<String, Object> createPaymentRequest(Order order) {
        String requestId = UUID.randomUUID().toString();
        String orderId = order.getOrderCode(); // Sử dụng orderCode thay vì order.getId()
        long amount = Math.round(order.getTotal());
        String orderInfo = "Thanh toán đơn hàng " + orderId;
        String extraData = "";

        // Tạo chữ ký
        String signature = createSignature(partnerCode, accessKey, requestId, amount, orderId, orderInfo, returnUrl, notifyUrl, extraData, secretKey);

        // Dữ liệu gửi đến MoMo API
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("partnerCode", partnerCode);
        requestData.put("accessKey", accessKey);
        requestData.put("requestId", requestId);
        requestData.put("amount", amount);
        requestData.put("orderId", orderId);
        requestData.put("orderInfo", orderInfo);
        requestData.put("redirectUrl", returnUrl);
        requestData.put("ipnUrl", notifyUrl);
        requestData.put("requestType", "payWithATM"); // Thanh toán bằng thẻ nội địa
        requestData.put("extraData", extraData);
        requestData.put("signature", signature);

        // Gọi API MoMo
        Map<String, Object> responseData = callMoMoAPI(requestData);

        // Xử lý phản hồi từ MoMo
        if (responseData.containsKey("resultCode") && "0".equals(responseData.get("resultCode").toString())) {
            responseData.put("success", true);
            responseData.put("payUrl", responseData.get("payUrl"));
        } else {
            responseData.put("success", false);
            responseData.put("message", responseData.getOrDefault("message", "Lỗi không xác định từ MoMo"));
        }

        return responseData;
    }

    /**
     * Tạo yêu cầu thanh toán với số tiền tùy chỉnh
     */
    public Map<String, Object> createPaymentRequest(Order order, Double customAmount) {
        String requestId = UUID.randomUUID().toString();
        String orderId = order.getOrderCode(); // Sử dụng orderCode thay vì order.getId()
        long amount = Math.round(customAmount);
        String orderInfo = "Thanh toán đơn hàng " + orderId;
        String extraData = "";

        String signature = createSignature(partnerCode, accessKey, requestId, amount, orderId, orderInfo, returnUrl, notifyUrl, extraData, secretKey);

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("partnerCode", partnerCode);
        requestData.put("accessKey", accessKey);
        requestData.put("requestId", requestId);
        requestData.put("amount", amount);
        requestData.put("orderId", orderId);
        requestData.put("orderInfo", orderInfo);
        requestData.put("redirectUrl", returnUrl);
        requestData.put("ipnUrl", notifyUrl);
        requestData.put("requestType", "payWithATM");
        requestData.put("extraData", extraData);
        requestData.put("signature", signature);

        Map<String, Object> responseData = callMoMoAPI(requestData);

        if (responseData.containsKey("resultCode") && "0".equals(responseData.get("resultCode").toString())) {
            responseData.put("success", true);
            responseData.put("payUrl", responseData.get("payUrl"));
        } else {
            responseData.put("success", false);
            responseData.put("message", responseData.getOrDefault("message", "Lỗi không xác định từ MoMo"));
        }

        return responseData;
    }

    /**
     * Xác minh thanh toán từ MoMo
     */
    public boolean verifyPayment(String orderId, String requestId, String amount) {
        String rawData = String.format("accessKey=%s&amount=%s&orderId=%s&requestId=%s", accessKey, amount, orderId, requestId);
        String signature = createSignature(rawData, secretKey);
        // TODO: Thêm logic xác minh chữ ký thực tế từ MoMo nếu cần
        return true; // Giả sử hợp lệ, cần cải thiện trong thực tế
    }

    /**
     * Tạo chữ ký cho yêu cầu thanh toán
     */
    private String createSignature(String partnerCode, String accessKey, String requestId, 
                                  long amount, String orderId, String orderInfo, 
                                  String returnUrl, String notifyUrl, String extraData, String secretKey) {
        Map<String, String> data = new TreeMap<>();
        data.put("accessKey", accessKey);
        data.put("amount", String.valueOf(amount));
        data.put("extraData", extraData);
        data.put("ipnUrl", notifyUrl);
        data.put("orderId", orderId);
        data.put("orderInfo", orderInfo);
        data.put("partnerCode", partnerCode);
        data.put("redirectUrl", returnUrl);
        data.put("requestId", requestId);
        data.put("requestType", "payWithATM");

        StringBuilder rawData = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (rawData.length() > 0) {
                rawData.append("&");
            }
            rawData.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return createSignature(rawData.toString(), secretKey);
    }

    private String createSignature(String rawData, String secretKey) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(rawData.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private Map<String, Object> callMoMoAPI(Map<String, Object> requestData) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestData, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(apiEndpoint, request, Map.class);

            return response != null ? response : new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi gọi API MoMo: " + e.getMessage());
            return errorResponse;
        }
    }
}