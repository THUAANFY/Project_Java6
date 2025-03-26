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
}