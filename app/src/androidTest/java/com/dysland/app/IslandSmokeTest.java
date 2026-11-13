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
    @Test public void previewAnimatesOnlyWhileVisibleAndStopsWithReducedMotion() {
        try (ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)) {
            final float[] angles=new float[2];
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.demo_music).performClick();
                angles[0]=activity.findViewById(R.id.preview_island).findViewById(R.id.artwork).getRotation();
            });
            SystemClock.sleep(650);
            scenario.onActivity(activity -> {
                android.view.View preview=activity.findViewById(R.id.preview_island);
                angles[1]=preview.findViewById(R.id.artwork).getRotation();
                assertTrue("Disc should rotate during playback preview",Math.abs(angles[1]-angles[0])>1);
                activity.findViewById(R.id.nav_settings).performClick();
                ((android.widget.Switch)activity.findViewById(R.id.reduce_motion)).setChecked(true);
                angles[0]=preview.findViewById(R.id.artwork).getRotation();
            });
            SystemClock.sleep(300);
            scenario.onActivity(activity -> {
                assertEquals(angles[0],activity.findViewById(R.id.preview_island).findViewById(R.id.artwork).getRotation(),.01f);
                ((android.widget.Switch)activity.findViewById(R.id.reduce_motion)).setChecked(false);
            });
        }
    }
    @Test public void cameraUsesAbsoluteCutoutAndManualCorrections() {
        com.dysland.app.data.Preferences prefs=new com.dysland.app.data.Preferences(context);
        prefs.set("camera_auto",true); prefs.set("camera_diameter",16);prefs.set("top",8);prefs.set("offset",0);
        android.view.DisplayCutout cutout=new android.view.DisplayCutout(new android.graphics.Rect(0,48,0,0),
                java.util.Collections.singletonList(new android.graphics.Rect(516,8,564,56)));
        android.view.WindowInsets insets=new android.view.WindowInsets.Builder().setDisplayCutout(cutout).build();
        com.dysland.app.island.CameraAnchor anchor=com.dysland.app.island.CameraAnchor.resolve(insets,1080,3f,prefs);
        assertTrue(anchor.detected); assertEquals(540,anchor.x,.01f);assertEquals(32,anchor.y,.01f);
        prefs.set("offset",10);
        assertEquals(570,com.dysland.app.island.CameraAnchor.resolve(insets,1080,3f,prefs).x,.01f);
        prefs.set("offset",0);
    }
    @Test public void overlayWindowUsesDisplayCoordinatesWithoutStatusBarInset() throws Exception {
        shell("appops set com.dysland.app SYSTEM_ALERT_WINDOW allow");
        com.dysland.app.data.Preferences prefs=new com.dysland.app.data.Preferences(context);
        prefs.set("reduce_motion",true);
        try (ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> activity.findViewById(R.id.stopwatch_start).performClick());
            SystemClock.sleep(1000);
            // Application overlays are not guaranteed to appear in AccessibilityWindowInfo.
            // Read the system's actual window frame through the test-only shell instead.
            String dump;
            try (ParcelFileDescriptor descriptor=instrumentation.getUiAutomation().executeShellCommand("dumpsys window windows");
                 FileInputStream input=new FileInputStream(descriptor.getFileDescriptor());
                 java.io.ByteArrayOutputStream output=new java.io.ByteArrayOutputStream()) {
                byte[] buffer=new byte[4096];int count;
                while((count=input.read(buffer))!=-1) output.write(buffer,0,count);
                dump=output.toString(java.nio.charset.StandardCharsets.UTF_8.name());
            }
            int index=dump.indexOf("Dysland · atividade");
            assertTrue("Overlay window must exist in the system dump",index>=0);
            String section=dump.substring(index,Math.min(dump.length(),index+8000));
            java.util.regex.Matcher frame=java.util.regex.Pattern.compile("frame=\\[(-?\\d+),(-?\\d+)\\]\\[(-?\\d+),(-?\\d+)\\]").matcher(section);
            assertTrue("System must report the overlay frame: "+section,frame.find());
            int actualTop=Integer.parseInt(frame.group(2));
            android.view.WindowManager manager=context.getSystemService(android.view.WindowManager.class);
            android.view.WindowMetrics metrics=manager.getMaximumWindowMetrics();
            float density=context.getResources().getDisplayMetrics().density;
            com.dysland.app.island.CameraAnchor camera=com.dysland.app.island.CameraAnchor.resolve(metrics.getWindowInsets(),metrics.getBounds().width(),density,prefs);
            int height=Math.max(Math.round(prefs.height()*density),Math.round(camera.diameter+8*density));
            assertEquals("No status bar offset may be added to the island",Math.max(0,Math.round(camera.y-height/2f)),actualTop,2);
        } finally {
            prefs.set("reduce_motion",false);
        }
    }
}
