package poly.asm.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ThongtinuserController {
    @GetMapping("/profileuser")
    public String thongtinuser( Model model ) {
        model.addAttribute("user", model);
        return "Home/thongtinuser";
    }
    
}
