package poly.asm.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpSession;
import poly.asm.DAO.CategoryDAO;
import poly.asm.DAO.ProductDAO;
import poly.asm.Models.Category;
import poly.asm.Models.Product;
import poly.asm.Models.User;

@Controller
public class ProductsController {

    @Autowired
    private CategoryDAO categoryDAO;

    @Autowired
    private ProductDAO productDAO;

    @GetMapping("/products")
    public String allProducts(Model model, HttpSession session) {
        List<Product> products = productDAO.findAll();
        List<Category> categories = categoryDAO.findAll();
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        String imagePath = (loggedInUser != null) ? loggedInUser.getImage() : "/user.png";
        
        model.addAttribute("image", imagePath);
        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        
        return "Home/products";
    }

    @GetMapping("/products/category/{id}")
    public String productsByCategory(@PathVariable("id") String categoryId, Model model, HttpSession session) {
        Category category = categoryDAO.findById(categoryId).orElse(null);
        List<Product> products = (category != null) ? productDAO.findByCategory(category) : List.of();
        
        List<Category> categories = categoryDAO.findAll();
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        String imagePath = (loggedInUser != null) ? loggedInUser.getImage() : "/user.png";
        
        model.addAttribute("image", imagePath);
        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        
        if (products.isEmpty()) {
            model.addAttribute("message", "Chưa có sản phẩm trong danh mục này.");
        }
        
        return "Home/products";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") Integer id, Model model, HttpSession session) {
        Product product = productDAO.findById(id).orElse(null);
        if (product == null) {
            return "redirect:/products";
        }
        
        Category category = product.getCategory();
        List<Product> relatedProducts = productDAO.findByCategory(category);
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        String imagePath = (loggedInUser != null) ? loggedInUser.getImage() : "/user.png";
        
        model.addAttribute("image", imagePath);
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("product", product);
        
        return "Home/product-details";
    }
}
