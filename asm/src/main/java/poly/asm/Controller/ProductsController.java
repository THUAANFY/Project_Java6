package poly.asm.Controller;

import java.util.List;

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
    public String productDetails(@PathVariable("id") Integer id, Model model) {
        Product product = productDAO.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid product Id: " + id));
        List<Category> categories = categoryDAO.findAll();
        
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        return "Home/product-details";
    }
}