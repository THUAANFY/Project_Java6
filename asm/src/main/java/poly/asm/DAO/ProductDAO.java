package poly.asm.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import poly.asm.Models.Category;
import poly.asm.Models.Product;

public interface ProductDAO extends JpaRepository<Product, Integer> {

    List<Product> findByCategory(Category category);
}
