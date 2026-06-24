package com.travel;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // 验证旧哈希
        boolean match = encoder.matches("admin123", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi");
        System.out.println("旧哈希匹配: " + match);
        // 生成新哈希
        if (!match) {
            String newHash = encoder.encode("admin123");
            System.out.println("新哈希: " + newHash);
        }
    }
}
