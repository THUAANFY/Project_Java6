package poly.asm.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "persistent_cart_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersistentCartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String userId; // User ID as String
    
    @Column(nullable = false)
    private Integer productId;
    
    @Column(nullable = false)
    private String productName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private Float price;
    
    @Column(nullable = false)
    private String image;
    
    // Constructor without id for creating new items
    public PersistentCartItem(String userId, Integer productId, String productName, 
                             Integer quantity, Float price, String image) {
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.image = image;
    }
    
    // Convert to CartItem
    public CartItem toCartItem() {
        CartItem item = new CartItem();
        item.setId(this.productId);
        item.setName(this.productName);
        item.setQuantity(this.quantity);
        item.setPrice(this.price);
        item.setImage(this.image);
        return item;
    }
}
