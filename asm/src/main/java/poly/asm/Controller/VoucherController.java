package poly.asm.Controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import poly.asm.Models.User;
import poly.asm.Models.Voucher;
import poly.asm.Services.VoucherService;

@Controller
public class VoucherController {
    
    @Autowired
    private VoucherService voucherService;
    
    /**
     * Hiển thị trang quản lý voucher (Admin)
     */
    @GetMapping("/admin/vouchers")
    public String showVoucherManagement(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRole()) {
            return "redirect:/login";
        }
        
        List<Voucher> vouchers = voucherService.getAllVouchers();
        model.addAttribute("vouchers", vouchers);
        
        return "Admin/voucher-management";
    }
    
    /**
     * Hiển thị form tạo voucher mới (Admin)
     */
    @GetMapping("/admin/vouchers/create")
    public String showCreateVoucherForm(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRole()) {
            return "redirect:/login";
        }
        
        Voucher voucher = new Voucher();
        voucher.setActive(true);
        voucher.setUsageCount(0);
        voucher.setMinimumOrderAmount(0.0);
        voucher.setCreatedAt(new Date());
        
        model.addAttribute("voucher", voucher);
        model.addAttribute("isEdit", false);
        
        return "Admin/voucher-form";
    }
    
    /**
     * Hiển thị form chỉnh sửa voucher (Admin)
     */
    @GetMapping("/admin/vouchers/edit/{id}")
    public String showEditVoucherForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRole()) {
            return "redirect:/login";
        }
        
        Optional<Voucher> optionalVoucher = voucherService.getVoucherById(id);
        if (optionalVoucher.isEmpty()) {
            return "redirect:/admin/vouchers";
        }
        
        model.addAttribute("voucher", optionalVoucher.get());
        model.addAttribute("isEdit", true);
        
        return "Admin/voucher-form";
    }
    
    /**
     * Lưu voucher (tạo mới hoặc cập nhật) (Admin)
     */
    @PostMapping("/admin/vouchers/save")
    public String saveVoucher(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("code") String code,
            @RequestParam("discountAmount") Double discountAmount,
            @RequestParam("expiryDate") String expiryDateStr,
            @RequestParam("usageLimit") Integer usageLimit,
            @RequestParam("minimumOrderAmount") Double minimumOrderAmount,
            @RequestParam(value = "active", required = false) Boolean active,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRole()) {
            return "redirect:/login";
        }
        
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Date expiryDate = dateFormat.parse(expiryDateStr);
            
            // Kiểm tra mã voucher đã tồn tại chưa (khi tạo mới)
            if (id == null && voucherService.isCodeExists(code)) {
                redirectAttributes.addFlashAttribute("error", "Mã voucher đã tồn tại, vui lòng chọn mã khác");
                return "redirect:/admin/vouchers/create";
            }
            
            Voucher voucher;
            if (id != null) {
                // Cập nhật voucher hiện có
                Optional<Voucher> optionalVoucher = voucherService.getVoucherById(id);
                if (optionalVoucher.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Không tìm thấy voucher");
                    return "redirect:/admin/vouchers";
                }
                
                voucher = optionalVoucher.get();
                voucher.setCode(code);
                voucher.setDiscountAmount(discountAmount);
                voucher.setExpiryDate(expiryDate);
                voucher.setUsageLimit(usageLimit);
                voucher.setMinimumOrderAmount(minimumOrderAmount);
                voucher.setActive(active != null ? active : false);
            } else {
                // Tạo voucher mới
                voucher = new Voucher();
                voucher.setCode(code);
                voucher.setDiscountAmount(discountAmount);
                voucher.setExpiryDate(expiryDate);
                voucher.setUsageLimit(usageLimit);
                voucher.setMinimumOrderAmount(minimumOrderAmount);
                voucher.setActive(active != null ? active : true);
                voucher.setUsageCount(0);
                voucher.setCreatedAt(new Date());
            }
            
            voucherService.createVoucher(voucher);
            
            redirectAttributes.addFlashAttribute("message", id != null ? "Cập nhật voucher thành công" : "Tạo voucher mới thành công");
            return "redirect:/admin/vouchers";
            
        } catch (ParseException e) {
            redirectAttributes.addFlashAttribute("error", "Định dạng ngày không hợp lệ");
            return "redirect:/admin/vouchers";
        }
    }
    
    /**
     * Xóa voucher (Admin)
     */
    @PostMapping("/admin/vouchers/delete/{id}")
    public String deleteVoucher(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRole()) {
            return "redirect:/login";
        }
        
        voucherService.deleteVoucher(id);
        redirectAttributes.addFlashAttribute("message", "Xóa voucher thành công");
        
        return "redirect:/admin/vouchers";
    }
    
    /**
     * Kích hoạt/Vô hiệu hóa voucher (Admin)
     */
    @PostMapping("/admin/vouchers/toggle-status/{id}")
    public String toggleVoucherStatus(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRole()) {
            return "redirect:/login";
        }
        
        Optional<Voucher> optionalVoucher = voucherService.getVoucherById(id);
        if (optionalVoucher.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy voucher");
            return "redirect:/admin/vouchers";
        }
        
        Voucher voucher = optionalVoucher.get();
        if (voucher.getActive()) {
            voucherService.deactivateVoucher(id);
            redirectAttributes.addFlashAttribute("message", "Đã vô hiệu hóa voucher");
        } else {
            voucherService.activateVoucher(id);
            redirectAttributes.addFlashAttribute("message", "Đã kích hoạt voucher");
        }
        
        return "redirect:/admin/vouchers";
    }
    
    /**
     * Tạo mã voucher ngẫu nhiên (Admin)
     */
    @GetMapping("/admin/vouchers/generate-code")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generateVoucherCode() {
        String code = voucherService.generateUniqueCode(8);
        return ResponseEntity.ok(Map.of("code", code));
    }
    
    /**
     * API kiểm tra và áp dụng voucher
     */
    @PostMapping("/api/vouchers/apply")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyVoucher(@RequestBody Map<String, Object> data) {
        String code = (String) data.get("code");
        Double orderAmount = Double.parseDouble(data.get("orderAmount").toString());
        
        Map<String, Object> result = voucherService.applyVoucher(code, orderAmount);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * API kiểm tra mã voucher đã tồn tại chưa
     */
    @GetMapping("/api/vouchers/check-code")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkVoucherCode(@RequestParam("code") String code) {
        boolean exists = voucherService.isCodeExists(code);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
    
    /**
     * Hiển thị trang chi tiết voucher (Admin)
     */
    @GetMapping("/admin/vouchers/detail/{id}")
    public String showVoucherDetail(@PathVariable("id") Long id, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRole()) {
            return "redirect:/login";
        }
        
        Optional<Voucher> optionalVoucher = voucherService.getVoucherById(id);
        if (optionalVoucher.isEmpty()) {
            return "redirect:/admin/vouchers";
        }
        
        model.addAttribute("voucher", optionalVoucher.get());
        
        return "Admin/voucher-detail";
    }
    
    /**
     * Hiển thị trang thống kê voucher (Admin)
     */
    @GetMapping("/admin/vouchers/stats")
    public String showVoucherStats(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRole()) {
            return "redirect:/login";
        }
        
        long totalVouchers = voucherService.getAllVouchers().size();
        long activeVouchers = voucherService.countActiveVouchers();
        long expiredVouchers = voucherService.countExpiredVouchers();
        
        model.addAttribute("totalVouchers", totalVouchers);
        model.addAttribute("activeVouchers", activeVouchers);
        model.addAttribute("expiredVouchers", expiredVouchers);
        
        return "Admin/voucher-stats";
    }


    /**
     * API lấy danh sách voucher khả dụng cho người dùng
     */
    @GetMapping("/api/vouchers/available")
    @ResponseBody
    public ResponseEntity<List<Voucher>> getAvailableVouchers() {
        List<Voucher> vouchers = voucherService.getAllAvailableVouchers();
        return ResponseEntity.ok(vouchers);
    }

    /**
     * Cung cấp danh sách voucher cho trang thanh toán
     */
    @ModelAttribute("availableVouchers")
    public List<Voucher> getAvailableVouchersForCheckout() {
        return voucherService.getAllAvailableVouchers();
    }
}