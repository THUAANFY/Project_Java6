package poly.asm.Services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import poly.asm.Models.Order;

@Service
public class MomoPaymentService {
    
    @Value("${momo.partner-code:MOMOBKUN20180529}")
    private String partnerCode;
    
    @Value("${momo.access-key:klm05TvNBzhg7h7j}")
    private String accessKey;
    
    @Value("${momo.secret-key:at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa}")
    private String secretKey;
    
    @Value("${momo.api-endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String apiEndpoint;
    
    @Value("${momo.return-url:http://localhost:8080/order/momo-return}")
    private String returnUrl;
    
    @Value("${momo.notify-url:http://localhost:8080/api/momo/ipn}")
    private String notifyUrl;
    
    /**
     * Tạo yêu cầu thanh toán MoMo
     * @param order Đơn hàng cần thanh toán
     * @return Kết quả từ MoMo API
     */
    public Map<String, Object> createPaymentRequest(Order order) {
        // Tạo requestId ngẫu nhiên
        String requestId = UUID.randomUUID().toString();
        
        // Tạo orderId từ mã đơn hàng
        String orderId = order.getId().toString(); // Sử dụng getId() thay vì getCode()
        
        // Số tiền thanh toán (đơn vị: VND)
        long amount = Math.round(order.getTotal());
        
        // Mô tả giao dịch
        String orderInfo = "Thanh toán đơn hàng " + orderId;
        
        // Tạo chữ ký (signature) - Đây là mã giả, cần thay thế bằng logic thực tế
        String signature = createSignature(partnerCode, accessKey, requestId, amount, orderId, orderInfo, returnUrl, notifyUrl, secretKey);
        
        // Tạo dữ liệu gửi đến MoMo API
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("partnerCode", partnerCode);
        requestData.put("accessKey", accessKey);
        requestData.put("requestId", requestId);
        requestData.put("amount", amount);
        requestData.put("orderId", orderId);
        requestData.put("orderInfo", orderInfo);
        requestData.put("returnUrl", returnUrl);
        requestData.put("notifyUrl", notifyUrl);
        requestData.put("requestType", "captureMoMoWallet");
        requestData.put("signature", signature);
        
        // Gọi API MoMo và nhận kết quả - Đây là mã giả, cần thay thế bằng logic thực tế
        Map<String, Object> responseData = callMoMoAPI(requestData);
        
        // Thêm thông tin thành công vào kết quả
        responseData.put("success", true);
        
        return responseData;
    }
    
    /**
     * Tạo yêu cầu thanh toán MoMo với số tiền tùy chỉnh
     * @param order Đơn hàng cần thanh toán
     * @param customAmount Số tiền tùy chỉnh
     * @return Kết quả từ MoMo API
     */
    public Map<String, Object> createPaymentRequest(Order order, Double customAmount) {
        // Tạo requestId ngẫu nhiên
        String requestId = UUID.randomUUID().toString();
        
        // Tạo orderId từ mã đơn hàng
        String orderId = order.getId().toString(); // Sử dụng getId() thay vì getCode()
        
        // Số tiền thanh toán (đơn vị: VND) - sử dụng số tiền tùy chỉnh
        long amount = Math.round(customAmount);
        
        // Mô tả giao dịch
        String orderInfo = "Thanh toán đơn hàng " + orderId;
        
        // Tạo chữ ký (signature) - Đây là mã giả, cần thay thế bằng logic thực tế
        String signature = createSignature(partnerCode, accessKey, requestId, amount, orderId, orderInfo, returnUrl, notifyUrl, secretKey);
        
        // Tạo dữ liệu gửi đến MoMo API
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("partnerCode", partnerCode);
        requestData.put("accessKey", accessKey);
        requestData.put("requestId", requestId);
        requestData.put("amount", amount);
        requestData.put("orderId", orderId);
        requestData.put("orderInfo", orderInfo);
        requestData.put("returnUrl", returnUrl);
        requestData.put("notifyUrl", notifyUrl);
        requestData.put("requestType", "captureMoMoWallet");
        requestData.put("signature", signature);
        
        // Gọi API MoMo và nhận kết quả - Đây là mã giả, cần thay thế bằng logic thực tế
        Map<String, Object> responseData = callMoMoAPI(requestData);
        
        // Thêm thông tin thành công vào kết quả
        responseData.put("success", true);
        
        return responseData;
    }
    
    /**
     * Xác minh thanh toán từ MoMo
     * @param orderId Mã đơn hàng
     * @param requestId Mã yêu cầu
     * @param amount Số tiền
     * @return true nếu thanh toán hợp lệ, false nếu không
     */
    public boolean verifyPayment(String orderId, String requestId, String amount) {
        // Đây là mã giả, cần thay thế bằng logic xác minh thực tế
        // Thông thường, bạn sẽ kiểm tra chữ ký từ MoMo và xác minh thông tin giao dịch
        
        return true; // Giả sử thanh toán luôn hợp lệ
    }
    
    /**
     * Tạo chữ ký (signature) cho yêu cầu MoMo
     * Đây là mã giả, cần thay thế bằng logic tạo chữ ký thực tế theo tài liệu MoMo
     */
    private String createSignature(String partnerCode, String accessKey, String requestId, 
                                  long amount, String orderId, String orderInfo, 
                                  String returnUrl, String notifyUrl, String secretKey) {
        // Đây là mã giả, cần thay thế bằng logic tạo chữ ký thực tế
        return "dummy_signature";
    }
    
    /**
     * Gọi API MoMo
     * Đây là mã giả, cần thay thế bằng logic gọi API thực tế
     */
    private Map<String, Object> callMoMoAPI(Map<String, Object> requestData) {
        // Đây là mã giả, cần thay thế bằng logic gọi API thực tế
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("requestId", requestData.get("requestId"));
        responseData.put("orderId", requestData.get("orderId"));
        responseData.put("amount", requestData.get("amount"));
        responseData.put("responseTime", System.currentTimeMillis());
        responseData.put("message", "Thành công");
        responseData.put("resultCode", 0);
        
        // Tạo QR code giả
        String dummyQRCode = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=";
        responseData.put("qrCode", dummyQRCode);
        
        return responseData;
    }
}

