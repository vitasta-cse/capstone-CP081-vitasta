package cricstats.storage;

import java.util.*;

public class JsonHelper {

    // Simple JSON Tokenizer & Parser
    public static class JsonValue {
        private final Object value;

        public JsonValue(Object value) {
            this.value = value;
        }

        public boolean isObject() { return value instanceof Map; }
        public boolean isArray() { return value instanceof List; }
        public boolean isString() { return value instanceof String; }
        public boolean isNumber() { return value instanceof Number; }
        public boolean isBoolean() { return value instanceof Boolean; }
        public boolean isNull() { return value == null; }

        @SuppressWarnings("unchecked")
        public Map<String, JsonValue> asObject() {
            return (Map<String, JsonValue>) value;
        }

        @SuppressWarnings("unchecked")
        public List<JsonValue> asArray() {
            return (List<JsonValue>) value;
        }

        public String asString() {
            return value != null ? value.toString() : "";
        }

        public int asInt(int defaultValue) {
            if (value instanceof Number) return ((Number) value).intValue();
            try { return Integer.parseInt(asString()); } catch (Exception e) { return defaultValue; }
        }

        public double asDouble(double defaultValue) {
            if (value instanceof Number) return ((Number) value).doubleValue();
            try { return Double.parseDouble(asString()); } catch (Exception e) { return defaultValue; }
        }

        public boolean asBoolean(boolean defaultValue) {
            if (value instanceof Boolean) return (Boolean) value;
            return defaultValue;
        }

        public JsonValue get(String key) {
            if (isObject()) {
                return asObject().get(key);
            }
            return null;
        }
    }

    public static JsonValue parse(String jsonStr) {
        if (jsonStr == null) return new JsonValue(null);
        String trimmed = jsonStr.trim();
        int[] index = new int[]{0};
        return parseValue(trimmed, index);
    }

    private static JsonValue parseValue(String s, int[] idx) {
        skipWhitespace(s, idx);
        if (idx[0] >= s.length()) return new JsonValue(null);

        char c = s.charAt(idx[0]);
        if (c == '{') {
            return parseObject(s, idx);
        } else if (c == '[') {
            return parseArray(s, idx);
        } else if (c == '"') {
            return parseString(s, idx);
        } else if (c == 't' || c == 'f') {
            return parseBoolean(s, idx);
        } else if (c == 'n') {
            return parseNull(s, idx);
        } else if (c == '-' || Character.isDigit(c)) {
            return parseNumber(s, idx);
        }
        return new JsonValue(null);
    }

    private static JsonValue parseObject(String s, int[] idx) {
        idx[0]++; // skip '{'
        Map<String, JsonValue> map = new LinkedHashMap<>();
        skipWhitespace(s, idx);

        while (idx[0] < s.length() && s.charAt(idx[0]) != '}') {
            skipWhitespace(s, idx);
            if (s.charAt(idx[0]) == '}') break;

            JsonValue keyVal = parseString(s, idx);
            String key = keyVal.asString();

            skipWhitespace(s, idx);
            if (idx[0] < s.length() && s.charAt(idx[0]) == ':') {
                idx[0]++; // skip ':'
            }

            JsonValue val = parseValue(s, idx);
            map.put(key, val);

            skipWhitespace(s, idx);
            if (idx[0] < s.length() && s.charAt(idx[0]) == ',') {
                idx[0]++; // skip ','
            }
        }
        if (idx[0] < s.length() && s.charAt(idx[0]) == '}') {
            idx[0]++;
        }
        return new JsonValue(map);
    }

    private static JsonValue parseArray(String s, int[] idx) {
        idx[0]++; // skip '['
        List<JsonValue> list = new ArrayList<>();
        skipWhitespace(s, idx);

        while (idx[0] < s.length() && s.charAt(idx[0]) != ']') {
            skipWhitespace(s, idx);
            if (s.charAt(idx[0]) == ']') break;

            JsonValue val = parseValue(s, idx);
            list.add(val);

            skipWhitespace(s, idx);
            if (idx[0] < s.length() && s.charAt(idx[0]) == ',') {
                idx[0]++; // skip ','
            }
        }
        if (idx[0] < s.length() && s.charAt(idx[0]) == ']') {
            idx[0]++;
        }
        return new JsonValue(list);
    }

    private static JsonValue parseString(String s, int[] idx) {
        idx[0]++; // skip opening '"'
        StringBuilder sb = new StringBuilder();
        while (idx[0] < s.length()) {
            char c = s.charAt(idx[0]++);
            if (c == '"') {
                break;
            } else if (c == '\\' && idx[0] < s.length()) {
                char esc = s.charAt(idx[0]++);
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(esc); break;
                }
            } else {
                sb.append(c);
            }
        }
        return new JsonValue(sb.toString());
    }

    private static JsonValue parseNumber(String s, int[] idx) {
        int start = idx[0];
        while (idx[0] < s.length()) {
            char c = s.charAt(idx[0]);
            if (Character.isDigit(c) || c == '.' || c == '-' || c == 'e' || c == 'E' || c == '+') {
                idx[0]++;
            } else {
                break;
            }
        }
        String numStr = s.substring(start, idx[0]);
        if (numStr.contains(".")) {
            return new JsonValue(Double.parseDouble(numStr));
        } else {
            return new JsonValue(Long.parseLong(numStr));
        }
    }

    private static JsonValue parseBoolean(String s, int[] idx) {
        if (s.startsWith("true", idx[0])) {
            idx[0] += 4;
            return new JsonValue(Boolean.TRUE);
        } else if (s.startsWith("false", idx[0])) {
            idx[0] += 5;
            return new JsonValue(Boolean.FALSE);
        }
        return new JsonValue(null);
    }

    private static JsonValue parseNull(String s, int[] idx) {
        if (s.startsWith("null", idx[0])) {
            idx[0] += 4;
        }
        return new JsonValue(null);
    }

    private static void skipWhitespace(String s, int[] idx) {
        while (idx[0] < s.length() && Character.isWhitespace(s.charAt(idx[0]))) {
            idx[0]++;
        }
    }

    // JSON Stringifier / Builder
    public static String escape(String str) {
        if (str == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c); break;
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
