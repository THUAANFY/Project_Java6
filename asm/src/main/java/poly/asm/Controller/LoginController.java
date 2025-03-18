package poly.asm.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import poly.asm.Models.User;
import poly.asm.DAO.UserRepository;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String login(Model model) {
        return "login/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam("idOrEmail") String idOrEmail,
            @RequestParam("password") String password,
            @RequestParam(value = "remember_me", defaultValue = "false") boolean rememberMe,
            Model model,
            HttpSession session) { // Thêm HttpSession vào đây
        User user = userRepository.findByIdOrEmail(idOrEmail);
        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "Invalid username/email or password!");
            return "login/login";
        }
        if (!user.isActivated()) {
            model.addAttribute("error", "Account is not activated!");
            return "login/login";
        }
        session.setAttribute("loggedInUser", user);
        model.addAttribute("success", "Login successful! Welcome, " + user.getFullname());
        return "redirect:/index";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, Model model) {
        session.invalidate();
        model.addAttribute("success", "You have been logged out successfully!");
        return "redirect:/login";
    }
}