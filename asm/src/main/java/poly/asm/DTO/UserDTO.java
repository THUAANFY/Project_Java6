package poly.asm.DTO;

import lombok.Data;

@Data
public class UserDTO {
    private String id;
    private String fullname;
    private String email;
    private String phone;
    private String image;
    private String province;
    private String district;
    private String ward;
    private String address;
    private boolean role;
    private boolean activated;
}

