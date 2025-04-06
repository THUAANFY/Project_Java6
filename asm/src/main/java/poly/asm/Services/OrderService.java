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
    private NotificationService notificationService;
    
    @Autowired
    private OrderDAO orderDAO;

    @Autowired
    private OrderDetailDAO orderDetailDAO;

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private HttpSession session;

    @Autowired
    private VoucherService voucherService;

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
        System.out.println("Creating order with orderCode: " + orderCode);

        // Lấy thông tin giỏ hàng
        Collection<CartItem> cartItems = cartService.getCartItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể tạo đơn hàng.");
        }

        Double subtotal = cartService.getTotalPrice();
        Double shippingFee = 30000.0; // Phí vận chuyển cố định

        // Tạo đối tượng đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setOrderCode(orderCode);
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);

        // Xử lý mã giảm giá (voucher)
        String voucherCode = null;
        Double discountAmount = 0.0;
        System.out.println("Order data received: " + orderData);

        if (orderData.containsKey("couponCode")) {
            voucherCode = (String) orderData.get("couponCode");
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
        order.setVoucherCode(voucherCode);
        order.setDiscountAmount(discountAmount);

        // Tính tổng tiền cuối cùng
        double total = subtotal + shippingFee - discountAmount;
        if (total < 0) total = 0; // Đảm bảo tổng tiền không âm
        System.out.println("Calculated total: " + total);
        order.setTotal(total);

        order.setStatus("Chờ xác nhận");
        order.setCreatedAt(new Date());

        // Xử lý thông tin địa chỉ giao hàng
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
        System.out.println("Order saved with orderCode: " + order.getOrderCode());

        // Tạo và lưu chi tiết đơn hàng
        List<OrderDetail> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderDetail orderItem = new OrderDetail();
            orderItem.setOrder(order);

            Product product = productDAO.findById(cartItem.getId()).orElse(null);
            if (product != null) {
                orderItem.setProduct(product);
                int currentQuantity = product.getAvailable();
                int orderedQuantity = cartItem.getQuantity();
                if (currentQuantity < orderedQuantity) {
                    throw new RuntimeException("Sản phẩm " + product.getName() + " chỉ còn " + currentQuantity + " trong kho.");
                }
                product.setAvailable(currentQuantity - orderedQuantity);
                productDAO.save(product);
                System.out.println("Updated product " + product.getName() + " quantity from " + currentQuantity + " to " + product.getAvailable());
            }

            orderItem.setProductName(cartItem.getName());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(cartItem.getTotalPrice());
            orderItem.setProductImage(cartItem.getImage());
            orderItems.add(orderItem);
        }

        orderDetailDAO.saveAll(orderItems);

        // Cập nhật số lần sử dụng voucher
        if (voucherCode != null && !voucherCode.isEmpty()) {
            voucherService.useVoucher(voucherCode);
            System.out.println("Increased usage count for voucher: " + voucherCode);
        }
        
        try {
            notificationService.createOrderNotification(orderCode, user.getFullname());
        } catch (Exception e) {
            // Log lỗi nhưng không ảnh hưởng đến quá trình đặt hàng
            System.err.println("Lỗi khi tạo thông báo: " + e.getMessage());
            e.printStackTrace();
        }
        // Xóa giỏ hàng sau khi tạo đơn hàng thành công
        cartService.clearCart();
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
            System.out.println("Order not found for orderCode: " + orderCode);
            return null;
        }

        List<OrderDetail> orderItems = orderDetailDAO.findByOrder(order);

        Map<String, Object> orderInfo = new HashMap<>();
        orderInfo.put("id", order.getOrderCode());
        orderInfo.put("subtotal", order.getSubtotal());
        orderInfo.put("shippingFee", order.getShippingFee());
        orderInfo.put("discountAmount", order.getDiscountAmount() != null ? order.getDiscountAmount() : 0.0);
        orderInfo.put("total", order.getTotal());
        orderInfo.put("status", order.getStatus());
        orderInfo.put("createdAt", order.getCreatedAt().getTime());
        orderInfo.put("items", orderItems);

        Date estimatedDeliveryDate = calculateEstimatedDeliveryDate(order);
        if (estimatedDeliveryDate != null) {
            orderInfo.put("estimatedDeliveryDate", estimatedDeliveryDate.getTime());
        }

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

        System.out.println("Fetched order info for orderCode: " + orderCode);
        return orderInfo;
    }

    /**
     * Lấy danh sách đơn hàng của người dùng
     * 
     * @param user Người dùng
     * @return Danh sách đơn hàng
     */
    public List<Order> getUserOrders(User user) {
        List<Order> orders = orderDAO.findByUserOrderByCreatedAtDesc(user);
        System.out.println("Fetched " + orders.size() + " orders for user: " + user.getId());
        return orders;
    }

    /**
     * Lấy chi tiết đơn hàng theo mã đơn hàng
     * 
     * @param orderCode Mã đơn hàng
     * @return Đơn hàng
     */
    public Order getOrderByCode(String orderCode) {
        Order order = orderDAO.findByOrderCode(orderCode);
        System.out.println("Fetching order by code: " + orderCode + ", found: " + (order != null ? order.getOrderCode() : "null"));
        return order;
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
            System.out.println("Failed to update status: Order not found for orderCode: " + orderCode);
            return false;
        }
        order.setStatus(status);
        orderDAO.save(order);
        System.out.println("Order status updated to: " + status + " for orderCode: " + orderCode);
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
            System.out.println("Failed to cancel order: Order not found or user mismatch for orderCode: " + orderCode);
            return false;
        }

        if (!"Chờ xác nhận".equals(order.getStatus())) {
            System.out.println("Failed to cancel order: Invalid status " + order.getStatus() + " for orderCode: " + orderCode);
            return false;
        }

        List<OrderDetail> orderDetails = orderDetailDAO.findByOrder(order);
        for (OrderDetail detail : orderDetails) {
            Product product = detail.getProduct();
            if (product != null) {
                int currentAvailable = product.getAvailable();
                int returnQuantity = detail.getQuantity();
                product.setAvailable(currentAvailable + returnQuantity);
                productDAO.save(product);
                System.out.println("Restored " + returnQuantity + " units of product " + product.getName() + " to stock");
            }
        }

        order.setStatus("Đã hủy");
        orderDAO.save(order);
        System.out.println("Order cancelled for orderCode: " + orderCode);
        return true;
    }

    /**
     * Tính ngày giao hàng dự kiến
     * 
     * @param order Đơn hàng
     * @return Ngày giao hàng dự kiến
     */
    public Date calculateEstimatedDeliveryDate(Order order) {
        if ("Đã hủy".equals(order.getStatus()) || "Đã giao hàng".equals(order.getStatus())) {
            return null;
        }
        return ShippingCalculator.calculateEstimatedDeliveryDate(order.getCreatedAt(), order.getShippingProvince());
    }

    /**
     * Lấy số ngày vận chuyển
     * 
     * @param order Đơn hàng
     * @return Số ngày vận chuyển
     */
    public int getShippingDays(Order order) {
        return ShippingCalculator.calculateShippingDays(order.getShippingProvince());
    }

    /**
     * Lấy mô tả khoảng thời gian giao hàng
     * 
     * @param order Đơn hàng
     * @return Mô tả khoảng thời gian giao hàng
     */
    public String getDeliveryTimeRangeText(Order order) {
        int shippingDays = getShippingDays(order);
        return ShippingCalculator.getDeliveryTimeRangeText(shippingDays);
    }

    /**
     * Tính số ngày vận chuyển cho một tỉnh
     * 
     * @param province Tên tỉnh
     * @return Số ngày vận chuyển
     */
    public int calculateShippingDaysForProvince(String province) {
        return ShippingCalculator.calculateShippingDays(province);
    }

    /**
     * Lấy mô tả khoảng thời gian giao hàng cho một tỉnh
     * 
     * @param province Tên tỉnh
     * @return Mô tả khoảng thời gian giao hàng
     */
    public String getDeliveryTimeRangeForProvince(String province) {
        int shippingDays = calculateShippingDaysForProvince(province);
        return ShippingCalculator.getDeliveryTimeRangeText(shippingDays);
    }
    
    /**
     * Get the current location of an order during shipping
     * 
     * @param order The order
     * @return A map containing information about the current location and status
     */
    public Map<String, Object> getCurrentOrderLocation(Order order) {
        // Only calculate location for orders in "Đang giao hàng" status
        if (order == null || !"Đang giao hàng".equals(order.getStatus())) {
            return null;
        }

        // Get the order confirmation date
        // Since we don't have a specific confirmation date field, we'll estimate it
        // by subtracting 1 day from the current date if the order was created more than 1 day ago
        Date orderConfirmationDate = order.getCreatedAt();
        Date currentDate = new Date();

        // If order is older than 1 day, assume it was confirmed 1 day after creation
        long dayInMillis = 24 * 60 * 60 * 1000L;
        if (currentDate.getTime() - orderConfirmationDate.getTime() > dayInMillis) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(orderConfirmationDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            orderConfirmationDate = calendar.getTime();
        }

        // Calculate the current location using ShippingCalculator
        return ShippingCalculator.calculateCurrentOrderLocation(
            orderConfirmationDate,
            order.getShippingProvince()
        );
    }

    // Add this method to handle marking an order as received
    /**
     * Đánh dấu đơn hàng là đã nhận được
     * 
     * @param orderCode Mã đơn hàng
     * @param user Người dùng
     * @return true nếu cập nhật thành công, false nếu không
     */
    @Transactional
    public boolean markOrderAsReceived(String orderCode, User user) {
        Order order = orderDAO.findByOrderCode(orderCode);
        if (order == null || !order.getUser().getId().equals(user.getId())) {
            System.out.println("Failed to mark order as received: Order not found or user mismatch for orderCode: " + orderCode);
            return false;
        }

        if (!"Đang giao hàng".equals(order.getStatus())) {
            System.out.println("Failed to mark order as received: Invalid status " + order.getStatus() + " for orderCode: " + orderCode);
            return false;
        }

        order.setStatus("Đã giao hàng");
        orderDAO.save(order);
        
        try {
            notificationService.createOrderNotification(orderCode, user.getFullname());
        } catch (Exception e) {
            // Log lỗi nhưng không ảnh hưởng đến quá trình cập nhật trạng thái
            System.err.println("Lỗi khi tạo thông báo: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Order marked as received for orderCode: " + orderCode);
        return true;
    }
}

