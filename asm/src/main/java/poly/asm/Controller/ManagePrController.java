package poly.asm.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ManagePrController {
    @GetMapping("/manageproduct")
    public String manageproduct(Model model) {
        return "Admin/ManageProduct";
    }
    
}
