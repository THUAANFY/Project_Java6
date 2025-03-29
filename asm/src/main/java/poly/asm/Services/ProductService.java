package poly.asm.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import poly.asm.DAO.ProductDAO;
import poly.asm.Models.Product;

@Service
public class ProductService {
    @Autowired
    private ProductDAO productDAO;

    // Tìm kiếm sản phẩm theo tên với phân trang
    public Page<Product> searchProductsByName(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword == null || keyword.trim().isEmpty()) {
            return productDAO.findAll(pageable); // Trả về tất cả sản phẩm nếu không có từ khóa
        }
        return productDAO.findByNameContainingIgnoreCase(keyword, pageable);
    }
}