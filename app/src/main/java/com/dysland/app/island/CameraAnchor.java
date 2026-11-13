package com.dysland.app.island;

import android.graphics.Rect;
import android.view.DisplayCutout;
import android.view.WindowInsets;
import com.dysland.app.data.Preferences;

/** Uses the physical top cutout's bounds, never a hard-coded screen resolution. */
public final class CameraAnchor {
    public final float x, y, diameter;
    public final boolean detected;
    private CameraAnchor(float x,float y,float diameter,boolean detected) {
        this.x=x; this.y=y; this.diameter=diameter; this.detected=detected;
    }
    public static CameraAnchor resolve(WindowInsets insets,int screenWidth,float density,Preferences prefs) {
        Rect hole=null;
        DisplayCutout cutout=insets==null ? null : insets.getDisplayCutout();
        if (prefs.enabled("camera_auto") && cutout!=null) {
            for (Rect bounds:cutout.getBoundingRects()) {
                // A wide notch isn't a circular camera. Fall back to explicit calibration.
                if (bounds.width()>0 && bounds.width()<density*80 && bounds.top<density*80) {
                    if (hole==null || bounds.top<hole.top) hole=bounds;
                }
            }
        }
        float diameter=prefs.number("camera_diameter",16)*density;
        float x=screenWidth/2f, y=(8+prefs.number("camera_diameter",16)/2f)*density;
        if (hole!=null) {
            x=hole.exactCenterX(); y=hole.exactCenterY();
            if (prefs.enabled("camera_size_auto")) diameter=Math.max(hole.width(),hole.height());
        }
        diameter=Math.max(8*density,diameter+prefs.number("diameter_px",0));
        x+=prefs.offset()*density+prefs.fineX();
        y+=(prefs.top()-8)*density+prefs.fineY();
        return new CameraAnchor(x,Math.max(diameter/2,y),diameter,hole!=null);
    }
}
