package com.example.web.tools;

/**
 * 隐私脱敏工具类（用于数据导出等场景）
 */
public class PrivacyMaskingUtils {

    private PrivacyMaskingUtils() {
    }

    public static String maskPhone(String phone) {
        if (!Extension.isNotNullOrEmpty(phone)) return phone;
        String p = phone.trim();
        if (p.length() < 7) return p;
        return p.substring(0, 3) + "****" + p.substring(p.length() - 4);
    }

    public static String maskEmail(String email) {
        if (!Extension.isNotNullOrEmpty(email)) return email;
        String e = email.trim();
        int at = e.indexOf("@");
        if (at <= 1) return e;
        return e.substring(0, 1) + "****" + e.substring(at);
    }

    public static String maskName(String name) {
        if (!Extension.isNotNullOrEmpty(name)) return name;
        String n = name.trim();
        if (n.length() <= 1) return n;
        return n.substring(0, 1) + "**";
    }
}

