package poly.asm.Models;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "UserId")
    User user;
    
    @Column(nullable = false, unique = true)
    private String orderCode;
    
    @Column(nullable = false)
    private Double subtotal;
    
    @Column(nullable = false)
    private Double shippingFee;

    // New field for voucher discount
    @Column
    private Double discount = 0.0;
    
    // New field for voucher code
    @Column
    private String voucherCode;
    
    @Column(nullable = false)
    private Double total;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private String paymentMethod;
    
    @Column(nullable = false)
    private String recipientName;
    
    @Column(nullable = false)
    private String recipientPhone;
    
    @Column(nullable = false)
    private String shippingProvince;
    
    @Column(nullable = false)
    private String shippingDistrict;
    
    @Column(nullable = false)
    private String shippingWard;
    
    @Column(nullable = false)
    private String shippingAddress;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetail;

    private Double discountAmount = 0.0;

    // Helper method to calculate total with discount
    public void calculateTotal() {
        if (this.discount == null) {
            this.discount = 0.0;
        }
        
        if (this.subtotal == null) {
            this.subtotal = 0.0;
        }
        
        if (this.shippingFee == null) {
            this.shippingFee = 0.0;
        }
        
        this.total = this.subtotal + this.shippingFee - this.discount;
    }
    
    // Helper method to apply voucher
    public void applyVoucher(Voucher voucher) {
        if (voucher != null && voucher.isValid() && voucher.canBeApplied(this.subtotal)) {
            this.voucherCode = voucher.getCode();
            this.discount = voucher.getDiscountAmount();
            calculateTotal();
        }
    }
    
    // Helper method to remove voucher
    public void removeVoucher() {
        this.voucherCode = null;
        this.discount = 0.0;
        calculateTotal();
    }
    
    // Helper method to check if order has a voucher applied
    public boolean hasVoucher() {
        return this.voucherCode != null && !this.voucherCode.isEmpty();
    }
}