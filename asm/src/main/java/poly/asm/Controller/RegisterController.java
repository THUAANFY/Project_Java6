package poly.asm.Controller;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import poly.asm.Models.User;
import poly.asm.Services.MailService;
import poly.asm.DAO.UserDAO;
import poly.asm.DTO.UserRegisterDTO;

@Controller
public class RegisterController {
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private MailService mailService;

    // Lưu trữ tạm mã kích hoạt (trong thực tế, nên dùng database)
    private Map<String, String> activationStorage = new HashMap<>();

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userRegisterDTO", new UserRegisterDTO());
        return "register/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("userRegisterDTO") UserRegisterDTO userDTO, BindingResult bindingResult, Model model) {
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
    
        // Debug giá trị fullname
        System.out.println("Fullname từ form: " + userDTO.getFullname());
    
        // Tạo user mới từ DTO
        User user = new User();
        user.setId(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFullname(userDTO.getFullname());
        user.setPhone(userDTO.getPhone());
        user.setPassword(userDTO.getPassword()); // Nên mã hóa password trong thực tế
        user.setRole(false);
        user.setImage("/Images/default.png");
        user.setActivated(false);
    
        // Lưu user
        userDAO.save(user);
    
        // Debug sau khi lưu
        User savedUser = userDAO.findByIdOrEmail(user.getId());
        System.out.println("Fullname từ DB: " + savedUser.getFullname());
    
        // Tạo mã kích hoạt
        String activationCode = Base64.getEncoder().encodeToString(user.getId().getBytes());
        activationStorage.put(activationCode, user.getEmail());
    
        // Tạo liên kết kích hoạt
        String activationLink = "http://localhost:8080/activate?code=" + activationCode;
    
        // Gửi email chứa liên kết kích hoạt
        try {
            mailService.send("philtpd10207@gmail.com", userDTO.getEmail(), "Xác nhận đăng ký", 
                "Chúc mừng bạn đã đăng ký thành công! Vui lòng nhấn vào liên kết sau để kích hoạt tài khoản: " + activationLink);
        } catch (MessagingException e) {
            e.printStackTrace();
            model.addAttribute("error", "Không thể gửi email kích hoạt. Vui lòng thử lại sau!");
            return "register/register";
        }
        return "redirect:/register-success";
    }

    @GetMapping("/register-success")
    public String showRegisterSuccess(Model model) {
        model.addAttribute("message", "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.");
        model.addAttribute("messageType", "success");
        return "register/register-success";
    }

    @GetMapping("/activate")
    public String activateAccount(@RequestParam("code") String code, Model model) {
        String email = activationStorage.get(code);

        if (email != null) {
            User user = userDAO.findByIdOrEmail(email);
            if (user != null) {
                if (!user.isActivated()) {
                    user.setActivated(true);
                    userDAO.save(user);
                    activationStorage.remove(code); // Xóa mã kích hoạt sau khi dùng
                    model.addAttribute("message", "Kích hoạt tài khoản thành công! Vui lòng đăng nhập.");
                    model.addAttribute("messageType", "success");
                } else {
                    model.addAttribute("message", "Tài khoản đã được kích hoạt trước đó!");
                    model.addAttribute("messageType", "warning");
                }
            } else {
                model.addAttribute("message", "Tài khoản không tồn tại!");
                model.addAttribute("messageType", "error");
            }
        } else {
            // Thử giải mã code để kiểm tra
            try {
                String decodedUsername = new String(Base64.getDecoder().decode(code));
                User user = userDAO.findByIdOrEmail(decodedUsername);
                if (user != null && !user.isActivated()) {
                    user.setActivated(true);
                    userDAO.save(user);
                    activationStorage.remove(code);
                    model.addAttribute("message", "Kích hoạt tài khoản thành công! Vui lòng đăng nhập.");
                    model.addAttribute("messageType", "success");
                } else if (user != null) {
                    model.addAttribute("message", "Tài khoản đã được kích hoạt trước đó!");
                    model.addAttribute("messageType", "warning");
                } else {
                    model.addAttribute("message", "Tài khoản không tồn tại!");
                    model.addAttribute("messageType", "error");
                }
            } catch (IllegalArgumentException e) {
                model.addAttribute("message", "Liên kết kích hoạt không hợp lệ!");
                model.addAttribute("messageType", "error");
            }
        }
        return "register/activation-success";
    }
}