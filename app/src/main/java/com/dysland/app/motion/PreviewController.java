package com.dysland.app.motion;

import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Looper;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.dysland.app.R;
import com.dysland.app.data.ActivityStore;
import com.dysland.app.data.Preferences;
import com.dysland.app.island.CameraAnchor;
import com.dysland.app.presentation.*;

/** Same layout, content binder and dimensions as the real island. Data/commands are isolated demos. */
public final class PreviewController {
    private final View root;
    private final Preferences prefs;
    private final PlaybackMotion playback;
    private final MediaPanel panel;
    private final Handler main=new Handler(Looper.getMainLooper());
    private ValueAnimator morph;
    private int state;
    private boolean resumed,reduce,expanded,playing=true,closed;
    private long position=65000;
    private final Runnable clock=new Runnable() {
        @Override public void run() {
            if(closed || !resumed) return;
            if(state==1 && playing) position=(position+1000)%214000;
            if(state==1) panel.update(position,214000,true);
            main.postDelayed(this,1000);
        }
    };
    public PreviewController(View root,Preferences prefs) {
        this.root=root;this.prefs=prefs;playback=new PlaybackMotion(root);
        panel=new MediaPanel(root,p -> { position=p;panelUpdate(); });
        root.setOnClickListener(v -> expand(!expanded));
        root.findViewById(R.id.collapse).setOnClickListener(v -> expand(false));
        root.findViewById(R.id.action_primary).setOnClickListener(v -> { playing=!playing;refresh(); });
        root.findViewById(R.id.action_previous).setOnClickListener(v -> { position=0;panelUpdate(); });
        root.findViewById(R.id.action_secondary).setOnClickListener(v -> { position=0;panelUpdate(); });
        root.findViewById(R.id.notification_action).setOnClickListener(v -> expand(false));
        show(0);
        root.post(this::refresh);
    }
    private void panelUpdate() { panel.update(position,214000,true); }
    public void configure(boolean resumed,boolean reduce) {
        boolean changed=this.reduce!=reduce;
        this.resumed=resumed;this.reduce=reduce;
        if(changed) refresh();
        main.removeCallbacks(clock);if(resumed) main.post(clock);
        playback.setPlaying(state==1 && playing && resumed,!reduce);
    }
    public void expand(boolean expanded) { this.expanded=expanded;refresh(); }
    public void show(int state) { this.state=Math.max(0,Math.min(11,state));refresh(); }
    public void refresh() {
        if(closed) return;
        ActivityStore.Event e=DemoEvents.event(state);
        boolean idle=e==null && !expanded,music=state==1;
        root.findViewById(R.id.compact_row).setVisibility(idle ? View.INVISIBLE : View.VISIBLE);
        root.findViewById(R.id.expanded_content).setVisibility(expanded ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.status_icon).setVisibility(View.GONE);
        root.findViewById(R.id.equalizer).setVisibility(music ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.island_title).setVisibility(music || idle ? View.GONE : View.VISIBLE);
        ((ImageView)root.findViewById(R.id.artwork)).setImageResource(e==null ? R.drawable.ic_island : e.icon);
        IslandPresentation.details(root,e,music,expanded,e!=null && e.key.startsWith("notification:") && !prefs.enabled("show_content"));
        IslandPresentation.arrival(root,e,!reduce && ValueAnimator.areAnimatorsEnabled());
        root.findViewById(R.id.action_primary).setVisibility(music ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.action_secondary).setVisibility(music ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.notification_action).setVisibility(!music && e!=null ? View.VISIBLE : View.GONE);
        ((android.widget.TextView)root.findViewById(R.id.notification_action)).setText(e!=null && "time".equals(e.key) ? "Encerrar" : "Abrir");
        ((ImageView)root.findViewById(R.id.action_primary)).setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play);
        panelUpdate();
        WindowManager manager=root.getContext().getSystemService(WindowManager.class);
        float density=root.getResources().getDisplayMetrics().density;
        int screen=root.getResources().getDisplayMetrics().widthPixels;
        CameraAnchor camera=CameraAnchor.resolve(Build.VERSION.SDK_INT>=30 ? manager.getMaximumWindowMetrics().getWindowInsets() : null,screen,density,prefs);
        int width=idle ? Math.round(camera.diameter) : Math.min(expanded ? dp(Math.max(330,prefs.width())) : IslandPresentation.compactWidth(e,prefs,density),screen-dp(20));
        int height=IslandPresentation.geometry(root,prefs,camera.diameter,density,width,expanded,idle);
        View parent=(View)root.getParent();
        int available=parent.getWidth()>0 ? parent.getWidth() : screen-dp(92);
        float scale=Math.min(1f,available/(float)width);
        root.setScaleX(scale);root.setScaleY(scale);
        ViewGroup.LayoutParams outer=parent.getLayoutParams();outer.height=Math.max(dp(112),Math.round(height*scale)+dp(28));parent.setLayoutParams(outer);
        FrameLayout.LayoutParams params=(FrameLayout.LayoutParams)root.getLayoutParams();
        int fromW=params.width,fromH=params.height;
        if(morph!=null) morph.cancel();
        // Use the same analytic spring as the overlay, sampled by the platform's frame clock.
        SpringValue springW=new SpringValue(fromW,.44,.84),springH=new SpringValue(fromH,.48,.88);
        springW.target(width);springH.target(height);
        morph=ValueAnimator.ofFloat(0,1);morph.setDuration(reduce || !ValueAnimator.areAnimatorsEnabled() ? 0 : 650);
        final long[] last={0};
        morph.addUpdateListener(animation -> {
            long now=animation.getCurrentPlayTime();double dt=last[0]==0 ? 1.0/60 : Math.max(0,now-last[0])/1000d;last[0]=now;
            if(reduce || (float)animation.getAnimatedValue()>=1) { springW.snap();springH.snap(); } else { springW.step(dt);springH.step(dt); }
            ViewGroup.LayoutParams p=root.getLayoutParams();p.width=Math.max(1,(int)Math.round(springW.value()));p.height=Math.max(1,(int)Math.round(springH.value()));root.setLayoutParams(p);
            ((android.graphics.drawable.GradientDrawable)root.getBackground().mutate()).setCornerRadius(Math.min(p.height/2f,36*density));
        });
        morph.start();playback.setPlaying(music && playing && resumed,!reduce);
    }
    public void close() { closed=true;main.removeCallbacksAndMessages(null);if(morph!=null) morph.cancel();playback.close(); }
    private int dp(int value) { return Math.round(value*root.getResources().getDisplayMetrics().density); }
}
