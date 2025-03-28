package poly.asm.Models;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vouchers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    @Column(nullable = false)
    private Double discountAmount;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(nullable = false)
    private Integer usageLimit;
    
    @Column(nullable = false)
    private Integer usageCount = 0;
    
    @Column(nullable = false)
    private Double minimumOrderAmount = 0.0;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;


    // Helper methods
    public boolean isExpired() {
        return expiryDate != null && expiryDate.before(new Date());
    }
    
    public boolean isUsageLimitReached() {
        return usageLimit != null && usageLimit > 0 && usageCount >= usageLimit;
    }
    
    public boolean isValid() {
        return active && !isExpired() && !isUsageLimitReached();
    }
    
    public boolean canBeApplied(Double orderAmount) {
        return isValid() && orderAmount >= minimumOrderAmount;
    }
    
    /**
     * Tăng số lần sử dụng voucher lên 1
     */
    public void incrementUsageCount() {
        if (this.usageCount == null) {
            this.usageCount = 1;
        } else {
            this.usageCount++;
        }
    }
}
