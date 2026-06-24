package com.travel.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单号生成工具（线程安全）
 */
public class OrderIdGenerator {

    private static final String DATE_FORMAT = "yyyyMMddHHmmss";
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER =
            ThreadLocal.withInitial(() -> new SimpleDateFormat(DATE_FORMAT));

    /**
     * 生成订单号：时间戳 + 4位随机数
     */
    public static String generate() {
        String timestamp = DATE_FORMATTER.get().format(new Date());
        int random = 1000 + ThreadLocalRandom.current().nextInt(9000);
        return timestamp + random;
    }
}
