package poly.asm.DAO;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import poly.asm.Models.Order;
import poly.asm.Models.User;

@Repository
public interface OrderDAO extends JpaRepository<Order, Integer>{
    List<Order> findByUser(User user);
    Order findByOrderCode(String orderCode);
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    // Tìm đơn hàng theo trạng thái
    List<Order> findByStatus(String status);
    
    // Tìm đơn hàng theo phương thức thanh toán
    List<Order> findByPaymentMethod(String paymentMethod);
    
    // Tìm đơn hàng theo khoảng thời gian
    List<Order> findByCreatedAtBetween(Date startDate, Date endDate);
    
    // Tìm đơn hàng theo tên người nhận hoặc số điện thoại
    List<Order> findByRecipientNameContainingOrRecipientPhoneContaining(String name, String phone);

    // Phân trang và tìm kiếm
    Page<Order> findByOrderCodeContainingOrRecipientNameContainingOrRecipientPhoneContaining(
            String orderCode, String name, String phone, Pageable pageable);
    
    // Đếm số lượng đơn hàng theo trạng thái
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = ?1")
    Long countByStatus(String status);
    
    // Tìm đơn hàng theo nhiều điều kiện
    @Query("SELECT o FROM Order o WHERE " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod) AND " +
            "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR o.createdAt <= :endDate) AND " +
            "(:search IS NULL OR o.orderCode LIKE %:search% OR o.recipientName LIKE %:search% OR o.recipientPhone LIKE %:search%)")
    Page<Order> findOrdersWithFilters(
            String status, 
            String paymentMethod, 
            Date startDate, 
            Date endDate, 
            String search, 
            Pageable pageable);
}
