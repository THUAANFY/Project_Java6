package poly.asm.DAO;

import org.springframework.data.jpa.repository.JpaRepository;

import poly.asm.Models.Category;

public interface CategoryDAO extends JpaRepository<Category, String> {

}
