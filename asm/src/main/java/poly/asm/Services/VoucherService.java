package poly.asm.Services;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import poly.asm.DAO.VoucherDAO;
import poly.asm.Models.Voucher;

@Service
public class VoucherService {
    
    @Autowired
    private VoucherDAO voucherDAO;
    
    /**
     * Lấy tất cả voucher
     */
    public List<Voucher> getAllVouchers() {
        return voucherDAO.findAll();
    }
    
    /**
     * Lấy tất cả voucher còn hiệu lực
     */
    public List<Voucher> getAllValidVouchers() {
        return voucherDAO.findAllValidVouchers(new Date());
    }
    
    /**
     * Lấy tất cả voucher còn khả dụng (còn hiệu lực và chưa hết lượt sử dụng)
     */
    public List<Voucher> getAllAvailableVouchers() {
        return voucherDAO.findAllAvailableVouchers(new Date());
    }
    
    /**
     * Lấy tất cả voucher đã hết hạn
     */
    public List<Voucher> getAllExpiredVouchers() {
        return voucherDAO.findAllExpiredVouchers(new Date());
    }
    
    /**
     * Tìm voucher theo ID
     */
    public Optional<Voucher> getVoucherById(Long id) {
        return voucherDAO.findById(id);
    }
    
    /**
     * Tìm voucher theo mã
     */
    public Optional<Voucher> getVoucherByCode(String code) {
        return voucherDAO.findByCode(code);
    }
    
    /**
     * Tìm voucher hợp lệ theo mã
     */
    public Optional<Voucher> getValidVoucherByCode(String code) {
        return voucherDAO.findValidVoucherByCode(code, new Date());
    }
    
    /**
     * Tạo voucher mới
     */
    public Voucher createVoucher(Voucher voucher) {
        // Nếu không có ngày tạo, thiết lập ngày tạo là hiện tại
        if (voucher.getCreatedAt() == null) {
            voucher.setCreatedAt(new Date());
        }
        
        // Nếu không có số lần sử dụng, thiết lập là 0
        if (voucher.getUsageCount() == null) {
            voucher.setUsageCount(0);
        }
        
        return voucherDAO.save(voucher);
    }
    
    /**
     * Cập nhật voucher
     */
    public Voucher updateVoucher(Voucher voucher) {
        return voucherDAO.save(voucher);
    }
    
    /**
     * Xóa voucher
     */
    public void deleteVoucher(Long id) {
        voucherDAO.deleteById(id);
    }
    
    /**
     * Vô hiệu hóa voucher
     */
    public Voucher deactivateVoucher(Long id) {
        Optional<Voucher> optionalVoucher = voucherDAO.findById(id);
        if (optionalVoucher.isPresent()) {
            Voucher voucher = optionalVoucher.get();
            voucher.setActive(false);
            return voucherDAO.save(voucher);
        }
        return null;
    }
    
    /**
     * Kích hoạt voucher
     */
    public Voucher activateVoucher(Long id) {
        Optional<Voucher> optionalVoucher = voucherDAO.findById(id);
        if (optionalVoucher.isPresent()) {
            Voucher voucher = optionalVoucher.get();
            voucher.setActive(true);
            return voucherDAO.save(voucher);
        }
        return null;
    }
    
    /**
     * Kiểm tra và áp dụng voucher
     */
    public Map<String, Object> applyVoucher(String code, Double orderAmount) {
        Optional<Voucher> optionalVoucher = getValidVoucherByCode(code);
        
        if (optionalVoucher.isEmpty()) {
            return Map.of(
                "success", false,
                "message", "Mã giảm giá không hợp lệ hoặc đã hết hạn"
            );
        }
        
        Voucher voucher = optionalVoucher.get();
        
        // Kiểm tra xem voucher có thể áp dụng cho đơn hàng này không
        if (orderAmount < voucher.getMinimumOrderAmount()) {
            return Map.of(
                "success", false,
                "message", "Đơn hàng không đủ điều kiện áp dụng mã giảm giá này (tối thiểu " + voucher.getMinimumOrderAmount() + "₫)"
            );
        }
        
        // Kiểm tra xem voucher có còn lượt sử dụng không
        if (voucher.getUsageLimit() > 0 && voucher.getUsageCount() >= voucher.getUsageLimit()) {
            return Map.of(
                "success", false,
                "message", "Mã giảm giá đã hết lượt sử dụng"
            );
        }
        
        // Kiểm tra xem voucher có hết hạn không
        if (voucher.getExpiryDate() != null && voucher.getExpiryDate().before(new Date())) {
            return Map.of(
                "success", false,
                "message", "Mã giảm giá đã hết hạn"
            );
        }
        
        Double discountAmount = voucher.getDiscountAmount();
        
        return Map.of(
            "success", true,
            "message", "Áp dụng mã giảm giá thành công",
            "voucherCode", voucher.getCode(),
            "discountAmount", discountAmount
        );
    }
    
    /**
     * Sử dụng voucher (tăng số lần sử dụng)
     */
    @Transactional
    public void useVoucher(String code) {
        Optional<Voucher> optionalVoucher = voucherDAO.findByCode(code);
        if (optionalVoucher.isPresent()) {
            Voucher voucher = optionalVoucher.get();
            
            // Tăng số lần sử dụng
            if (voucher.getUsageCount() == null) {
                voucher.setUsageCount(1);
            } else {
                voucher.setUsageCount(voucher.getUsageCount() + 1);
            }
            
            // Nếu đã đạt giới hạn sử dụng, vô hiệu hóa voucher
            if (voucher.getUsageLimit() > 0 && voucher.getUsageCount() >= voucher.getUsageLimit()) {
                voucher.setActive(false);
            }
            
            voucherDAO.save(voucher);
        }
    }
    
    /**
     * Tạo mã voucher ngẫu nhiên
     */
    public String generateRandomCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < length; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        return code.toString();
    }
    
    /**
     * Tạo mã voucher ngẫu nhiên và đảm bảo là duy nhất
     */
    public String generateUniqueCode(int length) {
        String code;
        do {
            code = generateRandomCode(length);
        } while (voucherDAO.findByCode(code).isPresent());
        
        return code;
    }
    
    /**
     * Kiểm tra xem mã voucher đã tồn tại chưa
     */
    public boolean isCodeExists(String code) {
        return voucherDAO.findByCode(code).isPresent();
    }
    
    /**
     * Kiểm tra xem voucher có hợp lệ không
     */
    public boolean isVoucherValid(String code) {
        Optional<Voucher> optionalVoucher = getValidVoucherByCode(code);
        return optionalVoucher.isPresent();
    }
    
    /**
     * Kiểm tra xem voucher có thể áp dụng cho đơn hàng không
     */
    public boolean canVoucherBeApplied(String code, Double orderAmount) {
        Optional<Voucher> optionalVoucher = getValidVoucherByCode(code);
        if (optionalVoucher.isEmpty()) {
            return false;
        }
        
        Voucher voucher = optionalVoucher.get();
        return orderAmount >= voucher.getMinimumOrderAmount();
    }
    
    /**
     * Lấy số lượng voucher đang hoạt động
     */
    public long countActiveVouchers() {
        return voucherDAO.findByActiveTrue().size();
    }
    
    /**
     * Lấy số lượng voucher đã hết hạn
     */
    public long countExpiredVouchers() {
        return voucherDAO.findAllExpiredVouchers(new Date()).size();
    }
}