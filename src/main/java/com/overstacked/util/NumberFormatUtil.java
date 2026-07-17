package com.overstacked.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Zero-allocation number formatter for inventory stack counts.
 *
 * WHY: Vanilla calls String.valueOf(int) every frame for every visible slot.
 * With our mod, counts like 10000 create new Strings 60-240×/sec per slot.
 * Across 40+ visible slots this causes ~10,000 String allocations/sec → GC spikes.
 *
 * FIX: Lazy-cache all formatted Strings in an Int2ObjectOpenHashMap.
 * First call for a given count allocates one String; all subsequent calls
 * are O(1) map lookups with ZERO allocation. The map stabilizes within
 * ~1-2 seconds of gameplay and never allocates again.
 *
 * Thread-safety: Rendering is single-threaded (client render thread only),
 * so no synchronization is needed.
 */
public final class NumberFormatUtil {

    private static final Int2ObjectOpenHashMap<String> CACHE = new Int2ObjectOpenHashMap<>(512);
    private static final String INFINITY = "\u221E";
    private static final String ZERO = "0";

    static {
        CACHE.defaultReturnValue(null);
        for (int i = 0; i < 100; i++) {
            CACHE.put(i, Integer.toString(i));
        }
        CACHE.put(Integer.MAX_VALUE, INFINITY);
    }

    private NumberFormatUtil() {}

    /**
     * Format a count into a compact 1-3 character string.
     * Returns a cached String reference — safe for identity comparison
     * and guaranteed to not allocate on repeated calls with the same value.
     */
    public static String formatCount(int count) {
        if (count < 0) return ZERO;
        if (count < 100) return CACHE.get(count);

        String cached = CACHE.get(count);
        if (cached != null) return cached;

        String formatted;
        if (count < 1_000) {
            formatted = Integer.toString(count);
        } else if (count < 1_000_000) {
            formatted = (count / 1_000) + "K";
        } else if (count < 1_000_000_000) {
            formatted = (count / 1_000_000) + "M";
        } else {
            formatted = "1B";
        }

        CACHE.put(count, formatted);
        return formatted;
    }
}
