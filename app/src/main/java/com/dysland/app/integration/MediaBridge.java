package com.dysland.app.integration;

import android.content.ComponentName;
import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import com.dysland.app.data.MediaTime;
import java.util.List;

/** Observes the current media session; actions are checked against advertised capabilities. */
public final class MediaBridge {
    private final MediaSessionManager manager;
    private final ComponentName listener;
    private final Runnable changed;
    private MediaController controller;
    private boolean registered,closed;
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
        if(closed) return;
        try {
            if (!registered) {
                manager.addOnActiveSessionsChangedListener(sessions, listener, new Handler(Looper.getMainLooper()));
                registered = true;
            }
            select(manager.getActiveSessions(listener));
        } catch (SecurityException denied) { select(java.util.Collections.emptyList()); }
    }
    private void select(List<MediaController> list) {
        if(closed) return;
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
    public void previous() { if(supports(controller,PlaybackState.ACTION_SKIP_TO_PREVIOUS)) controller.getTransportControls().skipToPrevious(); }
    public long duration() {
        MediaMetadata data=controller==null ? null : controller.getMetadata();
        return data==null ? 0 : Math.max(0,data.getLong(MediaMetadata.METADATA_KEY_DURATION));
    }
    public long position() {
        PlaybackState state=controller==null ? null : controller.getPlaybackState();
        return state==null ? 0 : MediaTime.position(state.getPosition(),state.getLastPositionUpdateTime(),SystemClock.elapsedRealtime(),state.getPlaybackSpeed(),playing(controller),duration());
    }
    public void seekTo(long position) {
        if(duration()>0 && supports(controller,PlaybackState.ACTION_SEEK_TO)) controller.getTransportControls().seekTo(Math.max(0,Math.min(duration(),position)));
    }
    public void close() {
        closed=true;
        if (controller != null) controller.unregisterCallback(callback);
        if (registered) manager.removeOnActiveSessionsChangedListener(sessions);
    }
}
