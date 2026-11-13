package com.dysland.app.data;

/** Monotonic clock arithmetic, independent of Android and wall-clock changes. */
public final class TimeState {
    private TimeState() {}
    public static long seconds(long now, long start, long duration) {
        long elapsed = Math.max(0, now - start);
        return duration > 0 ? Math.max(0, (duration - elapsed + 999) / 1000) : elapsed / 1000;
    }
    public static String format(long seconds) {
        return String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
    }
}
