package poly.asm.Controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    
    private static final long MIN_AMOUNT = 1000; // 1,000 VND
    private static final long MAX_AMOUNT = 50000000; // 50,000,000 VND

    @GetMapping("/momo-payment")
    public String redirectToMomoPayment(@RequestParam("orderId") String orderCode, 
                                        @RequestParam(value = "amount", required = false) Double amount,
                                        HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        
        Order order = orderService.getOrderByCode(orderCode);
        if (order == null) {
            return redirectWithEncodedError("Không tìm thấy đơn hàng");
        }
        
        if (!order.getUser().getId().equals(loggedInUser.getId())) {
            return redirectWithEncodedError("Bạn không có quyền truy cập đơn hàng này");
        }
        
        double paymentAmount = (amount != null) ? amount : order.getTotal();
        
        if (paymentAmount < MIN_AMOUNT || paymentAmount > MAX_AMOUNT) {
            return redirectWithEncodedError("Số tiền giao dịch phải từ 1,000 VND đến 50,000,000 VND");
        }
        
        Map<String, Object> paymentResult;
        if (amount != null) {
            paymentResult = momoPaymentService.createPaymentRequest(order, amount);
        } else {
            paymentResult = momoPaymentService.createPaymentRequest(order);
        }
        
        if (Boolean.TRUE.equals(paymentResult.get("success"))) {
            String payUrl = (String) paymentResult.get("payUrl");
            if (payUrl != null && !payUrl.isEmpty() && (payUrl.startsWith("http://") || payUrl.startsWith("https://"))) {
                return "redirect:" + payUrl; // Chuyển hướng đến trang nhập thông tin thẻ
            }
        }
        
        return redirectWithEncodedError("Không thể tạo yêu cầu thanh toán MoMo");
    }
    
    @GetMapping("/order/momo-return")
public String handleMomoReturn(@RequestParam(required = false) String orderId,
                               @RequestParam(required = false) String requestId,
                               @RequestParam(required = false) String amount,
                               @RequestParam(required = false) String resultCode,
                               @RequestParam(required = false) String message,
                               Model model) {
    System.out.println("MoMo return - orderId: " + orderId + ", resultCode: " + resultCode + ", message: " + message);

    if ("0".equals(resultCode) && orderId != null) {
        Order order = orderService.getOrderByCode(orderId);
        if (order != null) {
            orderService.updateOrderStatus(orderId, "Đã thanh toán");
            model.addAttribute("order", order);
            return "home/order-complete"; // Chuyển đến trang order-complete.html
        } else {
            return redirectWithEncodedError("Không tìm thấy đơn hàng với mã: " + orderId);
        }
    }
    

    String errorMessage = message != null ? message : "Thanh toán thất bại hoặc bị hủy.";
    System.out.println("Payment failed - resultCode: " + resultCode + ", message: " + errorMessage);
    return redirectWithEncodedError("Thanh toán không thành công: " + errorMessage);
}
    
    
    
    @PostMapping("/api/momo/ipn")
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleMomoIPN(@RequestBody Map<String, Object> ipnData) {
        String orderId = (String) ipnData.get("orderId");
        String requestId = (String) ipnData.get("requestId");
        String amount = String.valueOf(ipnData.get("amount"));
        String resultCode = String.valueOf(ipnData.get("resultCode"));
        
        if ("0".equals(resultCode)) {
            boolean isValid = momoPaymentService.verifyPayment(orderId, requestId, amount);
            if (isValid) {
                orderService.updateOrderStatus(orderId, "Đã thanh toán");
                return ResponseEntity.ok(Map.of("message", "Thanh toán thành công"));
            }
        }
        
        return ResponseEntity.ok(Map.of("message", "Thanh toán không thành công"));
    }

    private String redirectWithEncodedError(String errorMessage) {
        try {
            String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString());
            return "redirect:/Home/payment-error?error=" + encodedError;
        } catch (Exception e) {
            return "redirect:/Home/payment-error?error=Unknown%20error";
        }
    }

    @GetMapping("/order-complete")
    public String showOrderComplete(@RequestParam("orderId") String orderCode, Model model) {
        Order order = orderService.getOrderByCode(orderCode);
        if (order == null) {
            System.out.println("Order not found for orderCode: " + orderCode);
            return redirectWithEncodedError("Không tìm thấy đơn hàng");
        }
        model.addAttribute("order", order);
        System.out.println("Showing order-complete page for orderCode: " + orderCode);
        return "home/order-complete";
    }

    @GetMapping("/Home/payment-error")
public String showPaymentError(@RequestParam("error") String errorMessage, Model model) {
    model.addAttribute("errorMessage", errorMessage);
    return "home/payment-error"; // View trong templates/home/payment-error.html
}

}