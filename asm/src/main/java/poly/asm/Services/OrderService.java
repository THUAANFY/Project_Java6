package poly.asm.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import poly.asm.DAO.OrderDAO;
import poly.asm.DAO.OrderDetailDAO;
import poly.asm.DAO.ProductDAO;
import poly.asm.Models.CartItem;
import poly.asm.Models.Order;
import poly.asm.Models.OrderDetail;
import poly.asm.Models.Product;
import poly.asm.Models.User;
import poly.asm.Utils.ShippingCalculator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {
    @Autowired
    private CartService cartService;
    
    @Autowired
    private OrderDAO orderDAO;
    
    @Autowired
    private OrderDetailDAO orderDetailDAO;
    
    @Autowired
    private ProductDAO productDAO;
    
    @Autowired
    private HttpSession session;
    
    @Autowired
    private VoucherService voucherService; // Thêm VoucherService
    
    /**
     * Tạo đơn hàng mới từ giỏ hàng hiện tại
     * 
     * @param orderData Dữ liệu đơn hàng từ form
     * @param user Người dùng đang đăng nhập
     * @return Mã đơn hàng
     */
    @Transactional
    public String createOrder(Map<String, Object> orderData, User user) {
        // Tạo mã đơn hàng ngẫu nhiên
        String orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Lấy thông tin giỏ hàng
        Collection<CartItem> cartItems = cartService.getCartItems();
        Double subtotal = cartService.getTotalPrice();
        Double shippingFee = 30000.0; // Phí vận chuyển cố định
        
        // Tạo đối tượng đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setOrderCode(orderCode);
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        
        // Get voucher code and discount amount if provided
        String voucherCode = null;
        Double discountAmount = 0.0;
        
        // Thêm log để kiểm tra dữ liệu nhận được
        System.out.println("Order data received: " + orderData);
        
        if (orderData.containsKey("couponCode")) {
            voucherCode = (String) orderData.get("couponCode");
            
            // Kiểm tra và chuyển đổi discountAmount một cách an toàn
            if (orderData.containsKey("discountAmount")) {
                Object discountObj = orderData.get("discountAmount");
                System.out.println("Discount object type: " + (discountObj != null ? discountObj.getClass().getName() : "null"));
                System.out.println("Discount value: " + discountObj);
                
                if (discountObj instanceof Number) {
                    discountAmount = ((Number) discountObj).doubleValue();
                } else if (discountObj instanceof String) {
                    try {
                        discountAmount = Double.parseDouble((String) discountObj);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing discount amount: " + e.getMessage());
                    }
                }
            }
        }
        
        System.out.println("Voucher code: " + voucherCode);
        System.out.println("Discount amount: " + discountAmount);

        // Set voucher information in the order
        order.setVoucherCode(voucherCode);
        order.setDiscountAmount(discountAmount);

        // Calculate final total with discount
        double total = subtotal + shippingFee - discountAmount;
        System.out.println("Calculated total: " + total);
        order.setTotal(total);
        
        order.setStatus("Chờ xác nhận");
        order.setCreatedAt(new Date());
        
        // Xử lý thông tin địa chỉ
        boolean useProfileAddress = (boolean) orderData.get("useProfileAddress");
        if (useProfileAddress) {
            order.setRecipientName(user.getFullname());
            order.setRecipientPhone(user.getPhone());
            order.setShippingProvince(user.getProvince());
            order.setShippingDistrict(user.getDistrict());
            order.setShippingWard(user.getWard());
            order.setShippingAddress(user.getAddress());
        } else {
            @SuppressWarnings("unchecked")
            Map<String, String> newAddress = (Map<String, String>) orderData.get("newAddress");
            order.setRecipientName(newAddress.get("name"));
            order.setRecipientPhone(newAddress.get("phone"));
            order.setShippingProvince(newAddress.get("province"));
            order.setShippingDistrict(newAddress.get("district"));
            order.setShippingWard(newAddress.get("ward"));
            order.setShippingAddress(newAddress.get("addressDetail"));
        }
        
        // Xử lý phương thức thanh toán
        String paymentMethod = (String) orderData.get("paymentMethod");
        order.setPaymentMethod(paymentMethod);
        
        // Lưu đơn hàng vào cơ sở dữ liệu
        order = orderDAO.save(order);
        
        // Tạo và lưu các mục đơn hàng
        List<OrderDetail> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderDetail orderItem = new OrderDetail();
            orderItem.setOrder(order);
            
            // Lấy thông tin sản phẩm từ cơ sở dữ liệu
            Product product = productDAO.findById(cartItem.getId()).orElse(null);
            if (product != null) {
                orderItem.setProduct(product);
                
                // Cập nhật số lượng sản phẩm trong kho
                int currentQuantity = product.getAvailable();
                int orderedQuantity = cartItem.getQuantity();
                
                // Kiểm tra xem có đủ số lượng trong kho không
                if (currentQuantity < orderedQuantity) {
                    throw new RuntimeException("Sản phẩm " + product.getName() + " chỉ còn " + currentQuantity + " sản phẩm trong kho");
                }
                
                // Trừ số lượng sản phẩm đã mua
                product.setAvailable(currentQuantity - orderedQuantity);
                
                // Lưu cập nhật số lượng sản phẩm
                productDAO.save(product);
                System.out.println("Đã cập nhật số lượng sản phẩm " + product.getName() + " từ " + currentQuantity + " thành " + product.getAvailable());
            }
            
            orderItem.setProductName(cartItem.getName());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(cartItem.getTotalPrice());
            orderItem.setProductImage(cartItem.getImage());
            
            orderItems.add(orderItem);
        }
        
        // Lưu tất cả các mục đơn hàng
        orderDetailDAO.saveAll(orderItems);
        
        // Tăng số lần sử dụng voucher nếu có
        if (voucherCode != null && !voucherCode.isEmpty()) {
            voucherService.useVoucher(voucherCode);
            System.out.println("Increased usage count for voucher: " + voucherCode);
        }
        
        return orderCode;
    }
    
    /**
     * Lấy thông tin đơn hàng theo mã đơn hàng
     * 
     * @param orderCode Mã đơn hàng
     * @return Thông tin đơn hàng dưới dạng Map
     */
    public Map<String, Object> getOrderInfo(String orderCode) {
        Order order = orderDAO.findByOrderCode(orderCode);
        if (order == null) {
            return null;
        }
        
        List<OrderDetail> orderItems = orderDetailDAO.findByOrder(order);
        
        Map<String, Object> orderInfo = new HashMap<>();
        orderInfo.put("id", order.getOrderCode());
        orderInfo.put("subtotal", order.getSubtotal());
        orderInfo.put("shippingFee", order.getShippingFee());
        
        // Add discount amount to the order info
        orderInfo.put("discountAmount", order.getDiscountAmount() != null ? order.getDiscountAmount() : 0.0);
        
        orderInfo.put("total", order.getTotal());
        orderInfo.put("status", order.getStatus());
        orderInfo.put("createdAt", order.getCreatedAt().getTime());
        orderInfo.put("items", orderItems);
        
        // Add estimated delivery date
        Date estimatedDeliveryDate = calculateEstimatedDeliveryDate(order);
        if (estimatedDeliveryDate != null) {
            orderInfo.put("estimatedDeliveryDate", estimatedDeliveryDate.getTime());
        }
        
        // Add shipping days and delivery time range
        orderInfo.put("shippingDays", getShippingDays(order));
        orderInfo.put("deliveryTimeRange", getDeliveryTimeRangeText(order));
        
        Map<String, String> shippingAddress = new HashMap<>();
        shippingAddress.put("name", order.getRecipientName());
        shippingAddress.put("phone", order.getRecipientPhone());
        shippingAddress.put("province", order.getShippingProvince());
        shippingAddress.put("district", order.getShippingDistrict());
        shippingAddress.put("ward", order.getShippingWard());
        shippingAddress.put("addressDetail", order.getShippingAddress());
        
        orderInfo.put("shippingAddress", shippingAddress);
        orderInfo.put("paymentMethod", order.getPaymentMethod());
        
        return orderInfo;
    }
    
    /**
     * Lấy danh sách đơn hàng của người dùng
     * 
     * @param user Người dùng
     * @return Danh sách đơn hàng
     */
    public List<Order> getUserOrders(User user) {
        return orderDAO.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Lấy chi tiết đơn hàng theo mã đơn hàng
     * 
     * @param orderCode Mã đơn hàng
     * @return Đơn hàng
     */
    public Order getOrderByCode(String orderCode) {
        return orderDAO.findByOrderCode(orderCode);
    }
    
    /**
     * Cập nhật trạng thái đơn hàng
     * 
     * @param orderCode Mã đơn hàng
     * @param status Trạng thái mới
     * @return true nếu cập nhật thành công, false nếu không
     */
    @Transactional
    public boolean updateOrderStatus(String orderCode, String status) {
        Order order = orderDAO.findByOrderCode(orderCode);
        if (order == null) {
            return false;
        }
        
        order.setStatus(status);
        orderDAO.save(order);
        return true;
    }
    
    /**
     * Hủy đơn hàng
     * 
     * @param orderCode Mã đơn hàng
     * @param user Người dùng
     * @return true nếu hủy thành công, false nếu không
     */
    @Transactional
    public boolean cancelOrder(String orderCode, User user) {
        Order order = orderDAO.findByOrderCode(orderCode);
        if (order == null || !order.getUser().getId().equals(user.getId())) {
            return false;
        }
        
        // Chỉ cho phép hủy đơn hàng khi đang ở trạng thái "Chờ xác nhận"
        if (!"Chờ xác nhận".equals(order.getStatus())) {
            return false;
        }
        
        // Hoàn trả số lượng sản phẩm vào kho khi hủy đơn hàng
        List<OrderDetail> orderDetails = orderDetailDAO.findByOrder(order);
        for (OrderDetail detail : orderDetails) {
            Product product = detail.getProduct();
            if (product != null) {
                int currentAvailable = product.getAvailable();
                int returnQuantity = detail.getQuantity();
                product.setAvailable(currentAvailable + returnQuantity);
                productDAO.save(product);
                System.out.println("Đã hoàn trả " + returnQuantity + " sản phẩm " + product.getName() + " vào kho");
            }
        }
        
        order.setStatus("Đã hủy");
        orderDAO.save(order);
        return true;
    }
    
    /**
     * Calculate the estimated delivery date for an order
     * 
     * @param order The order
     * @return The estimated delivery date
     */
    public Date calculateEstimatedDeliveryDate(Order order) {
        // If the order is cancelled, return null
        if ("Đã hủy".equals(order.getStatus())) {
            return null;
        }
        
        // If the order is already delivered, return null or the actual delivery date if you track it
        if ("Đã giao hàng".equals(order.getStatus())) {
            return null; // Or return actual delivery date if you track it
        }
        
        // Calculate the estimated delivery date based on the order date and shipping province
        return ShippingCalculator.calculateEstimatedDeliveryDate(
            order.getCreatedAt(), 
            order.getShippingProvince()
        );
    }
    
    /**
     * Get the shipping days for an order
     * 
     * @param order The order
     * @return The number of shipping days
     */
    public int getShippingDays(Order order) {
        return ShippingCalculator.calculateShippingDays(order.getShippingProvince());
    }
    
    /**
     * Get the delivery time range text for an order
     * 
     * @param order The order
     * @return A text description of the delivery time range
     */
    public String getDeliveryTimeRangeText(Order order) {
        int shippingDays = getShippingDays(order);
        return ShippingCalculator.getDeliveryTimeRangeText(shippingDays);
    }
    
    /**
     * Calculate shipping days for a specific province
     * 
     * @param province The province name
     * @return The number of shipping days
     */
    public int calculateShippingDaysForProvince(String province) {
        return ShippingCalculator.calculateShippingDays(province);
    }
    
    /**
     * Get delivery time range text for a specific province
     * 
     * @param province The province name
     * @return A text description of the delivery time range
     */
    public String getDeliveryTimeRangeForProvince(String province) {
        int shippingDays = calculateShippingDaysForProvince(province);
        return ShippingCalculator.getDeliveryTimeRangeText(shippingDays);
    }
}