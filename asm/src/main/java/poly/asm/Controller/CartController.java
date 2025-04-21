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
        
        // Thêm thông tin giỏ hàng vào model (cả khi đăng nhập và không đăng nhập)
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        
        // Hiển thị thông báo đăng nhập nếu chưa đăng nhập
        if (loggedInUser == null) {
            model.addAttribute("loginMessage", "Vui lòng đăng nhập để xem giỏ hàng của bạn");
        }
        
        return "Home/cart";
    }
    
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("id") Integer id, 
                           @RequestParam(value = "quantity", required = false, defaultValue = "1") Integer quantity,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        try {
            // Thêm sản phẩm vào giỏ hàng (không cần kiểm tra đăng nhập)
            for (int i = 0; i < quantity; i++) {
                cartService.add(id);
            }
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
        try {
            boolean success = cartService.update(id, quantity);
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
        cartService.remove(id);
        return "redirect:/yourcart";
    }
    
    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        cartService.clear();
        return "redirect:/yourcart";
    }
}
