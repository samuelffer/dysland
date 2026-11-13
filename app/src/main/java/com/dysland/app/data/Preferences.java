package com.dysland.app.data;

import android.content.Context;
import android.content.SharedPreferences;

/** Single source of persistent, non-sensitive user configuration. Geometry is in dp. */
public final class Preferences {
    public final SharedPreferences values;
    public Preferences(Context context) { values = context.getSharedPreferences("island", Context.MODE_PRIVATE); }
    public int number(String key, int fallback) { return values.getInt(key, fallback); }
    public boolean enabled(String key) { return values.getBoolean(key, !"reduce_motion".equals(key)); }
    public void set(String key, int value) { values.edit().putInt(key, value).apply(); }
    public void set(String key, boolean value) { values.edit().putBoolean(key, value).apply(); }
    public int width() { return Math.max(140, Math.min(320, number("width", 190))); }
    public int height() { return Math.max(40, Math.min(80, number("height", 44))); }
    public int top() { return Math.max(0, Math.min(100, number("top", 8))); }
    public int offset() { return Math.max(-220, Math.min(220, number("offset", 0))); }
}
