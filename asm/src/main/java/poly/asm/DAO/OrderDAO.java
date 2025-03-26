package poly.asm.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import poly.asm.Models.Order;
import poly.asm.Models.User;

@Repository
public interface OrderDAO extends JpaRepository<Order, Integer>{
    List<Order> findByUser(User user);
    Order findByOrderCode(String orderCode);
    List<Order> findByUserOrderByCreatedAtDesc(User user);
}
