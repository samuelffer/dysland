package com.dysland.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.*;
import com.dysland.app.data.Preferences;
import com.dysland.app.integration.IslandNotificationListener;
import com.dysland.app.island.IslandService;

public final class MainActivity extends Activity {
    private Preferences prefs;
    private boolean binding;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs=new Preferences(this);
        setContentView(R.layout.activity_main);
        View root=findViewById(R.id.root);
        root.setOnApplyWindowInsetsListener((v,insets) -> {
            v.setPadding(0,insets.getSystemWindowInsetTop(),0,insets.getSystemWindowInsetBottom());
            return insets;
        });
        findViewById(R.id.overlay_permission).setOnClickListener(v -> {
            new AlertDialog.Builder(this).setTitle("Uma ilha sobre seus apps")
                .setMessage("A sobreposição permite exibir a ilha acima de outros aplicativos. Você pode desativá-la aqui ou nas configurações do Android a qualquer momento. Ela não lê o conteúdo da tela.")
                .setPositiveButton("Continuar", (d,w) -> open(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()))))
                .setNegativeButton("Agora não",null).show();
        });
        findViewById(R.id.notification_permission).setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle("Mídia e atividades, com sua autorização")
            .setMessage("O acesso a notificações permite ler títulos, detalhes e executar ações publicadas pelos apps. O Dysland usa isso apenas no aparelho, sem armazenar o histórico ou enviar dados. Não lê mensagens para exibir na ilha; apenas atividades contínuas compatíveis. O acesso pode ser revogado nas configurações.")
            .setPositiveButton("Abrir configurações",(d,w) -> open(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)))
            .setNegativeButton("Agora não",null).show());
        findViewById(R.id.post_permission).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 33) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},10);
            else Toast.makeText(this,"Permissão não exigida nesta versão do Android",Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.bluetooth_permission).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 31) requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},11);
            else Toast.makeText(this,"Conexões de áudio disponíveis sem permissão adicional",Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.battery_settings).setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle("Samsung e outros fabricantes")
            .setMessage("Se a ilha for encerrada pelo sistema, abra Informações do app › Bateria e permita uso sem restrições. Na One UI, remova o Dysland de Apps em suspensão. Isso pode aumentar o consumo de bateria.")
            .setPositiveButton("Informações do app",(d,w) -> open(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName()))))
            .setNegativeButton("Fechar",null).show());
        findViewById(R.id.enable).setOnClickListener(v -> {
            if (IslandService.running) {
                stopService(new Intent(this,IslandService.class));
                prefs.set("wanted",false);
                findViewById(R.id.enable).postDelayed(this::updateStatus,250);
            }
            else startIsland(IslandService.START,0);
        });
        setupGeometry();
        bindSwitch(R.id.hide_lock,"hide_lock"); bindSwitch(R.id.show_idle,"show_idle");
        bindSwitch(R.id.show_content,"show_content"); bindSwitch(R.id.media_enabled,"media_enabled");
        bindSwitch(R.id.events_enabled,"events_enabled"); bindSwitch(R.id.notifications_enabled,"notifications_enabled");
        findViewById(R.id.timer_start).setOnClickListener(v -> {
            String raw=((EditText)findViewById(R.id.timer_minutes)).getText().toString();
            try {
                int minutes=Integer.parseInt(raw);
                if (minutes < 1 || minutes > 999) throw new NumberFormatException();
                startIsland(IslandService.TIMER,minutes*60000L);
            } catch (NumberFormatException invalid) { ((EditText)findViewById(R.id.timer_minutes)).setError("Use de 1 a 999 minutos"); }
        });
        findViewById(R.id.stopwatch_start).setOnClickListener(v -> startIsland(IslandService.STOPWATCH,0));
        findViewById(R.id.timer_stop).setOnClickListener(v -> {
            if (IslandService.running) startService(new Intent(this,IslandService.class).setAction(IslandService.STOP_TIME));
        });
    }
    private void open(Intent intent) {
        try { startActivity(intent); } catch (android.content.ActivityNotFoundException e) { Toast.makeText(this,"Configuração indisponível neste aparelho",Toast.LENGTH_LONG).show(); }
    }
    private void startIsland(String action,long duration) {
        if (!Settings.canDrawOverlays(this)) { Toast.makeText(this,"Permita a sobreposição antes de ativar",Toast.LENGTH_LONG).show(); return; }
        Intent intent=new Intent(this,IslandService.class).setAction(action).putExtra("duration",duration);
        try { startForegroundService(intent); prefs.set("wanted",true); }
        catch (RuntimeException e) { Toast.makeText(this,"O Android bloqueou a inicialização. Verifique as permissões e tente novamente.",Toast.LENGTH_LONG).show(); }
        findViewById(R.id.enable).postDelayed(this::updateStatus,400);
    }
    private void bindSwitch(int id,String key) {
        Switch toggle=findViewById(id); toggle.setChecked(prefs.enabled(key));
        toggle.setOnCheckedChangeListener((v,checked) -> prefs.set(key,checked));
    }
    private void setupGeometry() {
        Spinner spinner=findViewById(R.id.preset);
        String[] presets={"Personalizado", "Galaxy S20 FE · 1080 × 2400", "Galaxy S23 / S24 · câmera central", "Galaxy S Ultra · câmera central", "Pixel · câmera central", "Câmera no canto esquerdo"};
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,presets);
        spinner.setAdapter(adapter); binding=true; spinner.setSelection(0); binding=false;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) {}
            @Override public void onItemSelected(AdapterView<?> parent,View v,int position,long id) {
                if (position==0 || binding) return;
                prefs.set("width", position==3 ? 210 : 190);
                prefs.set("height", position==3 ? 48 : 44);
                prefs.set("top",position==1 ? 6 : 8);
                prefs.set("offset",position==5 ? -90 : 0);
                syncGeometry();
            }
        });
        setupSeek(R.id.width,"width",140); setupSeek(R.id.top,"top",0);
        setupSeek(R.id.offset,"offset",-100); setupSeek(R.id.height,"height",40);
        findViewById(R.id.reset_geometry).setOnClickListener(v -> {
            prefs.set("width",190); prefs.set("height",44); prefs.set("top",8); prefs.set("offset",0); syncGeometry(); spinner.setSelection(0);
        });
        syncGeometry();
    }
    private void setupSeek(int id,String key,int base) {
        ((SeekBar)findViewById(id)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {}
            @Override public void onProgressChanged(SeekBar bar,int progress,boolean user) {
                if (user) { prefs.set(key,progress+base); geometryLabel(); }
            }
        });
    }
    private void syncGeometry() {
        ((SeekBar)findViewById(R.id.width)).setProgress(prefs.width()-140);
        ((SeekBar)findViewById(R.id.height)).setProgress(prefs.height()-40);
        ((SeekBar)findViewById(R.id.top)).setProgress(prefs.top());
        ((SeekBar)findViewById(R.id.offset)).setProgress(prefs.offset()+100); geometryLabel();
    }
    private void geometryLabel() { ((TextView)findViewById(R.id.geometry_label)).setText(prefs.width()+" × "+prefs.height()+" dp · topo "+prefs.top()+" · deslocamento "+prefs.offset()); }
    @Override protected void onResume() {
        super.onResume();
        updateStatus();
        // Refresh integrations only on returning from settings, never while rendering status.
        if (IslandService.running) startService(new Intent(this,IslandService.class).setAction(IslandService.REFRESH));
    }
    private void updateStatus() {
        boolean access=getSystemService(NotificationManager.class).isNotificationListenerAccessGranted(new ComponentName(this,IslandNotificationListener.class));
        ((Button)findViewById(R.id.enable)).setText(IslandService.running ? "Desativar minha ilha" : "Ativar minha ilha");
        ((TextView)findViewById(R.id.service_status)).setText(IslandService.running ? "Ativa · toque na ilha para expandir" : "Desativada · pronta quando você quiser");
        ((TextView)findViewById(R.id.permissions_status)).setText("Sobreposição: "+(Settings.canDrawOverlays(this)?"permitida":"pendente")+"\nMídia e notificações: "+(access?"permitidas":"pendentes")+"\nAvisos do Dysland: "+(getSystemService(NotificationManager.class).areNotificationsEnabled()?"permitidos":"bloqueados"));
    }
}
