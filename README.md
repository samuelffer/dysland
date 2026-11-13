# Dysland

Uma ilha de atividades para Android, inspirada na ergonomia da One UI, com identidade própria: superfícies claras, lavanda, tipografia Manrope, grandes títulos e cartões arredondados. **Prévia nativa 0.2.0, não um clone com APIs privadas.**

## Download e validação

[**Baixar Dysland 0.2.0 · APK debug em ZIP · Android 9+**](https://github.com/samuelffer/dysland/raw/refs/heads/arena/7e2b3c57-dysland/Zips/dysland-0.2.0-android-debug.zip)

A versão 0.2 está em validação de build/lint, mola, geometria da câmera e animações. [Design e referências pesquisadas](docs/DESIGN-0.2.md). [Evidências da entrega anterior 0.1](docs/VALIDATION.md). Não testado em Samsung físico nem com chamadas/Bluetooth reais.

## Novo na 0.2

- Repouso fundido ao círculo da câmera; detecção de cutout em coordenadas da tela no Android 11+ e alinhamento manual em versões anteriores.
- Transições de janela por molas analíticas, contínuas mesmo ao interromper uma expansão.
- Disco/capa circular em rotação e três barras animadas em reprodução, suspensos em pausa, tela apagada e movimento reduzido.
- Interface clara inteiramente refeita, navegação Ilha / Atividades / Ajustes e prévia interativa explicitamente identificada.
- ZIP anterior preservado em `Zips/`.

## Produto

- Sobreposição real `TYPE_APPLICATION_OVERLAY`, compacta/expandida, com presets aproximados e calibração em dp.
- MediaSession: capa, título, artista, play/pausa e próxima faixa; respeita capacidades publicadas pelo player.
- Integração local com notificações: chamadas (prioridade máxima), navegação e atividades contínuas, com ações originais via PendingIntent. Não solicita acesso a mensagens, contatos ou telefonia.
- Temporizador e cronômetro próprios com relógio monotônico; aviso de conclusão, recarga/bateria baixa e conexões de áudio/Bluetooth.
- Controles individuais de conteúdo, integrações e privacidade no bloqueio. Sem rede ou analytics.

Leia [instalação e limitações reais](docs/INSTALL.md). **Não inclui** monitoramento global de câmera/microfone/GPS, API universal de entregas, gravação própria, potência de carregamento ou ações de chamada não publicadas pelo discador. Nada disso é simulado na ilha.

## Estrutura

```text
app/src/main/java/com/dysland/app/
  MainActivity.java              # configuração e consentimento
  data/                         # preferências, eventos efêmeros, cálculo de tempo
  integration/                  # NotificationListener e MediaSession
  island/IslandService.java     # ciclo de vida da sobreposição e sinais do sistema
app/src/main/res/layout/         # toda a UI declarada em XML
app/src/main/res/drawable/       # ícones vetoriais originais e superfícies
app/src/test/                   # testes JVM da aritmética de tempo
scripts/package.py              # ZIP imutável, validação e SHA-256
Zips/                           # versões entregues (nunca sobrescrever)
```

Java 17, Android SDK 35, Gradle 8.11.1, AGP 8.9.2. Android mínimo 9 (API 28). Sem bibliotecas runtime externas. Manrope incluída com licença OFL. Nenhum desenho de interface por caracteres, Canvas ou construção de widgets em código; linhas de ação são infladas a partir de XML.

## Compilar e testar

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
python3 scripts/package.py
```

Instale o SDK 35 e configure `ANDROID_HOME` ou `local.properties`. O wrapper baixa Gradle; o build baixa AGP do Google Maven e JUnit do Maven Central. `assembleRelease` valida a variante otimizada, mas **não distribui APK release não assinado**. O pacote distribuído contém o APK debug instalável, instruções, licenças e SHA256SUMS, nunca chaves.

O workflow [Android](.github/workflows/android.yml) compila ambas as variantes, testa, executa lint, verifica assinatura debug e valida extração/estrutura do ZIP. Artifact expira em 30 dias; o download do GitHub tem um ZIP externo contendo o pacote final. O job de build tem apenas leitura. Após todos os testes, um job separado com escrita publica **somente o ZIP novo**, exclusivamente em push da branch confiável `arena/7e2b3c57-dysland`, verificando que ela não avançou. Não cria tags ou releases. Pull requests não recebem esse job. Cada nova entrega exige incrementar `versionCode`, `versionName` e o nome em `package.py`/workflow.

## Permissões e arquitetura de segurança

- `SYSTEM_ALERT_WINDOW`: solicitada por consentimento; serviço iniciado com a Activity visível.
- `FOREGROUND_SERVICE` + `SPECIAL_USE`: atividade persistente escolhida pelo usuário, com notificação e ação Desativar. Publicação na Play Store exige revisão dessa finalidade; não implica aprovação.
- `POST_NOTIFICATIONS`: Android 13+, opcional para avisos; o Android permite FGS sem essa permissão, mas oculta avisos da cortina.
- `BLUETOOTH_CONNECT`: opcional, Android 12+; nenhuma varredura nem localização.
- `BIND_NOTIFICATION_LISTENER_SERVICE`: proteção do serviço pelo sistema; habilitação manual e consentida. Conteúdo apenas em memória; notificações secretas são ignoradas. Acesso revogado remove a integração.

Não usa AccessibilityService, INTERNET, câmera, microfone, localização, contatos, leitura de chamadas ou bateria sem restrições automática. Sem boot/autostart. Serviço `START_NOT_STICKY`; parada pelo sistema cancela os tempos. Notificações sensíveis ficam ocultas no bloqueio por padrão. Android e fabricantes podem limitar sobreposição/execução; não há promessa de funcionamento universal.

## Validação de dispositivo necessária

Testes JVM e lint não validam uma tela física. Antes de lançamento, executar o roteiro [QA](docs/QA.md) em aparelhos/emuladores Android 9, 12, 13, 14, 15 e Samsung One UI, com recortes, escala de fonte, rotação, permissões revogadas e suspensão profunda.
