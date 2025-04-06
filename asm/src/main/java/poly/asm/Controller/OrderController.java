package poly.asm.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import poly.asm.Models.CartItem;
import poly.asm.Models.Order;
import poly.asm.Models.OrderDetail;
import poly.asm.Models.User;
import poly.asm.Models.Voucher;
import poly.asm.Services.CartService;
import poly.asm.Services.OrderService;
import poly.asm.Services.VoucherService;
import poly.asm.Utils.ShippingCalculator;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class OrderController {
    @Autowired
    private CartService cartService;
    
    @Autowired
    private OrderService orderService;

    @Autowired
    private VoucherService voucherService;
    
    /**
     * Hiển thị trang thanh toán
     */
    @GetMapping("/order/pay")
    public String showCheckoutPage(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        // Nếu user null, chuyển hướng đến trang đăng nhập
        if (loggedInUser == null) {
            model.addAttribute("loginMessage", "Vui lòng đăng nhập để tiếp tục thanh toán");
            return "Home/checkout";
        }
        
        // Kiểm tra giỏ hàng có trống không
        if (cartService.getCartItems().isEmpty()) {
            return "redirect:/yourcart";
        }

        // Lấy tổng tiền từ service thay vì tính toán thủ công
        double totalPrice = cartService.getTotalPrice();
        
        // Thêm thông tin giỏ hàng vào model
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        
        // Thêm thông tin người dùng vào model
        model.addAttribute("fullname", loggedInUser.getFullname());
        model.addAttribute("phone", loggedInUser.getPhone());
        model.addAttribute("province", loggedInUser.getProvince());
        model.addAttribute("district", loggedInUser.getDistrict());
        model.addAttribute("ward", loggedInUser.getWard());
        model.addAttribute("address", loggedInUser.getAddress());
        model.addAttribute("image", loggedInUser.getImage());

        // Lấy danh sách voucher khả dụng cho người dùng
        List<Voucher> availableVouchers = voucherService.getAllAvailableVouchers();

        // Sử dụng biến final cho lambda expression
        final double finalTotalPrice = totalPrice;
        
        // Lọc voucher theo điều kiện đơn hàng tối thiểu
        List<Voucher> applicableVouchers = availableVouchers.stream()
                .filter(v -> v.canBeApplied(finalTotalPrice))
                .collect(Collectors.toList());
        
        model.addAttribute("availableVouchers", applicableVouchers);
        
        // Thêm thông tin thời gian giao hàng dự kiến nếu có địa chỉ
        if (loggedInUser.getProvince() != null && !loggedInUser.getProvince().isEmpty()) {
            int shippingDays = ShippingCalculator.calculateShippingDays(loggedInUser.getProvince());
            String deliveryTimeRange = ShippingCalculator.getDeliveryTimeRangeText(shippingDays);
            model.addAttribute("shippingDays", shippingDays);
            model.addAttribute("deliveryTimeRange", deliveryTimeRange);
        }
        
        return "Home/checkout";
    }
    
    /**
     * Tạo đơn hàng mới
     */
    @PostMapping("/order/create")
    @ResponseBody
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> orderData, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            throw new RuntimeException("Vui lòng đăng nhập để đặt hàng");
        }
        
        // Kiểm tra giỏ hàng có trống không
        if (cartService.getCartItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng của bạn đang trống");
        }
        
        // Gọi service để tạo đơn hàng
        String orderCode = orderService.createOrder(orderData, loggedInUser);
        
        // Xóa giỏ hàng sau khi đặt hàng thành công
        cartService.clearCart();
        
        // Trả về mã đơn hàng để chuyển hướng
        return Map.of("orderId", orderCode);
    }
    
    /**
     * Hiển thị trang hoàn tất đơn hàng
     */
    @GetMapping("/order/complete")
    public String showOrderCompletePage(@RequestParam("id") String orderCode, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        
        // Lấy thông tin đơn hàng
        // Map<String, Object> orderInfo = orderService.getOrderInfo(orderCode);
        // if (orderInfo == null) {
        //     return "redirect:/";
        // }
        Order order = orderService.getOrderByCode(orderCode);
        model.addAttribute("order", order);
        // model.addAttribute("order", orderInfo);

        
        // Thêm thông tin thời gian giao hàng dự kiến
        // Order order = orderService.getOrderByCode(orderCode);
        if (order != null) {
            Date estimatedDeliveryDate = orderService.calculateEstimatedDeliveryDate(order);
            if (estimatedDeliveryDate != null) {
                model.addAttribute("estimatedDeliveryDate", estimatedDeliveryDate);
            }
            
            int shippingDays = orderService.getShippingDays(order);
            String deliveryTimeRange = orderService.getDeliveryTimeRangeText(order);
            model.addAttribute("shippingDays", shippingDays);
            model.addAttribute("deliveryTimeRange", deliveryTimeRange);
        }
        
        return "Home/order-complete";
    }
    
    /**
     * Hiển thị danh sách đơn hàng của người dùng
     */
    @GetMapping("/orders")
    public String showUserOrders(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        String imagePath = (loggedInUser != null) ? loggedInUser.getImage() : "/user.png";
        model.addAttribute("image", imagePath);
        // Lấy danh sách đơn hàng của người dùng
        List<Order> userOrders = orderService.getUserOrders(loggedInUser);
        model.addAttribute("orders", userOrders);
        
        return "Home/orders";
    }
    
    /**
     * Hiển thị chi tiết đơn hàng
     */
    @GetMapping("/order/detail")
    public String showOrderDetail(@RequestParam("id") String orderCode, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        
        // Lấy thông tin đơn hàng
        Order order = orderService.getOrderByCode(orderCode);
        if (order == null || !order.getUser().getId().equals(loggedInUser.getId())) {
            return "redirect:/orders";
        }
        
        String imagePath = (loggedInUser != null) ? loggedInUser.getImage() : "/user.png";
        model.addAttribute("image", imagePath);
        model.addAttribute("order", order);
        
        // Calculate and add estimated delivery date
        Date estimatedDeliveryDate = orderService.calculateEstimatedDeliveryDate(order);
        model.addAttribute("estimatedDeliveryDate", estimatedDeliveryDate);
        
        // Calculate shipping days for display
        int shippingDays = orderService.getShippingDays(order);
        model.addAttribute("shippingDays", shippingDays);
        
        // Get delivery time range text
        String deliveryTimeRange = orderService.getDeliveryTimeRangeText(order);
        model.addAttribute("deliveryTimeRange", deliveryTimeRange);
        
        // Add location information for orders in shipping status
        if ("Đang giao hàng".equals(order.getStatus())) {
            Map<String, Object> locationInfo = orderService.getCurrentOrderLocation(order);
            model.addAttribute("locationInfo", locationInfo);
        }
        
        return "Home/order-detail";
    }
    
    /**
     * Hủy đơn hàng
     */
    @PostMapping("/order/cancel")
    public String cancelOrder(@RequestParam("id") String orderCode, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        
        boolean success = orderService.cancelOrder(orderCode, loggedInUser);
        
        if (success) {
            redirectAttributes.addFlashAttribute("message", "Đơn hàng đã được hủy thành công");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể hủy đơn hàng này");
        }
        
        return "redirect:/order/detail?id=" + orderCode;
    }

    // Add this method to handle the "Received Order" button click
    /**
     * Xử lý khi người dùng xác nhận đã nhận được hàng
     */
    @PostMapping("/order/received")
    public String markOrderAsReceived(@RequestParam("orderCode") String orderCode, 
                                     HttpSession session, 
                                     RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        
        boolean success = orderService.markOrderAsReceived(orderCode, loggedInUser);
        
        if (success) {
            redirectAttributes.addFlashAttribute("message", "Cảm ơn bạn đã xác nhận! Đơn hàng đã được đánh dấu là đã giao thành công.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể cập nhật trạng thái đơn hàng. Vui lòng thử lại sau.");
        }
        
        return "redirect:/order/detail?id=" + orderCode;
    }
    
    /**
     * API để cập nhật trạng thái đơn hàng (dành cho admin)
     */
    @PostMapping("/api/order/update-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@RequestBody Map<String, String> data, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRole()) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Không có quyền truy cập"));
        }
        
        String orderCode = data.get("orderCode");
        String status = data.get("status");
        
        boolean success = orderService.updateOrderStatus(orderCode, status);
        
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật trạng thái đơn hàng thành công"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không thể cập nhật trạng thái đơn hàng"));
        }
    }
    
    /**
     * API endpoint to get shipping time estimate
     */
    @GetMapping("/api/shipping/estimate")
    @ResponseBody
    public Map<String, Object> getShippingEstimate(@RequestParam("province") String province) {
        int shippingDays = ShippingCalculator.calculateShippingDays(province);
        String deliveryTimeRange = ShippingCalculator.getDeliveryTimeRangeText(shippingDays);
        
        // Calculate estimated delivery date based on current date
        Date currentDate = new Date();
        Date estimatedDeliveryDate = ShippingCalculator.calculateEstimatedDeliveryDate(currentDate, province);
        
        Map<String, Object> response = new HashMap<>();
        response.put("shippingDays", shippingDays);
        response.put("deliveryTimeRange", deliveryTimeRange);
        
        if (estimatedDeliveryDate != null) {
            response.put("estimatedDeliveryDate", estimatedDeliveryDate.getTime());
        }
        
        return response;
    }

    /**
     * API kiểm tra trạng thái thanh toán
     */
    @GetMapping("/api/order/check-payment-status")
    @ResponseBody
    public Map<String, String> checkPaymentStatus(@RequestParam("orderCode") String orderCode) {
        Order order = orderService.getOrderByCode(orderCode);
        if (order != null) {
            return Map.of("status", order.getStatus());
        }
        return Map.of("status", "unknown");
    }
    
    /**
     * API endpoint to get order tracking information
     */
    @GetMapping("/api/order/tracking")
    @ResponseBody
    public Map<String, Object> getOrderTracking(@RequestParam("orderCode") String orderCode) {
        Order order = orderService.getOrderByCode(orderCode);
        Map<String, Object> response = new HashMap<>();

        if (order != null && "Đang giao hàng".equals(order.getStatus())) {
            Map<String, Object> locationInfo = orderService.getCurrentOrderLocation(order);
            response.put("locationInfo", locationInfo);
        }

        return response;
    }
}

