package com.tp.snapseat.booking_service.util;

/** Tiny JSON helpers for the few places that must write JSON outside Spring MVC (security errors, Kafka payload). */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static String quote(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + escape(value) + "\"";
    }

    public static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
