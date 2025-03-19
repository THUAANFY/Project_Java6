package poly.asm.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {

    @GetMapping("/index")
    public String index(Model model) {
        return "Home/index";
    }
    
    @GetMapping("/changepassword")
    public String changePassword(Model model) {
        return "Home/changepass";
    } 
}
