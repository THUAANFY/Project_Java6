package poly.asm.DTO;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDTO {
    @NotBlank(message = "ID cannot be empty")
    @Size(min = 1, max = 50, message = "ID must be between 1 and 50 characters")
    private String id;

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    private List<ProductDTO> products;
}
