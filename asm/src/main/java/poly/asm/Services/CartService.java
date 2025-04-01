package poly.asm.Services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import poly.asm.DAO.ProductDAO;
import poly.asm.Models.CartItem;
import poly.asm.Models.Product;

@Service
public class CartService {
    @Autowired
    private ProductDAO productDAO;

    @Autowired
    HttpSession session;

    public Map<Integer, CartItem> getCart() {
        Map<Integer, CartItem> cart = (Map<Integer, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    public void addToCart(Integer id, Integer quantity) {
        Map<Integer, CartItem> cart = getCart();
        Product product = productDAO.findById(id).orElse(null);
        if (product != null) {
            // Check if requested quantity exceeds available inventory
            if (quantity > product.getAvailable()) {
                throw new IllegalArgumentException("Số lượng sản phẩm tồn kho không đủ. Chỉ còn " + product.getAvailable() + " sản phẩm.");
            }
            
            CartItem item = cart.get(id);
            if (item == null) {
                item = new CartItem(product.getId(), product.getName(), quantity, product.getPrice(), product.getImage());
            } else {
                // Check if updated quantity exceeds available inventory
                if (item.getQuantity() + quantity > product.getAvailable()) {
                    throw new IllegalArgumentException("Số lượng sản phẩm tồn kho không đủ. Chỉ còn " + product.getAvailable() + " sản phẩm.");
                }
                item.setQuantity(item.getQuantity() + quantity);
            }
            cart.put(id, item);
            session.setAttribute("cart", cart); // Cập nhật session
        }
    }

    public void removeFromCart(Integer id) {
        Map<Integer, CartItem> cart = getCart();
        cart.remove(id);
        session.setAttribute("cart", cart);
    }

    public void clearCart() {
        session.removeAttribute("cart");
    }

    public Collection<CartItem> getCartItems() {
        return getCart().values();
    }

    public Double getTotalPrice() {
        return getCart().values().stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }

    public boolean updateCartItem(Integer id, Integer quantity) {
        Map<Integer, CartItem> cart = getCart();
        CartItem item = cart.get(id);
        Product product = productDAO.findById(id).orElse(null);
        
        if (item != null && quantity > 0 && product != null) {
            // Check if requested quantity exceeds available inventory
            if (quantity > product.getAvailable()) {
                throw new IllegalArgumentException("Số lượng sản phẩm tồn kho không đủ. Chỉ còn " + product.getAvailable() + " sản phẩm.");
            }
            
            item.setQuantity(quantity);
            cart.put(id, item);
            session.setAttribute("cart", cart); // Cập nhật session
            return true; // Return true to indicate success
        }
        return false;
    }
}

