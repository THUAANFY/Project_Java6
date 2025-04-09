package poly.asm.Models;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String message;
    private String type; // ORDER_CREATED, ORDER_CANCELLED, etc.
    private String orderCode;
    private String customerName;
    @Column(name = "read_status")
    private boolean readStatus;
    private Date createdAt;
    
    // Constructor cho thông báo mới
    public Notification(String message, String type, String orderCode, String customerName) {
        this.message = message;
        this.type = type;
        this.orderCode = orderCode;
        this.customerName = customerName;
        this.readStatus = false;
        this.createdAt = new Date();
    }
}
