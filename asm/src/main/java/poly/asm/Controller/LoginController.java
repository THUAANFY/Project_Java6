package poly.asm.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import poly.asm.Models.User;
import poly.asm.DAO.UserDAO;
import poly.asm.DTO.UserLoginDTO;

import java.io.File;

@Controller
public class LoginController {

    @Autowired
    private UserDAO userDAO; // Inject UserDAO để truy vấn database

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        // Khởi tạo UserLoginDTO để bind với form
        if (!model.containsAttribute("userLoginDTO")) {
            model.addAttribute("userLoginDTO", new UserLoginDTO());
        }
        return "login/login";
    }

    @PostMapping("/login")
    public String processLogin(
            @Valid @ModelAttribute("userLoginDTO") UserLoginDTO userLoginDTO,
            BindingResult result,
            @RequestParam(value = "remember_me", defaultValue = "false") boolean rememberMe,
            Model model,
            HttpSession session) {

        // Kiểm tra lỗi validation từ UserLoginDTO (để trống hoặc sai định dạng email)
        if (result.hasErrors()) {
            System.out.println("Validation errors: " + result.getAllErrors());
            return "login/login"; // Trả về form login nếu có lỗi
        }

        String id = userLoginDTO.getId();
        User user = userDAO.findByIdOrEmail(id);

        // Kiểm tra user tồn tại
        if (user == null) {
            model.addAttribute("error", "Tài khoản không tồn tại!");
            System.out.println("User not found for: " + id);
            return "login/login";
        }

        // Kiểm tra mật khẩu
        if (!user.getPassword().equals(userLoginDTO.getPassword())) {
            model.addAttribute("error", "Mật khẩu không đúng!");
            System.out.println("Password incorrect for user: " + id);
            return "login/login";
        }

        // Kiểm tra tài khoản đã kích hoạt chưa (nếu có thuộc tính isActivated trong User)
        if (!user.isActivated()) {
            model.addAttribute("error", "Tài khoản chưa được kích hoạt!");
            return "login/login";
        }

        // Đăng nhập thành công
        session.setAttribute("loggedInUser", user);
        
        // Kiểm tra thư mục avatar tồn tại
        checkAvatarDirectory();
        
        // Kiểm tra xem người dùng đã có địa chỉ chưa
        if (user.getAddress() == null || user.getAddress().trim().isEmpty()) {
            // Kiểm tra xem đã hiển thị thông báo cập nhật địa chỉ chưa
            Boolean addressPromptShown = (Boolean) session.getAttribute("addressPromptShown_" + user.getId());
            
            // Nếu chưa hiển thị thông báo (null hoặc false)
            if (addressPromptShown == null || !addressPromptShown) {
                // Đánh dấu đã hiển thị thông báo
                session.setAttribute("addressPromptShown_" + user.getId(), true);
                
                // Thêm thông báo yêu cầu cập nhật địa chỉ
                session.setAttribute("message", "Vui lòng cập nhật địa chỉ của bạn để tiếp tục sử dụng dịch vụ.");
                
                // Chuyển hướng đến trang cập nhật thông tin cá nhân
                return "redirect:/profile";
            }
        }
        
        model.addAttribute("success", "Đăng nhập thành công! Chào mừng, " + user.getFullname());
        return "redirect:/index"; // Chuyển hướng đến trang index
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, Model model) {
        session.invalidate();
        model.addAttribute("success", "Bạn đã đăng xuất thành công!");
        return "redirect:/index";
    }
    
    /**
     * Kiểm tra và tạo thư mục avatar nếu chưa tồn tại
     */
    private void checkAvatarDirectory() {
        String uploadDir = "src/main/resources/static/images/avatars/";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}

