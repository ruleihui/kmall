package net.kaaass.kmall.util;

import org.springframework.util.ClassUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class StringUtils {

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String orderId(String lastOrderId) {
        if (lastOrderId.length() != 12) {
            return Constants.INIT_ORDER_ID;
        }
        var dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        var oldOrderNumStr = lastOrderId.substring(lastOrderId.length() - 4, lastOrderId.length());
        var oldNum = Integer.valueOf(oldOrderNumStr);
        var orderNum = String.format("%04d", oldNum + 1);
        return dateStr + orderNum;
    }

    public static String resourcePath() {
        return Objects.requireNonNull(Objects.requireNonNull(ClassUtils.getDefaultClassLoader()).getResource("")).getPath();
    }
}
