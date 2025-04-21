package poly.asm.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;
import poly.asm.DAO.CategoryDAO;
import poly.asm.DAO.ProductDAO;
import poly.asm.Models.Category;
import poly.asm.Models.Product;
import poly.asm.Models.User;

@Controller
public class HomeController {

    @Autowired
    ProductDAO productDAO;

    @Autowired
    CategoryDAO categoryDAO; // Thêm CategoryDAO để lấy danh sách danh mục

    @ResponseBody
    @GetMapping("/pro")
    public List<Product> prod(Model model) {
        List<Product> products = productDAO.findAll();
        return products;
    }

    @GetMapping("/index")
    public String index(Model model, HttpSession session) {
        // Lấy danh sách danh mục
        List<Category> categories = categoryDAO.findAll();

        // Map để lưu sản phẩm mới nhất theo danh mục
        Map<Category, List<Product>> categoryProductsMap = new HashMap<>();
        
        // Lấy 4 sản phẩm mới nhất cho mỗi danh mục
        Pageable pageable = PageRequest.of(0, 4); // Lấy 4 sản phẩm
        for (Category category : categories) {
            List<Product> newProducts = productDAO.findTopByCategoryId(category.getId(), pageable);
            categoryProductsMap.put(category, newProducts);
        }

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        String imagePath = (loggedInUser != null) ? loggedInUser.getImage() : "/user.png";

        model.addAttribute("image", imagePath);
        model.addAttribute("categoryProductsMap", categoryProductsMap); // Truyền map vào model
        model.addAttribute("categories", categories);
        return "Home/index";
    }
    
}