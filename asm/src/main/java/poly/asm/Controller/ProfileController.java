package poly.asm.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import poly.asm.DAO.UserDAO;
import poly.asm.Models.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class ProfileController {
    @Autowired
    private UserDAO userDAO;
    
    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        // Lấy thông tin người dùng từ session
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        // Kiểm tra xem người dùng đã đăng nhập hay chưa
        if (loggedInUser == null) {
            // Nếu chưa đăng nhập, chuyển hướng về trang login
            return "redirect:/login";
        }
        // Thêm thông tin người dùng vào model để gửi đến giao diện
        model.addAttribute("fullname", loggedInUser.getFullname());
        model.addAttribute("phone", loggedInUser.getPhone());
        model.addAttribute("email", loggedInUser.getEmail());
        model.addAttribute("province", loggedInUser.getProvince());
        model.addAttribute("district", loggedInUser.getDistrict());
        model.addAttribute("ward", loggedInUser.getWard());
        model.addAttribute("address", loggedInUser.getAddress());
        String imagePath = loggedInUser.getImage();
        model.addAttribute("image", imagePath != null && !imagePath.isEmpty() ? imagePath : "default-avatar.png");
        model.addAttribute("timestamp", System.currentTimeMillis()); // Thêm timestamp để tránh cache
        return "Home/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(RedirectAttributes redirectAttributes, HttpSession session, 
            @RequestParam("fullname") String fullname, 
            @RequestParam("phone") String phone, 
            @RequestParam(value = "avatar", required = false) MultipartFile avatar, 
            @RequestParam("email") String email,
            @RequestParam(value = "province", required = false) String province,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "ward", required = false) String ward,
            @RequestParam(value = "address", required = false) String address) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Cập nhật thông tin cơ bản
        loggedInUser.setFullname(fullname);
        loggedInUser.setPhone(phone);
        loggedInUser.setEmail(email);
        
        // Cập nhật thông tin địa chỉ
        loggedInUser.setProvince(province);
        loggedInUser.setDistrict(district);
        loggedInUser.setWard(ward);
        loggedInUser.setAddress(address);
        
        // Xử lý upload ảnh nếu có
        if (avatar != null && !avatar.isEmpty()) {
            try {
                // Sửa đường dẫn thư mục lưu ảnh để đảm bảo đúng định dạng
                String uploadDir = "D:\\HOCTAP\\HocKyV\\BLOCK_II\\Java6\\Project_Java6\\asm\\src\\main\\resources\\static\\Images\\"; // Thư mục lưu ảnh
                
                // Tạo tên file duy nhất với UUID để tránh trùng lặp
                String originalFilename = avatar.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String fileName = UUID.randomUUID().toString() + extension;
                
                Path filePath = Paths.get(uploadDir + fileName);
                Files.createDirectories(filePath.getParent()); // Tạo thư mục nếu chưa có
                Files.write(filePath, avatar.getBytes()); // Lưu file vào hệ thống
                loggedInUser.setImage(fileName); // Lưu đường dẫn vào user
            
                // Thêm một khoảng thời gian nhỏ để đảm bảo file được lưu hoàn tất
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Không thể upload ảnh!");
                return "redirect:/profile";
            }
        }
        // Nếu không có ảnh mới, giữ nguyên ảnh cũ

        // Lưu thông tin người dùng vào database
        userDAO.save(loggedInUser);
        
        // Cập nhật lại session
        session.setAttribute("loggedInUser", loggedInUser);
        
        // Thêm thông báo thành công
        redirectAttributes.addFlashAttribute("message", "Thông tin đã được cập nhật thành công!");
        
        // Redirect để tránh form resubmission
        return "redirect:/profile";
    }

    
}