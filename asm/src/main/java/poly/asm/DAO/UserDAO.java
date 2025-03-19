package poly.asm.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import poly.asm.Models.User;

public interface UserDAO extends JpaRepository<User, String>{
    @Query("SELECT u FROM User u WHERE u.id = :idOrEmail OR u.email = :idOrEmail")
    User findByIdOrEmail(String idOrEmail);
}
