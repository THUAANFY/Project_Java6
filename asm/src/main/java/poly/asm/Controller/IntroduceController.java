package poly.asm.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import poly.asm.Models.User;


@Controller
public class IntroduceController {
    @GetMapping("/Introduce")
    public String Introduce(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        String imagePath = (loggedInUser != null) ? loggedInUser.getImage() : "/user.png";
        
        model.addAttribute("image", imagePath);
        return "Home/introduce";
    }
    
}
