package poly.asm.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import poly.asm.Models.Product;
import poly.asm.Models.ProductImage;

@Repository
public interface ProductImageDAO extends JpaRepository<ProductImage, Integer>{
    List<ProductImage> findByProduct(Product product);
    void deleteByProduct(Product product);
}
