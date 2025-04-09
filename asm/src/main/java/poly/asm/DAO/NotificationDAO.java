package poly.asm.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import poly.asm.Models.Notification;

@Repository
public interface NotificationDAO extends JpaRepository<Notification, Long> {
    // Cập nhật tên trường thành readStatus
    List<Notification> findByReadStatusOrderByCreatedAtDesc(boolean readStatus);
    List<Notification> findAllByOrderByCreatedAtDesc();
    long countByReadStatus(boolean readStatus);
}
