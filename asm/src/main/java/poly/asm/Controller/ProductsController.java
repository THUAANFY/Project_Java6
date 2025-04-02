package poly.asm.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import poly.asm.DAO.CategoryDAO;
import poly.asm.DAO.ProductDAO;
import poly.asm.Models.Category;
import poly.asm.Models.Product;
import poly.asm.Models.User;
import poly.asm.Services.ProductService;

@Controller
public class ProductsController {

    @Autowired
    private CategoryDAO categoryDAO;

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public String allProducts(
            Model model,
            HttpSession session,
            @RequestParam(required = false) String search,        // Tham số tìm kiếm
            @RequestParam(defaultValue = "0") int page,          // Trang hiện tại
            @RequestParam(defaultValue = "12") int size) {       // Số sản phẩm mỗi trang
        // Tìm kiếm sản phẩm với phân trang
        Page<Product> productPage = productService.searchProductsByName(search, page, size);
        List<Category> categories = categoryDAO.findAll();

        // Lấy thông tin người dùng từ session
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        String imagePath = (loggedInUser != null) ? loggedInUser.getImage() : "/user.png";

        // Truyền dữ liệu sang giao diện
        model.addAttribute("image", imagePath);
        model.addAttribute("categories", categories);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("search", search); // Giữ từ khóa tìm kiếm

        if (productPage.isEmpty()) {
            model.addAttribute("message", "Không tìm thấy sản phẩm nào.");
        }

        return "Home/products";
    }

    @GetMapping("/products/category/{id}")
    public String productsByCategory(
            @PathVariable("id") String categoryId,
            Model model,
            HttpSession session,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Category category = categoryDAO.findById(categoryId).orElse(null);
        Page<Product> productPage;

        if (category != null) {
            if (search != null && !search.trim().isEmpty()) {
                productPage = productDAO.findByNameContainingIgnoreCaseAndCategory_Id(search, categoryId, PageRequest.of(page, size));
            } else {
                productPage = productDAO.findByCategory_Id(categoryId, PageRequest.of(page, size));
            }
        } else {
            productPage = productService.searchProductsByName(search, page, size);
        }

        List<Category> categories = categoryDAO.findAll();
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        String imagePath = (loggedInUser != null) ? loggedInUser.getImage() : "/user.png";

        model.addAttribute("image", imagePath);
        model.addAttribute("categories", categories);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("search", search);

        if (productPage.isEmpty()) {
            model.addAttribute("message", "Chưa có sản phẩm trong danh mục này hoặc không tìm thấy sản phẩm.");
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