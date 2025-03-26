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
import poly.asm.Models.Order;
import poly.asm.Models.OrderDetail;
import poly.asm.Models.User;
import poly.asm.Services.CartService;
import poly.asm.Services.OrderService;

import java.util.List;
import java.util.Map;

@Controller
public class OrderController {
    @Autowired
    private CartService cartService;
    
    @Autowired
    private OrderService orderService;
    
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
        Map<String, Object> orderInfo = orderService.getOrderInfo(orderCode);
        if (orderInfo == null) {
            return "redirect:/";
        }
        
        model.addAttribute("order", orderInfo);
        
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
}