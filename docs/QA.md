# Roteiro de aceitação em Android

O ciclo básico de abertura/permissão/ativação/desativação foi executado em emulador Android 15; veja [VALIDATION-0.3.1.md](VALIDATION-0.3.1.md) para os nove cenários automatizados atuais. Os cenários abaixo continuam pendentes de validação manual e em hardware físico.

- Instalação limpa, negar cada permissão: app abre, não inicia ilha sem sobreposição, controles próprios funcionam sem acesso às notificações.
- Conceder permissões, ativar/desativar pelo app e pela notificação; revogar sobreposição com serviço ativo; verificar ausência de crash/janela órfã.
- Player compatível: título/capa, play/pausa/next; trocar player, fechar sessão e revogar listener. Player sem next deve desabilitar botão.
- Chamada do discador, WhatsApp e outro VoIP: ações correspondem exclusivamente à notificação, chamada tem prioridade, remoção libera a ilha. Sem ação publicada, não inventar botão.
- Maps, gravador e app de entrega: confirmar categoria/notificação contínua; app incompatível não gera integração fictícia.
- Conectar/desconectar carregador e fone, bateria abaixo de 15%. Nenhuma declaração de carga rápida não comprovada.
- Temporizador 1 minuto e cronômetro: expansão mostra tempo monotônico, encerramento, som de conclusão conforme permissão e canal. Validar limites de 1/999 minutos. Encerrar processo cancela tempo (documentado).
- Bloquear/desbloquear, tela desligada, rotação, tela dividida, fonte 200%, TalkBack, área de câmera e presets. Recalibrar conforme densidade, não apenas resolução.
- Android 13+ configurações restritas e POST_NOTIFICATIONS negada; Android 14/15 foreground specialUse. Testar economia de energia One UI.
- Confirmar que apps seguros ocultam a sobreposição e que a ilha não recebe toques fora de seus limites. App não tenta contornar proteções.
- Desativar exibição de conteúdo, integrar chamada e confirmar que nenhum título/detalhe/ação textual sensível aparece.
- Auditoria: nenhuma permissão de rede/câmera/microfone/localização; nenhum histórico salvo; desinstalação remove configurações.

## Complementos da 0.3

- Calibrar diâmetro manual/pelo sistema, espessura extra zero e valores intermediários; mover X/Y em um pixel no Samsung físico; testar escala de tela/fonte e rotação.
- Timeline em Spotify, Samsung Music e YouTube Music: arrastar/soltar, duração ausente/live, anterior desabilitado quando não suportado e janela de chamada durante o arraste.
- Google Maps/Waze: distância, manobra, remoção da notificação e textos em português/inglês; não inventar manobras quando não publicadas.
- Carga com música simultânea: chegada temporária, indicador menor, prioridade correta e remoção ao desconectar. Não exibir alegação de carga rápida.
- Quatro acentos: persistência após recriar/fechar o app, diálogos escuros e cor da ilha real.
- Ícone em launchers com máscara circular/quadrada; conferir que o Android preserva a imagem dentro da área segura.
