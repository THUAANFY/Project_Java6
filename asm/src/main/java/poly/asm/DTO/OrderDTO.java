package poly.asm.DTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDTO {
    private Integer id;

    @NotBlank(message = "Address cannot be empty")
    @Size(max = 200, message = "Address cannot exceed 200 characters")
    private String address;

    @NotNull(message = "Create date cannot be null")
    @PastOrPresent(message = "Create date cannot be in the future")
    private Date createDate;

    @NotNull(message = "User is required")
    private UserLoginDTO user;

    private List<OrderDetailDTO> orderDetails = new ArrayList<>();
}
