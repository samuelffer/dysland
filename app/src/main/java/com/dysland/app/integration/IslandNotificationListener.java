package com.dysland.app.integration;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.dysland.app.R;
import com.dysland.app.data.ActivityStore;

/** Public notification bridge; no accessibility service, scraping or private APIs. */
public final class IslandNotificationListener extends NotificationListenerService {
    @Override public void onListenerConnected() {
        ActivityStore.INSTANCE.clearNotifications();
        for (StatusBarNotification sbn : getActiveNotifications()) onNotificationPosted(sbn);
    }
    @Override public void onListenerDisconnected() { ActivityStore.INSTANCE.clearNotifications(); }
    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn.getPackageName().equals(getPackageName())) return;
        Notification n = sbn.getNotification();
        String key = "notification:" + sbn.getKey();
        // MediaSession is handled separately. Only ongoing public categories are shown.
        if (n.extras.containsKey(Notification.EXTRA_MEDIA_SESSION)) { ActivityStore.INSTANCE.remove(key); return; }
        boolean call = Notification.CATEGORY_CALL.equals(n.category);
        boolean navigation = Notification.CATEGORY_NAVIGATION.equals(n.category);
        boolean progress = Notification.CATEGORY_PROGRESS.equals(n.category);
        boolean service = Notification.CATEGORY_SERVICE.equals(n.category);
        if ((!call && !navigation && !progress && !service) || (!call && !sbn.isOngoing())) {
            ActivityStore.INSTANCE.remove(key); return;
        }
        Bundle extras = n.extras;
        String label;
        try { label = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(sbn.getPackageName(),0)).toString(); }
        catch (android.content.pm.PackageManager.NameNotFoundException e) { label = sbn.getPackageName(); }
        CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence body = extras.getCharSequence(Notification.EXTRA_TEXT);
        ActivityStore.Event event = new ActivityStore.Event(key,
                title == null ? label : title.toString(), body == null ? label : body.toString(),
                call ? R.drawable.ic_phone : navigation ? R.drawable.ic_location : R.drawable.ic_info,
                call ? 100 : navigation ? 60 : 40);
        // Secret notifications are not surfaced by this overlay at all.
        if (n.visibility == Notification.VISIBILITY_SECRET) { ActivityStore.INSTANCE.remove(key); return; }
        event.open = n.contentIntent;
        event.actions = n.actions;
        ActivityStore.INSTANCE.put(event);
    }
    @Override public void onNotificationRemoved(StatusBarNotification sbn) {
        ActivityStore.INSTANCE.remove("notification:" + sbn.getKey());
    }
}
