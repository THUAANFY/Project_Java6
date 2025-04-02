package poly.asm.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import poly.asm.Models.Order;
import poly.asm.Models.User;
import poly.asm.Services.MomoPaymentService;
import poly.asm.Services.OrderService;

@Controller 
public class MomoController {
    @Autowired
    private MomoPaymentService momoPaymentService;
    
    @Autowired
    private OrderService orderService;
    
    /**
     * Hiển thị trang thanh toán MoMo
     */
    @GetMapping("/momo-payment")
    public String showMomoPaymentPage(@RequestParam("orderId") String orderCode, 
                                     @RequestParam(value = "amount", required = false) Double amount,
                                     Model model, 
                                     HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        
        Order order = orderService.getOrderByCode(orderCode);
        if (order == null) {
            model.addAttribute("error", "Không tìm thấy đơn hàng");
            return "Home/payment-error";
        }
        
        // Kiểm tra xem đơn hàng có thuộc về người dùng hiện tại không
        if (!order.getUser().getId().equals(loggedInUser.getId())) {
            model.addAttribute("error", "Bạn không có quyền truy cập đơn hàng này");
            return "Home/payment-error";
        }
        
        // Thêm thông tin đơn hàng vào model
        model.addAttribute("orderCode", orderCode);
        
        // Sử dụng giá trị amount từ tham số nếu có, nếu không thì lấy từ đơn hàng
        if (amount != null) {
            model.addAttribute("amount", amount);
        } else {
            model.addAttribute("amount", order.getTotal());
        }
        
        return "Home/momo-payment";
    }
    
    /**
     * API để tạo yêu cầu thanh toán MoMo
     */
    @PostMapping("/api/momo/create-payment")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createMomoPayment(@RequestBody Map<String, String> requestData, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng đăng nhập để thanh toán"));
        }
        
        String orderCode = requestData.get("orderCode");
        Order order = orderService.getOrderByCode(orderCode);
        
        if (order == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy đơn hàng"));
        }
        
        // Lấy amount từ request nếu có
        Double customAmount = null;
        if (requestData.containsKey("amount") && requestData.get("amount") != null) {
            try {
                customAmount = Double.parseDouble(requestData.get("amount"));
            } catch (NumberFormatException e) {
                // Nếu không parse được, sử dụng giá trị từ đơn hàng
            }
        }
        
        // Tạo yêu cầu thanh toán MoMo
        Map<String, Object> paymentResult = momoPaymentService.createPaymentRequest(order);

        // Nếu có amount tùy chỉnh, cập nhật lại amount trong kết quả
        if (customAmount != null) {
            // Giả sử paymentResult có chứa amount, cập nhật nó
            if (paymentResult.containsKey("amount")) {
                paymentResult.put("amount", customAmount);
            }
        }
        
        return ResponseEntity.ok(paymentResult);
    }
    
    /**
     * Xử lý callback từ MoMo
     */
    @GetMapping("/order/momo-return")
    public String handleMomoReturn(@RequestParam(required = false) String orderId,
                                  @RequestParam(required = false) String requestId,
                                  @RequestParam(required = false) String amount,
                                  @RequestParam(required = false) String resultCode,
                                  @RequestParam(required = false) String message,
                                  Model model) {
        
        // Kiểm tra kết quả thanh toán
        if ("0".equals(resultCode)) {
            // Thanh toán thành công
            Order order = orderService.getOrderByCode(orderId);
            if (order != null) {
                // Cập nhật trạng thái đơn hàng
                orderService.updateOrderStatus(orderId, "Đã thanh toán");
                
                // Chuyển hướng đến trang hoàn tất đơn hàng
                return "redirect:/order/complete?id=" + orderId;
            }
        }
        
        // Thanh toán thất bại
        model.addAttribute("error", "Thanh toán không thành công: " + message);
        return "Home/payment-error";
    }
    
    /**
     * API nhận thông báo từ MoMo (IPN)
     */
    @PostMapping("/api/momo/ipn")
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleMomoIPN(@RequestBody Map<String, Object> ipnData) {
        String orderId = (String) ipnData.get("orderId");
        String requestId = (String) ipnData.get("requestId");
        String amount = String.valueOf(ipnData.get("amount"));
        String resultCode = String.valueOf(ipnData.get("resultCode"));
        
        // Kiểm tra kết quả thanh toán
        if ("0".equals(resultCode)) {
            // Xác minh thanh toán
            boolean isValid = momoPaymentService.verifyPayment(orderId, requestId, amount);
            
            if (isValid) {
                // Cập nhật trạng thái đơn hàng
                orderService.updateOrderStatus(orderId, "Đã thanh toán");
                return ResponseEntity.ok(Map.of("message", "Thanh toán thành công"));
            }
        }
        
        return ResponseEntity.ok(Map.of("message", "Thanh toán không thành công"));
    }
    
    /**
     * Xử lý trường hợp người dùng truy cập vào momo-payment.html trực tiếp
     * Đây là một fallback để xử lý lỗi 404
     */
    @GetMapping("/momo-payment.html")
    public String handleLegacyMomoPaymentUrl(@RequestParam(value = "orderId", required = false) String orderCode,
                                           @RequestParam(value = "amount", required = false) String amount,
                                           Model model,
                                           HttpSession session) {
        // Chuyển hướng đến endpoint mới
        if (orderCode != null) {
            return "redirect:/momo-payment?orderId=" + orderCode + (amount != null ? "&amount=" + amount : "");
        }
        
        // Nếu không có orderId, chuyển hướng về trang chủ
        return "redirect:/";
    }
}

