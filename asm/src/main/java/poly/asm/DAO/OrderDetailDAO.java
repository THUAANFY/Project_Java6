package poly.asm.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import poly.asm.Models.Order;
import poly.asm.Models.OrderDetail;

@Repository
public interface OrderDetailDAO extends JpaRepository<OrderDetail, Integer>{
    List<OrderDetail> findByOrder(Order order);
}
