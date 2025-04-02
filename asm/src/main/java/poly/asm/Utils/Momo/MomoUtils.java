package poly.asm.Utils.Momo;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

@Component
public class MomoUtils {
    // Tạo mã giao dịch ngẫu nhiên
    public static String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
    
    // Tạo chữ ký cho request MoMo
    public static String signHmacSHA256(String data, String secretKey) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        
        return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
    
    // Tạo chuỗi raw signature
    public static String createRawSignature(String requestId, String orderId, String amount, String orderInfo, String extraData, String requestType) {
        return "accessKey=" + MomoConfig.ACCESS_KEY + 
               "&amount=" + amount + 
               "&extraData=" + extraData + 
               "&ipnUrl=" + MomoConfig.IPN_URL + 
               "&orderId=" + orderId + 
               "&orderInfo=" + orderInfo + 
               "&partnerCode=" + MomoConfig.PARTNER_CODE + 
               "&redirectUrl=" + MomoConfig.REDIRECT_URL + 
               "&requestId=" + requestId + 
               "&requestType=" + requestType;
    }
}

