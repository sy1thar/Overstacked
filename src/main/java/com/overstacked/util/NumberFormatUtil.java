package com.overstacked.util;

/**
 * Zero-allocation number formatter for inventory stack counts.
 * Uses precomputed immutable String tables for all possible 1-3 char outputs.
 */
public final class NumberFormatUtil {

    private static final String[] RAW = new String[1000];
    private static final String[] THOUSANDS = new String[1000];
    private static final String[] MILLIONS = new String[1000];
    private static final String BILLION = "1B";
    private static final String INFINITY = "\u221E";

    static {
        for (int i = 0; i < 1000; i++) {
            RAW[i] = Integer.toString(i);
            THOUSANDS[i] = i + "K";
            MILLIONS[i] = i + "M";
        }
    }

    private NumberFormatUtil() {}

    public static String formatCount(int count) {
        if (count < 0) return RAW[0];
        if (count == Integer.MAX_VALUE) return INFINITY;
        if (count < 1_000) return RAW[count];
        if (count < 1_000_000) return THOUSANDS[count / 1_000];
        if (count < 1_000_000_000) return MILLIONS[count / 1_000_000];
        return BILLION;
    }
}
