package poly.asm.DAO;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import poly.asm.Models.Voucher;

@Repository
public interface VoucherDAO extends JpaRepository<Voucher, Long>{
    Optional<Voucher> findByCode(String code);
    
    List<Voucher> findByActiveTrue();
    
    @Query("SELECT v FROM Voucher v WHERE v.active = true AND (v.expiryDate IS NULL OR v.expiryDate > ?1)")
    List<Voucher> findAllValidVouchers(Date currentDate);
    
    @Query("SELECT v FROM Voucher v WHERE v.active = true AND (v.expiryDate IS NULL OR v.expiryDate > ?1) AND (v.usageLimit IS NULL OR v.usageCount < v.usageLimit)")
    List<Voucher> findAllAvailableVouchers(Date currentDate);
    
    @Query("SELECT v FROM Voucher v WHERE v.expiryDate < ?1")
    List<Voucher> findAllExpiredVouchers(Date currentDate);
    
    @Query("SELECT v FROM Voucher v WHERE v.active = true AND v.code = ?1 AND (v.expiryDate IS NULL OR v.expiryDate > ?2) AND (v.usageLimit IS NULL OR v.usageCount < v.usageLimit)")
    Optional<Voucher> findValidVoucherByCode(String code, Date currentDate);
}
