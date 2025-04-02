package poly.asm.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
        List<Product> products = productDAO.findAll();
        List<Category> categories = categoryDAO.findAll(); // Lấy danh sách danh mục

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        String imagePath = (loggedInUser != null) ? loggedInUser.getImage() : "/user.png";

        model.addAttribute("image", imagePath);
        model.addAttribute("products", products);
        model.addAttribute("categories", categories); // Truyền categories vào model
        return "Home/index";
    }
    
}