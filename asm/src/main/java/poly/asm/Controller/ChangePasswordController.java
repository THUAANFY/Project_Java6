package poly.asm.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import poly.asm.DAO.UserDAO;
import poly.asm.Models.User;
import jakarta.servlet.http.HttpSession;

@Controller
public class ChangePasswordController {

    @Autowired
    private UserDAO userDAO;

    // Hiển thị form đổi mật khẩu
    @GetMapping("/changepassword")
    public String showChangePasswordForm() {
        return "Home/changepass"; // Trả về tệp Home/changepass.html
    }

    // Xử lý yêu cầu đổi mật khẩu
    @PostMapping("/changepassword")
    public String changePassword(
            @RequestParam("username") String username,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session, // Thêm HttpSession để quản lý phiên
            Model model) {

        // Kiểm tra xem username có tồn tại không
        User user = userDAO.findByIdOrEmail(username);
        if (user == null) {
            model.addAttribute("error", "Tên tài khoản không tồn tại!");
            return "Home/changepass";
        }

        // Kiểm tra mật khẩu cũ
        if (!user.getPassword().equals(oldPassword)) { // Giả sử mật khẩu chưa mã hóa
            model.addAttribute("error", "Mật khẩu cũ không đúng!");
            return "Home/changepass";
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            return "Home/changepass";
        }

        // Cập nhật mật khẩu mới
        user.setPassword(newPassword); // Nên mã hóa mật khẩu trong thực tế
        userDAO.save(user);

        // Đăng xuất người dùng bằng cách hủy session
        session.invalidate();

        // Chuyển hướng về trang đăng nhập với thông báo đăng xuất
        return "redirect:/login?logout";
    }
}