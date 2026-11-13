package com.dysland.app.presentation;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import com.dysland.app.R;

/** Dark/AMOLED surfaces stay black around the physical hole; users customize the accent. */
public final class ThemePalette {
    private ThemePalette() {}
    public static int accent(int theme) {
        switch(theme) { case 1:return 0xFF60E5A1;case 2:return 0xFF70D8F5;case 3:return 0xFFFFBB86;default:return 0xFFC5B9FF; }
    }
    public static int style(int theme) {
        switch(theme) { case 1:return R.style.Theme_Dysland_Emerald;case 2:return R.style.Theme_Dysland_Arctic;case 3:return R.style.Theme_Dysland_Amber;default:return R.style.Theme_Dysland; }
    }
    public static void app(View root,int theme) {
        ColorStateList tint=ColorStateList.valueOf(accent(theme));
        root.findViewById(R.id.enable).setBackgroundTintList(tint);
        root.findViewById(R.id.timer_start).setBackgroundTintList(tint);
        for(int id:new int[]{R.id.nav_home,R.id.nav_activities,R.id.nav_settings}) {
            ((TextView)root.findViewById(id)).setCompoundDrawableTintList(tint);
        }
    }
    public static void island(View root,int theme) {
        ColorStateList tint=ColorStateList.valueOf(accent(theme));
        for(int id:new int[]{R.id.eq_one,R.id.eq_two,R.id.eq_three}) root.findViewById(id).setBackgroundTintList(tint);
        ((SeekBar)root.findViewById(R.id.media_seek)).setProgressTintList(tint);
        ((SeekBar)root.findViewById(R.id.media_seek)).setThumbTintList(tint);
        ((TextView)root.findViewById(R.id.source_label)).setTextColor(accent(theme));
        ((TextView)root.findViewById(R.id.notification_action)).setTextColor(accent(theme));
    }
}
