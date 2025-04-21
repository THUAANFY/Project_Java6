package poly.asm.DAO;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.asm.Models.PersistentCartItem;

@Repository
public interface PersistentCartItemDAO extends JpaRepository<PersistentCartItem, Integer> {
    List<PersistentCartItem> findByUserId(String userId);
    PersistentCartItem findByUserIdAndProductId(String userId, Integer productId);
    void deleteByUserIdAndProductId(String userId, Integer productId);
    void deleteByUserId(String userId);
}
