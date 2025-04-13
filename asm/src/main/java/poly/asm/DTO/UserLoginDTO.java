package poly.asm.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginDTO {
    @NotBlank(message = "Tên tài khoản không được để trống")
    // @Size(min = 3, max = 50, message = "ID must be between 3 and 50 characters")
    private String id;

    @NotBlank(message = "Mật khẩu không được để trống")
    // @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

}
