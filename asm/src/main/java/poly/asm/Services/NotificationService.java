package poly.asm.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import poly.asm.DAO.NotificationDAO;
import poly.asm.Models.Notification;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationDAO notificationDAO;
    
    /**
     * Tạo thông báo đơn hàng mới
     */
    public Notification createOrderNotification(String orderCode, String customerName) {
        String message = "Đơn hàng mới #" + orderCode + " từ " + customerName + " cần xác nhận";
        Notification notification = new Notification(message, "ORDER_CREATED", orderCode, customerName);
        return notificationDAO.save(notification);
    }
    
    /**
     * Lấy tất cả thông báo
     */
    public List<Notification> getAllNotifications() {
        return notificationDAO.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * Lấy thông báo chưa đọc
     */
    public List<Notification> getUnreadNotifications() {
        // Cập nhật tên phương thức
        return notificationDAO.findByReadStatusOrderByCreatedAtDesc(false);
    }
    
    /**
     * Đếm số thông báo chưa đọc
     */
    public long countUnreadNotifications() {
        // Cập nhật tên phương thức
        return notificationDAO.countByReadStatus(false);
    }
    
    /**
     * Đánh dấu thông báo đã đọc
     */
    public void markAsRead(Long notificationId) {
        Notification notification = notificationDAO.findById(notificationId).orElse(null);
        if (notification != null) {
            // Cập nhật tên trường
            notification.setReadStatus(true);
            notificationDAO.save(notification);
        }
    }
    
    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    public void markAllAsRead() {
        // Cập nhật tên phương thức
        List<Notification> unreadNotifications = notificationDAO.findByReadStatusOrderByCreatedAtDesc(false);
        for (Notification notification : unreadNotifications) {
            // Cập nhật tên trường
            notification.setReadStatus(true);
        }
        notificationDAO.saveAll(unreadNotifications);
    }
}