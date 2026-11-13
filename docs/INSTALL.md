# Instalar o Dysland 0.3.1 (prévia Android)

Requisito: Android 9 (API 28) ou superior. APK universal, sem runtime adicional.

1. Extraia o ZIP e abra `dysland-0.3.1-debug.apk` no telefone.
2. Autorize temporariamente a instalação dessa fonte, se solicitado pelo Android.
3. Abra Dysland e conceda **Exibir sobre outros apps**. Ative a ilha.
4. Para mídia/chamadas/atividades, autorize **Acesso às notificações**. Esse acesso é sensível: o Android permite ler notificações. O Dysland processa apenas atividades compatíveis em memória, sem histórico, rede ou telemetria.
5. No Android 13+, permita os avisos para receber o alerta sonoro do temporizador. Em APKs instalados fora de uma loja, o Android pode bloquear o acesso às notificações: use Informações do app › menu › Permitir configurações restritas **apenas se confiar no APK**.
6. Bluetooth é opcional (Android 12+); permite identificar conexões de aparelhos, não procura dispositivos. Conexões de áudio usam também a API pública de áudio.
7. Em Ajustes, ative a detecção de câmera (Android 11+) ou calibre diâmetro, largura/altura/topo/deslocamento. Android 9/10 usam alinhamento manual. Valores em dp; os presets são aproximações, não medições certificadas. Ajuste manualmente para não cobrir a câmera ou relógio.
8. Em repouso a ilha recolhe em um círculo preto na câmera. Durante a música a capa gira e três barras indicam reprodução; pause para interromper o movimento. Após cerca de oito segundos em pausa, recolhe novamente. Toque para expandir; toque longo abre as configurações. A ilha recolhe após dez segundos. Desative no app ou na notificação persistente.

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

## Movimento e nova interface

Três áreas: Ilha, Atividades e Ajustes. A demonstração da tela inicial é identificada como prévia e não controla o player. Reduzir movimento (no app ou no sistema) remove os loops e a transição elástica. Não é necessário dar acesso ao microfone para as barras: elas são um indicador decorativo de reprodução, não um medidor de áudio.

O pacote gerado no CI inclui `PREVIA.png`, uma captura real da tela inicial no emulador (a ilha dentro do cartão está explicitamente identificada como demonstração).

## Novo na 0.3

- Todo o app volta ao modo escuro. Em Ajustes escolha um dos quatro acentos; a ilha permanece preta para se fundir à câmera.
- Ajuste a espessura extra (0–28 dp), não uma altura mínima imposta. Zero aproxima a altura à câmera. Há ajuste fino X/Y em pixels (inclusive botões de 1 px) e correção fina do diâmetro. Por padrão, o diâmetro é manual (16 dp), mantendo a posição automática. Ative “Usar diâmetro detectado” apenas se o recorte informado corresponder bem à sua câmera.
- O player expandido inclui anterior, pausa/play, próxima, posição e duração. Arraste a timeline e solte para buscar. Botões/seek podem ficar desabilitados se o player não publicar suporte ou se a transmissão não tiver duração.
- Ao carregar, o aviso inicial dura cerca de quatro segundos e recolhe para uma porcentagem discreta, com menor prioridade que música/GPS. O raio é visual de energia, não comprovação de carga rápida.
- Navegação tem controle separado e mostra textos reais de notificações compatíveis, inclusive distância/manobra quando publicadas. Não solicita GPS e não calcula rotas próprias.
- Na tela Ilha, selecione cada função na galeria e alterne compacta/expandida. Os dados são de demonstração; não ligam para ninguém, não iniciam gravação nem acionam uma entrega.
- Ícone do launcher: imagem fornecida pelo usuário, adaptada à máscara do Android. O indicador monocromático de notificação continua um glifo próprio, como exigido pelo sistema.
