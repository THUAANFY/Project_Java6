package poly.asm.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import poly.asm.DAO.CategoryDAO;
import poly.asm.Models.Category;


@Controller
public class ProductsController {

    @Autowired
    CategoryDAO categoryDAO;

    @ResponseBody
    @GetMapping("/api/categories")
    public List<Category> getCategories() {
        return categoryDAO.findAll();
    }

    @GetMapping("/products")
    public String products(Model model) {
        List<Category> categories =  categoryDAO.findAll();
        model.addAttribute("categories", categories); 
        return "Home/products";
    }

}
