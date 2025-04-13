package poly.asm.Controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    public String manageorder(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "startDate", required = false) String startDateStr,
            @RequestParam(name = "endDate", required = false) String endDateStr,
            Model model) {
        
        List<Order> orders;
        
        // Xử lý lọc theo trạng thái và ngày
        if (status != null && !status.isEmpty()) {
            // Nếu có lọc theo trạng thái
            if ((startDateStr != null && !startDateStr.isEmpty()) && (endDateStr != null && !endDateStr.isEmpty())) {
                // Nếu có cả lọc theo ngày
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date startDate = dateFormat.parse(startDateStr);
                    Date endDate = dateFormat.parse(endDateStr);
                    
                    // Điều chỉnh endDate để bao gồm cả ngày kết thúc
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    calendar.setTime(endDate);
                    calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
                    calendar.add(java.util.Calendar.MILLISECOND, -1);
                    endDate = calendar.getTime();
                    
                    // Lọc theo cả trạng thái và khoảng thời gian
                    orders = orderDAO.findByStatusAndCreatedAtBetween(status, startDate, endDate);
                } catch (ParseException e) {
                    // Nếu có lỗi khi parse ngày, chỉ lọc theo trạng thái
                    orders = orderDAO.findByStatus(status);
                }
            } else {
                // Chỉ lọc theo trạng thái
                orders = orderDAO.findByStatus(status);
            }
        } else if ((startDateStr != null && !startDateStr.isEmpty()) && (endDateStr != null && !endDateStr.isEmpty())) {
            // Chỉ lọc theo khoảng thời gian
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date startDate = dateFormat.parse(startDateStr);
                Date endDate = dateFormat.parse(endDateStr);
                
                // Điều chỉnh endDate để bao gồm cả ngày kết thúc
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.setTime(endDate);
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
                calendar.add(java.util.Calendar.MILLISECOND, -1);
                endDate = calendar.getTime();
                
                orders = orderDAO.findByCreatedAtBetween(startDate, endDate);
            } catch (ParseException e) {
                // Nếu có lỗi khi parse ngày, lấy tất cả đơn hàng
                orders = orderDAO.findAll();
            }
        } else {
            // Không có bộ lọc, lấy tất cả đơn hàng
            orders = orderDAO.findAll();
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        
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
