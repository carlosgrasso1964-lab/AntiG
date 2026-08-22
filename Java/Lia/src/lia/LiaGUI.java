package lia;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;

public class LiaGUI extends JFrame {

    private JTextArea areaLog;
    private JTextField campoComando;
    private JButton btnExecutar;
    private JButton btnVoz;
    private ReconhecedorVoz reconhecedorVoz;
    private final SintetizadorVoz sintetizador;
    private JLabel avatarLabel;
    private ImageIcon avatarParado;
    private ImageIcon avatarFalando;
    private ImageIcon avatarCochilando;
    private ImageIcon avatarOuvindo;
    private Timer timerCochilo;
    private Timer timerPulso;
    private float alphaPulso = 0f; // Controla a transparência do brilho
    private boolean aumentandoAlpha = true;
    private ServicoWeb servicoWeb = new ServicoWeb();
    private GeminiService gemini = new GeminiService();
    private boolean liaFalando = false; // A trava mágica
    private long ultimaChamadaTime = 0;
    private final String CAMINHO_NOTAS = "notas_lia.txt";
    private PostItGUI editorAtual = null; // Variável global na LiaGUI
    private int consultasHoje = 0;
    private final int COTA_MAXIMA = 50; // Ajuste conforme sua cota
    private OllamaService ollama = new OllamaService();
    private ManusService manusService = new ManusService();
    private javax.swing.JTextArea txtResposta;

    public LiaGUI() {
        MemoriaDAO.criarTabela();
        MemoriaDAO.criarTabelaFatos();
        MemoriaDAO.criarTabelaFatosV2();
        sintetizador = new SintetizadorVoz(this);  // Passa 'this' como referência
        // Configurações básicas da janela
        setTitle("🤖 Lia - Assistente Pessoal");
        //setSize(500, 700); // Largura e Altura

        // 1. Define o tamanho que você escolheu (exemplo: 350 largura, 750 altura)
        setSize(650, 1000);

        // 2. Obtém as dimensões da tela do usuário
        Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();

        // 3. Calcula a posição X (Largura da tela - largura da janela - margem)
        // Colocamos - 10 para não ficar colado na borda física do monitor
        int x = (int) tela.getWidth() - getWidth() - 10;

        // 4. Calcula a posição Y (Margem do topo)
        int y = 10;

        // 5. Aplica a posição na janela
        setLocation(x, y);

        setAlwaysOnTop(true);

        // Impede que o usuário mude o tamanho manualmente e quebre o layout
        //setResizable(false);
        // Centraliza na tela (opcional)
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setLocationRelativeTo(null);
        inicializarComponentes();

        setVisible(true);
    }

    // ADICIONE AQUI O NOVO MÉTODO
    public void setAvatarFalando(boolean falando) {
        timerCochilo.stop();
        this.liaFalando = falando; // ESSA LINHA É OBRIGATÓRIA
        SwingUtilities.invokeLater(() -> {
            if (falando) {
                if (timerCochilo != null) {
                    timerCochilo.stop(); // Para de contar cochilo se começou a falar
                }
                avatarLabel.setIcon(avatarFalando);
            } else {
                avatarLabel.setIcon(avatarParado);
                if (timerCochilo != null) {
                    timerCochilo.restart(); // Reinicia os 30s para cochilar
                }
            }
            avatarLabel.repaint();
        });
    }

    public void setAvatarOuvindo(boolean ouvindo) {
        SwingUtilities.invokeLater(() -> {
            if (ouvindo) {
                timerCochilo.stop(); // Não dorme enquanto tenta te ouvir
                // Inicia o efeito de pulso visual
                if (timerPulso == null) {
                    timerPulso = new Timer(50, e -> {
                        // Oscila o alpha entre 0.1 e 0.8
                        if (aumentandoAlpha) {
                            alphaPulso += 0.05f;
                            if (alphaPulso >= 0.8f) {
                                aumentandoAlpha = false;
                            }
                        } else {
                            alphaPulso -= 0.05f;
                            if (alphaPulso <= 0.1f) {
                                aumentandoAlpha = true;
                            }
                        }

                        // Aplica uma borda colorida que "pulsa"
                        avatarLabel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(0, 255, 0, (int) (alphaPulso * 255)), 3, true),
                                BorderFactory.createEmptyBorder(5, 5, 5, 5)
                        ));
                    });
                }
                timerPulso.start();
                //System.out.println("👂 Lia está te ouvindo...");
            } else {
                if (timerPulso != null) {
                    timerPulso.stop();
                }
                avatarLabel.setBorder(null);
                avatarLabel.setIcon(avatarParado);
                timerCochilo.restart();
            }
        });
    }

    private void inicializarComponentes() {
        // Painel principal
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== AVATAR =====
        try {
            avatarParado = new ImageIcon(getClass().getResource("/Imagens/stop.png"));
            avatarFalando = new ImageIcon(getClass().getResource("/Imagens/speak.png"));
            avatarCochilando = new ImageIcon(getClass().getResource("/Imagens/sleep.png"));
            avatarOuvindo = new ImageIcon(getClass().getResource("/Imagens/listening.png"));

            avatarParado = redimensionarImagem(avatarParado, 120, 120);
            avatarFalando = redimensionarImagem(avatarFalando, 120, 120);
            avatarCochilando = redimensionarImagem(avatarCochilando, 120, 120);
            avatarOuvindo = redimensionarImagem(avatarOuvindo, 120, 120);
        } catch (Exception e) {
            System.out.println("PNGs não encontrados: " + e.getMessage());
        }

        avatarLabel = new JLabel();
        avatarLabel.setHorizontalAlignment(JLabel.CENTER);
        avatarLabel.setPreferredSize(new Dimension(130, 130));
        avatarLabel.setIcon(avatarParado != null ? avatarParado : null);
        painelPrincipal.add(avatarLabel, BorderLayout.NORTH);

        // ===== ÁREA DE LOG =====
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollLog = new JScrollPane(areaLog);
        painelPrincipal.add(scrollLog, BorderLayout.CENTER);

        // ===== PAINEL INFERIOR =====
        JPanel painelInferior = new JPanel(new BorderLayout(5, 5));

        // Botões à esquerda
        JPanel painelBotoesVoz = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        btnVoz = new JButton("🎤 Ativar Voz");
        btnVoz.addActionListener(e -> alternarReconhecimentoVoz());
        painelBotoesVoz.add(btnVoz);

        // --- NOVO BOTÃO PARAR ---
        JButton btnParar = new JButton("🛑 Parar Fala");
        btnParar.setBackground(new Color(255, 100, 100));
        btnParar.addActionListener(e -> pararFalaLia()); // Chamará o método que vamos criar
        painelBotoesVoz.add(btnParar);

        // --- BOTÃO AJUDA ---
        JButton btnAjuda = new JButton("❓ Ajuda");
        btnAjuda.addActionListener(e -> mostrarAjuda());
        painelBotoesVoz.add(btnAjuda);

        // Campo de comando
        campoComando = new JTextField();
        campoComando.setFont(new Font("Monospaced", Font.PLAIN, 12));
        campoComando.addActionListener(e -> processarComando());

        // Botão executar
        btnExecutar = new JButton("Executar Comando");
        btnExecutar.addActionListener(e -> processarComando());

        // Monta painel inferior
        painelInferior.add(painelBotoesVoz, BorderLayout.WEST);
        painelInferior.add(campoComando, BorderLayout.CENTER);
        painelInferior.add(btnExecutar, BorderLayout.EAST);
        painelPrincipal.add(painelInferior, BorderLayout.SOUTH);

        // ===== FINALIZA =====
        add(painelPrincipal);

        carregarNotasSalvas();

        // ===== TIMER COCHILO =====
        timerCochilo = new Timer(30000, e -> {
            // Se não estiver ouvindo e não estiver falando, ela dorme
            if (reconhecedorVoz == null) {
                avatarLabel.setIcon(avatarCochilando);
                System.out.println("😴 Avatar: COCHILANDO");
            }
        });
        timerCochilo.setRepeats(false);
        timerCochilo.start(); // Inicia a contagem assim que o programa abre

        // ===== MENSAGEM INICIAL =====
        areaLog.append("🤖 Lia iniciada! Digite 'ajuda' ou clique no botão ❓ para ver os comandos.\n");

        // ===== SAUDAÇÃO INICIAL COMPLETA =====
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1200); // Aumentei um pouquinho para garantir o carregamento total

                    if (sintetizador != null) {
                        // 1. Instâncias de data e hora
                        java.util.Date agoraData = new java.util.Date();
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        int horaNum = cal.get(java.util.Calendar.HOUR_OF_DAY);

                        // 2. Formatadores para a fala ficar natural
                        // Ex: "27 de fevereiro" e "10 horas e 30 minutos"
                        java.text.SimpleDateFormat formatadorData = new java.text.SimpleDateFormat("dd 'de' MMMM");
                        java.text.SimpleDateFormat formatadorHora = new java.text.SimpleDateFormat("HH 'horas e' mm 'minutos'");

                        String dataExtenso = formatadorData.format(agoraData);
                        String horaExtenso = formatadorHora.format(agoraData);
                        String nomeUsuario = System.getProperty("user.name");

                        // 3. Define a saudação base baseada na hora
                        String saudacaoBase;
                        if (horaNum >= 6 && horaNum < 12) {
                            saudacaoBase = "Bom dia, ";
                        } else if (horaNum >= 12 && horaNum < 18) {
                            saudacaoBase = "Boa tarde, ";
                        } else {
                            saudacaoBase = "Boa noite, ";
                        }

                        // 4. Monta a frase completa
                        String fraseFinal = saudacaoBase + nomeUsuario + ". \n"
                                + "Sou Lia e hoje é dia " + dataExtenso + ". \n"
                                + "São " + horaExtenso + ". \n"
                                + "Como posso lhe ajudar?";

                        // 5. Envia para o log (que já tem o filtro 🤖 para falar)
                        adicionarLog("🤖 " + fraseFinal);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Método para redimensionar imagens
    private ImageIcon redimensionarImagem(ImageIcon original, int largura, int altura) {
        Image imagem = original.getImage();
        Image novaImagem = imagem.getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
        return new ImageIcon(novaImagem);
    }

    public void processarComandoVoz(String comando) {
        // Apenas coloca o texto no campo e chama o processador único
        campoComando.setText(comando);
        processarComando();
    }

//    private void processarComando() {
//        String comando = campoComando.getText().toLowerCase().trim();
//        System.out.println("DEBUG: Comando recebido: [" + comando + "]");
//        campoComando.setText("");
//
//        // ============================================================
//        // 1. MODO BLINDADO (GEMINI) - VEM ANTES DE TUDO
//        // ============================================================
//        if (comando.startsWith("consulte") || comando.startsWith("consulta") || comando.startsWith("pergunta") || comando.startsWith("gemini")) {
//
//            // Aqui ela ignora qualquer comando interno (como 'estado' ou 'lembrete')
//            // e foca apenas na pergunta.
//            String duvidaLimpa = comando.replace("consulte", "")
//                    .replace("pergunta", "")
//                    .replace("gemini", "").trim();
//
//            if (!duvidaLimpa.isEmpty()) {
//                processarGemini(duvidaLimpa);
//                contabilizarConsulta(); // Vamos criar este método abaixo
//            }
//            return; // O 'return' é a chave: ele impede que o código continue e ache o comando 'estado'
//        }
//
//        if (comando.startsWith("manus")) {
//            String prompt = comando.replace("manus", "").trim();
//            if (!prompt.isEmpty()) {
//                sintetizador.falar("Aguarde, estou consultando o Manus.");
//                areaLog.append("🌐 Consultando Manus...\n");
//                new Thread(() -> {
//                    String resposta = manusService.processarComando(prompt);
//                    adicionarLog("🤖 (Manus) " + resposta);
//                }).start();
//            }
//            return;
//        }
//
//        // Escolher modelo na pergunta: lia mini explique java ou lia qwen explique java
//        if (comando.startsWith("mini") || comando.startsWith("qwen")) {
//
//            String modelo = "phi3";
//
//            if (comando.startsWith("qwen")) {
//                modelo = "qwen2.5:3b";
//            }
//
//            String pergunta = comando
//                    .replace("mini", "")
//                    .replace("qwen", "")
//                    .trim();
//
//            final String modeloFinal = modelo;
//
//            if (!pergunta.isEmpty()) {
//                sintetizador.falar("Aguarde estou pesquisando."); // Adiciona a voz
//                areaLog.append("🧠 LIA (" + modeloFinal + ") pensando...\n");
//
//                new Thread(() -> {
//
//                    // 1️⃣ Recupera memória recente da LIA
//                    //String contexto = MemoriaService.recuperarContexto(5);
//                    String contextoRecente = MemoriaService.recuperarContexto(3);
//                    String contextoRelevante = MemoriaService.buscarMemoriaRelevante(pergunta);
//
//                    String contexto = contextoRelevante + "\n" + contextoRecente;
//
//                    // 2️⃣ Monta prompt com histórico
//                    String promptFinal
//                            = contexto
//                            + "\nUsuário: " + pergunta
//                            + "\nLia:";
//
//                    // 3️⃣ Consulta o modelo local
//                    String resposta = ollama.consultarLocal(promptFinal, modeloFinal);
//
//                    // 4️⃣ Salva a conversa no SQLite
//                    MemoriaDAO.salvar(pergunta, resposta);
//
//                    adicionarLog("🤖 (" + modeloFinal + ") " + resposta);
//
//                }).start();
//            }
//
//            return;
//        }
//
//        //Usar o interpretador no processamento
//        if (InterpretadorFatos.interpretar(comando)) {
//            sintetizador.falar("Ok, informação armazenada.");
//            return;
//        }
//
//        if (MemoriaService.aprenderFato(comando)) {
//            sintetizador.falar("Ok, vou lembrar disso.");
//            return;
//        }
//
//        if (comando.contains("minha neta se chama")) {
//            String nome = comando.replace("minha neta se chama", "").trim();
//            MemoriaService.salvarFato("neta", nome);
//            sintetizador.falar("Ok. Vou lembrar que sua neta se chama " + nome);
//            return;
//        }
//
//        String entidade = DicionarioEntidades.detectarEntidade(comando);
//
//        if (comando.contains("quantas") && entidade != null) {
//
//            int total = MemoriaService.contarFatos(entidade);
//
//            sintetizador.falar("Você tem " + total + " " + entidade + "(s).");
//
//            return;
//        }
//
//        // =========================
//        // CONSULTAR FATO
//        // =========================
//        if (comando.contains("nome da minha neta") || comando.contains("qual o nome da minha neta")) {
//            String nome = MemoriaDAO.buscarFato("neta");
//            if (nome != null) {
//                sintetizador.falar("O nome da sua neta é " + nome);
//                adicionarLog("🤖 Lia: O nome da sua neta é " + nome);
//            } else {
//                sintetizador.falar("Ainda não sei o nome da sua neta.");
//            }
//            return;
//        }
//
//        if (comando.contains("quem é minha neta")) {
//            String neta = MemoriaDAO.buscarFato("neta");
//            if (neta != null) {
//                adicionarLog("🤖 Sua neta se chama " + neta);
//                adicionarLog("🤖 Lia: O nome da sua neta é " + neta);
//            } else {
//                adicionarLog("🤖 Ainda não sei quem é sua neta.");
//            }
//            return;
//        }
//
//        if (comando.contains("nomes das minhas netas")) {
//            var netas = MemoriaService.listarFatos("neta");
//            if (netas.isEmpty()) {
//                sintetizador.falar("Ainda não sei os nomes das suas netas.");
//            } else {
//                String nomes = String.join(", ", netas);
//                sintetizador.falar("Suas netas se chamam " + nomes);
//                adicionarLog("🤖 Lia: Suas netas se chamam " + nomes);
//            }
//            return;
//        }
//
//        if (comando.contains("nome do meu filho")) {
//            String nome = MemoriaDAO.buscarFato("filho");
//            if (nome != null) {
//                sintetizador.falar("Seu filho se chama " + nome);
//            } else {
//                sintetizador.falar("Ainda não sei o nome do seu filho.");
//            }
//            return;
//        }
//
//        //Memória inteligente
//        if (MemoriaInteligente.tentarSalvar(comando)) {
//            sintetizador.falar("Ok, vou lembrar disso.");
//            return;
//        }
//
//        // NOVO
//        if (InterpretadorPerguntas.interpretar(comando, this)) {
//            return;
//        }
//
//        Fato fato = InterpretadorUniversalFatos.interpretar(comando);
//
//        if (fato != null) {
//
//            MemoriaService.salvarFato(
//                    fato.getRelacao(),
//                    fato.getObjeto()
//            );
//
//            //txtResposta.setText("Entendi e aprendi 👍");
//            txtResposta.append("\nLIA: Entendi e aprendi 👍");
//        }
//
//        areaLog.append("> " + comando + "\n");
//        areaLog.append("🧠 Recuperando memória recente...\n");
//        String contextoRecente = MemoriaService.recuperarContexto(3);
//        String contextoRelevante = MemoriaService.buscarMemoriaRelevante(comando);
//        String contexto = contextoRelevante + "\n" + contextoRecente;
//        System.out.println("DEBUG contexto relevante:\n" + contextoRelevante);
//
//        // ============================================================
//        // 1. PRIORIDADE MÁXIMA: ENCERRAR
//        // ============================================================
//        if (comando.contains("sair") || comando.contains("saia") || comando.contains("fechar")) {
//            areaLog.append("👋 Até logo!\n");
//            sintetizador.falar("Até logo, Carlos! Estarei aqui quando precisar."); // Adiciona a voz
//            // Aguarda 2 segundos para o áudio sair antes de matar o processo
//            try {
//                Thread.sleep(2000);
//            } catch (Exception e) {
//            }
//            System.exit(0);
//            return;
//        }
//
//        // ============================================================
//        // 2. SEUS SISTEMAS JAVA (NFIN) - Verificamos antes do Gemini!
//        // ============================================================
//        if (comando.contains("leite") || comando.contains("laite") || comando.contains("laiti")) {
//            sintetizador.falar("Vou abrir o financeiro S Q Laite.");
//            abrirAppJava("C:\\Users\\Carlos\\Dropbox\\NFin\\dist_slt\\jFinNslt.jar", "Financeiro SQLite");
//            return; // IMPORTANTE: Para a execução aqui!
//        }
//
//        if (comando.contains("post") || comando.contains("posti") || comando.contains("poste")) {
//            sintetizador.falar("Vou abrir o financeiro Post Gree.");
//            abrirAppJava("C:\\Users\\Carlos\\Dropbox\\NFin\\dist_pgL\\jFinpg.jar", "Financeiro PostgreSQL");
//            return;
//        }
//
//        if (comando.contains("jfip") || comando.contains("jota")) {
//            sintetizador.falar("Vou abrir o financeiro Jota FIP. Verificando o servidor primeiro.");
//
//            // 1. Verifica e abre o Wamp se necessário
//            if (!isProcessoRodando("wampmanager.exe")) {
//                areaLog.append("⚠️ Wamp desligado. Iniciando...\n");
//                abrirWampServer();
//
//                // 2. Aguarda o Wamp carregar um pouco (ajuste o tempo se necessário)
//                try {
//                    Thread.sleep(5000);
//                } catch (Exception e) {
//                }
//            } else {
//                areaLog.append("✅ Servidor já estava ativo.\n");
//            }
//
//            // 3. Agora sim, abre o seu App Java
//            abrirAppJava("C:\\Users\\Carlos\\Dropbox\\NFin\\dist_sql\\jFIP.jar", "Financeiro SQL");
//            return;
//        }
//
//        if (comando.contains("maria") || comando.contains("maia")) {
//            sintetizador.falar("Vou abrir o financeiro Maria DB. Verificando o servidor primeiro.");
//            // 1. Verifica e abre o Wamp se necessário
//            if (!isProcessoRodando("wampmanager.exe")) {
//                areaLog.append("⚠️ Wamp desligado. Iniciando...\n");
//                abrirWampServer();
//                // 2. Aguarda o Wamp carregar um pouco (ajuste o tempo se necessário)
//                try {
//                    Thread.sleep(5000);
//                } catch (Exception e) {
//                }
//            } else {
//                areaLog.append("✅ Servidor já estava ativo.\n");
//            }
//            // 3. Agora sim, abre o seu App Java
//            abrirAppJava("C:\\Users\\Carlos\\Dropbox\\NFin\\dist_mdb\\MdjFin.jar", "Financeiro Maria DB");
//            return;
//        }
//
//        // ============================================================
//        // 3. PROGRAMAS DO WINDOWS E UTILITÁRIOS
//        // ============================================================
//        if (comando.contains("calculadora")) {
//            abrirCalculadora();
//            return;
//        }
//
//        if (comando.contains("correio")) {
//            abrirPrograma("C:\\Program Files\\Mozilla Thunderbird\\thunderbird.exe", "Thunderbird");
//            return;
//        }
//
//        if (comando.contains("dados") || comando.contains("dbeaver")) {
//            // Usamos o comando "cmd /c" com "start" para solicitar a elevação
//            abrirProgramaAdmin("C:\\Users\\Carlos\\AppData\\Local\\DBeaver\\dbeaver.exe", "DBeaver");
//            return;
//        }
//
//        if (comando.contains("celular")) {
//            abrirPrograma("C:\\Program Files\\Android\\Android Studio\\bin\\studio64.exe", "Android Studio");
//            return;
//        }
//
//        if (comando.contains("youtube")) {
//            pesquisarYouTube(comando);
//            return;
//        }
//
//        if (comando.contains("estado") || comando.contains("status")) {
//            verificarStatus();
//            return;
//        }
//
//        if (comando.contains("limpar") || comando.contains("limpa")) {
//            limparArquivosTemporarios();
//            return;
//        }
//
//        if (comando.contains("lixeira")) {
//            verificarLixeira();
//            return;
//        }
//
//        if (comando.contains("texto") || comando.contains("word")) {
//            abrirPrograma("C:\\Program Files\\Microsoft Office\\root\\Office16\\WINWORD.EXE", "Word");
//            return;
//        }
//
//        if (comando.contains("planilha") || comando.contains("excel")) {
//            abrirPrograma("C:\\Program Files\\Microsoft Office\\root\\Office16\\EXCEL.EXE", "Excel");
//            return;
//        }
//
//        if (comando.contains("notas") || comando.contains("bloco")) {
//            adicionarLog("📝 Lia: Abrindo o Bloco de Notas...");
//            try {
//                new ProcessBuilder("notepad.exe").start();
//            } catch (IOException e) {
//                adicionarLog("Não consegui abrir o Bloco de Notas.");
//            }
//            return;
//        }
//
//        if (comando.contains("visual") || comando.contains("code")) {
//            abrirPrograma("C:\\Users\\Carlos\\AppData\\Local\\Programs\\Microsoft VS Code\\Code.exe", "VS Code");
//            return;
//        }
//
//        if (comando.contains("servidor") || comando.contains("wampserver")) {
//            abrirWampServer();
//            return;
//        }
//
//        if (comando.contains("som") || comando.contains("aimp")) {
//            abrirPrograma("C:\\Program Files\\AIMP\\AIMP.exe", "AIMP");// ... (Mantenha aqui os outros comandos como Word, Excel, etc., sempre com return)
//            return;
//        }
//
//        if (comando.contains("navegador") || comando.contains("google") || comando.contains("chrome")) {
//            abrirPrograma("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe", "Google Chrome");
//            return;
//        }
//
//        if (comando.contains("programa") || comando.contains("netbeans")) {
//            abrirPrograma("C:\\Program Files\\Apache NetBeans\\bin\\netbeans64.exe", "NetBeans");
//            return;
//        }
//
//        if (comando.contains("visual") || comando.contains("code")) {
//            abrirPrograma("C:\\Users\\Carlos\\AppData\\Local\\Programs\\Microsoft VS Code\\Code.exe", "VS Code");
//            return;
//        }
//
//        if (comando.contains("ipca")
//                || comando.contains("indices economicos")
//                || comando.contains("inflação")) {
//            adicionarLog("📊 Lia: Consultando índices econômicos...");
//            new Thread(() -> {
//                String resultado = servicoWeb.obterIndicesEconomicos();
//                adicionarLog("📊 " + resultado);
//            }).start();
//        }
//
//        if (comando.contains("moedas")
//                || comando.contains("dolar")
//                || comando.contains("bitcoin")
//                || comando.contains("btc")
//                || comando.contains("cripto")) {
//            adicionarLog("📊 Lia: Consultando mercado financeiro...");
//            new Thread(() -> {
//                String resultado = servicoWeb.obterCotacoes();
//                adicionarLog("📊 " + resultado);
//            }).start();
//        }
//
//        if (comando.contains("mercado") || comando.contains("economia")) {
//            adicionarLog("📊 Lia: Consultando mercado financeiro...");
//            new Thread(() -> {
//                String resultado = servicoWeb.obterMercado();
//                adicionarLog(resultado);
//            }).start();
//            return;
//        }
//
//        if (comando.contains("manchete") || comando.contains("ultimas")) {
//            adicionarLog("🤖 Lia: Buscando as últimas notícias... aguarde.");
//            new Thread(() -> {
//                String resultado = servicoWeb.obterNoticias();
//                adicionarLog("🤖 " + resultado);
//            }).start();
//        }
//
//        if (comando.contains("notícias")) {
//            String resposta = NoticiasLocais.buscarNoticias();
//            sintetizador.falar("Buscando notícias de Sorocaba.");
//            adicionarLog("📰 Notícias locais(Sorocaba e Região):");
//            adicionarLog("--------------------------------------");
//            adicionarLog(resposta);
//            return;
//        }
//
//        if (comando.contains("panorama")) {
//            adicionarLog("🌅 Panorama do momento.");
//            new Thread(() -> {
//                String resposta = Panorama.gerar(servicoWeb);
//                String[] linhas = resposta.split("\n");
//                for (String linha : linhas) {
//                    if (!linha.trim().isEmpty()) {
//                        falarLinha(linha);
//                        try {
//                            Thread.sleep(1200);
//                        } catch (Exception e) {
//                        }
//                    }
//                }
//            }).start();
//        }
//
//        if (comando.contains("o que você faz") || comando.contains("ajuda") || comando.contains("comandos")) {
//            sintetizador.falar("Carlos, eu posso abrir o sistema financeiro, gerenciar o servidor Wamp, "
//                    + "fazer consultas no Gemini, limpar nossa memória de conversa ou encerrar o sistema. "
//                    + "Basta me chamar pelo nome e dar a ordem.");
//            return;
//        }
//
//        if (comando.contains("quanto usei") || comando.contains("minha cota") || comando.contains("limite do gemini")) {
//            informarCota();
//            return;
//        }
//
//        // === BLOCO DE LEMBRETES CORRIGIDO ===
//        if (comando.contains("lembrete") || comando.contains("lembrei") || comando.contains("anote") || comando.contains("post it")) {
//            String nota = "";
//            if (comando.contains("anote")) {
//                nota = comando.substring(comando.indexOf("anote") + 5).trim();
//            } else if (comando.contains("post it")) {
//                nota = comando.substring(comando.indexOf("post it") + 7).trim();
//            } else if (comando.contains("lembrete")) {
//                nota = comando.substring(comando.indexOf("lembrete") + 8).trim();
//            } else if (comando.contains("lembrei")) {
//                nota = comando.substring(comando.indexOf("lembrei") + 7).trim();
//            }
//
//            if (nota.isEmpty()) {
//                sintetizador.falar("O que exatamente você quer que eu anote, Carlos?");
//            } else {
//                gerenciarAnotacaoVoz(nota);
//                sintetizador.falar("Anotei seu lembrete.");
//                areaLog.append("📝 Post-it criado: " + nota + "\n");
//            }
//            return; // Encerra aqui para não tentar consultar o Gemini
//        }
//
//        // === BLOCO DE ESQUECER CONVERSA (AGORA FORA DO LEMBRETE) ===
//        if (comando.contains("esquecer conversa") || comando.contains("reiniciar papo") || comando.contains("limpar histórico")) {
//            gemini.limparMemoria();
//            sintetizador.falar("Memória de conversa reiniciada, Carlos. Podemos começar um novo assunto.");
//            areaLog.append("🧹 Histórico de conversa esvaziado.\n");
//            return;
//        }
//
//        // Se chegou até aqui e não entrou em nenhum 'if' anterior
//        // Significa que não é um comando conhecido e NÃO vamos ao Gemini.
//        if (!comando.isEmpty()) {
//            System.out.println("DEBUG: Comando ignorado (não reconhecido): " + comando);
//            // Opcional: areaLog.append("❓ Comando não reconhecido.\n");
//        }
//
//    }
    private void processarComando() {

        String comando = campoComando.getText().toLowerCase().trim();
        System.out.println("DEBUG: Comando recebido: [" + comando + "]");
        campoComando.setText("");

        if (comando.isEmpty()) {
            return;
        }

        // ============================================================
        // 1. MODO GEMINI (PRIORIDADE MÁXIMA)
        // ============================================================
        if (comando.startsWith("consulte") || comando.startsWith("consulta")
                || comando.startsWith("pergunta") || comando.startsWith("gemini")) {

            String duvida = comando.replace("consulte", "")
                    .replace("consulta", "")
                    .replace("pergunta", "")
                    .replace("gemini", "")
                    .trim();

            if (!duvida.isEmpty()) {
                processarGemini(duvida);
                contabilizarConsulta();
            }
            return;
        }

        // ============================================================
        // 2. MANUS
        // ============================================================
        if (comando.startsWith("manus")) {
            String prompt = comando.replace("manus", "").trim();

            if (!prompt.isEmpty()) {
                sintetizador.falar("Aguarde, consultando Manus...");
                areaLog.append("🌐 Consultando Manus...\n");

                new Thread(() -> {
                    String resposta = manusService.processarComando(prompt);
                    adicionarLog("🤖 (Manus) " + resposta);
                }).start();
            }
            return;
        }

        // ============================================================
        // 3. OLLAMA (LOCAL)
        // ============================================================
        if (comando.startsWith("mini") || comando.startsWith("qwen")) {

            String modelo = comando.startsWith("qwen") ? "qwen2.5:3b" : "phi3";

            String pergunta = comando.replace("mini", "")
                    .replace("qwen", "")
                    .trim();

            if (!pergunta.isEmpty()) {

                sintetizador.falar("Aguarde, estou pensando...");
                areaLog.append("🧠 LIA (" + modelo + ") pensando...\n");

                new Thread(() -> {

                    String contextoRecente = MemoriaService.recuperarContexto(3);
                    String contextoRelevante = MemoriaService.buscarMemoriaRelevante(pergunta);
                    String contexto = contextoRelevante + "\n" + contextoRecente;

                    String promptFinal = contexto
                            + "\nUsuário: " + pergunta
                            + "\nLia:";

                    String resposta = ollama.consultarLocal(promptFinal, modelo);

                    MemoriaDAO.salvar(pergunta, resposta);

                    adicionarLog("🤖 (" + modelo + ") " + resposta);

                }).start();
            }
            return;
        }

        // ============================================================
        // 4. APRENDIZADO UNIVERSAL (ESTRUTURADO)
        // ============================================================
        Fato fato = InterpretadorUniversalFatos.interpretar(comando);

        if (fato != null) {

            MemoriaService.salvarTripla(
                    fato.getSujeito(),
                    fato.getRelacao(),
                    fato.getObjeto()
            );

            // compatibilidade (opcional futuramente remover)
            MemoriaService.salvarFato(
                    fato.getRelacao(),
                    fato.getObjeto()
            );

            // 🔥 SALVAR CONTEXTO
            Contexto.setUltimaEntidade(fato.getSujeito());

            sintetizador.falar("Entendi e aprendi.");
            areaLog.append("🧠 Aprendido: " + fato.getRelacao() + " = " + fato.getObjeto() + "\n");

            return;
        }

        // ============================================================
        // PAINEL DIÁRIO FINANCEIRO
        // ============================================================
        System.out.println("DEBUG: Verificando comando painel...");
        if (comando.contains("painel") || comando.contains("resumo financeiro") || comando.contains("diário")) {
            System.out.println("DEBUG: Abrindo painel financeiro...");
            PainelDiarioFinanceiro.abrir();
            return;
        }

        // ============================================================
        // 5. APRENDIZADO AUTOMÁTICO (NOVO 🔥)
        // ============================================================
        System.out.println("DEBUG: Verificando aprendiz automatico...");
        if (AprendizAutomatico.aprender(comando, this)) {
            System.out.println("DEBUG: AprendizAutomatico retornou true");
            return;
        }

        // ============================================================
        // 6. PERGUNTAS
        // ============================================================
        if (InterpretadorPerguntas.interpretar(comando, this)) {
            return;
        }

        // ============================================================
        // 7. COMANDOS DO SISTEMA
        // ============================================================
        if (comando.contains("sair") || comando.contains("fechar")) {
            sintetizador.falar("Até logo!");
            System.exit(0);
            return;
        }

        if (comando.contains("youtube")) {
            pesquisarYouTube(comando);
            return;
        }

        if (comando.contains("som") || comando.contains("aimp")) {
            abrirPrograma("C:\\Program Files\\AIMP\\AIMP.exe", "AIMP");// ... (Mantenha aqui os outros comandos como Word, Excel, etc., sempre com return)
            return;
        }

        if (comando.contains("calculadora")) {
            abrirCalculadora();
            return;
        }

        if (comando.contains("texto") || comando.contains("word")) {
            abrirPrograma("C:\\Program Files\\Microsoft Office\\root\\Office16\\WINWORD.EXE", "Word");
            return;
        }

        if (comando.contains("planilha") || comando.contains("excel")) {
            abrirPrograma("C:\\Program Files\\Microsoft Office\\root\\Office16\\EXCEL.EXE", "Excel");
            return;
        }

        if (comando.contains("notas") || comando.contains("bloco")) {
            adicionarLog("📝 Lia: Abrindo o Bloco de Notas...");
            try {
                new ProcessBuilder("notepad.exe").start();
            } catch (IOException e) {
                adicionarLog("Não consegui abrir o Bloco de Notas.");
            }
            return;
        }

        if (comando.contains("chrome") || comando.contains("google") || comando.contains("navegador")) {
            abrirPrograma("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe", "Chrome");
            return;
        }

        if (comando.contains("programa") || comando.contains("netbeans")) {
            abrirPrograma("C:\\Program Files\\Apache NetBeans\\bin\\netbeans64.exe", "NetBeans");
            return;
        }

        if (comando.contains("code") || comando.contains("visual")) {
            abrirPrograma("C:\\Users\\Carlos\\AppData\\Local\\Programs\\Microsoft VS Code\\Code.exe", "VS Code");
            return;
        }

        if (comando.contains("celular")) {
            abrirPrograma("C:\\Program Files\\Android\\Android Studio\\bin\\studio64.exe", "Android Studio");
            return;
        }

        if (comando.contains("estado") || comando.contains("status")) {
            verificarStatus();
            return;
        }

        if (comando.contains("lixeira")) {
            verificarLixeira();
            return;
        }

        if (comando.contains("correio")) {
            abrirPrograma("C:\\Program Files\\Mozilla Thunderbird\\thunderbird.exe", "Thunderbird");
            return;
        }

        if (comando.contains("dados") || comando.contains("dbeaver")) {
            // Usamos o comando "cmd /c" com "start" para solicitar a elevação
            abrirProgramaAdmin("C:\\Users\\Carlos\\AppData\\Local\\DBeaver\\dbeaver.exe", "DBeaver");
            return;
        }

        if (comando.contains("servidor") || comando.contains("wampserver")) {
            abrirWampServer();
            return;
        }

        // ============================================================
        // X. SEUS SISTEMAS JAVA (NFIN)
        // ============================================================
        if (comando.contains("leite") || comando.contains("laite") || comando.contains("laiti")) {
            sintetizador.falar("Vou abrir o financeiro S Q Laite.");
            abrirAppJava("C:\\Users\\Carlos\\Dropbox\\NFin\\dist_slt\\jFinNslt.jar", "Financeiro SQLite");
            return; // IMPORTANTE: Para a execução aqui!
        }

        if (comando.contains("post") || comando.contains("posti") || comando.contains("poste")) {
            sintetizador.falar("Vou abrir o financeiro Post Gree.");
            abrirAppJava("C:\\Users\\Carlos\\Dropbox\\NFin\\dist_pgL\\jFinpg.jar", "Financeiro PostgreSQL");
            return;
        }

        if (comando.contains("jfip") || comando.contains("jota")) {
            sintetizador.falar("Vou abrir o financeiro Jota FIP. Verificando o servidor primeiro.");

            // 1. Verifica e abre o Wamp se necessário
            if (!isProcessoRodando("wampmanager.exe")) {
                areaLog.append("⚠️ Wamp desligado. Iniciando...\n");
                abrirWampServer();

                // 2. Aguarda o Wamp carregar um pouco (ajuste o tempo se necessário)
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                }
            } else {
                areaLog.append("✅ Servidor já estava ativo.\n");
            }

            // 3. Agora sim, abre o seu App Java
            abrirAppJava("C:\\Users\\Carlos\\Dropbox\\NFin\\dist_sql\\jFIP.jar", "Financeiro SQL");
            return;
        }

        if (comando.contains("maria") || comando.contains("maia")) {
            sintetizador.falar("Vou abrir o financeiro Maria DB. Verificando o servidor primeiro.");
            // 1. Verifica e abre o Wamp se necessário
            if (!isProcessoRodando("wampmanager.exe")) {
                areaLog.append("⚠️ Wamp desligado. Iniciando...\n");
                abrirWampServer();
                // 2. Aguarda o Wamp carregar um pouco (ajuste o tempo se necessário)
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                }
            } else {
                areaLog.append("✅ Servidor já estava ativo.\n");
            }
            // 3. Agora sim, abre o seu App Java
            abrirAppJava("C:\\Users\\Carlos\\Dropbox\\NFin\\dist_mdb\\MdjFin.jar", "Financeiro Maria DB");
            return;
        }

        // ============================================================
        // 7. WEB / DADOS
        // ============================================================
        if (comando.contains("manchete") || comando.contains("ultimas")) {
            adicionarLog("🤖 Lia: Buscando as últimas notícias... aguarde.");
            new Thread(() -> {
                String resultado = servicoWeb.obterNoticias();
                adicionarLog("🤖 " + resultado);
            }).start();
        }

        if (comando.contains("moedas")
                || comando.contains("cotações")
                || comando.contains("dolar")
                || comando.contains("bitcoin")
                || comando.contains("btc")
                || comando.contains("cripto")) {
            adicionarLog("📊 Lia: Consultando mercado financeiro...");
            new Thread(() -> {
                String resultado = servicoWeb.obterCotacoes();
                adicionarLog("📊 " + resultado);
            }).start();
            return;
        }

        if (comando.contains("ipca")
                || comando.contains("indices economicos")
                || comando.contains("inflação")) {
            adicionarLog("📊 Lia: Consultando índices econômicos...");
            new Thread(() -> {
                String resultado = servicoWeb.obterIndicesEconomicos();
                adicionarLog("📊 " + resultado);
            }).start();
            return;
        }

        if (comando.contains("mercado") || comando.contains("economia")) {
            adicionarLog("📊 Lia: Consultando mercado financeiro...");
            new Thread(() -> {
                String resultado = servicoWeb.obterMercado();
                adicionarLog(resultado);
            }).start();
            return;
        }

//        if (comando.contains("notícias") || comando.contains("região")) {
//            sintetizador.falar("Buscando notícias locais...");
//            String resposta = NoticiasLocais.buscarNoticias();
//
//            adicionarLog("📰 Notícias:");
//            adicionarLog(resposta);
//            return;
//        }
        if (comando.contains("panorama")) {
            adicionarLog("🌅 Panorama do momento.");
            new Thread(() -> {
                String resposta = Panorama.gerar(servicoWeb);
                String[] linhas = resposta.split("\n");
                for (String linha : linhas) {
                    if (!linha.trim().isEmpty()) {
                        falarLinha(linha);
                        try {
                            Thread.sleep(1200);
                        } catch (Exception e) {
                        }
                    }
                }
            }).start();
        }

        // ============================================================
        // 8. LEMBRETES
        // ============================================================
        if (comando.contains("lembrete") || comando.contains("anote")) {

            String nota = comando.replace("lembrete", "")
                    .replace("anote", "")
                    .trim();

            if (nota.isEmpty()) {
                sintetizador.falar("O que devo anotar?");
            } else {
                gerenciarAnotacaoVoz(nota);
                sintetizador.falar("Anotado.");
                areaLog.append("📝 " + nota + "\n");
            }

            return;
        }

        if (comando.contains("ajuda") || comando.contains("comandos") || comando.contains("o que você faz")) {
            mostrarAjuda();
            return;
        }

        // ============================================================
        // PAINEL DIÁRIO FINANCEIRO
        // ============================================================
        if (comando.contains("painel") || comando.contains("resumo financeiro") || comando.contains("diário")) {
            System.out.println("DEBUG: Abrindo painel financeiro...");
            PainelDiarioFinanceiro.abrir();
            return;
        }

        if (comando.contains("quanto usei") || comando.contains("minha cota") || comando.contains("limite do gemini")) {
            informarCota();
            return;
        }

        // === BLOCO DE ESQUECER CONVERSA (AGORA FORA DO LEMBRETE) ===
        if (comando.contains("esquecer conversa") || comando.contains("reiniciar papo") || comando.contains("limpar histórico")) {
            gemini.limparMemoria();
            sintetizador.falar("Memória de conversa reiniciada, Carlos. Podemos começar um novo assunto.");
            areaLog.append("🧹 Histórico de conversa esvaziado.\n");
            return;
        }

        // ============================================================
        // 9. FALLBACK (NÃO ENTENDIDO)
        // ============================================================
        areaLog.append("❓ Não entendi: " + comando + "\n");
    }

    public void falarLinha(String texto) {
        areaLog.append(texto + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
        if (sintetizador != null) {
            String voz = texto
                    .replace("📊", "")
                    .replace("📈", "")
                    .replace("💵", "")
                    .replace("💶", "")
                    .replace("₿", "")
                    .replace("🔹", "")
                    .replace("*", "")
                    .replace("🌎", "")
                    .replace("\n", " ")
                    .trim();
            sintetizador.falar(voz);
        }
    }

    // Método para salvar uma nota no arquivo (adiciona ao final)
    public void salvarNotaNoArquivo(String texto) {
        // Usamos o FileWriter com 'true' para anexar ao arquivo existente
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("notas_lia.txt", true)))) {
            // O trim() limpa espaços e o println garante que a próxima nota pule linha
            out.println(texto.trim() + " @@@");
        } catch (IOException e) {
            areaLog.append("❌ Erro ao salvar nota: " + e.getMessage() + "\n");
        }
    }

    public void gerenciarAnotacaoVoz(String novaNota) {
        salvarNotaNoArquivo(novaNota);
        if (editorAtual != null && editorAtual.isVisible()) {
            editorAtual.adicionarTextoExterno(novaNota);
            editorAtual.toFront();
        } else {
            editorAtual = new PostItGUI(carregarTextoDoArquivo(), "notas_lia.txt");
        }
    }

    // Método para carregar as notas ao iniciar a Lia
    public void carregarNotasSalvas() {
        File arquivo = new File(CAMINHO_NOTAS);
        if (!arquivo.exists()) {
            return;
        }

        // Em vez de abrir um por um no loop, chamamos o editor uma única vez
        abrirEditorNotas();
    }

    public String carregarTextoDoArquivo() {
        StringBuilder conteudo = new StringBuilder();
        File arquivo = new File("notas_lia.txt");
        if (!arquivo.exists()) {
            return "";
        }
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                // Removemos as arrobas para o usuário editar o texto limpo
                String textoLimpo = linha.replace(" @@@", "").trim();
                if (!textoLimpo.isEmpty()) {
                    conteudo.append(textoLimpo).append("\n");
                }
            }
        } catch (IOException e) {
            areaLog.append("❌ Erro ao ler arquivo: " + e.getMessage() + "\n");
        }
        return conteudo.toString();
    }

    public void abrirEditorNotas() {
        // Se a janela já estiver aberta, apenas trazemos para frente
        if (editorAtual != null && editorAtual.isVisible()) {
            editorAtual.toFront();
            return;
        }
        // Busca o texto que está no arquivo
        String textoParaExibir = carregarTextoDoArquivo();
        // Abre a janela única e guarda a referência nela
        editorAtual = new PostItGUI(textoParaExibir, "notas_lia.txt");
    }

    private void processarGemini(String comando) {
        long agora = System.currentTimeMillis();
        // 1. Filtro de tempo: evita chamadas duplicadas em menos de 3 segundos
        if (agora - ultimaChamadaTime < 3000) {
            areaLog.append("⏳ Calma, Carlos! Aguardando intervalo entre consultas...\n");
            return;
        }
        // 2. Filtro de tamanho: evita enviar "ruído" ou apenas "Lia" para o Google
        if (comando.trim().length() < 5) {
            return;
        }
        ultimaChamadaTime = agora; // Atualiza o tempo da última chamada
        String pergunta = comando.replace("lia", "")
                .replace("consulta", "")
                .replace("pergunta", "")
                .trim();
        // Se a pergunta tiver qualquer conteúdo, manda ver!
        if (pergunta.length() >= 2) {
            adicionarLog("🧠 Lia: Consultando o Gemini...");
            // Aqui segue o seu código normal de envio para o GeminiService...
            // areaLog.append("🤖 Consultando Gemini: " + comando + "\n");
            new Thread(() -> {
                String respostaIA = gemini.consultarIA(pergunta);
                adicionarLog("🤖 " + respostaIA);
            }).start();
        } else {
            adicionarLog("🤖 Sim? O que deseja perguntar ao Gemini?");
        }
    }

    private void verificarStatus() {
        areaLog.append("📊 Iniciando check-up completo do sistema...\n");
        if (sintetizador != null) {
            sintetizador.falar("Iniciando verificação de status. Vou checar os servidores e o armazenamento.");
        }
        // --- 1. VERIFICAR SERVIDORES (Wamp, Apache, MySQL) ---
        boolean wamp = isProcessoRodando("wampmanager.exe");
        boolean apache = isProcessoRodando("httpd.exe");
        boolean mysql = isProcessoRodando("mysqld.exe");
        areaLog.append(String.format("🌐 Servidor Wamp: %s\n", wamp ? "✅ ATIVO" : "❌ DESLIGADO"));
        areaLog.append(String.format("📂 Apache: %s | 🗄️ MySQL: %s\n",
                apache ? "✅ OK" : "⚠️ PARADO", mysql ? "✅ OK" : "⚠️ PARADO"));
        if (sintetizador != null) {
            String msgServidor = wamp ? "O servidor Wamp está ativo." : "O servidor Wamp está desligado.";
            if (wamp && (!apache || !mysql)) {
                msgServidor += " Atenção: um dos serviços internos não está respondendo.";
            }
            sintetizador.falar(msgServidor);
        }

        // --- 2. VERIFICAR ESPAÇO EM DISCO ---
        File[] roots = File.listRoots();
        for (File root : roots) {
            long total = root.getTotalSpace();
            long livre = root.getFreeSpace();
            double totalGB = total / (1024.0 * 1024.0 * 1024.0);
            double livreGB = livre / (1024.0 * 1024.0 * 1024.0);
            double percentualLivre = (livreGB / totalGB) * 100;
            areaLog.append(String.format("💾 Disco %s: %.2f GB livres de %.2f GB (%.1f%% livre)\n",
                    root, livreGB, totalGB, percentualLivre));
            // Se o disco estiver com menos de 10% livre, avisar por voz
            if (percentualLivre < 10 && sintetizador != null) {
                sintetizador.falar("Atenção, Carlos: o espaço no disco " + root + " está acabando.");
            }
        }

        // --- 3. VERIFICAR ARQUIVOS TEMPORÁRIOS ---
        String tempDir = System.getProperty("java.io.tmpdir");
        File tempFolder = new File(tempDir);
        if (tempFolder.exists() && tempFolder.isDirectory()) {
            long tempSize = calcularTamanhoPasta(tempFolder);
            double tempSizeMB = tempSize / (1024.0 * 1024.0);
            if (tempSizeMB > 100) {
                areaLog.append(String.format("⚠️ Pasta temporária: %.2f MB de arquivos.\n", tempSizeMB));
                if (sintetizador != null) {
                    sintetizador.falar("A pasta temporária está com " + String.format("%.0f", tempSizeMB) + " megabytes.");
                }
                int resposta = JOptionPane.showConfirmDialog(
                        this,
                        String.format("A pasta temporária tem %.2f MB.\nDeseja limpá-la agora?", tempSizeMB),
                        "Limpeza Recomendada",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (resposta == JOptionPane.YES_OPTION) {
                    limparArquivosTemporarios();
                }
            } else {
                areaLog.append(String.format("✅ Pasta temporária: %.2f MB (tudo ok!)\n", tempSizeMB));
            }
        }
    }

    private void abrirProgramaAdmin(String caminho, String nomeApp) {
        try {
            adicionarLog("🛡️ Solicitando permissão de Admin para: " + nomeApp);
            // Criamos um processo que chama o Shell do Windows para executar como admin
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "", caminho);
            pb.start();
        } catch (Exception e) {
            adicionarLog("❌ Erro ao abrir " + nomeApp + ": " + e.getMessage());
        }
    }

    private void pararFalaLia() {
        try {
            // 1. Limpa a fila de frases pendentes no sintetizador
            if (sintetizador != null) {
                sintetizador.interromperLimparFila();
            }
            // 2. Mata o processo do PowerShell (Maria) imediatamente
            new ProcessBuilder("taskkill", "/F", "/IM", "powershell.exe").start();
            // 3. Reseta o estado visual e a trava de segurança
            setAvatarFalando(false);
            // 4. Avisar o reconhecedor para descartar o que ouviu até agora
            // Se você tiver acesso ao objeto 'reconhecedorVoz', chame um reset nele.
            adicionarLog("🛑 Interrupção manual: Fala e comandos cancelados.");
        } catch (Exception e) {
            adicionarLog("❌ Erro ao parar: " + e.getMessage());
        }
    }

    private void abrirAppJava(String caminhoJar, String nomeApp) {
        try {
            adicionarLog("🚀 Abrindo app Java: " + nomeApp);
            File arquivo = new File(caminhoJar);
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", arquivo.getName());
            // Define a pasta de trabalho como a pasta onde o JAR está
            pb.directory(arquivo.getParentFile());
            pb.start();
        } catch (Exception e) {
            adicionarLog("❌ Erro ao abrir " + nomeApp + ": " + e.getMessage());
        }
    }

    private void pesquisarYouTube(String comando) {
        try {
            // Remove as palavras de ativação para pegar só o que deve ser buscado
            String busca = comando.replace("lia", "")
                    .replace("procure no youtube", "")
                    .replace("pesquisar no youtube", "")
                    .replace("youtube", "")
                    .trim();
            if (busca.isEmpty()) {
                adicionarLog("📺 Abrindo YouTube...");
                Desktop.getDesktop().browse(new URI("https://www.youtube.com"));
            } else {
                adicionarLog("📺 Pesquisando no YouTube: " + busca);
                // Formata o termo de busca para a URL (troca espaços por +)
                String urlBusca = "https://www.youtube.com/results?search_query=" + busca.replace(" ", "+");
                Desktop.getDesktop().browse(new URI(urlBusca));
            }
        } catch (Exception e) {
            adicionarLog("❌ Erro ao abrir YouTube: " + e.getMessage());
        }
    }

    private void limparArquivosTemporarios() {
        falarComAnimacao("Estou iniciando a limpeza de arquivos temporários");
        areaLog.append("🧹 Iniciando limpeza de arquivos temporários...\n");
        String tempDir = System.getProperty("java.io.tmpdir");
        File tempFolder = new File(tempDir);
        // Primeiro, calcular o tamanho para informar ao usuário
        long tamanhoAtual = calcularTamanhoPasta(tempFolder);
        double tamanhoMB = tamanhoAtual / (1024.0 * 1024.0);
        // Perguntar usando uma caixa de diálogo gráfica
        int resposta = JOptionPane.showConfirmDialog(
                this,
                String.format("A pasta temporária tem %.2f MB.\nDeseja realmente deletar todos os arquivos temporários?", tamanhoMB),
                "Confirmar Limpeza",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (resposta != JOptionPane.YES_OPTION) {
            areaLog.append("❌ Limpeza cancelada pelo usuário.\n");
            return;
        }
        areaLog.append("⏳ Limpando arquivos (pode levar alguns segundos)...\n");
        // Realizar a limpeza em uma thread separada para não travar a interface
        new Thread(new Runnable() {
            @Override
            public void run() {
                int arquivosDeletados = deletarArquivosRecursivamente(tempFolder);
                // Atualizar a interface na thread correta
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        areaLog.append(String.format("✅ Limpeza concluída! %d arquivos/pastas removidos.\n", arquivosDeletados));
                        // Mostrar o espaço liberado
                        long tamanhoFinal = calcularTamanhoPasta(tempFolder);
                        double liberadoMB = (tamanhoAtual - tamanhoFinal) / (1024.0 * 1024.0);
                        if (liberadoMB > 0) {
                            areaLog.append(String.format("📊 Espaço liberado: %.2f MB\n", liberadoMB));
                        }
                    }
                });
            }
        }).start();
    }

    private long calcularTamanhoPasta(File pasta) {
        long tamanho = 0;
        File[] arquivos = pasta.listFiles();
        if (arquivos != null) {
            for (File arquivo : arquivos) {
                if (arquivo.isFile()) {
                    tamanho += arquivo.length();
                } else if (arquivo.isDirectory()) {
                    tamanho += calcularTamanhoPasta(arquivo);
                }
            }
        }
        return tamanho;
    }

    private int deletarArquivosRecursivamente(File pasta) {
        int contador = 0;
        File[] arquivos = pasta.listFiles();
        if (arquivos != null) {
            for (File arquivo : arquivos) {
                try {
                    if (arquivo.isDirectory()) {
                        contador += deletarArquivosRecursivamente(arquivo);
                    }
                    if (arquivo.delete()) {
                        contador++;
                        // Opcional: atualizar o log a cada X arquivos para não sobrecarregar
                        if (contador % 50 == 0) {
                            final int arquivosAteAgora = contador;
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    areaLog.append(String.format("   ...%d arquivos deletados...\n", arquivosAteAgora));
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    // Arquivos em uso não podem ser deletados - ignoramos
                }
            }
        }
        return contador;
    }

    private void abrirPrograma(String caminho, String nome) {
        // ANIMAÇÃO antes de falar
        if (sintetizador != null) {
            sintetizador.falar("Iniciando " + nome);
        }
        try {
            ProcessBuilder pb = new ProcessBuilder(caminho);
            pb.start();
            areaLog.append("✅ " + nome + " iniciado!\n");
        } catch (Exception e) {
            areaLog.append("❌ Erro ao abrir " + nome + ": " + e.getMessage() + "\n");
        }
        // A animação vai parar automaticamente quando a fala terminar
    }

    private void abrirCalculadora() {
        // Lia fala o que vai fazer
        if (sintetizador != null) {
            sintetizador.falar("Abrindo calculadora");
        }
        try {
            if (Desktop.isDesktopSupported()) {
                // A calculadora não é um arquivo, então esta opção não funciona bem
                // Fica apenas como referência
            }
            // Para programas, o ProcessBuilder ainda é melhor
            new ProcessBuilder("calc.exe").start();
            areaLog.append("✅ Calculadora iniciada!\n");
        } catch (Exception e) {
            areaLog.append("❌ Erro ao abrir Calculadora: " + e.getMessage() + "\n");
        }
    }

    private void abrirWampServer() {
        // Lia fala o que vai fazer
        if (sintetizador != null) {
            sintetizador.falar("Abrindo wampserver");
        }
        areaLog.append("🔌 Iniciando WampServer...\n");
        // Possíveis locais do WampServer
        String[] possiveisCaminhos = {
            "C:\\wamp64\\wampmanager.exe",
            "C:\\wamp\\wampmanager.exe",
            System.getenv("ProgramFiles") + "\\wamp64\\wampmanager.exe",
            System.getenv("ProgramFiles(x86)") + "\\wamp\\wampmanager.exe",
            System.getenv("ProgramFiles") + "\\WampServer\\wampmanager.exe"
        };
        boolean encontrado = false;
        for (String caminho : possiveisCaminhos) {
            File arquivo = new File(caminho);
            if (arquivo.exists()) {
                try {
                    // Método 1: Usando ProcessBuilder normal
                    ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "start", "\"\"", "\"" + caminho + "\"");
                    pb.start();
                    areaLog.append("✅ WampServer iniciado! (verifique o ícone na bandeja)\n");
                    encontrado = true;
                    break;
                } catch (Exception e) {
                    // Se falhar, tenta método 2 com elevação de privilégio (moderno)
                    try {
                        ProcessBuilder pbAdmin = new ProcessBuilder("powershell.exe",
                                "-Command",
                                "Start-Process",
                                "'" + caminho + "'",
                                "-Verb", "RunAs");
                        pbAdmin.start();
                        areaLog.append("✅ WampServer iniciado com privilégios de administrador! (confirme o UAC)\n");
                        encontrado = true;
                        break;
                    } catch (Exception ex) {
                        // Continua tentando outros caminhos
                    }
                }
            }
        }

        if (!encontrado) {
            areaLog.append("❌ WampServer não encontrado.\n");
            areaLog.append("   Caminhos procurados:\n");
            for (String caminho : possiveisCaminhos) {
                areaLog.append("   - " + caminho + "\n");
            }
        }
    }

    // Método para verificar lixeira
    private void verificarLixeira() {
        // Lia fala o que vai fazer
        if (sintetizador != null) {
            sintetizador.falar("Estou verificando a lixeira do sistema");
        }
        areaLog.append("🗑️ Verificando lixeira...\n");
        try {
            File[] roots = File.listRoots();
            long tamanhoTotalLixeira = 0;
            int itensNaLixeira = 0;
            for (File root : roots) {
                File lixeira = new File(root, "$Recycle.Bin");
                if (lixeira.exists() && lixeira.isDirectory()) {
                    long tamanho = calcularTamanhoPasta(lixeira);
                    tamanhoTotalLixeira += tamanho;

                    // Contar itens (aproximado)
                    itensNaLixeira += contarArquivos(lixeira);
                }
            }
            double tamanhoMB = tamanhoTotalLixeira / (1024.0 * 1024.0);
            if (tamanhoMB > 10) { // Se tiver mais de 10MB
                areaLog.append(String.format("⚠️ Lixeira: %.2f MB (%d itens aproximadamente)\n",
                        tamanhoMB, itensNaLixeira));
                int resposta = JOptionPane.showConfirmDialog(
                        this,
                        String.format("A lixeira tem %.2f MB.\nDeseja esvaziá-la agora?", tamanhoMB),
                        "Esvaziar Lixeira",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (resposta == JOptionPane.YES_OPTION) {
                    esvaziarLixeira();
                }
            } else {
                areaLog.append("✅ Lixeira vazia ou com poucos arquivos.\n");
            }
        } catch (Exception e) {
            areaLog.append("❌ Erro ao verificar lixeira: " + e.getMessage() + "\n");
        }
    }

    // Método auxiliar para contar arquivos
    private int contarArquivos(File pasta) {
        int contador = 0;
        File[] arquivos = pasta.listFiles();
        if (arquivos != null) {
            for (File arquivo : arquivos) {
                if (arquivo.isFile()) {
                    contador++;
                } else if (arquivo.isDirectory()) {
                    contador += contarArquivos(arquivo);
                }
            }
        }
        return contador;
    }

    // Método para esvaziar lixeira
    private void esvaziarLixeira() {
        // Lia fala o que vai fazer
        if (sintetizador != null) {
            sintetizador.falar("Esvaziando lixeira do sistema");
        }
        areaLog.append("🗑️ Esvaziando lixeira...\n");
        try {
            // Comando do Windows para esvaziar lixeira (moderno)
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c",
                    "powershell -Command \"Clear-RecycleBin -Force\"");
            pb.start();
            areaLog.append("✅ Lixeira esvaziada com sucesso!\n");
        } catch (Exception e) {
            areaLog.append("❌ Erro ao esvaziar lixeira: " + e.getMessage() + "\n");
            areaLog.append("   Tentando método alternativo...\n");
            // Método alternativo: abrir a lixeira para o usuário esvaziar manualmente (corrigido)
            try {
                ProcessBuilder pb = new ProcessBuilder("explorer.exe", "shell:RecycleBinFolder");
                pb.start();
                areaLog.append("   Lixeira aberta para esvaziar manualmente.\n");
            } catch (Exception ex) {
                areaLog.append("❌ Falha também no método alternativo.\n");
            }
        }
    }

    // Novos métodos:
    private void alternarReconhecimentoVoz() {
        if (reconhecedorVoz == null) {
            try {
                // Ajuste o caminho para onde você extraiu o modelo
                String caminhoModelo = "C:\\Users\\Carlos\\OneDrive\\Documentos\\NetBeansProjects\\Lia\\models\\vosk-model-small-pt-0.3";
                org.vosk.LibVosk.setLogLevel(org.vosk.LogLevel.DEBUG);
                reconhecedorVoz = new ReconhecedorVoz(this, caminhoModelo);
                reconhecedorVoz.iniciarEscuta();
                btnVoz.setText("🔴 Parar Voz");
                adicionarLog("🎤 Modo voz ativado - Diga 'Lia' seguido do comando");
                avatarLabel.setIcon(avatarOuvindo); // Ou uma imagem de 'ouvindo' se tiver
                timerCochilo.stop(); // Não dorme enquanto estiver ouvindo
            } catch (Exception ex) {
                adicionarLog("❌ Erro ao iniciar voz: " + ex.getMessage());
            }
        } else {
            reconhecedorVoz.pararEscuta();
            reconhecedorVoz = null;
            btnVoz.setText("🎤 Ativar Voz");
            adicionarLog("🎤 Modo voz desativado");
            timerCochilo.restart(); // Volta a contar para dormir
        }

    }

    // Método público para adicionar log
    public void adicionarLog(String mensagem) {
        areaLog.append(mensagem + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
        if (sintetizador != null) {
            boolean ehResultado = mensagem.contains("✅") || mensagem.contains("⚠️")
                    || mensagem.contains("❌") || mensagem.contains("💾")
                    || mensagem.contains("📊") || mensagem.contains("🤖");
            if (ehResultado && !mensagem.startsWith(">")) {
                String voz = mensagem.replace("✅", "Sucesso: ")
                        .replace("⚠️", "Atenção: ")
                        .replace("❌", "Erro: ")
                        .replace("💾", "Disco: ")
                        .replace("🤖", "")
                        .replace("📊", "")
                        .replace("🔹", "") // <--- ADEUS DIAMANTE AZUL!
                        .replace("*", "") // <--- ADEUS ASTERÍSCO!
                        .replace("🌎", "") // <--- ADEUS GLOBO!
                        .replace("💰", "") // <--- ADEUS SACO DE DINHEIRO!
                        .replace("📈", "") // <--- ADEUS BOLSA!
                        .replace("₿", "") // <--- ADEUS BITCOIN!
                        .replace("💵", "") // <--- ADEUS BITCOIN!
                        .replace("💶", "") // <--- ADEUS EURO!
                        .replace("📈", "")
                        .replace("📉", "")
                        .replace("📊", "")
                        .replace("💵", "")
                        .replace("💶", "")
                        .replace("₿", "")
                        .replace("\n", " ")
                        .trim();
                sintetizador.falar(voz);
            }
        }
    }

    private void falarComAnimacao(String texto) {
        new Thread(() -> {
            try {
                liaFalando = true; // 🔒 FECHA O OUVIDO
                setAvatarFalando(true);
                sintetizador.falar(texto);
                // Aguarda um pouco para garantir que o som parou de sair na caixa
                Thread.sleep(800);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                setAvatarFalando(false);
                liaFalando = false; // 🔓 ABRE O OUVIDO
            }
        }).start();
        // A animação vai parar quando a fala terminar (já acontece no Sintetizador)
    }

    // Getter público para o sintetizador
    public SintetizadorVoz getSintetizador() {
        return sintetizador;
    }

    // Não esqueça de parar a voz ao fechar
    @Override
    public void dispose() {
        if (reconhecedorVoz != null) {
            reconhecedorVoz.pararEscuta();
        }
        if (sintetizador != null) {
            //sintetizador.parar();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        // Executar a interface gráfica na thread correta
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LiaGUI();
            }
        });
    }

    public boolean isProcessoRodando(String nomeProcesso) {
        try {
            // O ProcessBuilder recebe os comandos como uma lista de strings, o que é mais seguro
            ProcessBuilder pb = new ProcessBuilder("tasklist", "/FI", "IMAGENAME eq " + nomeProcesso);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String linha;
            while ((linha = reader.readLine()) != null) {
                // Se a linha contiver o nome do processo, significa que ele foi encontrado
                if (linha.toLowerCase().contains(nomeProcesso.toLowerCase())) {
                    return true;
                }
            }
        } catch (Exception e) {
            if (areaLog != null) {
                areaLog.append("Erro ao checar processo: " + e.getMessage() + "\n");
            }
        }
        return false;
    }

    public void iniciarEstrutura(String caminhoJar, String nomeAmigavel) {
        // 1. Verifica se o Wamp já está rodando
        if (!isProcessoRodando("wampmanager.exe")) {
            areaLog.append("🚀 Servidor desligado. Acionando WampServer...\n");
            abrirWampServer(); // Chama o seu método que já existe
            // Dá um tempo para os serviços (Apache/MySQL) subirem
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
            }
        } else {
            areaLog.append("✅ Servidor Wamp já está ativo.\n");
        }
        // 2. Agora chama o SEU método de abrir App Java com os dados que você passou
        abrirAppJava(caminhoJar, nomeAmigavel);
    }

    private void contabilizarConsulta() {
        consultasHoje++;
        System.out.println("📊 Consultas realizadas hoje: " + consultasHoje);
    }

    // Chame este comando para perguntar à Lia
    private void informarCota() {
        int restam = COTA_MAXIMA - consultasHoje;
        String msg = "Carlos, você já realizou " + consultasHoje + " consultas hoje. "
                + "Ainda restam " + restam + " para atingir o limite diário.";
        sintetizador.falar(msg);
        adicionarLog("📊 Cota Gemini: " + consultasHoje + "/" + COTA_MAXIMA);
    }

    private void mostrarAjuda() {
        JDialog dialog = new JDialog(this, "Ajuda da Lia", true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(this);

        JTextArea areaAjuda = new JTextArea();
        areaAjuda.setEditable(false);
        areaAjuda.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaAjuda.setText("""
            🤖 LIA - ASSISTENTE PESSOAL

            -------------------------------- COMANDOS DE VOZ --------------------------------
            Diga 'Lia' antes de qualquer comando para ativar o reconhecimento de voz.

            -------------------------------- APRENDIZADO ------------------------------------
            A Lia pode aprender informações sobre você e seus pertences!

            Exemplos:
              • "meu carro é preto" - aprende a cor do seu carro
              • "minha casa é branca" - aprende a cor da sua casa
              • "meu cachorro se chama Rex" - aprende o nome do seu cachorro
              • "qual a cor dele?" - pergunta sobre a última coisa aprendida
              • "qual a cor dela?" - pergunta sobre algo feminino

            -------------------------------- SISTEMA ----------------------------------------
              • -liga(liga o PC)      • -desliga(desliga o PC)
              • -reinicia(reinicia)   • -estado(status)
              • -lixeira(esvaziar)

            -------------------------------- COMUNICAÇÃO ------------------------------------
              • -navegador(Google)    • -correio(Thunderbird)

            -------------------------------- PROGRAMAÇÃO ----------------------------------
              • -programa(NetBeans)   • -visual(VSCode)
              • -celular(Android Studio)

            -------------------------------- ESCRITÓRIO -----------------------------------
              • -calculadora           • -planilha(Excel)
              • -notas(Bloco de Notas) • -texto(Word)

            -------------------------------- BANCO DE DADOS --------------------------------
              • -dados(DBeaver)       • -servidor(WampServer)

            -------------------------------- APLICATIVOS -----------------------------------
              • -laite(jFinNslt)      • -post(jFinPG)
              • -jota(jFIP)           • -maria(MdjFin)

            -------------------------------- SERVIÇOS --------------------------------------
              • -manchete(notícias)   • -moedas(cotações)
              • -ipca(inflação)       • -mercado(painel)
              • -panorama(geral)

            -------------------------------- SUPORTE (IA) --------------------------------
              • -consulte(Gemini)     • -qwen(Ollama qwen)
              • -manus(manus)

            -------------------------------- LAZER -----------------------------------------
              • -som(AIMP)            • -youtube(Youtube)

            -------------------------------- FINALIZAR ------------------------------------
              • -sair

            -------------------------------- OUTROS ---------------------------------------
              Para limpar a conversa com o Gemini, digite: 'Lia, esquecer conversa'
              Para verificar sua cota do Gemini, digite: 'quanto usei' ou 'minha cota'

            """);

        JScrollPane scrollPane = new JScrollPane(areaAjuda);
        dialog.add(scrollPane);

        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dialog.dispose());
        JPanel painelSul = new JPanel();
        painelSul.add(btnFechar);
        dialog.add(painelSul, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    public void processarVoz(String texto) {
        System.out.println("🎯 processarVoz: '" + texto + "'");
        // Coloca direto no campo de texto e processa
        campoComando.setText(texto);
        processarComando();
    }

    public boolean isLiaFalando() {
        return this.liaFalando;
    }

    public void falarNoticias(String texto) {
        try {
            String[] linhas = texto.split("\n");
            for (String linha : linhas) {
                if (!linha.trim().isEmpty()) {
                    sintetizador.falar(linha);
                    if (linha.contains("Jornal")) {
                        Thread.sleep(2000); // pausa maior
                    } else {
                        Thread.sleep(1200); // pausa normal
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public String processarComandoTelegram(String comando) {
        comando = comando.toLowerCase().trim();
        if (comando.startsWith("consulte") || comando.startsWith("consulta")
                || comando.startsWith("pergunta") || comando.startsWith("gemini")) {
            String duvida = comando.replace("consulte", "")
                    .replace("consulta", "")
                    .replace("pergunta", "")
                    .replace("gemini", "").trim();
            if (!duvida.isEmpty()) {
                return gemini.consultarIA(duvida);
            }
            return "Pergunta vazia.";
        }

        if (comando.startsWith("manus")) {
            String prompt = comando.replace("manus", "").trim();
            if (!prompt.isEmpty()) {
                return manusService.processarComando(prompt);
            }
            return "Comando Manus vazio.";
        }

        if (comando.startsWith("mini") || comando.startsWith("qwen")) {
            String modelo = comando.startsWith("qwen") ? "qwen2.5:3b" : "phi3";
            String pergunta = comando.replace("mini", "").replace("qwen", "").trim();
            if (!pergunta.isEmpty()) {
                String contextoRecente = MemoriaService.recuperarContexto(3);
                String contextoRelevante = MemoriaService.buscarMemoriaRelevante(pergunta);
                String contexto = contextoRelevante + "\n" + contextoRecente;
                String promptFinal = contexto + "\nUsuário: " + pergunta + "\nLia:";
                String resposta = ollama.consultarLocal(promptFinal, modelo);
                MemoriaDAO.salvar(pergunta, resposta);
                return resposta;
            }
            return "Pergunta vazia.";
        }

        if (comando.contains("moedas") || comando.contains("dolar")
                || comando.contains("bitcoin") || comando.contains("cotações")) {
            return servicoWeb.obterCotacoes();
        }

        if (comando.contains("manchete") || comando.contains("ultimas")) {
            return servicoWeb.obterNoticias();
        }

        if (comando.contains("mercado") || comando.contains("economia")) {
            return servicoWeb.obterMercado();
        }

        if (comando.contains("estado") || comando.contains("status")) {
            return "Lia está online e funcionando.";
        }

        if (comando.contains("painel diário") || comando.contains("painel diario") || comando.contains("resumo financeiro") || comando.contains("resumo do dia")) {
            // Via Telegram usa reserva padrão R$ 500,00 (sem GUI para pedir ao usuário)
            return PainelDiarioFinanceiroService.gerarRelatorioTexto(new java.util.Date(), new java.math.BigDecimal("500.00"));
        }

        return "Comando não reconhecido via Telegram: " + comando;
    }

}
