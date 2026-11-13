package com.dysland.app.integration;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.dysland.app.R;
import com.dysland.app.data.ActivityStore;
import com.dysland.app.data.NavigationText;

/** Public notification bridge; no accessibility service, scraping or private APIs. */
public final class IslandNotificationListener extends NotificationListenerService {
    @Override public void onListenerConnected() {
        ActivityStore.INSTANCE.clearNotifications();
        StatusBarNotification[] active=getActiveNotifications();
        if(active!=null) for (StatusBarNotification sbn : active) onNotificationPosted(sbn);
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
        if ((!call && !navigation && !progress && !service) || (!sbn.isOngoing() && !(call && n.fullScreenIntent != null))) {
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
        if(navigation) {
            event.kind="navigation";
            java.util.LinkedHashSet<String> details=new java.util.LinkedHashSet<>();
            for(String field:new String[]{Notification.EXTRA_TEXT,Notification.EXTRA_BIG_TEXT,Notification.EXTRA_SUB_TEXT}) {
                CharSequence value=extras.getCharSequence(field);
                if(value!=null && value.length()>0) details.add(value.toString());
            }
            CharSequence[] lines=extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
            if(lines!=null) for(CharSequence line:lines) if(line!=null && line.length()>0) details.add(line.toString());
            event.subtitle=String.join(" • ",details);
            if(event.subtitle.length()>600) event.subtitle=event.subtitle.substring(0,600);
            event.compactText=NavigationText.distance(event.title+" "+event.subtitle);
            int turn=NavigationText.turn(event.title+" "+event.subtitle);
            event.icon=turn<0 ? R.drawable.ic_turn_left : turn>0 ? R.drawable.ic_turn_right : R.drawable.ic_location;
            event.source="NAVEGAÇÃO · "+label;
        } else if(call) { event.kind="call";event.compactText="Chamada"; }
        event.open = n.contentIntent;
        event.actions = n.actions;
        ActivityStore.INSTANCE.put(event);
    }
    @Override public void onNotificationRemoved(StatusBarNotification sbn) {
        ActivityStore.INSTANCE.remove("notification:" + sbn.getKey());
    }
}
