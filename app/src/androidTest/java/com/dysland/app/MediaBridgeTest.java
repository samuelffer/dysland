package com.dysland.app;

import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.dysland.app.integration.MediaBridge;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

/** Real framework MediaSession + transport commands, not a mocked player or fake service event. */
@RunWith(AndroidJUnit4.class)
public final class MediaBridgeTest {
    private void shell(String command) throws Exception {
        try (ParcelFileDescriptor descriptor=InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(command);
             FileInputStream in=new FileInputStream(descriptor.getFileDescriptor())) {
            byte[] bytes=new byte[4096];while(in.read(bytes)!=-1) { }
        }
    }
    @Test public void actualSessionDeliversMetadataPauseAndNext() throws Exception {
        shell("cmd notification allow_listener com.dysland.app/com.dysland.app.integration.IslandNotificationListener");
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        final MediaSession[] session=new MediaSession[1]; final MediaBridge[] bridge=new MediaBridge[1];
        CountDownLatch pause=new CountDownLatch(1),next=new CountDownLatch(1);
        try (ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                session[0]=new MediaSession(context,"Dysland integration test");
                session[0].setCallback(new MediaSession.Callback() {
                    @Override public void onPause() { pause.countDown(); }
                    @Override public void onSkipToNext() { next.countDown(); }
                });
                session[0].setMetadata(new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE,"Sessão de teste").build());
                session[0].setPlaybackState(new PlaybackState.Builder().setActions(PlaybackState.ACTION_PLAY|PlaybackState.ACTION_PAUSE|PlaybackState.ACTION_SKIP_TO_NEXT).setState(PlaybackState.STATE_PLAYING,0,1).build());
                session[0].setActive(true);
                bridge[0]=new MediaBridge(context,() -> { });
            });
            SystemClock.sleep(500);
            scenario.onActivity(activity -> {
                bridge[0].refresh();
                assertNotNull(bridge[0].current());
                assertEquals("Sessão de teste",bridge[0].current().getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE));
                assertTrue(MediaBridge.playing(bridge[0].current()));
                bridge[0].toggle();bridge[0].next();
            });
            assertTrue("Pause command delivered to session",pause.await(3,TimeUnit.SECONDS));
            assertTrue("Next command delivered to session",next.await(3,TimeUnit.SECONDS));
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                if(bridge[0]!=null) bridge[0].close();if(session[0]!=null) session[0].release();
            });
            shell("cmd notification disallow_listener com.dysland.app/com.dysland.app.integration.IslandNotificationListener");
        }
    }
}
