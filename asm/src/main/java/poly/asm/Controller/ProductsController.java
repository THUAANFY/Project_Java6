package poly.asm.Controller;

import java.util.List;
// import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import poly.asm.DAO.CategoryDAO;
import poly.asm.DAO.ProductDAO;
import poly.asm.Models.Category;
import poly.asm.Models.Product;
import poly.asm.Models.User;

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
    public String products(Model model, HttpSession session) {
        List<Category> categories = categoryDAO.findAll();
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        // Nếu user null, dùng icon user mặc định
        String imagePath = (loggedInUser != null) ? loggedInUser.getImage() : "/user.png";
        model.addAttribute("image", imagePath);
        model.addAttribute("categories", categories);
        return "Home/products";
    }

    @GetMapping("/product/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, HttpSession session) {
        Product product = productDAO.findById(id).orElse(null);
        if (product == null){
            return "redirect:/products";
        }
        // Lấy danh mục của sản phẩm hiện tại
        Category category = product.getCategory();
        // Lấy các sản phẩm liên quan (cùng danh mục)
        List<Product> relatedProducts = productDAO.findByCategory(category);
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        // Nếu user null, dùng icon user mặc định
        String imagePath = (loggedInUser != null) ? loggedInUser.getImage() : "/user.png";
        model.addAttribute("image", imagePath);
        model.addAttribute("relatedProducts", relatedProducts);

        model.addAttribute("product", product);
        return "/Home/product-details";
    } 
}