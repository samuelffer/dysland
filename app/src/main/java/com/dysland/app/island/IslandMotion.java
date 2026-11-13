package com.dysland.app.island;

import android.graphics.drawable.GradientDrawable;
import android.view.Choreographer;
import android.view.View;
import android.view.WindowManager;
import com.dysland.app.motion.SpringValue;

/** One frame clock morphs the actual window, not a scaled bitmap or an oversized touch layer. */
public final class IslandMotion implements Choreographer.FrameCallback {
    private final View view;
    private final WindowManager windows;
    private final WindowManager.LayoutParams params;
    private final SpringValue width,height,x,y;
    private boolean scheduled,closed;
    private long last;
    public IslandMotion(View view,WindowManager windows,WindowManager.LayoutParams params) {
        this.view=view; this.windows=windows; this.params=params;
        view.getBackground().mutate();
        width=new SpringValue(params.width,.44,.84);
        height=new SpringValue(params.height,.48,.88);
        x=new SpringValue(params.x,.42,.9); y=new SpringValue(params.y,.46,.9);
    }
    public void to(int w,int h,int px,int py,boolean animate) {
        width.target(w); height.target(h); x.target(px); y.target(py);
        if (!animate) { width.snap(); height.snap(); x.snap(); y.snap(); apply(); }
        else if (!scheduled && !closed) { scheduled=true; last=0; Choreographer.getInstance().postFrameCallback(this); }
    }
    @Override public void doFrame(long nanos) {
        if (closed) return;
        double dt=last==0 ? 1.0/60 : Math.min(.064,(nanos-last)/1e9);
        last=nanos;
        width.step(dt); height.step(dt); x.step(dt); y.step(dt); apply();
        if (!closed && (!width.settled() || !height.settled() || !x.settled() || !y.settled())) Choreographer.getInstance().postFrameCallback(this);
        else { scheduled=false; last=0; }
    }
    private void apply() {
        if (closed) return;
        params.width=Math.max(1,(int)Math.round(width.value()));
        params.height=Math.max(1,(int)Math.round(height.value()));
        params.x=(int)Math.round(x.value()); params.y=(int)Math.round(y.value());
        ((GradientDrawable)view.getBackground()).setCornerRadius(Math.min(params.height/2f,view.getResources().getDisplayMetrics().density*36));
        try { windows.updateViewLayout(view,params); }
        catch (IllegalArgumentException removed) { close(); }
    }
    public void close() { closed=true; Choreographer.getInstance().removeFrameCallback(this); }
}
