package poly.asm.Utils.Momo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
// import javax.annotation.PostConstruct;

@Configuration
public class MomoConfig {
   // Thông tin tài khoản test MoMo
   public static final String PARTNER_CODE = "MOMOBKUN20180529";
   public static final String ACCESS_KEY = "klm05TvNBzhg7h7j";
   public static final String SECRET_KEY = "at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa";
   
   // URL API của MoMo
   public static final String CREATE_ORDER_URL = "https://test-payment.momo.vn/v2/gateway/api/create";
   public static final String QUERY_ORDER_URL = "https://test-payment.momo.vn/v2/gateway/api/query";
   
   // URL callback khi thanh toán hoàn tất - sẽ được khởi tạo từ baseUrl
   public static String IPN_URL;
   public static String REDIRECT_URL;
   
   @Value("${app.base-url:http://localhost:8080}")
   private String baseUrl;
   
   @jakarta.annotation.PostConstruct
   public void init() {
       // Khởi tạo các URL static khi bean được tạo
       IPN_URL = baseUrl + "/api/momo/ipn";
       REDIRECT_URL = baseUrl + "/order/momo-return";
   }
   
   public String getIpnUrl() {
       return baseUrl + "/api/momo/ipn";
   }
   
   public String getRedirectUrl() {
       return baseUrl + "/order/momo-return";
   }
}

