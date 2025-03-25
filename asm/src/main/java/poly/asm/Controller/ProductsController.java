package poly.asm.Controller;

import java.util.List;
// import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import poly.asm.DAO.CategoryDAO;
import poly.asm.DAO.ProductDAO;
import poly.asm.Models.Category;
import poly.asm.Models.Product;

@Controller
public class ProductsController {

    @Autowired
    CategoryDAO categoryDAO;

    @Autowired
    ProductDAO productDAO;

    @ResponseBody
    @GetMapping("/api/categories")
    public List<Category> getCategories() {
        return categoryDAO.findAll();
    }

    @GetMapping("/products")
    public String products(Model model) {
        List<Category> categories = categoryDAO.findAll();
        model.addAttribute("categories", categories);
        return "Home/products";
    }

    @GetMapping("/product/{id}")
    // public String productDetails(@PathVariable("id") Integer id, Model model) {
    //     // Lấy thông tin sản phẩm hiện tại
    //     Product product = productDAO.findById(id)
    //             .orElseThrow(() -> new IllegalArgumentException("Invalid product Id: " + id));

    //     // Lấy danh sách tất cả danh mục
    //     List<Category> categories = categoryDAO.findAll();

    //     // Lấy danh sách sản phẩm liên quan từ cùng danh mục (trừ sản phẩm hiện tại)
    //     List<Product> relatedProducts = productDAO.findByCategory(product.getCategory())
    //             .stream()
    //             .filter(p -> !p.getId().equals(id)) // Loại bỏ sản phẩm hiện tại
    //             .collect(Collectors.toList()); // Bỏ limit(4) để hiển thị tất cả

    //     // Thêm các thuộc tính vào model
    //     model.addAttribute("product", product);
    //     model.addAttribute("categories", categories);
    //     model.addAttribute("relatedProducts", relatedProducts);

    //     return "Home/product-details";
    // }
    public String detail(Model model, @PathVariable("id") Integer id) {
        Product product = productDAO.findById(id).orElse(null);
        if (product == null){
            return "redirect:/products";
        }

        // Lấy danh mục của sản phẩm hiện tại
        Category category = product.getCategory();
        // Lấy các sản phẩm liên quan (cùng danh mục)
        List<Product> relatedProducts = productDAO.findByCategory(category);
        model.addAttribute("relatedProducts", relatedProducts);

        model.addAttribute("product", product);
        return "/Home/product-details";
    } 
}