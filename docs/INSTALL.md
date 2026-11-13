# Instalar o Dysland 0.1.0 (prévia Android)

Requisito: Android 9 (API 28) ou superior. APK universal, sem runtime adicional.

1. Extraia o ZIP e abra `dysland-0.1.0-debug.apk` no telefone.
2. Autorize temporariamente a instalação dessa fonte, se solicitado pelo Android.
3. Abra Dysland e conceda **Exibir sobre outros apps**. Ative a ilha.
4. Para mídia/chamadas/atividades, autorize **Acesso às notificações**. Esse acesso é sensível: o Android permite ler notificações. O Dysland processa apenas atividades compatíveis em memória, sem histórico, rede ou telemetria.
5. No Android 13+, permita os avisos para receber o alerta sonoro do temporizador. Em APKs instalados fora de uma loja, o Android pode bloquear o acesso às notificações: use Informações do app › menu › Permitir configurações restritas **apenas se confiar no APK**.
6. Bluetooth é opcional (Android 12+); permite identificar conexões de aparelhos, não procura dispositivos. Conexões de áudio usam também a API pública de áudio.
7. Escolha um preset, ajuste largura/altura/topo/deslocamento. Valores em dp; os presets são aproximações, não medições certificadas. Ajuste manualmente para não cobrir a câmera ou relógio.
8. Toque para expandir; toque longo abre as configurações. A ilha recolhe após dez segundos. Desative no app ou na notificação persistente.

## Limites desta prévia
- APK **debug** assinado automaticamente pelo Android SDK; não é um release de loja. Builds CI diferentes podem usar chaves debug diferentes: para atualizar pode ser necessário desinstalar (perdendo ajustes). Não acompanha chave privada.
- Mídia requer MediaSession publicada pelo player. Capa e ações dependem do player.
- Chamadas e outras atividades vêm de notificações contínuas das categorias chamada, navegação, progresso ou serviço. Atender/recusar/silenciar só aparecem se o app de origem fornecer essas ações; não há telefonia privilegiada. Duração da chamada e progresso da viagem não são inferidos.
- O temporizador e cronômetro são próprios. Se o sistema encerrar o serviço, eles são cancelados. Não são despertadores exatos e podem atrasar em suspensão profunda. Não use para tarefas críticas.
- Recarga mostra conexão e porcentagem, não potência/velocidade de carga (varia por fabricante). Não há leitura de bateria de fones.
- Não grava tela/voz; pode mostrar notificações compatíveis de gravadores. Não substitui o sistema de indicadores de câmera, microfone e GPS. Não há integração direta com APIs de entregas/transporte.
- Telas seguras, permissões e apps bancários podem impedir sobreposições. A ilha não fica acima da cortina de notificações do sistema. O bloqueio fica oculto por padrão.
- One UI: permita uso em segundo plano nas opções de bateria e remova o app dos apps em suspensão se necessário. Não é iniciado automaticamente ao ligar o telefone.
- Para revogar: desative a ilha e remova sobreposição/acesso às notificações nas configurações do Android, ou desinstale.

Sem anúncios, acesso à internet, contas ou coleta de dados. Fonte Manrope (SIL OFL); ícones vetoriais originais incluídos. Código sob a licença LICENSE do repositório.
