package com.dysland.app.presentation;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.dysland.app.R;
import com.dysland.app.data.ActivityStore;
import com.dysland.app.data.Preferences;

/** Shared declarative-layout renderer: the gallery and actual overlay use identical metrics/content. */
public final class IslandPresentation {
    private IslandPresentation() {}
    public static void arrival(View root,ActivityStore.Event event,boolean animations) {
        String key=event==null ? "idle" : event.key;
        View art=root.findViewById(R.id.artwork);
        Object previous=art.getTag(R.id.artwork);art.setTag(R.id.artwork,key);
        if(animations && "charge_intro".equals(key) && !key.equals(previous)) {
            art.setScaleX(.65f);art.setScaleY(.65f);
            art.animate().scaleX(1f).scaleY(1f).setDuration(480).setInterpolator(new android.view.animation.OvershootInterpolator(.7f)).start();
        }
    }
    public static int compactHeight(float camera,float density,int extraDp) { return Math.max(Math.round(12*density),Math.round(camera+extraDp*density)); }
    public static int compactWidth(ActivityStore.Event event,Preferences prefs,float density) {
        return Math.round((event!=null && "charge_status".equals(event.key) ? Math.min(112,prefs.width()) : prefs.width())*density);
    }
    public static void details(View root,ActivityStore.Event event,boolean media,boolean expanded,boolean privateContent) {
        String title=event==null ? "Sua ilha, no seu ritmo." : privateContent ? "Atividade em andamento" : event.title;
        ((TextView)root.findViewById(R.id.expanded_title)).setText(title);
        ((TextView)root.findViewById(R.id.island_subtitle)).setText(event==null ? "Tudo pronto no seu Android." : privateContent ? "Abra o app para ver os detalhes" : event.subtitle);
        String compact=event==null ? "" : privateContent ? "Atividade" : event.compactText!=null ? event.compactText : event.title;
        ((TextView)root.findViewById(R.id.island_title)).setText(compact);
        TextView source=root.findViewById(R.id.source_label);
        source.setText(event==null ? "" : event.source);
        source.setVisibility(event!=null && !event.source.isEmpty() && expanded && !privateContent ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.media_timeline).setVisibility(media ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.action_previous).setVisibility(media ? View.VISIBLE : View.GONE);
    }
    public static int geometry(View root,Preferences prefs,float camera,float density,int width,boolean expanded,boolean idle) {
        int compact=compactHeight(camera,density,prefs.extraHeight());
        int padding=idle ? 0 : Math.round((expanded ? 16 : 3)*density);
        int rowHeight=expanded ? Math.round(32*density) : Math.max(1,compact-padding*2);
        LinearLayout.LayoutParams row=(LinearLayout.LayoutParams)root.findViewById(R.id.compact_row).getLayoutParams();row.height=rowHeight;
        root.findViewById(R.id.compact_row).setLayoutParams(row);
        int art=Math.min(Math.round(28*density),rowHeight);
        FrameLayout.LayoutParams image=(FrameLayout.LayoutParams)root.findViewById(R.id.artwork).getLayoutParams();image.width=art;image.height=art;
        root.findViewById(R.id.artwork).setLayoutParams(image);
        FrameLayout.LayoutParams label=(FrameLayout.LayoutParams)root.findViewById(R.id.island_title).getLayoutParams();
        label.width=Math.max(0,(width-Math.round(camera))/2-padding-Math.round(4*density));root.findViewById(R.id.island_title).setLayoutParams(label);
        ((TextView)root.findViewById(R.id.island_title)).setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,Math.min(11*root.getResources().getDisplayMetrics().scaledDensity,rowHeight*.76f));
        for(int id:new int[]{R.id.eq_one,R.id.eq_two,R.id.eq_three}) {
            LinearLayout.LayoutParams bar=(LinearLayout.LayoutParams)root.findViewById(id).getLayoutParams();bar.height=Math.min(rowHeight,Math.round(16*density));root.findViewById(id).setLayoutParams(bar);
        }
        FrameLayout.LayoutParams eq=(FrameLayout.LayoutParams)root.findViewById(R.id.equalizer).getLayoutParams();eq.height=rowHeight;root.findViewById(R.id.equalizer).setLayoutParams(eq);
        root.setPadding(padding,expanded ? Math.round(camera+8*density) : padding,padding,padding);
        ThemePalette.island(root,prefs.number("theme",0));
        if(idle) return Math.round(camera);
        if(!expanded) return compact;
        root.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
        return root.getMeasuredHeight();
    }
}
