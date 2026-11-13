package com.dysland.app.data;

import java.util.Locale;

/** Millisecond arithmetic independent of the platform; unknown/live durations cannot be scrubbed. */
public final class MediaTime {
    private MediaTime() {}
    public static long position(long base,long updated,long now,float speed,boolean playing,long duration) {
        long value=Math.max(0,base);
        if (playing && updated>0) value+=(long)(Math.max(0,now-updated)*speed);
        return Math.max(0,duration>0 ? Math.min(duration,value) : value);
    }
    public static long fromProgress(int progress,long duration) { return duration<=0 ? 0 : Math.round(Math.max(0,Math.min(1000,progress))/1000d*duration); }
    public static String label(long millis) {
        long seconds=Math.max(0,millis)/1000;
        return seconds>=3600 ? String.format(Locale.ROOT,"%d:%02d:%02d",seconds/3600,seconds/60%60,seconds%60) : String.format(Locale.ROOT,"%d:%02d",seconds/60,seconds%60);
    }
}
