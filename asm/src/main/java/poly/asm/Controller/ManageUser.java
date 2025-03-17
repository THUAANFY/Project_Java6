package poly.asm.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ManageUser {

    @GetMapping("/manageuser")
    public String manageUser(Model model){
        return "Admin/ManageUser";
    }

}
