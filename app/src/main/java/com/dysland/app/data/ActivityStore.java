package com.dysland.app.data;

import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import java.util.LinkedHashMap;
import java.util.Map;

/** In-memory only: notification content is never saved to disk or sent over the network. */
public final class ActivityStore {
    public static final ActivityStore INSTANCE = new ActivityStore();
    public static final class Event {
        public String key, title, subtitle;
        public int icon, priority;
        public long expires;
        public Bitmap art;
        public PendingIntent open;
        public Notification.Action[] actions;
        public Event(String key, String title, String subtitle, int icon, int priority) {
            this.key=key; this.title=title; this.subtitle=subtitle; this.icon=icon; this.priority=priority;
        }
    }
    private final Map<String,Event> events = new LinkedHashMap<>();
    private final Handler main = new Handler(Looper.getMainLooper());
    public Runnable observer;
    public void put(Event event) { main.post(() -> { events.remove(event.key); events.put(event.key,event); changed(); }); }
    public void remove(String key) { main.post(() -> { events.remove(key); changed(); }); }
    public void clearNotifications() { main.post(() -> { events.entrySet().removeIf(e -> e.getKey().startsWith("notification:")); changed(); }); }
    public Event top(long now, boolean notifications, boolean signals) {
        events.values().removeIf(e -> e.expires > 0 && e.expires <= now);
        Event result = null;
        for (Event e : events.values()) {
            if (e.key.startsWith("notification:") ? !notifications : !signals) continue;
            if (result == null || e.priority >= result.priority) result=e;
        }
        return result;
    }
    private void changed() { if (observer != null) observer.run(); }
}
