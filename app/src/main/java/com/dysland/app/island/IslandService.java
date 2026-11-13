package com.dysland.app.island;

import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.media.*;
import android.media.session.*;
import android.media.session.MediaController;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import com.dysland.app.MainActivity;
import com.dysland.app.R;
import com.dysland.app.data.*;
import com.dysland.app.integration.MediaBridge;
import com.dysland.app.motion.PlaybackMotion;
import com.dysland.app.presentation.IslandPresentation;
import com.dysland.app.presentation.MediaPanel;
import android.animation.ValueAnimator;

/** User-started foreground overlay. No boot receiver, hidden restart, wake lock or internet. */
public final class IslandService extends Service {
    public static final String START="com.dysland.START", REFRESH="com.dysland.REFRESH",
            TIMER="com.dysland.TIMER", STOPWATCH="com.dysland.STOPWATCH", STOP_TIME="com.dysland.STOP_TIME", STOP="com.dysland.STOP";
    public static volatile boolean running;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private Preferences prefs;
    private WindowManager windows;
    private View island;
    private WindowManager.LayoutParams layout;
    private MediaBridge media;
    private MediaPanel mediaPanel;
    private boolean expanded, attached, receiverRegistered, audioRegistered, destroyed;
    private boolean lastCharging, batteryKnown, lowNotified;
    private boolean screenOn=true;
    private long timeStart=-1, duration, lastRefresh;
    private ActivityStore.Event current;
    private MediaController currentMedia;
    private AudioManager audio;
    private String renderedKey="";
    private IslandMotion motion;
    private PlaybackMotion playbackMotion;
    private boolean lastExpanded;
    private long lastPlayingAt;
    private int cutoutHash=Integer.MIN_VALUE;
    private float desiredAlpha=-1f;
    private String lastMediaTitle="";
    private final android.content.SharedPreferences.OnSharedPreferenceChangeListener preferenceListener=(p,k) -> render();
    private final Runnable collapseTask=() -> { if(mediaPanel!=null && mediaPanel.isDragging()) { scheduleCollapse();return; } expanded=false; render(); };
    private final Runnable tick=new Runnable() {
        @Override public void run() {
            if (destroyed) return;
            long now=SystemClock.elapsedRealtime();
            if (now-lastRefresh>10000) { lastRefresh=now; media.refresh(); }
            if (duration>0 && timeStart>=0 && now-timeStart>=duration) {
                timeStart=-1; duration=0;
                ActivityStore.Event event=new ActivityStore.Event("timer_done","Tempo concluído","Seu temporizador chegou ao fim",R.drawable.ic_timer,90);
                event.expires=now+15000; ActivityStore.INSTANCE.put(event);
                Notification done=new Notification.Builder(IslandService.this,"timers")
                    .setSmallIcon(R.drawable.ic_timer).setContentTitle("Tempo concluído")
                    .setContentText("Seu temporizador Dysland chegou ao fim.").setContentIntent(openApp()).setAutoCancel(true).build();
                if (Build.VERSION.SDK_INT < 33 || checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    getSystemService(NotificationManager.class).notify(2,done);
                }
            }
            render();
            handler.postDelayed(this,timeStart>=0 || expanded || current!=null && "charge_intro".equals(current.key) ? 1000 : 3000);
        }
    };
    private final BroadcastReceiver signals=new BroadcastReceiver() {
        @Override public void onReceive(Context context,Intent intent) {
            String action=intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) { screenOn=false; expanded=false; render(); return; }
            if (Intent.ACTION_SCREEN_ON.equals(action) || Intent.ACTION_USER_PRESENT.equals(action)) { screenOn=true; render(); return; }
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int level=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0), scale=intent.getIntExtra(BatteryManager.EXTRA_SCALE,100);
                int percent=scale>0 ? Math.round(level*100f/scale) : 0;
                boolean charging=intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,0)!=0;
                if(charging) {
                    ActivityStore.Event status=new ActivityStore.Event("charge_status","Carregando · "+percent+"%","Energia conectada ao aparelho",R.drawable.ic_charge,20);
                    status.kind="charging";status.compactText=percent+"%";status.source="ENERGIA";ActivityStore.INSTANCE.put(status);
                    if(!batteryKnown || !lastCharging) {
                        ActivityStore.Event intro=new ActivityStore.Event("charge_intro",status.title,"Carregador conectado. O aviso recolhe para um indicador discreto.",R.drawable.ic_charge,85);
                        intro.kind="charging";intro.compactText=percent+"%";intro.source="ENERGIA";
                        intro.expires=SystemClock.elapsedRealtime()+4000;ActivityStore.INSTANCE.put(intro);
                    }
                } else { ActivityStore.INSTANCE.remove("charge_status");ActivityStore.INSTANCE.remove("charge_intro"); }
                if (percent<=15 && !charging && !lowNotified) { flash("low","Bateria baixa · "+percent+"%","Hora de recarregar",R.drawable.ic_battery); lowNotified=true; }
                if (percent>20 || charging) lowNotified=false;
                batteryKnown=true; lastCharging=charging;
            }
            if (android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED.equals(action) &&
                    (Build.VERSION.SDK_INT<31 || checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT)==android.content.pm.PackageManager.PERMISSION_GRANTED)) {
                android.bluetooth.BluetoothDevice device=intent.getParcelableExtra(android.bluetooth.BluetoothDevice.EXTRA_DEVICE);
                try { flash("bluetooth", device!=null && device.getName()!=null ? device.getName() : "Dispositivo Bluetooth", "Bluetooth conectado", R.drawable.ic_bluetooth); }
                catch (SecurityException ignored) { /* Permission may have been revoked between the check and access. */ }
            }
        }
    };
    private final AudioDeviceCallback audioCallback=new AudioDeviceCallback() {
        @Override public void onAudioDevicesAdded(AudioDeviceInfo[] devices) {
            if (!audioRegistered) return; // Ignore the initial inventory callback.
            for (AudioDeviceInfo device:devices) {
                int t=device.getType();
                if (device.isSink() && (t==AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || t==AudioDeviceInfo.TYPE_BLUETOOTH_SCO || t==AudioDeviceInfo.TYPE_WIRED_HEADPHONES || t==AudioDeviceInfo.TYPE_USB_HEADSET || t==AudioDeviceInfo.TYPE_BLE_HEADSET)) {
                    flash("audio","Áudio conectado",device.getProductName().toString(),R.drawable.ic_bluetooth); break;
                }
            }
        }
    };
    @Override public void onCreate() {
        super.onCreate(); prefs=new Preferences(this);
        NotificationManager nm=getSystemService(NotificationManager.class);
        NotificationChannel persistent=new NotificationChannel("island","Ilha ativa",NotificationManager.IMPORTANCE_LOW);
        persistent.setDescription("Mantém a sobreposição solicitada por você em funcionamento.");
        nm.createNotificationChannel(persistent);
        nm.createNotificationChannel(new NotificationChannel("timers","Temporizadores",NotificationManager.IMPORTANCE_HIGH));
        Intent stop=new Intent(this,IslandService.class).setAction(STOP);
        Notification notification=new Notification.Builder(this,"island").setSmallIcon(R.drawable.ic_island)
            .setContentTitle("Dysland está ativa").setContentText("Toque para ajustar. Você pode desativar a qualquer momento.")
            .setContentIntent(openApp()).setOngoing(true).setCategory(Notification.CATEGORY_SERVICE)
            .addAction(new Notification.Action.Builder(null,"Desativar",PendingIntent.getService(this,2,stop,PendingIntent.FLAG_IMMUTABLE)).build()).build();
        startForeground(1,notification);
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return; }
        running=true; windows=getSystemService(WindowManager.class);
        island=LayoutInflater.from(this).inflate(R.layout.overlay_island,null,false);
        layout=new WindowManager.LayoutParams(dp(prefs.width()),dp(prefs.height()),WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        layout.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;
        // Anchor coordinates are absolute display coordinates, including the status bar.
        if (Build.VERSION.SDK_INT>=30) layout.setFitInsetsTypes(0);
        layout.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        layout.setTitle("Dysland · atividade");
        try { windows.addView(island,layout); attached=true; }
        catch (RuntimeException denied) { stopSelf(); return; }
        motion=new IslandMotion(island,windows,layout);
        playbackMotion=new PlaybackMotion(island);
        island.setOnApplyWindowInsetsListener((v,insets) -> {
            // Root cutout coordinates move with this window. Hash the display's stable
            // insets instead, otherwise every spring frame would schedule a full render.
            android.view.DisplayCutout displayCutout=Build.VERSION.SDK_INT>=30
                    ? windows.getMaximumWindowMetrics().getWindowInsets().getDisplayCutout() : null;
            int hash=displayCutout==null ? 0 : displayCutout.hashCode();
            if (hash!=cutoutHash) { cutoutHash=hash; handler.post(this::render); }
            return insets;
        });
        island.requestApplyInsets();
        island.setOnClickListener(v -> { expanded=!expanded; render(); scheduleCollapse(); });
        island.setOnLongClickListener(v -> { startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); return true; });
        island.findViewById(R.id.collapse).setOnClickListener(v -> { expanded=false; render(); });
        island.findViewById(R.id.action_primary).setOnClickListener(v -> { media.toggle(); scheduleCollapse(); });
        island.findViewById(R.id.action_previous).setOnClickListener(v -> { media.previous();scheduleCollapse(); });
        island.findViewById(R.id.action_secondary).setOnClickListener(v -> { media.next(); scheduleCollapse(); });
        island.findViewById(R.id.notification_action).setOnClickListener(v -> {
            if (current!=null && "time".equals(current.key)) { timeStart=-1; duration=0; render(); }
            else if (current!=null && current.open!=null) send(current.open);
        });
        media=new MediaBridge(this,this::render);
        mediaPanel=new MediaPanel(island,position -> { media.seekTo(position);scheduleCollapse(); });
        ActivityStore.INSTANCE.observer=this::render;
        prefs.values.registerOnSharedPreferenceChangeListener(preferenceListener);
        IntentFilter filter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_SCREEN_ON); filter.addAction(Intent.ACTION_SCREEN_OFF); filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED);
        // All actions are protected system broadcasts; no app-defined commands are accepted here.
        if (Build.VERSION.SDK_INT>=33) registerReceiver(signals,filter,Context.RECEIVER_EXPORTED); else registerReceiver(signals,filter);
        receiverRegistered=true;
        audio=getSystemService(AudioManager.class); audio.registerAudioDeviceCallback(audioCallback,handler);
        handler.post(() -> audioRegistered=true);
        screenOn=getSystemService(PowerManager.class).isInteractive();
        handler.post(tick);
    }
    @Override public int onStartCommand(Intent intent,int flags,int id) {
        if (intent==null || !running) { stopSelf(); return START_NOT_STICKY; }
        String action=intent.getAction();
        if (STOP.equals(action)) { prefs.set("wanted",false); stopSelf(); return START_NOT_STICKY; }
        if (TIMER.equals(action) || STOPWATCH.equals(action)) {
            timeStart=SystemClock.elapsedRealtime();
            duration=TIMER.equals(action) ? Math.max(60000,Math.min(999*60000L,intent.getLongExtra("duration",60000))) : 0;
        } else if (STOP_TIME.equals(action)) { timeStart=-1; duration=0; }
        if (media!=null) media.refresh();
        render(); return START_NOT_STICKY;
    }
    private void flash(String key,String title,String detail,int icon) {
        ActivityStore.Event event=new ActivityStore.Event(key,title,detail,icon,85);
        event.compactText="low".equals(key) ? title.substring(title.lastIndexOf('·')+1).trim() : "Conectado";
        event.expires=SystemClock.elapsedRealtime()+6500; ActivityStore.INSTANCE.put(event);
    }
    private void scheduleCollapse() { handler.removeCallbacks(collapseTask); if (expanded) handler.postDelayed(collapseTask,10000); }
    private int dp(int value) { return Math.round(value*getResources().getDisplayMetrics().density); }
    private void render() {
        if (!attached || destroyed) return;
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return; }
        long now=SystemClock.elapsedRealtime();
        ActivityStore.Event best=ActivityStore.INSTANCE.top(now,prefs.enabled("notifications_enabled"),prefs.enabled("events_enabled"),prefs.enabled("navigation_enabled"));
        if (timeStart>=0 && (best==null || best.priority<80)) best=new ActivityStore.Event("time",TimeState.format(TimeState.seconds(now,timeStart,duration)),duration>0 ? "Temporizador em andamento" : "Cronômetro em andamento",R.drawable.ic_timer,80);
        currentMedia=null;
        MediaController controller=media==null ? null : media.current();
        if (MediaBridge.playing(controller)) lastPlayingAt=now;
        boolean mediaRecent=MediaBridge.playing(controller) || (lastPlayingAt>0 && now-lastPlayingAt<8000);
        if (prefs.enabled("media_enabled") && controller!=null && mediaRecent && (best==null || best.priority<50)) {
            MediaMetadata metadata=controller.getMetadata();
            if (metadata!=null) {
                String title=metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
                String artist=metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
                best=new ActivityStore.Event("media",title==null ? "Sua música" : title,artist==null ? "Controle de mídia" : artist,R.drawable.record_art,50);
                best.art=metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
                if (best.art==null) best.art=metadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
                best.kind="media";
                currentMedia=controller; best.open=controller.getSessionActivity();
            }
        }
        current=best;
        boolean locked=getSystemService(KeyguardManager.class).isKeyguardLocked();
        boolean visible=screenOn && !(locked && prefs.enabled("hide_lock"));
        island.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        if (!visible) {
            expanded=false;
            if (playbackMotion!=null) playbackMotion.setPlaying(false,false);
            return;
        }
        boolean idle=best==null && !expanded;
        boolean animations=!prefs.enabled("reduce_motion") && ValueAnimator.areAnimatorsEnabled();
        String key=best==null ? "idle" : best.key;
        boolean stateChanged=!key.equals(renderedKey) || expanded!=lastExpanded;
        if (stateChanged) {
            renderedKey=key;
            View row=island.findViewById(R.id.compact_row);
            row.animate().cancel();
            row.setAlpha(animations ? 0f : 1f);
            row.animate().alpha(idle ? 0f : 1f).setStartDelay(animations ? 90 : 0).setDuration(animations ? 240 : 0).start();
            View content=island.findViewById(R.id.expanded_content);
            content.animate().cancel();
            if (expanded) {
                content.setVisibility(View.VISIBLE);
                content.setAlpha(animations ? 0f : 1f); content.setTranslationY(animations ? dp(8) : 0);
                content.animate().alpha(1f).translationY(0).setStartDelay(animations ? 100 : 0).setDuration(animations ? 260 : 0).start();
            } else {
                content.animate().alpha(0f).translationY(animations ? dp(-4) : 0).setStartDelay(0).setDuration(animations ? 140 : 0)
                    .withEndAction(() -> { if (!expanded) content.setVisibility(View.GONE); }).start();
            }
            lastExpanded=expanded;
        }
        float alpha=idle && !prefs.enabled("show_idle") ? 0f : 1f;
        if (desiredAlpha!=alpha) {
            desiredAlpha=alpha;
            island.animate().alpha(alpha).setDuration(animations ? 350 : 0).start();
        }
        island.setImportantForAccessibility(idle ? View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS : View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        if (idle && !prefs.enabled("show_idle")) layout.flags|=WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        else layout.flags&=~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        boolean privateContent=best!=null && best.key.startsWith("notification:") && !prefs.enabled("show_content");
        ((TextView)island.findViewById(R.id.island_title)).setText(best==null ? "Dysland" : privateContent ? "Atividade em andamento" : best.title);
        ((TextView)island.findViewById(R.id.island_subtitle)).setText(best==null ? "Seu Android, no seu ritmo." : privateContent ? "Abra o app para ver os detalhes" : best.subtitle);
        ((TextView)island.findViewById(R.id.expanded_title)).setText(best==null ? "Seu Android, presente." : privateContent ? "Atividade em andamento" : best.title);
        ImageView artwork=island.findViewById(R.id.artwork);
        if (best!=null && best.art!=null) artwork.setImageBitmap(best.art); else artwork.setImageResource(best==null ? R.drawable.ic_island : best.icon);
        ((ImageView)island.findViewById(R.id.status_icon)).setImageResource(currentMedia!=null ? MediaBridge.playing(currentMedia) ? R.drawable.ic_pause : R.drawable.ic_play : R.drawable.ic_island);
        boolean isMedia=currentMedia!=null;
        IslandPresentation.details(island,best,isMedia,expanded,privateContent);
        IslandPresentation.arrival(island,best,animations);
        if(isMedia && mediaPanel!=null) mediaPanel.update(media.position(),media.duration(),MediaBridge.supports(currentMedia,PlaybackState.ACTION_SEEK_TO));
        island.findViewById(R.id.action_previous).setEnabled(MediaBridge.supports(currentMedia,PlaybackState.ACTION_SKIP_TO_PREVIOUS));
        if (isMedia && !best.title.equals(lastMediaTitle)) {
            lastMediaTitle=best.title;
            artwork.animate().cancel(); artwork.setAlpha(animations ? 0f : 1f);
            artwork.setScaleX(animations ? .82f : 1f); artwork.setScaleY(animations ? .82f : 1f);
            artwork.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(animations ? 260 : 0).start();
        }
        island.findViewById(R.id.equalizer).setVisibility(isMedia ? View.VISIBLE : View.GONE);
        island.findViewById(R.id.island_title).setVisibility(isMedia || idle ? View.GONE : View.VISIBLE);
        island.findViewById(R.id.status_icon).setVisibility(View.GONE);
        if (playbackMotion!=null) playbackMotion.setPlaying(isMedia && MediaBridge.playing(currentMedia),animations);
        island.findViewById(R.id.compact_row).setVisibility(View.VISIBLE);
        if (expanded) island.findViewById(R.id.expanded_content).setVisibility(View.VISIBLE);
        ImageButton primary=island.findViewById(R.id.action_primary), secondary=island.findViewById(R.id.action_secondary);
        primary.setVisibility(currentMedia==null ? View.GONE : View.VISIBLE); secondary.setVisibility(currentMedia==null ? View.GONE : View.VISIBLE);
        boolean playing=MediaBridge.playing(currentMedia);
        primary.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play);
        primary.setContentDescription(playing ? "Pausar" : "Reproduzir");
        primary.setEnabled(MediaBridge.supports(currentMedia,playing ? PlaybackState.ACTION_PAUSE : PlaybackState.ACTION_PLAY) || MediaBridge.supports(currentMedia,PlaybackState.ACTION_PLAY_PAUSE));
        secondary.setEnabled(MediaBridge.supports(currentMedia,PlaybackState.ACTION_SKIP_TO_NEXT));
        Button open=island.findViewById(R.id.notification_action);
        open.setVisibility(best!=null && !isMedia && (best.open!=null || "time".equals(best.key)) ? View.VISIBLE : View.GONE);
        open.setText(best!=null && "time".equals(best.key) ? "Encerrar" : "Abrir");
        LinearLayout actions=island.findViewById(R.id.notification_actions);
        actions.removeAllViews();
        if (expanded && best!=null && best.actions!=null && !privateContent) {
            int count=0;
            for (Notification.Action action:best.actions) {
                if (action.actionIntent==null || action.getRemoteInputs()!=null || count++>=3) continue;
                Button button=(Button)LayoutInflater.from(this).inflate(R.layout.notification_action,actions,false);
                button.setText(action.title); button.setOnClickListener(v -> send(action.actionIntent)); actions.addView(button);
            }
        }
        int screenWidth=getResources().getDisplayMetrics().widthPixels;
        if (Build.VERSION.SDK_INT>=30) screenWidth=windows.getMaximumWindowMetrics().getBounds().width();
        float density=getResources().getDisplayMetrics().density;
        CameraAnchor camera=CameraAnchor.resolve(Build.VERSION.SDK_INT>=30 ? windows.getMaximumWindowMetrics().getWindowInsets() : null,screenWidth,density,prefs);
        int compactHeight=IslandPresentation.compactHeight(camera.diameter,density,prefs.extraHeight());
        int width=idle ? Math.round(camera.diameter) : Math.min(expanded ? dp(Math.max(330,prefs.width())) : IslandPresentation.compactWidth(best,prefs,density),screenWidth-dp(20));
        int height=IslandPresentation.geometry(island,prefs,camera.diameter,density,width,expanded,idle);
        int left=Math.round(camera.x-width/2f);
        left=Math.max(0,Math.min(screenWidth-width,left));
        int offset=left+width/2-screenWidth/2;
        int top=Math.max(0,Math.round(camera.y-(idle ? height/2f : expanded ? camera.diameter/2f : compactHeight/2f)));
        if (motion!=null) motion.to(width,height,offset,top,animations);
    }
    private void send(PendingIntent intent) {
        try {
            if (Build.VERSION.SDK_INT>=34) {
                ActivityOptions options=ActivityOptions.makeBasic();
                options.setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED);
                intent.send(this,0,null,null,null,null,options.toBundle());
            } else intent.send();
        } catch (PendingIntent.CanceledException | SecurityException e) { Toast.makeText(this,"Ação indisponível. Abra o aplicativo de origem.",Toast.LENGTH_SHORT).show(); }
        expanded=false; render();
    }
    private PendingIntent openApp() { return PendingIntent.getActivity(this,1,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT); }
    @Override public void onConfigurationChanged(Configuration config) { super.onConfigurationChanged(config); render(); }
    @Override public void onDestroy() {
        destroyed=true; running=false; handler.removeCallbacksAndMessages(null);
        ActivityStore.INSTANCE.observer=null;
        if (prefs!=null) prefs.values.unregisterOnSharedPreferenceChangeListener(preferenceListener);
        if (motion!=null) motion.close();
        if (playbackMotion!=null) playbackMotion.close();
        if (media!=null) media.close();
        if (receiverRegistered) unregisterReceiver(signals);
        if (audio!=null) audio.unregisterAudioDeviceCallback(audioCallback);
        if (attached) { windows.removeView(island); attached=false; }
        stopForeground(STOP_FOREGROUND_REMOVE); super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
