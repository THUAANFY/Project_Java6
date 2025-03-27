package poly.asm.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import poly.asm.DAO.OrderDAO;
import poly.asm.DAO.OrderDetailDAO;
import poly.asm.Models.Order;
import poly.asm.Models.OrderDetail;
import poly.asm.Services.OrderService;

@Controller
public class ManageOrder {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderDAO orderDAO;
    
    @Autowired
    private OrderDetailDAO orderDetailDAO;
    
    @GetMapping("/manageorder")
    public String manageorder(Model model) {
        // Lấy tất cả đơn hàng
        List<Order> orders = orderDAO.findAll();
        model.addAttribute("orders", orders);
        
        // Đếm số lượng đơn hàng theo trạng thái
        long pendingCount = orders.stream().filter(o -> "Chờ xác nhận".equals(o.getStatus())).count();
        long confirmedCount = orders.stream().filter(o -> "Đã xác nhận".equals(o.getStatus())).count();
        long shippingCount = orders.stream().filter(o -> "Đang giao hàng".equals(o.getStatus())).count();
        long deliveredCount = orders.stream().filter(o -> "Đã giao hàng".equals(o.getStatus())).count();
        long cancelledCount = orders.stream().filter(o -> "Đã hủy".equals(o.getStatus())).count();
        
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("shippingCount", shippingCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("totalCount", orders.size());
        
        return "Admin/ManageOrder";
    }
    
    @GetMapping("/manageorder/detail/{orderCode}")
    public String orderDetail(@PathVariable("orderCode") String orderCode, Model model) {
        Order order = orderService.getOrderByCode(orderCode);
        
        if (order != null) {
            List<OrderDetail> orderDetails = orderDetailDAO.findByOrder(order);
            model.addAttribute("order", order);
            model.addAttribute("orderDetails", orderDetails);
            return "Admin/OrderDetail";
        } else {
            return "redirect:/manageorder";
        }
    }
    
    @PostMapping("/manageorder/update-status")
    public String updateOrderStatus(
            @RequestParam("orderCode") String orderCode,
            @RequestParam("status") String status,
            @RequestParam("note") String note,
            RedirectAttributes redirectAttributes) {
        
        boolean updated = orderService.updateOrderStatus(orderCode, status);
        
        if (updated) {
            redirectAttributes.addFlashAttribute("message", "Cập nhật trạng thái đơn hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng!");
        }
        
        return "redirect:/manageorder";
    }
    
    @PostMapping("/manageorder/delete")
    public String deleteOrder(
            @RequestParam("orderCode") String orderCode,
            RedirectAttributes redirectAttributes) {
        
        Order order = orderService.getOrderByCode(orderCode);
        
        if (order != null) {
            try {
                // Xóa chi tiết đơn hàng trước
                List<OrderDetail> orderDetails = orderDetailDAO.findByOrder(order);
                orderDetailDAO.deleteAll(orderDetails);
                
                // Sau đó xóa đơn hàng
                orderDAO.delete(order);
                
                redirectAttributes.addFlashAttribute("message", "Xóa đơn hàng thành công!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa đơn hàng: " + e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng!");
        }
        
        return "redirect:/manageorder";
    }
}
