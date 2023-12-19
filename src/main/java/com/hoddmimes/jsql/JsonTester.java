package com.hoddmimes.jsql;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.function.BiPredicate;
import java.util.regex.Pattern;

public class JsonTester {
    BiPredicate<Object, Object> GT = (x, y) -> {
        if (x instanceof Double) {
            return (((Double) x) > ((Double) y));
        }
        if (x instanceof Float) {
            return (((Float) x) > ((Float) y));
        }
        if (x instanceof Long) {
            return (((Long) x) > ((Long) y));
        }
        if (x instanceof Integer) {
            return (((Integer) x) > ((Integer) y));
        }
        if (x instanceof String) {
            int t = ((String) x).compareTo((String) y);
            return (t > 0);
        }
        throw new RuntimeException("Invalid number type");
    };

    BiPredicate<Object, Object> GTE = (x, y) -> {
        if (x instanceof Double) {
            return (((Double) x) >= ((Double) y));
        }
        if (x instanceof Float) {
            return (((Float) x) >= ((Float) y));
        }
        if (x instanceof Long) {
            return (((Long) x) >= ((Long) y));
        }
        if (x instanceof Integer) {
            return (((Integer) x) >= ((Integer) y));
        }
        if (x instanceof String) {
            int t = ((String) x).compareTo((String) y);
            return (t >= 0);
        }
        throw new RuntimeException("Invalid number type");
    };

    BiPredicate<Object, Object> LT = (x, y) -> {
        if (x instanceof Double) {
            return (((Double) x) < ((Double) y));
        }
        if (x instanceof Float) {
            return (((Float) x) < ((Float) y));
        }
        if (x instanceof Long) {
            return (((Long) x) < ((Long) y));
        }
        if (x instanceof Integer) {
            return (((Integer) x) < ((Integer) y));
        }
        if (x instanceof String) {
            int t = ((String) x).compareTo((String) y);
            return (t < 0);
        }
        throw new RuntimeException("Invalid number type");
    };

    BiPredicate<Object, Object> LTE = (x, y) -> {
        if (x instanceof Double) {
            return (((Double) x) <= ((Double) y));
        }
        if (x instanceof Float) {
            return (((Float) x) <= ((Float) y));
        }
        if (x instanceof Long) {
            return (((Long) x) <= ((Long) y));
        }
        if (x instanceof Integer) {
            return (((Integer) x) <= ((Integer) y));
        }
        if (x instanceof String) {
            int t = ((String) x).compareTo((String) y);
            return (t <= 0);
        }
        throw new RuntimeException("Invalid number type");
    };

    BiPredicate<Object, Object> EQ = (x, y) -> {
        if (x instanceof Double) {
            return (((Double) x) == ((Double) y));
        }
        if (x instanceof Float) {
            return (((Float) x) == ((Float) y));
        }
        if (x instanceof Long) {
            return (((Long) x) == ((Long) y));
        }
        if (x instanceof Integer) {
            return (((Integer) x) == ((Integer) y));
        }
        if (x instanceof String) {
            int t = ((String) x).compareTo((String) y);
            return (t == 0);
        }
        if (x instanceof Boolean) {
            return (((Boolean) x) == ((Boolean) y));
        }
        throw new RuntimeException("Invalid number type");
    };

    BiPredicate<Object, Object> NE = (x, y) -> {
        if (x instanceof Double) {
            return (((Double) x) != ((Double) y));
        }
        if (x instanceof Float) {
            return (((Float) x) != ((Float) y));
        }
        if (x instanceof Long) {
            return (((Long) x) != ((Long) y));
        }
        if (x instanceof Integer) {
            return (((Integer) x) != ((Integer) y));
        }
        if (x instanceof String) {
            int t = ((String) x).compareTo((String) y);
            return (t != 0);
        }
        if (x instanceof Boolean) {
            return (((Boolean) x) != ((Boolean) y));
        }
        throw new RuntimeException("Invalid number type");
    };

    BiPredicate<Object, Object> AND = (x, y) -> {
        return ((boolean) x && (boolean) y);
    };
    BiPredicate<Object, Object> OR = (x, y) -> {
        return ((boolean) x || (boolean) y);
    };

    BiPredicate<Object, Object> LIKE = (s, pattern) -> {
        final String regex = sqlToRegexLike((String) pattern, (char) 0);
        return Pattern.matches(regex, (String) s);
    };


    static final String JAVA_REGEX_SPECIALS = "([](){}.*+?$^|#\\)";

    static String sqlToRegexLike(String sqlPattern, char escapeChar) {
        int i;
        final int len = sqlPattern.length();
        final StringBuilder javaPattern = new StringBuilder(len + len);
        for (i = 0; i < len; i++) {
            char c = sqlPattern.charAt(i);
            if (JAVA_REGEX_SPECIALS.indexOf(c) >= 0) {
                javaPattern.append('\\');
            }
            if (c == escapeChar) {
                if (i == (sqlPattern.length() - 1)) {
                    throw new RuntimeException("Invalid escape character in \"" + sqlPattern + "\" at position " + String.valueOf(i));
                }
                char nextChar = sqlPattern.charAt(i + 1);
                if ((nextChar == '_')
                        || (nextChar == '%')
                        || (nextChar == escapeChar)) {
                    javaPattern.append(nextChar);
                    i++;
                } else {
                    throw new RuntimeException("Invalid escape character in \"" + sqlPattern + "\" at position " + String.valueOf(i));
                }
            } else if (c == '_') {
                javaPattern.append('.');
            } else if (c == '%') {
                javaPattern.append("(?s:.*)");
            } else {
                javaPattern.append(c);
            }
        }
        return javaPattern.toString();
    }

    boolean jsonTest(JSDBFilter.FILTER_LOGIC pLogic, Object x, Object y) {
        switch (pLogic) {
            case $GT:
                return this.GT.test(x, y);
            case $GTE:
                return this.GTE.test(x, y);
            case $EQ:
                return this.EQ.test(x, y);
            case $NE:
                return this.NE.test(x, y);
            case $LT:
                return this.LT.test(x, y);
            case $LTE:
                return this.LTE.test(x, y);
            case $LIKE:
                return this.LIKE.test(x, y);
            case $AND:
                return this.AND.test(x, y);
            case $OR:
                return this.OR.test(x, y);
        }
        throw new RuntimeException("Unsupported json-tester logical operator");
    }

    public static JsonElement getJsonElement( String pFieldId, JsonObject jObject ) throws JsonParseException {
        JsonObject jObj = jObject;
        String[] jfids = pFieldId.split("\\.");
        for (int i = 0; i < jfids.length - 1; i++) {
            if (!jObj.has(jfids[i])) {
                return null;
            }
            jObj = jObj.getAsJsonObject(jfids[i]);
        }
        if (!jObj.has(jfids[jfids.length - 1])) {
            return null;
        }
        return jObj.get(jfids[jfids.length - 1]);
    }



    public static void main(String[] args) {
        JsonTester t = new JsonTester();
        System.out.println(t.jsonTest(JSDBFilter.FILTER_LOGIC.$LIKE, "foobar", "fo_bar"));
    }
}
