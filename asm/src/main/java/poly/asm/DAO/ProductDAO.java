package poly.asm.DAO;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import poly.asm.Models.Category;
import poly.asm.Models.Product;

public interface ProductDAO extends JpaRepository<Product, Integer> {
    List<Product> findByCategory(Category category);
    
    // Tìm kiếm theo tên
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    
    // Lọc theo danh mục
    Page<Product> findByCategory_Id(String categoryId, Pageable pageable);
    
    // Tìm kiếm và lọc theo danh mục
    Page<Product> findByNameContainingIgnoreCaseAndCategory_Id(String keyword, String categoryId, Pageable pageable);
    
    // Lọc theo tình trạng còn hàng
    Page<Product> findByAvailableGreaterThan(Integer minAvailable, Pageable pageable);
    
    // Truy vấn tùy chỉnh để lọc với nhiều điều kiện
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:inStock IS NULL OR (:inStock = true AND p.available > 0) OR (:inStock = false))")
    Page<Product> filterProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") String categoryId,
            @Param("inStock") Boolean inStock,
            Pageable pageable);

    // Lấy sản phẩm mới nhất theo danh mục, giới hạn số lượng
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId ORDER BY p.id DESC")
    List<Product> findTopByCategoryId(@Param("categoryId") String categoryId, Pageable pageable);
}