package com.dysland.app.motion;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.dysland.app.R;

/** Explicitly labelled in-app demo. Does not post fake events to the real overlay. */
public final class PreviewController {
    private final View root;
    private final PlaybackMotion playback;
    private ValueAnimator morph;
    private int state;
    private boolean resumed;
    private boolean reduce;
    public PreviewController(View root) {
        this.root=root; playback=new PlaybackMotion(root);
        root.setOnClickListener(v -> show((state+1)%3));
        root.findViewById(R.id.expanded_content).setVisibility(View.GONE);
        root.findViewById(R.id.status_icon).setVisibility(View.GONE);
        show(0);
    }
    public void configure(boolean resumed,boolean reduce) {
        this.resumed=resumed; this.reduce=reduce;
        playback.setPlaying(state==1 && resumed,!reduce);
    }
    public void show(int state) {
        this.state=state;
        boolean idle=state==0, music=state==1;
        root.findViewById(R.id.compact_row).setVisibility(idle ? View.INVISIBLE : View.VISIBLE);
        root.findViewById(R.id.equalizer).setVisibility(music ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.island_title).setVisibility(music ? View.GONE : View.VISIBLE);
        ((TextView)root.findViewById(R.id.island_title)).setText("04:59");
        ((ImageView)root.findViewById(R.id.artwork)).setImageResource(music ? R.drawable.record_art : R.drawable.ic_timer);
        root.setPadding(idle ? 0 : dp(10),idle ? 0 : dp(10),idle ? 0 : dp(10),idle ? 0 : dp(10));
        if (morph!=null) morph.cancel();
        int w=root.getLayoutParams().width, h=root.getLayoutParams().height;
        int targetW=dp(idle ? 24 : music ? 220 : 190), targetH=dp(idle ? 24 : 48);
        morph=ValueAnimator.ofFloat(0,1);
        morph.setDuration(reduce ? 0 : 480);
        morph.setInterpolator(new PathInterpolator(.18f,.85f,.23f,1f));
        morph.addUpdateListener(a -> {
            float fraction=(float)a.getAnimatedValue();
            ViewGroup.LayoutParams params=root.getLayoutParams();
            params.width=Math.round(w+(targetW-w)*fraction); params.height=Math.round(h+(targetH-h)*fraction);
            root.setLayoutParams(params);
        });
        morph.start();
        playback.setPlaying(music && resumed,!reduce);
    }
    public void close() { if (morph!=null) morph.cancel(); playback.close(); }
    private int dp(int value) { return Math.round(value*root.getResources().getDisplayMetrics().density); }
}
