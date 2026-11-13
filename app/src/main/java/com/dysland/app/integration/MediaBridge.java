package com.dysland.app.integration;

import android.content.ComponentName;
import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import java.util.List;

/** Observes the current media session; actions are checked against advertised capabilities. */
public final class MediaBridge {
    private final MediaSessionManager manager;
    private final ComponentName listener;
    private final Runnable changed;
    private MediaController controller;
    private boolean registered;
    private final MediaController.Callback callback = new MediaController.Callback() {
        @Override public void onPlaybackStateChanged(PlaybackState state) { changed.run(); }
        @Override public void onMetadataChanged(MediaMetadata metadata) { changed.run(); }
        @Override public void onSessionDestroyed() { refresh(); }
    };
    private final MediaSessionManager.OnActiveSessionsChangedListener sessions = this::select;
    public MediaBridge(Context context, Runnable changed) {
        manager = context.getSystemService(MediaSessionManager.class);
        listener = new ComponentName(context, IslandNotificationListener.class);
        this.changed = changed;
    }
    public void refresh() {
        try {
            if (!registered) {
                manager.addOnActiveSessionsChangedListener(sessions, listener, new Handler(Looper.getMainLooper()));
                registered = true;
            }
            select(manager.getActiveSessions(listener));
        } catch (SecurityException denied) { select(java.util.Collections.emptyList()); }
    }
    private void select(List<MediaController> list) {
        MediaController next = null;
        if (list != null) for (MediaController candidate : list) {
            if (next == null) next=candidate;
            PlaybackState state=candidate.getPlaybackState();
            if (state != null && state.getState() == PlaybackState.STATE_PLAYING) { next=candidate; break; }
        }
        if (controller != null) controller.unregisterCallback(callback);
        controller=next;
        if (controller != null) controller.registerCallback(callback, new Handler(Looper.getMainLooper()));
        changed.run();
    }
    public MediaController current() { return controller; }
    public static boolean playing(MediaController c) {
        return c != null && c.getPlaybackState() != null && c.getPlaybackState().getState() == PlaybackState.STATE_PLAYING;
    }
    public static boolean supports(MediaController c, long action) {
        return c != null && c.getPlaybackState() != null && (c.getPlaybackState().getActions() & action) != 0;
    }
    public void toggle() {
        if (controller == null) return;
        boolean playing=playing(controller);
        if (supports(controller, playing ? PlaybackState.ACTION_PAUSE : PlaybackState.ACTION_PLAY) || supports(controller, PlaybackState.ACTION_PLAY_PAUSE)) {
            if (playing) controller.getTransportControls().pause(); else controller.getTransportControls().play();
        }
    }
    public void next() { if (supports(controller, PlaybackState.ACTION_SKIP_TO_NEXT)) controller.getTransportControls().skipToNext(); }
    public void close() {
        if (controller != null) controller.unregisterCallback(callback);
        if (registered) manager.removeOnActiveSessionsChangedListener(sessions);
    }
}
