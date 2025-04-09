package poly.asm.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import poly.asm.Models.Notification;
import poly.asm.Models.User;
import poly.asm.Services.NotificationService;

@Controller
@RequestMapping("/admin/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * API lấy tất cả thông báo
     */
    @GetMapping("/api/all")
    @ResponseBody
    public List<Notification> getAllNotifications(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRole()) {
            return List.of(); // Trả về danh sách rỗng nếu không phải admin
        }
        return notificationService.getAllNotifications();
    }
    
    /**
     * API lấy thông báo chưa đọc
     */
    @GetMapping("/api/unread")
    @ResponseBody
    public List<Notification> getUnreadNotifications(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRole()) {
            return List.of(); // Trả về danh sách rỗng nếu không phải admin
        }
        return notificationService.getUnreadNotifications();
    }
    
    /**
     * API đếm số thông báo chưa đọc
     */
    @GetMapping("/api/count-unread")
    @ResponseBody
    public Map<String, Long> countUnreadNotifications(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRole()) {
            return Map.of("count", 0L); // Trả về 0 nếu không phải admin
        }
        long count = notificationService.countUnreadNotifications();
        return Map.of("count", count);
    }
    
    /**
     * API đánh dấu thông báo đã đọc
     */
    @PostMapping("/api/mark-read/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> markAsRead(@PathVariable("id") Long notificationId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRole()) {
            return ResponseEntity.status(403).body(Map.of("success", false));
        }
        
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(Map.of("success", true));
    }
    
    /**
     * API đánh dấu tất cả thông báo đã đọc
     */
    @PostMapping("/api/mark-all-read")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> markAllAsRead(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRole()) {
            return ResponseEntity.status(403).body(Map.of("success", false));
        }
        
        notificationService.markAllAsRead();
        return ResponseEntity.ok(Map.of("success", true));
    }
    
    /**
     * Trang quản lý thông báo
     */
    @GetMapping("")
    public String showNotificationsPage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRole()) {
            return "redirect:/login";
        }
        
        List<Notification> notifications = notificationService.getAllNotifications();
        model.addAttribute("notifications", notifications);
        
        return "Admin/notifications";
    }
}