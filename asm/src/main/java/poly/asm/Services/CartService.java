package poly.asm.Services;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import poly.asm.DAO.ProductDAO;
import poly.asm.DAO.PersistentCartItemDAO;
import poly.asm.Models.CartItem;
import poly.asm.Models.PersistentCartItem;
import poly.asm.Models.Product;
import poly.asm.Models.User;

@Service
public class CartService {
    @Autowired
    private ProductDAO productDAO;
    
    @Autowired
    private PersistentCartItemDAO persistentCartItemDAO;

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

    public void add(Integer id) {
        Map<Integer, CartItem> cart = getCart();
        CartItem item = cart.get(id);
        if (item == null) {
            Product product = productDAO.findById(id).orElse(null);
            if (product != null) {
                item = new CartItem();
                item.setId(product.getId());
                item.setName(product.getName());
                item.setPrice(product.getPrice());
                item.setQuantity(1);
                item.setImage(product.getImage());
                cart.put(id, item);
            }
        } else {
            // Check if product is available
            Product product = productDAO.findById(id).orElse(null);
            if (product != null && item.getQuantity() + 1 <= product.getAvailable()) {
                item.setQuantity(item.getQuantity() + 1);
            } else if (product != null) {
                throw new IllegalArgumentException("Số lượng sản phẩm tồn kho không đủ. Chỉ còn " + product.getAvailable() + " sản phẩm.");
            }
        }
        session.setAttribute("cart", cart);
    }

    public void remove(Integer id) {
        Map<Integer, CartItem> cart = getCart();
        cart.remove(id);
        session.setAttribute("cart", cart);
    }

    public boolean update(Integer id, int quantity) {
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

    public void clear() {
        session.removeAttribute("cart");
    }

    public Collection<CartItem> getCartItems() {
        return getCart().values();
    }

    public Double getTotalPrice() {
        return getCart().values().stream()
                .mapToDouble(item -> item.getQuantity() * item.getPrice())
                .sum();
    }

    public int getCount() {
        Map<Integer, CartItem> cart = getCart();
        return cart.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public double getAmount() {
        Map<Integer, CartItem> cart = getCart();
        return cart.values().stream().mapToDouble(item -> item.getQuantity() * item.getPrice()).sum();
    }
    
    // Methods for persistent cart
    
    @Transactional
    public void saveCartToDatabase(User user) {
        if (user == null) return;

        Map<Integer, CartItem> cart = getCart();

        // First clear existing items
        persistentCartItemDAO.deleteByUserId(user.getId());

        // Save current cart items
        for (Map.Entry<Integer, CartItem> entry : cart.entrySet()) {
            CartItem item = entry.getValue();
            
            // Create a new PersistentCartItem with the correct constructor
            PersistentCartItem persistentItem = new PersistentCartItem();
            persistentItem.setUserId(user.getId());
            persistentItem.setProductId(item.getId());
            persistentItem.setProductName(item.getName());
            persistentItem.setQuantity(item.getQuantity());
            persistentItem.setPrice(item.getPrice());
            persistentItem.setImage(item.getImage());
            
            persistentCartItemDAO.save(persistentItem);
        }
    }

    @Transactional(readOnly = true)
    public void loadCartFromDatabase(User user) {
        if (user == null) return;

        // Xóa giỏ hàng hiện tại trong session
        clear();

        // Tạo giỏ hàng mới
        Map<Integer, CartItem> sessionCart = new HashMap<>();

        // Load saved cart items from database
        List<PersistentCartItem> savedItems = persistentCartItemDAO.findByUserId(user.getId());

        // Merge with session cart
        for (PersistentCartItem savedItem : savedItems) {
            CartItem cartItem = savedItem.toCartItem();

            // Check if product is still available and in stock
            Product product = productDAO.findById(cartItem.getId()).orElse(null);
            if (product != null && product.getAvailable() >= cartItem.getQuantity()) {
                sessionCart.put(cartItem.getId(), cartItem);
            }
        }

        // Update session
        session.setAttribute("cart", sessionCart);
    }

    @Transactional
    public void mergeAnonymousCartWithUserCart(User user, Map<Integer, CartItem> anonymousCart) {
        if (user == null || anonymousCart == null || anonymousCart.isEmpty()) return;

        // Load user's saved cart
        List<PersistentCartItem> savedItems = persistentCartItemDAO.findByUserId(user.getId());
        Map<Integer, CartItem> userCart = new HashMap<>();

        // Convert saved items to cart items
        for (PersistentCartItem savedItem : savedItems) {
            userCart.put(savedItem.getProductId(), savedItem.toCartItem());
        }

        // Merge anonymous cart with user cart
        for (Map.Entry<Integer, CartItem> entry : anonymousCart.entrySet()) {
            Integer productId = entry.getKey();
            CartItem anonymousItem = entry.getValue();

            if (userCart.containsKey(productId)) {
                // Product already in user cart, update quantity if possible
                CartItem userItem = userCart.get(productId);
                Product product = productDAO.findById(productId).orElse(null);

                if (product != null) {
                    int newQuantity = userItem.getQuantity() + anonymousItem.getQuantity();
                    if (newQuantity <= product.getAvailable()) {
                        userItem.setQuantity(newQuantity);
                    } else {
                        userItem.setQuantity(product.getAvailable());
                    }
                }
            } else {
                // Product not in user cart, add it
                userCart.put(productId, anonymousItem);
            }
        }

        // Save merged cart to database
        persistentCartItemDAO.deleteByUserId(user.getId());
        for (Map.Entry<Integer, CartItem> entry : userCart.entrySet()) {
            CartItem item = entry.getValue();
            
            // Create a new PersistentCartItem with the correct constructor
            PersistentCartItem persistentItem = new PersistentCartItem();
            persistentItem.setUserId(user.getId());
            persistentItem.setProductId(item.getId());
            persistentItem.setProductName(item.getName());
            persistentItem.setQuantity(item.getQuantity());
            persistentItem.setPrice(item.getPrice());
            persistentItem.setImage(item.getImage());
            
            persistentCartItemDAO.save(persistentItem);
        }

        // Update session cart
        session.setAttribute("cart", userCart);
    }
}