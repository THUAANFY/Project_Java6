package poly.asm.Models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "Users")
public class User {
    @Id
    String id;
    String fullname;
    String password;
    String email;
    String phone;
    String image;
    String province;
    String district;
    String ward;
    String address;
    boolean role;
    boolean activated;
    @OneToMany(mappedBy = "user")
    List<Order> orders;
}
