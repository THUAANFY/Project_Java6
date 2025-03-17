package poly.asm.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ThongtinuserController {
    @GetMapping("/thongtinuser")
    public String thongtinuser( Model model ) {
        return "Home/thongtinuser";
    }
    
}
