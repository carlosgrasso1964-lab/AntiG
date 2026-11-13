# IndexTracker — contexto do projeto

App Android (Kotlin + SQLite) para acompanhar cotações diárias, baseado no dashboard HTML
`C:\Users\Carlos\Desktop\NewInd..html`, **sem ouro, iene, yuan, bolsa de valores e bitcoin**.
Apenas o **Dólar** vem cadastrado; o usuário cadastra os demais índices.

- Pacote/namespace: `com.carlos.indextracker`
- applicationId: `com.carlos.indextracker`
- minSdk 24 · targetSdk/compileSdk 34
- Kotlin 1.9.24 · AGP 8.5.2 · Gradle wrapper 8.13 · viewBinding
- Banco: SQLite puro via `SQLiteOpenHelper` (sem Room)

## O que já está implementado

- **Cards de cotação** em grade de 2 colunas: nome, selo `API`/`MANUAL`, valor com sufixo,
  variação percentual em relação ao registro anterior do índice.
- **Índices manuais**: campo de valor + botão ✓ no próprio card; grava o valor de hoje.
- **Índices automáticos**:
  - tipo **Moeda**: código (USD, EUR, GBP...) convertido para BRL pela AwesomeAPI,
    com fallback no `exchangerate-api` (base USD, taxa BRL × taxa da moeda).
  - tipo **URL personalizada**: URL que retorna JSON + caminho opcional
    (ex.: `results.0.price`, `data.rates.BRL`); sem caminho, usa o primeiro número do JSON.
- **Gráfico de histórico** por índice (últimos 30 registros), desenhado em Canvas
  (`ui/LineChartView.kt`), sem biblioteca externa.
  - A cor do gráfico é escolhida em uma **paleta de 24 cores** (toque no campo
    "Cor do gráfico" ou na bolinha de prévia); o código hexadecimal não precisa mais
    ser digitado.
  - Grade e rótulos do gráfico usam cores do tema (funcionam no claro e no escuro).
- **Atualizar agora**: grava/atualiza o valor de hoje de todos os índices
  (manual mantém o último valor conhecido; API usa o valor buscado e, se falhar,
  último valor salvo ou valor padrão). Quando algum índice automático falha, o status
  informa **qual** índice não foi atualizado.
- **Backup/Restauração em JSON** (botões 💾 Backup e 📥 Restaurar): exporta todos os
  índices e todo o histórico pelo seletor de arquivos do Android (nome
  `indextracker-backup-AAAA-MM-DD.json`) e restaura em outro aparelho. A restauração
  substitui os dados atuais, após confirmação descrevendo a quantidade de índices e
  registros do arquivo.
- **Tema claro e escuro**: o app segue o modo do sistema (`Theme.MaterialComponents.DayNight`).
  O tema escuro tem paleta própria em `values-night/` (cards, textos, chips, gráfico e
  barras do sistema).
- **Gerenciar**: diálogo com lista de índices, editar e excluir (excluir apaga o histórico).
- **Reiniciar histórico**: apaga somente `tb_cotacao`, mantém os índices.
- Toque longo no card abre a edição. Botão `Adicionar` no gerenciamento também.
- Persistência diária no SQLite; fechar/reabrir mantém tudo.

## Estrutura

```
app/src/main/java/com/carlos/indextracker/
├── activities/MainActivity.kt        # dashboard, cards, gráficos, atualizar/gerenciar/reset
├── activities/IndexFormActivity.kt   # cadastro/edição (manual, moeda, URL)
├── activities/IndiceAdapter.kt       # lista do diálogo de gerenciamento
├── database/Indice.kt                # modelos Indice + Cotacao (FONTE_MANUAL/MOEDA/CUSTOM)
├── database/BackupDados.kt           # payload do backup em JSON
├── database/IndexTrackerDbHelper.kt  # schema v2 e consultas
├── network/CotacaoService.kt         # AwesomeAPI + exchangerate + URL JSON
└── ui/LineChartView.kt, ui/Formato.kt# gráfico custom e formatação pt-BR
```

Layouts: `activity_main.xml`, `item_indice.xml` (card), `item_grafico.xml`,
`dialog_gerenciar.xml`, `item_gerenciar.xml`, `activity_index_form.xml`.
Recursos novos: `bg_chip`, `bg_badge`, `bg_input`, `bg_button_manual`, estilos em `themes.xml`
e cores/tema escuro em `values-night/` (`colors.xml`, `themes.xml`).

## Banco de dados (versão 2)

`tb_indice`: `id`, `nome`, `fonte` (`manual` | `moeda` | `custom`), `codigo`, `api_url`,
`json_path`, `sufixo`, `cor`, `casas_decimais`, `valor_padrao`, `ativo`, `ordem`, `criado_em`.

`tb_cotacao`: `id`, `indice_id`, `data` (yyyy-MM-dd), `valor`, `criado_em`,
`UNIQUE(indice_id, data)`, FK para `tb_indice`.

- Seed: apenas `Indice.dolar()` (USD, 3 casas, padrão 5,70, ordem 0).
- **Backup**: exporta `BackupDados` (versão, data, índices e cotações); a restauração
  limpa as duas tabelas em uma transação e reinsere os índices preservando os ids.
- **Atenção**: `onUpgrade` derruba e recria as tabelas — instalar sobre a versão antiga
  apaga o histórico anterior.

## Como compilar

O projeto precisa de JDK 21 para o Gradle (o wrapper/daemon criteria já aponta para 21;
na máquina existe em `~/.gradle/jdks/jetbrains_s_r_o_-21-amd64-windows.2`).

```bash
cd C:/Users/Carlos/Documents/AntiG/AndroidStudioProjects/IndexTracker
export JAVA_HOME="/c/Users/Carlos/.gradle/jdks/jetbrains_s_r_o_-21-amd64-windows.2"
./gradlew :app:assembleDebug
```

APK gerado: `app/build/outputs/apk/debug/app-debug.apk`.
Instalar: `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

No Android Studio, se pedir JDK do Gradle, selecione 21.

## Estado atual da sessão (13/11/2026)

- **APK gerado**: `app/build/outputs/apk/debug/app-debug.apk` (13/11/2026 10:49, ~5,9 MB).
- **Banco continua na versão 2, sem mudança de schema**: instalar por cima da versão atual
  (`adb install -r` ou Run no Android Studio) **mantém os dados existentes**. Ainda assim,
  fazer o backup antes de testar a restauração é recomendado.
- **Nada foi commitado no git** até aqui.
- **Aguardando teste no aparelho do Carlos** para confirmar paleta, backup/restauração e
  modo escuro. Se algo falhar, anotar a etapa e a mensagem exibida e retomar daqui.

### Checklist de teste no aparelho

1. Abrir o app com o celular no **modo claro** e depois no **modo escuro**; conferir cards,
   textos, chips, campos e barras do sistema.
2. Editar um índice (ou criar um novo), tocar no campo **Cor do gráfico** ou na bolinha,
   escolher uma cor na paleta, salvar e conferir a bolinha e a linha do gráfico.
3. Com o **Dólar no tipo Moeda**, tocar em **Atualizar** e conferir se o valor de hoje entra.
   Se falhar, o status deve dizer qual índice falhou e que foi mantido o último valor.
4. Tocar em **💾 Backup**, salvar o `.json`, abrir o arquivo para conferir que contém
   `indices` e `cotacoes`; depois tocar em **📥 Restaurar**, escolher o arquivo e confirmar
   as quantidades no diálogo.
5. Testar um índice **Manual**: digitar o valor no card e tocar em ✓.
6. Opcional: fazer backup, reinstalar em outro aparelho e restaurar o mesmo arquivo.

## Verificação já feita

- `assembleDebug` → BUILD SUCCESSFUL após paleta de cores, backup/restauração,
  detalhamento de falhas e tema claro/escuro.
- **Teste em execução pendente**: o emulador não inicia nesta máquina
  (`x86_64 emulation currently requires hardware acceleration` — Hyper-V/WHPX desativado).
  Paleta, backup e modo escuro ainda não foram validados em runtime.
- API do dólar testada por `curl` em 13/11/2026: AwesomeAPI respondeu HTTP 200 com
  USD-BRL `5.6459`; o fallback exchangerate-api também respondeu. A falha relatada
  não era da fonte — o app só busca ao tocar em **Atualizar** (não há busca diária
  automática) e, antes, não indicava qual índice havia falhado.

## Próximos passos / pendências

1. **Aguardando o Carlos**: executar o checklist acima no aparelho e confirmar o resultado.
2. Atualização diária automática com WorkManager (hoje é manual).
3. Permitir editar valores de datas passadas (hoje só o dia atual).
4. Avaliar reuso de dados antigos na migração do banco, se necessário.

## Decisões tomadas

- Tema **DayNight** (segue o sistema). O modo escuro usa cores próprias em `values-night/`,
  em vez de inverter automaticamente as cores claras.
- Gráfico próprio em Canvas em vez de MPAndroidChart, para não adicionar dependência/repositório.
- Sem chamada automática de rede ao abrir o app: a busca só ocorre em **Atualizar**.
- Sem preenchimento artificial de histórico ao cadastrar índice novo (o gráfico começa no
  primeiro registro real).
- `sufixo` é salvo com espaço inicial (ex.: `" R$"`), como no HTML.
- Nenhum commit foi feito no git; o repositório local tem histórico de outro projeto.
