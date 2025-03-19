package poly.asm.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import poly.asm.Models.User;
import poly.asm.DAO.UserRepository;

@Controller
public class RegisterController {

    @Autowired
    private UserRepository userRepository;
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register/register";
    }
    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user,
                          @RequestParam("confirm_password") String confirmPassword,
                          Model model) {

        if (user.getId() == null || user.getId().trim().isEmpty() ||
            user.getEmail() == null || user.getEmail().trim().isEmpty() ||
            user.getFullname() == null || user.getFullname().trim().isEmpty() ||
            user.getPhone() == null ||
            user.getPassword() == null || user.getPassword().trim().isEmpty() ||
            confirmPassword == null || confirmPassword.trim().isEmpty()) {
            model.addAttribute("error", "All fields are required!");
            return "register/register";
        }
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            return "register/register";
        }
        if (userRepository.existsById(user.getId()) || userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Username or email already exists!");
            return "register/register";
        }
        user.setRole(false);
        user.setActivated(true);
        userRepository.save(user);
        model.addAttribute("success", "Registration successful! Please log in.");
        return "redirect:/login";
    }
}