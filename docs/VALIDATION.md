# Entrega 0.1.0 — evidências de validação

Data: 13/11/2026. Fonte compilada: `c604ec51d8b4a056ad03a8736178e03f34092713`.
Commit que acrescentou **somente** o ZIP: `1dfc7f9`.

[Execução aprovada no GitHub Actions](https://github.com/samuelffer/dysland/actions/runs/43353952000)

## Executado

- Compilação debug e release (release otimizado não assinado, não distribuído).
- Seis testes JVM de relógio monotônico, arredondamento, limites e formatação.
- Android Lint sem erros bloqueadores (isso não significa ausência de avisos).
- Três testes instrumentados em emulador Android 15 / API 35, perfil Pixel 6:
  - abertura das configurações e presença dos controles;
  - negar sobreposição impede início do serviço;
  - conceder sobreposição permite iniciar, renderizar e parar a ilha.
- Verificação da assinatura do APK debug com `apksigner verify --verbose`.
- ZIP: teste de CRC, extração limpa e SHA-256 do APK. Verificação local adicional da estrutura do APK, do ZIP e da cópia publicada na branch.

O teste instrumentado detectou um reinício indevido do serviço ao desativar; o problema foi corrigido antes desta entrega. Uma falha de extração de screenshot após a limpeza automática do app pelo test runner também foi corrigida no CI.

## Pacote

`Zips/dysland-0.1.0-android-debug.zip` — 121489 bytes.

SHA-256 do ZIP:
```text
7dff96fce5987dd7eb3400e8d717da46ad52db3e7a93daa05ebf7ecce18e05bd
```

SHA-256 do APK:
```text
de8869265cabf962d38403a7b9a10a280368d9779e533b3d3159f374dcbc2481
```

- [Baixar ZIP na branch de trabalho](https://github.com/samuelffer/dysland/raw/refs/heads/arena/7e2b3c57-dysland/Zips/dysland-0.1.0-android-debug.zip)
- [Artifact do pacote](https://github.com/samuelffer/dysland/actions/runs/43353952000/artifacts/14004828724)
- [Relatórios de teste/lint](https://github.com/samuelffer/dysland/actions/runs/43353952000/artifacts/14005053195)
- [Screenshot real do app no emulador](https://github.com/samuelffer/dysland/actions/runs/43353952000/artifacts/14004583939)

Artifacts exigem login/acesso e expiram (pacote/screenshot: 30 dias; relatórios: 14). O artifact do pacote é um ZIP externo contendo o ZIP final. O ZIP versionado não depende da retenção dos artifacts.

## Não validado nesta entrega

Nenhum Samsung físico, ligação real, player externo, Bluetooth físico, app de entrega/GPS ou suspensão prolongada foi testado. As integrações existem no código via APIs públicas, mas dependem do que cada app/aparelho disponibiliza. A compatibilidade anunciada mínima (Android 9) ainda precisa da matriz de testes em versões anteriores. O roteiro completo está em [QA.md](QA.md).

Esta é uma prévia instalável para avaliação, não uma garantia de equivalência com o iPhone nem uma release de produção aprovada por loja. Consulte [INSTALL.md](INSTALL.md) para todas as limitações e permissões.
