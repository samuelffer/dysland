# Dysland 0.2 — câmera primeiro, movimento contínuo

## Pesquisa anterior à implementação

Referências consultadas no GitHub em 13/11/2026:

- [Arnav-Dugad/dynamic-island-android](https://github.com/Arnav-Dugad/dynamic-island-android): README, `IslandGeometry.kt` e `MotionSpec.kt`. Ideias observadas: câmera como âncora, estados idle/compacto/expandido, molas interrompíveis, respeito ao movimento reduzido.
- [ajitpal/dynamic-island-android](https://github.com/ajitpal/dynamic-island-android): README e organização visual das telas referenciadas no documento. Ideias: sumir no recorte em repouso, barras durante reprodução, separar configurações de módulos.
- [andreapianidev/Dynamic_Island-Android](https://github.com/andreapianidev/Dynamic_Island-Android): README da abordagem Java/XML e screenshots referenciadas. Não adotamos anúncios, launchers externos nem recursos daquele app.

Nenhum código, imagem ou asset desses projetos foi copiado. Referências não são dependências e não significam que todas as funções anunciadas por eles existam no Dysland.

## Linguagem visual

Interface clara, superfícies brancas, tipografia escura, acentos lavanda e uma área de demonstração em gradiente frio. Navegação fixa: Ilha, Atividades e Ajustes. Permissões secundárias ficam recolhidas em um painel expansível. Sem a antiga lista interminável de cartões escuros e verde-lima.

Todas as views permanecem em XML. A animação altera propriedades e a geometria da janela; não desenha interfaces com Canvas/caracteres nem constrói widgets programaticamente.

## Estados reais

- Repouso: círculo preto na posição da câmera, sem texto/ícones. Opcionalmente totalmente invisível e sem captura de toque.
- Música: ilha mais larga, capa circular girando à esquerda e três barras decorativas à direita. Giram/movem apenas em reprodução; pausa interrompe os loops imediatamente e oito segundos depois recolhe a ilha. Não são um espectro real do áudio.
- Atividade: ícone à esquerda e informação curta à direita, deixando o centro livre para a câmera.
- Expandido: título, descrição e controles abaixo do recorte. Toque para abrir/recolher; recolhimento automático após dez segundos.
- Evento: entra e sai com a mesma mola usada nos outros estados, sem pular a geometria.

`SpringValue` integra a solução analítica da mola amortecida. Retarget mantém velocidade e posição. `IslandMotion` usa Choreographer, atualiza tamanho/posição reais da janela e interrompe o loop ao estabilizar. `PlaybackMotion` possui/cancela animações contínuas de disco e barras. Não reinicia os loops a cada atualização de metadados/relógio. Tela apagada, bloqueio, Activity pausada e movimento reduzido suspendem loops correspondentes.

## Câmera e limites

Android 11+: `getMaximumWindowMetrics().getWindowInsets().getDisplayCutout()` fornece coordenadas estáveis da tela, sem realimentar as coordenadas relativas à janela animada. São aceitos recortes pequenos no topo; notch largo não é considerado uma câmera circular. Android 9/10 ou aparelhos que não informam o recorte usam calibração manual. Auto não é promessa de alinhamento perfeito para todo firmware.

Diâmetro manual de 10–36 dp, correção horizontal de ±220 dp e correção vertical continuam disponíveis. Presets são aproximações. A janela é limitada às bordas da tela; uma câmera no canto pode não ficar no centro da pílula quando ela se expande, devido a esse limite. A câmera física não pode ser coberta por pixels: a ilha apenas se funde visualmente à área preta ao redor.
