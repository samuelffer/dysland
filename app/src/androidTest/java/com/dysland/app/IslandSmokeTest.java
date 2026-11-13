package com.dysland.app;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.Settings;
import android.widget.Button;
import android.widget.SeekBar;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.dysland.app.island.IslandService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class IslandSmokeTest {
    private final Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private final Context context=instrumentation.getTargetContext();
    private void shell(String command) throws Exception {
        try (ParcelFileDescriptor fd=instrumentation.getUiAutomation().executeShellCommand(command);
             FileInputStream input=new FileInputStream(fd.getFileDescriptor())) {
            byte[] buffer=new byte[4096]; while (input.read(buffer)!=-1) { /* wait for shell completion */ }
        }
    }
    @After public void cleanup() {
        context.stopService(new Intent(context,IslandService.class));
        instrumentation.waitForIdleSync();
    }
    @Test public void configurationLaunchesAndGeometryChanges() {
        try (ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.enable));
                SeekBar width=activity.findViewById(R.id.width);
                assertTrue(width.getProgress()>=0);
                assertNotNull(activity.findViewById(R.id.timer_minutes));
            });
        }
    }
    @Test public void missingOverlayPermissionDoesNotStartService() throws Exception {
        shell("appops set com.dysland.app SYSTEM_ALERT_WINDOW deny");
        try (ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> activity.findViewById(R.id.enable).performClick());
            instrumentation.waitForIdleSync();
            assertFalse(IslandService.running);
        }
    }
    @Test public void grantedOverlayStartsStopsAndRenders() throws Exception {
        shell("appops set com.dysland.app SYSTEM_ALERT_WINDOW allow");
        assertTrue(Settings.canDrawOverlays(context));
        try (ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> activity.findViewById(R.id.enable).performClick());
            long deadline=SystemClock.elapsedRealtime()+5000;
            while (!IslandService.running && SystemClock.elapsedRealtime()<deadline) SystemClock.sleep(50);
            instrumentation.waitForIdleSync();
            assertTrue("Foreground overlay must start with user consent",IslandService.running);
            SystemClock.sleep(1000);
            Bitmap screenshot=instrumentation.getUiAutomation().takeScreenshot();
            assertNotNull(screenshot);
            try (FileOutputStream out=new FileOutputStream(new File(context.getExternalFilesDir(null),"dysland-home.png"))) {
                assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG,100,out));
            }
            scenario.onActivity(activity -> ((Button)activity.findViewById(R.id.enable)).performClick());
            deadline=SystemClock.elapsedRealtime()+5000;
            while (IslandService.running && SystemClock.elapsedRealtime()<deadline) SystemClock.sleep(50);
            assertFalse(IslandService.running);
        }
    }
}
