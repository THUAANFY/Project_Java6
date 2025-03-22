package poly.asm.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import poly.asm.DAO.ProductDAO;
import poly.asm.Models.Product;


@Controller
public class HomeController {

    @Autowired
    ProductDAO productDAO;

    @ResponseBody
    @GetMapping("pro")
    public List<Product> prod(Model model){
        List<Product> products = productDAO.findAll();
        return products;
    }

    @GetMapping("/index")
    public String index(Model model) {
        List<Product> products = productDAO.findAll();
        model.addAttribute("products", products);
        return "Home/index";
    }
}
