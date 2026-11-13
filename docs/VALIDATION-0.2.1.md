# Entrega 0.2.1 — redesign e movimento

Data: 13/11/2026. Fonte compilada: `5cffd8d572724c8d68fb2dd713a679b8c94fdaa0`.
Commit que acrescentou somente o ZIP: `fc732bc`.

[Execução aprovada](https://github.com/samuelffer/dysland/actions/runs/43358495999)

## Verificações executadas

- Compilação debug e release otimizada, Android SDK 35 / Java 17.
- Android Lint sem erros bloqueadores; existem avisos/deprecações, não é uma alegação de zero warnings.
- 10 testes JVM: seis de tempo e quatro da mola (convergência, retarget sem perda de velocidade/posição, independência de FPS e snap).
- 7 testes instrumentados em emulador Android 15, com animações habilitadas:
  1. abertura e controles de configuração;
  2. negar sobreposição impede início;
  3. conceder sobreposição permite iniciar/renderizar/parar;
  4. prévia gira enquanto visível e para ao sair/reduzir movimento;
  5. coordenadas de câmera a partir de um cutout sintético e correção manual;
  6. frame real da janela, lido do WindowManager, sem deslocamento extra da barra de status;
  7. MediaSession real de teste: metadados, estado de reprodução e entrega de comandos pausa/próxima.
- Assinatura do APK verificada com apksigner.
- Screenshot real da interface, revisada visualmente. Captura exige que a Activity possua o foco, rejeitando diálogos sobrepostos. Incluída como `PREVIA.png`; o cartão é uma demonstração explicitamente identificada.
- CRC, estrutura do APK, extração e SHA-256 do pacote verificados no CI e novamente na cópia sincronizada do repositório.

O teste da posição verificou a janela real do emulador. Isso não certifica alinhamento físico de todos os modelos. O cutout sintético testa a geometria, não uma câmera física.

## Download

- [ZIP versionado, Android 9+](https://github.com/samuelffer/dysland/raw/refs/heads/arena/7e2b3c57-dysland/Zips/dysland-0.2.1-android-debug.zip)
- [Artifact do pacote](https://github.com/samuelffer/dysland/actions/runs/43358495999/artifacts/14007740848)
- [Relatórios](https://github.com/samuelffer/dysland/actions/runs/43358495999/artifacts/14007003871)
- [Screenshot](https://github.com/samuelffer/dysland/actions/runs/43358495999/artifacts/14007437282)

Artifacts exigem login/acesso e expiram em 30 dias (relatórios: 14). O artifact do pacote é um ZIP externo contendo o ZIP final. O pacote versionado permanece na branch, independentemente dessa retenção.

ZIP: 310094 bytes. SHA-256:
```text
7773a193f037285a6f6162087a0f7ed193877babbc1a3fd4666ff40c360dbf7e
```
APK SHA-256:
```text
698b259d1b67c47ea233beb2fc468e781fec8488c1a5f4de766fc99d7ed628cc
```

## Limites e histórico

Nenhum Samsung físico, player comercial, ligação, dispositivo Bluetooth ou cenário de suspensão prolongada foi testado. Android 9/10 dependem de calibração manual. As limitações de integrações e assinatura debug continuam em [INSTALL.md](INSTALL.md).

A 0.2.0 permanece arquivada, mas foi substituída pela 0.2.1: a família Manrope foi corrigida para pesos 400/600/700, o encaixe na barra de status ficou explícito e a captura de tela agora é validada. A prévia arquivada da 0.2.0 contém um diálogo de ANR do launcher do emulador, não do Dysland; não é a prévia recomendada. A 0.1.0 também foi preservada.

Referências e decisões de design: [DESIGN-0.2.md](DESIGN-0.2.md).
