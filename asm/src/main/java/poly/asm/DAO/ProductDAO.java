package poly.asm.DAO;

import org.springframework.data.jpa.repository.JpaRepository;

import poly.asm.Models.Product;

public interface ProductDAO extends JpaRepository<Product, Integer> {

    
}
