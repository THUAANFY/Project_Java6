package poly.asm.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private Integer id;
    private String name;
    private Integer quantity;
    private Float price;
    private String image;

    public Float getTotalPrice() {
        return price * quantity;
    }
}
