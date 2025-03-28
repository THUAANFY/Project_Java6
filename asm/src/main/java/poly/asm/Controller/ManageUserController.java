package poly.asm.Controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import poly.asm.DAO.UserDAO;
import poly.asm.Models.User;

@Controller
public class ManageUserController {

    @Autowired
    private UserDAO userDAO;

    @GetMapping("/manageuser")
    // public String manageUser(Model model, HttpSession session) {
    //     try {
    //         List<User> users = userDAO.findAll();
    //         User loggedInUser = (User) session.getAttribute("loggedInUser");
            
    //         if (loggedInUser != null) {
    //             String imagePath = loggedInUser.getImage();
    //             model.addAttribute("image", imagePath);
    //         }
            
    //         model.addAttribute("users", users);
    //         return "Admin/testuser";
    //     } catch (Exception e) {
    //         // Log the exception
    //         e.printStackTrace();
    //         model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
    //         return "error"; // Create a simple error page
    //     }
    // }
    public String manageUser(Model model, HttpSession session) {
        try {
            List<User> users = userDAO.findAll();
            User loggedInUser = (User) session.getAttribute("loggedInUser");
            
            if (loggedInUser != null) {
                model.addAttribute("image", loggedInUser.getImage());
            }
            
            model.addAttribute("users", users != null ? users : Collections.emptyList());
            return "Admin/testuser";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "error";
        }
    }


    @PostMapping("/manageuser/updateStatus")
    public String updateStatus(
            @RequestParam("userId") String userId,
            @RequestParam("status") boolean activated,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userDAO.findById(userId).orElse(null);
            if (user != null) {
                user.setActivated(activated);
                userDAO.save(user);
                redirectAttributes.addFlashAttribute("message", "Cập nhật trạng thái thành công!");
                redirectAttributes.addFlashAttribute("alertClass", "success");
            } else {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy người dùng!");
                redirectAttributes.addFlashAttribute("alertClass", "danger");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi khi cập nhật: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "danger");
        }
        return "redirect:/manageuser";
    }
    
    @GetMapping("/manageuser/delete/{id}")
    public String deleteUser(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra xem người dùng có tồn tại không
            User user = userDAO.findById(id).orElse(null);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy người dùng!");
                redirectAttributes.addFlashAttribute("alertClass", "danger");
                return "redirect:/manageuser";
            }
            
            // Kiểm tra xem người dùng có phải là admin không
            if (user.isRole()) {
                redirectAttributes.addFlashAttribute("message", "Không thể xóa tài khoản admin!");
                redirectAttributes.addFlashAttribute("alertClass", "warning");
                return "redirect:/manageuser";
            }
            
            // Tiến hành xóa người dùng
            userDAO.deleteById(id);
            
            redirectAttributes.addFlashAttribute("message", "Xóa người dùng thành công!");
            redirectAttributes.addFlashAttribute("alertClass", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi khi xóa người dùng: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "danger");
        }
        
        return "redirect:/manageuser";
    }
}