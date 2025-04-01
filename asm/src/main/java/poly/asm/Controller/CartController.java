package poly.asm.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import poly.asm.Models.User;
import poly.asm.Services.CartService;

@Controller
public class CartController {
    @Autowired
    private CartService cartService;
    
    @GetMapping("/yourcart")
    public String showCart(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        // Nếu user null, dùng icon user mặc định
        String imagePath = (loggedInUser != null) ? loggedInUser.getImage() : "/user.png";
        model.addAttribute("image", imagePath);
        
        // Kiểm tra đăng nhập trước khi hiển thị giỏ hàng
        if (loggedInUser == null) {
            model.addAttribute("loginMessage", "Vui lòng đăng nhập để xem giỏ hàng của bạn");
            return "Home/cart";
        }
        
        // Thêm thông tin giỏ hàng vào model
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        
        return "Home/cart";
    }
    
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("id") Integer id, 
                           @RequestParam("quantity") Integer quantity,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        // Kiểm tra đăng nhập trước khi thêm vào giỏ hàng
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("loginRequired", true);
            redirectAttributes.addFlashAttribute("loginMessage", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng");
            return "redirect:/login"; // Chuyển hướng đến trang đăng nhập
        }
        
        try {
            cartService.addToCart(id, quantity);
            redirectAttributes.addFlashAttribute("addSuccess", true);
        } catch (IllegalArgumentException e) {
            // Handle inventory validation error
            redirectAttributes.addFlashAttribute("inventoryError", true);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/yourcart";
    }
    
    @PostMapping("/cart/update")
    @ResponseBody
    public String updateCart(@RequestParam("id") Integer id, 
                           @RequestParam("quantity") Integer quantity,
                           HttpSession session) {
        // Kiểm tra đăng nhập trước khi cập nhật giỏ hàng
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "login_required";
        }
        
        try {
            boolean success = cartService.updateCartItem(id, quantity);
            if (!success) {
                return "Số lượng sản phẩm tồn kho không đủ";
            }
            return "success";
        } catch (IllegalArgumentException e) {
            // Return the error message directly
            return e.getMessage();
        }
    }
    
    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("id") Integer id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        // Kiểm tra đăng nhập trước khi xóa khỏi giỏ hàng
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("loginRequired", true);
            return "redirect:/login";
        }
        
        cartService.removeFromCart(id);
        return "redirect:/yourcart";
    }
    
    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        // Kiểm tra đăng nhập trước khi xóa giỏ hàng
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("loginRequired", true);
            return "redirect:/login";
        }
        
        cartService.clearCart();
        return "redirect:/yourcart";
    }
}

