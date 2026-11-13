# Dysland 0.3 — calibração, mídia e tema escuro

## Pesquisa antes da implementação

Consultados em 13/11/2026, através do GitHub:

- [IslandGeometry.kt (Arnav-Dugad)](https://github.com/Arnav-Dugad/dynamic-island-android/blob/main/app/src/main/java/com/arnav/island/island/IslandGeometry.kt): distinção de correções em pixels físicos versus dimensões em dp. Implementação Dysland própria, sem copiar código/assets.
- [AndroidX Media — documentação e amostras](https://github.com/androidx/media): organização de sessões, comandos de transporte e posição. Não adicionamos um player próprio/ExoPlayer: o Dysland continua controlando a sessão publicada pelo app de música.
- [AOSP MediaController](https://github.com/aosp-mirror/platform_frameworks_base/blob/master/media/java/android/media/session/MediaController.java) e [PlaybackState](https://github.com/aosp-mirror/platform_frameworks_base/blob/master/media/java/android/media/session/PlaybackState.java): `seekTo`, ações suportadas e timestamp monotônico da posição. APIs públicas; nenhuma reflexão ou API escondida.
- [AOSP Notification](https://github.com/aosp-mirror/platform_frameworks_base/blob/master/core/java/android/app/Notification.java): `CATEGORY_NAVIGATION`, `EXTRA_BIG_TEXT`, `EXTRA_SUB_TEXT` e `EXTRA_TEXT_LINES` para orientações reais publicadas pelo navegador.

## Decisões

- Altura compacta = diâmetro da câmera + espessura extra (0–28 dp), mínimo técnico 12 dp. Não força mais 40–80 dp nem adiciona 8 dp incondicionais. Ícone, fonte compacta e barras se ajustam ao espaço. Texto/controles maiores continuam no expandido. Uma ilha muito fina tem área de toque pequena: use Ajustes para aumentar a folga se necessário.
- Posição: ajuste amplo em dp e fino em pixels físicos (±40 px, botões de 1 px). Correção de diâmetro de ±12 px. É possível desligar só o diâmetro automático mantendo o centro detectado.
- Player expandido: anterior, play/pausa, próxima, timeline e duração. Comandos só são habilitados quando a MediaSession declara suporte. Seek enviado uma vez ao soltar o slider. Duração ausente/live desabilita seek; posição interpolada pelo relógio monotônico é limitada à duração.
- Carga: aviso de chegada com raio e entrada animada, quatro segundos de prioridade, depois indicador menor de porcentagem (112 dp). Mídia/navegação têm prioridade sobre a carga discreta. Ao desconectar, ambos são removidos. Não deduzimos “carga rápida” de corrente instantânea ou de campos privados do fabricante.
- Navegação: distância e direção extraídas somente de textos recebidos. Suporta português/inglês para ícones de esquerda/direita, fallback genérico. Sem texto compatível, não inventa ETA, manobra, mapa ou localização. Módulo tem opção própria, independente das outras atividades. Ainda exige acesso às notificações.
- UI exclusivamente escura, com acentos Lavanda/Esmeralda/Ártico/Âmbar. Ilha preta para fundir à câmera; não usa transparência que comprometa essa fusão.
- Galeria: mesma árvore XML, mesmo `IslandPresentation`, mesma geometria, timeline e animação de reprodução da sobreposição. Dados fictícios isolados em `DemoEvents` e controles locais. O expandido é reduzido proporcionalmente para caber na área de prévia. Ações específicas de um discador/app podem variar; não é uma promessa de integração universal.
- Ícone adaptativo criado a partir de `icon_dysland.png` fornecido pelo usuário, preservando a imagem original e aplicando margem de segurança para a máscara do launcher. Nenhuma imagem gerada por IA.

## Organização

`presentation/`: geometria/conteúdo compartilhados, paletas, timeline e fixtures isoladas.
`data/MediaTime` e `NavigationText`: lógica pura testável.
`integration/`: leitura e comandos públicos; não guarda histórico nem usa rede.
