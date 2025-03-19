package poly.asm.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailDTO {
    private Integer id;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    private Float price;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Product is required")
    private ProductDTO product;

    @NotNull(message = "Order is required")
    private OrderDTO order;
}
