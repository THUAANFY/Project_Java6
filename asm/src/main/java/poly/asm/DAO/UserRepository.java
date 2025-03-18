package poly.asm.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import poly.asm.Models.User;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsById(String id);
    boolean existsByEmail(String email);
    @Query("SELECT u FROM User u WHERE u.id = ?1 OR u.email = ?1")
    User findByIdOrEmail(String idOrEmail);
}
