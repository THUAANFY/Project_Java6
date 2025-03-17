package poly.asm.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ManageUser {

    @GetMapping("/manageUser")
    public String manageUser(Model model){
        model.addAttribute(null, model);
        return "Admin/ManageUser";
    }

}
