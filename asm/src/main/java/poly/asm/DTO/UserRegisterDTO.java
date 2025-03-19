package poly.asm.DTO;

import jakarta.validation.constraints.Email;
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
public class UserRegisterDTO {
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullname;

    @NotBlank(message = "Số điện thoại không được để trống")
    // @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải là 10 chữ số")
    private String phone; // Dùng String thay vì Integer để dễ kiểm tra định dạng

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Xác nhận mật khẩu phải từ 6 đến 100 ký tự")
    private String confirmPassword;

    // Phương thức kiểm tra password và confirmPassword có khớp nhau không
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
