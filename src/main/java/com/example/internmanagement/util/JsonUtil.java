package com.example.internmanagement.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tiện ích JSON tối giản, KHÔNG dùng thư viện ngoài (Gson/Jackson...),
 * để tránh phụ thuộc vào pom.xml mà project có thể chưa khai báo sẵn.
 *
 * Chỉ hỗ trợ đúng nhu cầu của các Controller trong ticket này:
 *   - toJson(Map<String,Object>)  -> chuỗi JSON phẳng (không lồng object/array)
 *   - parseFlatObject(String json) -> Map<String,String> từ 1 JSON object phẳng
 *     (value luôn trả về dạng String, Controller tự ép kiểu khi cần số/long).
 *
 * Nếu sau này project cần JSON phức tạp hơn (object lồng nhau, mảng...),
 * nên thay bằng thư viện chuẩn (Gson/Jackson) và thêm vào pom.xml.
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static String toJson(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append(quote(entry.getKey())).append(":").append(toJsonValue(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    private static String toJsonValue(Object value) {
        if (value == null) return "null";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        return quote(value.toString());
    }

    private static String quote(String text) {
        return "\"" + text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "") + "\"";
    }

    /**
     * Parse một JSON object PHẲNG, dạng {"key":"value","key2":123}.
     * Không hỗ trợ object/array lồng nhau - đủ dùng cho body request
     * của các API xác nhận/yêu cầu chỉnh hợp đồng (chỉ gồm vài field string/number).
     */
    public static Map<String, String> parseFlatObject(String json) {
        Map<String, String> result = new LinkedHashMap<>();
        if (json == null) return result;
        String body = json.trim();
        if (body.startsWith("{")) body = body.substring(1);
        if (body.endsWith("}")) body = body.substring(0, body.length() - 1);
        if (body.isBlank()) return result;

        for (String pair : splitTopLevel(body)) {
            int colonIdx = pair.indexOf(':');
            if (colonIdx < 0) continue;
            String rawKey = pair.substring(0, colonIdx).trim();
            String rawValue = pair.substring(colonIdx + 1).trim();
            result.put(unquote(rawKey), unquote(rawValue));
        }
        return result;
    }

    private static String[] splitTopLevel(String body) {
        // Vì giá trị chỉ là string/number/boolean (không lồng object/array),
        // tách theo dấu phẩy ngoài cặp nháy kép là đủ.
        java.util.List<String> parts = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < body.length(); i++) {
            char ch = body.charAt(i);
            if (ch == '"' && (i == 0 || body.charAt(i - 1) != '\\')) inQuotes = !inQuotes;
            if (ch == ',' && !inQuotes) {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        if (current.length() > 0) parts.add(current.toString());
        return parts.toArray(new String[0]);
    }

    private static String unquote(String value) {
        String v = value.trim();
        if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
            return v.substring(1, v.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return v; // số, boolean, null giữ nguyên dạng chuỗi, Controller tự ép kiểu
    }
}
