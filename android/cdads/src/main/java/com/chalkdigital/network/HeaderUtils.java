package com.chalkdigital.network;

import android.support.annotation.Nullable;

import com.chalkdigital.common.util.ResponseHeader;
import com.chalkdigital.common.util.Utils;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class HeaderUtils {
    @Nullable
    public static String extractHeader(Map<String, String> headers, ResponseHeader responseHeader) {
        return headers.get(responseHeader.getKey());
    }

    public static Integer extractIntegerHeader(Map<String, String> headers, ResponseHeader responseHeader) {
        return formatIntHeader(extractHeader(headers, responseHeader));
    }

    public static boolean extractBooleanHeader(Map<String, String> headers, ResponseHeader responseHeader, boolean defaultValue) {
        return formatBooleanHeader(extractHeader(headers, responseHeader), defaultValue);
    }

    public static Integer extractPercentHeader(Map<String, String> headers, ResponseHeader responseHeader) {
        return formatPercentHeader(extractHeader(headers, responseHeader));
    }

    @Nullable
    public static String extractPercentHeaderString(Map<String, String> headers,
            ResponseHeader responseHeader) {
        Integer percentHeaderValue = extractPercentHeader(headers, responseHeader);
        return percentHeaderValue != null ? percentHeaderValue.toString() : null;
    }

    private static boolean formatBooleanHeader(@Nullable String headerValue, boolean defaultValue) {
        if (headerValue == null) {
            return defaultValue;
        }
        return headerValue.equals("1");
    }

    private static Integer formatIntHeader(String headerValue) {
        try {
            return Integer.parseInt(headerValue);
        } catch (Exception e) {
                        Utils.logStackTrace(e);
            // Continue below if we can't parse it quickly
        }

        // The number format way of parsing integers is way slower than Integer.parseInt, but
        // for numbers like 3.14, we would like to return 3, not null.
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        numberFormat.setParseIntegerOnly(true);

        try {
            Number value = numberFormat.parse(headerValue.trim());
            return value.intValue();
        } catch (Exception e) {
                        Utils.logStackTrace(e);
            return null;
        }
    }

    @Nullable
    private static Integer formatPercentHeader(@Nullable String headerValue) {
        if (headerValue == null) {
            return null;
        }

        final Integer percentValue = formatIntHeader(headerValue.replace("%", ""));

        if (percentValue == null || percentValue < 0 || percentValue > 100) {
            return null;
        }

        return percentValue;
    }
}
