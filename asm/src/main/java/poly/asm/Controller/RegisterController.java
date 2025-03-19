package poly.asm.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import poly.asm.Models.User;
import poly.asm.DAO.UserDAO;
import poly.asm.DTO.UserRegisterDTO;

@Controller
public class RegisterController {

    // @Autowired
    // private UserRepository userRepository;
    // @GetMapping("/register")
    // public String showRegisterForm(Model model) {
    //     model.addAttribute("user", new User());
    //     return "register/register";
    // }
    // @PostMapping("/register")
    // public String register(@ModelAttribute("user") User user,
    //                       @RequestParam("confirm_password") String confirmPassword,
    //                       Model model) {

    //     if (user.getId() == null || user.getId().trim().isEmpty() ||
    //         user.getEmail() == null || user.getEmail().trim().isEmpty() ||
    //         user.getFullname() == null || user.getFullname().trim().isEmpty() ||
    //         user.getPhone() == null ||
    //         user.getPassword() == null || user.getPassword().trim().isEmpty() ||
    //         confirmPassword == null || confirmPassword.trim().isEmpty()) {
    //         model.addAttribute("error", "All fields are required!");
    //         return "register/register";
    //     }
    //     if (!user.getPassword().equals(confirmPassword)) {
    //         model.addAttribute("error", "Passwords do not match!");
    //         return "register/register";
    //     }
    //     if (userRepository.existsById(user.getId()) || userRepository.existsByEmail(user.getEmail())) {
    //         model.addAttribute("error", "Username or email already exists!");
    //         return "register/register";
    //     }
    //     user.setRole(false);
    //     user.setActivated(true);
    //     userRepository.save(user);
    //     model.addAttribute("success", "Registration successful! Please log in.");
    //     return "redirect:/login";
    // }

    @Autowired
    private UserDAO userDAO;  // Thay UserRepository thành UserDAO

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userRegisterDTO", new UserRegisterDTO());
        return "register/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("userRegisterDTO") UserRegisterDTO userDTO,BindingResult bindingResult, Model model) {

        // Kiểm tra validation từ DTO
        if (bindingResult.hasErrors()) {
            return "register/register";
        }

        // Kiểm tra password matching
        if (!userDTO.isPasswordMatching()) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Mật khẩu xác nhận không khớp");
            return "register/register";
        }

        // Kiểm tra username hoặc email đã tồn tại
        User existingUser = userDAO.findByIdOrEmail(userDTO.getUsername());
        if (existingUser != null) {
            if (existingUser.getId().equals(userDTO.getUsername())) {
                bindingResult.rejectValue("username", "error.username", "Tên tài khoản đã tồn tại");
            } else if (existingUser.getEmail().equals(userDTO.getEmail())) {
                bindingResult.rejectValue("email", "error.email", "Email đã tồn tại");
            }
            return "register/register";
        }

        // Tạo user mới từ DTO
        User user = new User();
        user.setId(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFullname(userDTO.getFullname());
        user.setPhone(userDTO.getPhone());
        user.setPassword(userDTO.getPassword());  // Nên mã hóa password trong thực tế
        user.setRole(false);
        user.setActivated(true);

        // Lưu user và chuyển hướng
        userDAO.save(user);
        return "redirect:/login?success=Registration successful! Please log in.";
    }
}