package poly.asm.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ManageCategoryController {
    @GetMapping("/managecategory")
    public String managecategory(Model model) {
        return "Admin/ManageCategory";
    }
    
}
