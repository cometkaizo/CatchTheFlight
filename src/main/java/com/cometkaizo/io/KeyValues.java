package com.cometkaizo.io;

public class KeyValues {
    public static final char SEPARATOR = ':';
    public static final char ENTRY_SEPARATOR = '\n';

    public static String build(String key, String value) {
        String escape = escape(value);
        return escape(key) + SEPARATOR + escape + ENTRY_SEPARATOR;
    }

    public static String escape(String string) {
        return string == null ? null : string
                .replaceAll("\\\\", "\\\\\\\\")
                .replaceAll(String.valueOf(SEPARATOR), "\\\\" + SEPARATOR)
                .replaceAll(String.valueOf(ENTRY_SEPARATOR), "\\\\" + ENTRY_SEPARATOR);
    }

    public static String unescape(String escaped) {
        return escaped == null ? null : escaped
                .replaceAll("\\\\\\\\", "\\\\")
                .replaceAll("\\\\" + SEPARATOR, String.valueOf(SEPARATOR))
                .replaceAll("\\\\" + ENTRY_SEPARATOR, String.valueOf(ENTRY_SEPARATOR));
    }

}
