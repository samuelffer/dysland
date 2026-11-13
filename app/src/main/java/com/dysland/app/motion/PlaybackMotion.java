package com.dysland.app.motion;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;
import com.dysland.app.R;
import java.util.ArrayList;
import java.util.List;

/** Decorative playback indicator, not a microphone visualizer. No recording permission. */
public final class PlaybackMotion {
    private final View disc;
    private final View[] bars;
    private final List<ObjectAnimator> running=new ArrayList<>();
    private boolean active;
    public PlaybackMotion(View root) {
        disc=root.findViewById(R.id.artwork);
        bars=new View[]{root.findViewById(R.id.eq_one),root.findViewById(R.id.eq_two),root.findViewById(R.id.eq_three)};
    }
    public void setPlaying(boolean playing,boolean animations) {
        boolean next=playing && animations && ValueAnimator.areAnimatorsEnabled();
        if (next==active) return;
        active=next;
        for (ObjectAnimator animator:running) animator.cancel();
        running.clear();
        if (!next) { for (View bar:bars) bar.setScaleY(playing ? .6f : .2f); return; }
        ObjectAnimator rotation=ObjectAnimator.ofFloat(disc,View.ROTATION,disc.getRotation(),disc.getRotation()+360);
        rotation.setDuration(11000); rotation.setInterpolator(new LinearInterpolator()); rotation.setRepeatCount(ValueAnimator.INFINITE);
        running.add(rotation); rotation.start();
        for (int i=0;i<bars.length;i++) {
            ObjectAnimator wave=ObjectAnimator.ofFloat(bars[i],View.SCALE_Y,.22f,1f,.4f,.8f,.22f);
            wave.setDuration(820+i*170L); wave.setStartDelay(i*90L);
            wave.setRepeatCount(ValueAnimator.INFINITE); wave.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
            running.add(wave); wave.start();
        }
    }
    public void close() { active=false; for (ObjectAnimator animator:running) animator.cancel(); running.clear(); }
}
