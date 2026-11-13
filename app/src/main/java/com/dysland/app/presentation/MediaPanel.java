package com.dysland.app.presentation;

import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import com.dysland.app.R;
import com.dysland.app.data.MediaTime;
import java.util.function.LongConsumer;

/** Same seek UI in real island and gallery. Sends one command on release, not on each pixel. */
public final class MediaPanel {
    private final View root;
    private final SeekBar seek;
    private final LongConsumer commit;
    private long duration;
    private boolean dragging;
    public MediaPanel(View root,LongConsumer commit) {
        this.root=root;this.commit=commit;seek=root.findViewById(R.id.media_seek);
        seek.setMax(1000);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStartTrackingTouch(SeekBar bar) { dragging=true; }
            @Override public void onProgressChanged(SeekBar bar,int progress,boolean user) {
                if(user) ((TextView)root.findViewById(R.id.media_elapsed)).setText(MediaTime.label(MediaTime.fromProgress(progress,duration)));
            }
            @Override public void onStopTrackingTouch(SeekBar bar) {
                if(seek.isEnabled()) commit.accept(MediaTime.fromProgress(bar.getProgress(),duration));
                dragging=false;
            }
        });
    }
    public boolean isDragging() { return dragging; }
    public void update(long position,long duration,boolean canSeek) {
        this.duration=duration;
        seek.setEnabled(canSeek && duration>0);
        seek.setAlpha(seek.isEnabled() ? 1f : .35f);
        if(!dragging) {
            seek.setProgress(duration>0 ? (int)Math.min(1000,Math.max(0,position)*1000d/duration) : 0);
            ((TextView)root.findViewById(R.id.media_elapsed)).setText(MediaTime.label(position));
        }
        ((TextView)root.findViewById(R.id.media_duration)).setText(duration>0 ? MediaTime.label(duration) : "Ao vivo / sem duração");
    }
}
