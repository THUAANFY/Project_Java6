package poly.asm.Controller;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.mail.MessagingException;
import poly.asm.DAO.UserDAO;
import poly.asm.Models.User;
import poly.asm.Services.MailService;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private MailService mailService;

    // Hiển thị form quên mật khẩu
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "Home/forgot-password"; // Trả về tệp forgot-password.html
    }

    // Xử lý yêu cầu quên mật khẩu
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        User user = userDAO.findByIdOrEmail(email);
        if (user == null) {
            model.addAttribute("error", "Email hoặc tên tài khoản không tồn tại!");
            return "Home/forgot-password";
        }

        // Tạo mật khẩu ngẫu nhiên
        String newPassword = generateRandomPassword(10); // Tạo mật khẩu ngẫu nhiên 10 ký tự

        // Cập nhật mật khẩu mới vào cơ sở dữ liệu
        user.setPassword(newPassword); // Nên mã hóa mật khẩu trong thực tế
        userDAO.save(user);

        // Định dạng thời gian
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        // Thiết kế nội dung email dạng văn bản thuần
        String emailContent =
            "Xin chào " + user.getFullname() + ",\n\n" +
            "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn tại Ứng dụng WebCongNghe vào lúc " + formattedDateTime + ".\n\n" +
            "Mật khẩu mới của bạn là: " + newPassword + "\n\n" +
            "Vui lòng đăng nhập bằng mật khẩu này và đổi mật khẩu ngay sau khi đăng nhập để đảm bảo an toàn.";
        // Gửi email chứa mật khẩu mới
        try {
            mailService.send("philtpd10207@gmail.com", email, "Mật khẩu mới của bạn", emailContent);
            model.addAttribute("success", "Một email chứa mật khẩu mới đã được gửi đến " + email + ". Vui lòng kiểm tra hộp thư!");
        } catch (MessagingException e) {
            e.printStackTrace();
            model.addAttribute("error", "Không thể gửi email. Vui lòng thử lại sau!");
        }
        return "Home/forgot-password";
    }

    // Hàm tạo mật khẩu ngẫu nhiên
    private String generateRandomPassword(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }
}