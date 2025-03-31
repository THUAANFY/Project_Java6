// src/main/java/poly/asm/Utils/ShippingCalculator.java
package poly.asm.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;

public class ShippingCalculator {

    // Map of regions to shipping days
    private static final Map<String, Integer> regionShippingDays = new HashMap<>();

    // Initialize the shipping days for different regions
    static {
        // Major cities - faster delivery
        regionShippingDays.put("Thành phố Hà Nội", 1);
        regionShippingDays.put("Thành phố Hồ Chí Minh", 1);
        regionShippingDays.put("Thành phố Đà Nẵng", 2);
        regionShippingDays.put("Thành phố Hải Phòng", 2);
        regionShippingDays.put("Thành phố Cần Thơ", 2);

        // Northern regions
        regionShippingDays.put("Tỉnh Bắc Ninh", 2);
        regionShippingDays.put("Tỉnh Hải Dương", 2);
        regionShippingDays.put("Tỉnh Hưng Yên", 2);
        regionShippingDays.put("Tỉnh Vĩnh Phúc", 2);
        regionShippingDays.put("Tỉnh Quảng Ninh", 3);
        regionShippingDays.put("Tỉnh Thái Nguyên", 3);
        regionShippingDays.put("Tỉnh Phú Thọ", 3);
        regionShippingDays.put("Tỉnh Bắc Giang", 3);
        regionShippingDays.put("Tỉnh Nam Định", 3);
        regionShippingDays.put("Tỉnh Thái Bình", 3);
        regionShippingDays.put("Tỉnh Ninh Bình", 3);
        regionShippingDays.put("Tỉnh Hà Nam", 3);

        // Central regions
        regionShippingDays.put("Tỉnh Thành phố Huế", 3);
        regionShippingDays.put("Tỉnh Quảng Nam", 3);
        regionShippingDays.put("Tỉnh Quảng Ngãi", 3);
        regionShippingDays.put("Tỉnh Bình Định", 3);
        regionShippingDays.put("Tỉnh Phú Yên", 4);
        regionShippingDays.put("Tỉnh Khánh Hòa", 4);
        regionShippingDays.put("Tỉnh Ninh Thuận", 4);
        regionShippingDays.put("Tỉnh Bình Thuận", 4);
        regionShippingDays.put("Tỉnh Thanh Hóa", 3);
        regionShippingDays.put("Tỉnh Nghệ An", 3);
        regionShippingDays.put("Tỉnh Hà Tĩnh", 3);
        regionShippingDays.put("Tỉnh Quảng Bình", 3);
        regionShippingDays.put("Tỉnh Quảng Trị", 3);

        // Southern regions
        regionShippingDays.put("Tỉnh Bình Dương", 2);
        regionShippingDays.put("Tỉnh Đồng Nai", 2);
        regionShippingDays.put("Tỉnh Long An", 2);
        regionShippingDays.put("Tỉnh Bà Rịa - Vũng Tàu", 3);
        regionShippingDays.put("Tỉnh Tây Ninh", 3);
        regionShippingDays.put("Tỉnh Tiền Giang", 3);
        regionShippingDays.put("Tỉnh Bến Tre", 3);
        regionShippingDays.put("Tỉnh Vĩnh Long", 3);
        regionShippingDays.put("Tỉnh Trà Vinh", 3);
        regionShippingDays.put("Tỉnh Đồng Tháp", 3);
        regionShippingDays.put("Tỉnh An Giang", 3);
        regionShippingDays.put("Tỉnh Kiên Giang", 4);
        regionShippingDays.put("Tỉnh Cà Mau", 4);
        regionShippingDays.put("Tỉnh Hậu Giang", 3);
        regionShippingDays.put("Tỉnh Sóc Trăng", 3);
        regionShippingDays.put("Tỉnh Bạc Liêu", 4);

        // Highland regions
        regionShippingDays.put("Tỉnh Lâm Đồng", 4);
        regionShippingDays.put("Tỉnh Đắk Lắk", 4);
        regionShippingDays.put("Tỉnh Đắk Nông", 4);
        regionShippingDays.put("Tỉnh Gia Lai", 4);
        regionShippingDays.put("Tỉnh Kon Tum", 4);

        // Remote northern regions
        regionShippingDays.put("Tỉnh Lào Cai", 5);
        regionShippingDays.put("Tỉnh Yên Bái", 5);
        regionShippingDays.put("Tỉnh Hòa Bình", 4);
        regionShippingDays.put("Tỉnh Sơn La", 5);
        regionShippingDays.put("Tỉnh Điện Biên", 6);
        regionShippingDays.put("Tỉnh Lai Châu", 6);
        regionShippingDays.put("Tỉnh Hà Giang", 5);
        regionShippingDays.put("Tỉnh Cao Bằng", 5);
        regionShippingDays.put("Tỉnh Bắc Kạn", 5);
        regionShippingDays.put("Tỉnh Lạng Sơn", 5);
        regionShippingDays.put("Tỉnh Tuyên Quang", 5);
    }

    /**
     * Calculate the estimated shipping days based on the province
     * 
     * @param province The shipping province
     * @return The number of shipping days
     */
    public static int calculateShippingDays(String province) {
        // Default shipping days if the province is not in our map
        int defaultDays = 4;

        // Return the shipping days for the province, or the default if not found
        return regionShippingDays.getOrDefault(province, defaultDays);
    }

    /**
     * Calculate the estimated delivery date based on the order date and shipping
     * province
     * 
     * @param orderDate The date the order was placed
     * @param province  The shipping province
     * @return The estimated delivery date
     */
    public static Date calculateEstimatedDeliveryDate(Date orderDate, String province) {
        // Get the shipping days for the province
        int shippingDays = calculateShippingDays(province);

        // Create a calendar instance and set it to the order date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(orderDate);

        // Add processing time (1 day)
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        // Add shipping days
        calendar.add(Calendar.DAY_OF_MONTH, shippingDays);

        // Skip weekends (optional, remove if you deliver on weekends)
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, 2); // Skip to Monday
        } else if (dayOfWeek == Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Skip to Monday
        }

        return calendar.getTime();
    }

    /**
     * Get the delivery time range text based on shipping days
     * 
     * @param shippingDays The number of shipping days
     * @return A text description of the delivery time range
     */
    public static String getDeliveryTimeRangeText(int shippingDays) {
        switch (shippingDays) {
            case 1:
                return "1-2 ngày";
            case 2:
                return "2-3 ngày";
            case 3:
                return "3-4 ngày";
            case 4:
                return "4-5 ngày";
            case 5:
                return "5-7 ngày";
            case 6:
                return "6-8 ngày";
            default:
                return "7-10 ngày";
        }
    }
}