package com.dysland.app.presentation;

import com.dysland.app.R;
import com.dysland.app.data.ActivityStore;

/** Gallery fixtures stay local to the preview; they are never posted to ActivityStore. */
public final class DemoEvents {
    private DemoEvents() {}
    public static ActivityStore.Event event(int state) {
        if(state==0) return null;
        String[] titles={"","Horizonte","04:59","00:12:38","Carregando · 72%","Carregando · 72%","Bateria baixa · 15%","Fones conectados","Vire à direita em 200 m","Chamada em andamento","Gravação em andamento","Entrega a caminho"};
        String[] subtitles={"","Artista de demonstração","Temporizador em andamento","Cronômetro em andamento","Carregador conectado. O aviso recolhe para um indicador discreto.","Energia conectada ao aparelho","Hora de recarregar","Dispositivo Bluetooth de demonstração","Av. Beira Mar • orientação publicada pelo navegador","Ações reais dependem do discador","Exemplo de notificação contínua de um gravador","Exemplo de notificação contínua do app de entrega"};
        int[] icons={R.drawable.ic_island,R.drawable.record_art,R.drawable.ic_timer,R.drawable.ic_timer,R.drawable.ic_charge,R.drawable.ic_charge,R.drawable.ic_battery,R.drawable.ic_bluetooth,R.drawable.ic_turn_right,R.drawable.ic_phone,R.drawable.ic_mic,R.drawable.ic_info};
        int index=Math.max(0,Math.min(11,state));
        ActivityStore.Event e=new ActivityStore.Event(index==5 ? "charge_status" : index==4 ? "charge_intro" : index==2 || index==3 ? "time" : "demo",titles[index],subtitles[index],icons[index],50);
        e.kind=index==1 ? "media" : index==4 || index==5 ? "charging" : index==8 ? "navigation" : "activity";
        e.compactText=index==8 ? "200 m" : index==4 || index==5 ? "72%" : index==6 ? "15%" : index==7 ? "Conectado" : index==9 ? "Chamada" : index==10 ? "Gravando" : index==11 ? "Entrega" : e.title;
        if(index>=8) e.key="notification:demo:"+index;
        e.source=index==8 ? "NAVEGAÇÃO · Exemplo" : index==4 || index==5 ? "ENERGIA" : index==1 ? "" : "DADOS DE DEMONSTRAÇÃO";
        return e;
    }
}
