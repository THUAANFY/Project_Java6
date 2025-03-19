package poly.asm.DTO;

import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {
    private Integer id;

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    private Float price;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 200, message = "Image URL cannot exceed 200 characters")
    private String image;

    @NotNull(message = "Available quantity cannot be null")
    @Min(value = 0, message = "Available quantity cannot be negative")
    private Integer available;

    @NotNull(message = "Create date cannot be null")
    @PastOrPresent(message = "Create date cannot be in the future")
    private Date createDate;

    @NotNull(message = "Category is required")
    private CategoryDTO category;

    private List<OrderDetailDTO> orderDetails;
}
