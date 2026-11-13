package com.dysland.app.data;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses only text actually published by the navigation app; never invents an ETA or route. */
public final class NavigationText {
    private static final Pattern DISTANCE=Pattern.compile("(?i)(?<![\\d.,])\\d+(?:[.,]\\d+)?\\s?(?:km|m)\\b");
    private NavigationText() {}
    public static String distance(String text) { Matcher matcher=DISTANCE.matcher(text==null ? "" : text);return matcher.find() ? matcher.group() : "GPS"; }
    public static int turn(String text) {
        String value=text==null ? "" : text.toLowerCase(Locale.ROOT);
        if(value.contains("esquerda") || value.contains("left")) return -1;
        if(value.contains("direita") || value.contains("right")) return 1;
        return 0;
    }
}
