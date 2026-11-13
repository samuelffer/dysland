# Entrega 0.3.1 — calibração fina, player, escuro e galeria

Data: 13/11/2026. Fonte compilada: `65249986ec0a29bd995d8f3eca4d1f98c996650f`.
Commit que acrescentou somente o ZIP: `0ef635d`.

[Execução aprovada](https://github.com/samuelffer/dysland/actions/runs/43365902674)

## Executado

- Compilação debug e release otimizada com SDK 35/Java 17; Android Lint sem erros bloqueadores (não significa zero avisos).
- **24 testes JVM**: tempo (6), mola (4), posição/seek/duração de mídia (7), texto de navegação (4), espessura relativa à câmera (3).
- **9 testes instrumentados** em Android 15/API 35, animações habilitadas:
  - abertura/configuração;
  - negar sobreposição;
  - iniciar/renderizar/parar após concessão;
  - rotação da prévia e suspensão/redução de movimento;
  - geometria de cutout sintético e ajuste manual;
  - frame real da janela sem offset indevido da barra de status;
  - correção X/Y de exatamente um pixel físico e altura extra zero;
  - galeria compacta/expandida usando o conteúdo compartilhado em 12 estados;
  - MediaSession real de teste, metadados e comandos pausa/próxima/**anterior/seek para 123000 ms**.
- Assinatura APK debug verificada por apksigner.
- Estrutura/CRC, extração limpa, SHA-256 e cópia remota do ZIP verificados. Conferida presença da imagem fornecida e dos ícones adaptativos no APK.
- Captura real da tela inicial revista visualmente e incluída no ZIP. Confirma modo escuro, pílula fina padrão e ativação fixa ao alcance. A área de galeria é explicitamente demonstração.

Não houve teste com Samsung físico, GPS rodando em navegador comercial, player comercial, Bluetooth físico ou carregador real. As ações de MediaSession foram testadas na sessão de teste do Android, não em Spotify/YouTube Music. O cutout sintético não certifica alinhamento em hardware.

## Download e hashes

- [ZIP versionado (Android 9+)](https://github.com/samuelffer/dysland/raw/refs/heads/arena/7e2b3c57-dysland/Zips/dysland-0.3.1-android-debug.zip)
- [Artifact do pacote](https://github.com/samuelffer/dysland/actions/runs/43365902674/artifacts/14010287128)
- [Relatórios](https://github.com/samuelffer/dysland/actions/runs/43365902674/artifacts/14010153026)
- [Screenshot](https://github.com/samuelffer/dysland/actions/runs/43365902674/artifacts/14009913653)

Artifacts exigem login/acesso e expiram em 30 dias (relatórios: 14). O artifact do pacote é um ZIP externo contendo o ZIP final. A cópia versionada não depende dessa retenção.

ZIP: **666490 bytes**. SHA-256:
```text
8cd9f6040d73b197912422dc70d136274964ac89e25a7048c8234ae51e4e5e81
```
APK SHA-256:
```text
c12b3c59edddf0af3287be58b3aeec51387bcb94383c82afe4157a547ffbdad8
```

A imagem original `icon_dysland.png` foi copiada sem alteração para `drawable-nodpi/dysland_brand.png` (hash de ambas: `eab02b35bef7bb0c98eb869c0a4027535b339cae417e1fdc506a363c3631f489`). A margem adaptativa é aplicada em XML, não alterando o original.

## Limites e histórico

A 0.3.1 substitui a 0.3.0: padrão de diâmetro manual de 16 dp, mantendo posição automática, e ativação fixa fora da área rolável da galeria. A 0.3.0 usava por padrão o diâmetro do cutout, que pode incluir uma área de proteção maior que a lente. Os ZIPs anteriores foram preservados.

A velocidade de carga não é inferida. GPS significa integração com notificações de navegação compatíveis, não leitura de localização/rotas próprias. As prévias usam os mesmos layouts, binder e métricas; dados/ações são exemplos locais e o expandido é reduzido proporcionalmente para caber. Botões de apps externos dependem das ações disponibilizadas por eles.

Mais detalhes: [INSTALAÇÃO](INSTALL.md), [pesquisa/decisões](DESIGN-0.3.md), [roteiro manual](QA.md).
