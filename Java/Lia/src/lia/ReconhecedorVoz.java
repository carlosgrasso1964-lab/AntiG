package lia;

import org.vosk.Model;
import org.vosk.Recognizer;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class ReconhecedorVoz {

    private Model modelo;
    private Recognizer reconhecedor;
    private LiaGUI liaGUI;
    private boolean ouvindo = false;
    private Thread threadEscuta;
    private long ultimoComando = 0;

    public ReconhecedorVoz(LiaGUI gui, String caminhoModelo) throws IOException {
        this.liaGUI = gui;
        File modeloFile = new File(caminhoModelo);
        if (!modeloFile.exists()) {
            throw new IOException("Modelo não encontrado: " + caminhoModelo);
        }
        modelo = new Model(caminhoModelo);
    }

    public void iniciarEscuta() {
        ouvindo = true;

        //Avisa a GUI que começou a ouvir
        SwingUtilities.invokeLater(() -> {
            if (liaGUI != null) {
                liaGUI.setAvatarOuvindo(true);
            }
        });

        threadEscuta = new Thread(() -> {
            try {
                AudioCapturador capturador = new AudioCapturador();
                capturador.iniciarCaptura();

                int sampleRate = capturador.getSampleRate();
                reconhecedor = new Recognizer(modelo, sampleRate);

                System.out.println("🎤 Reconhecedor iniciado - aguardando comandos...");

                // Dá um tempinho para o VOSK estabilizar
                Thread.sleep(500);

                // No loop do ReconhecedorVoz.java
                while (ouvindo) {
                    byte[] audio = capturador.capturarAudio(3);

                    if (reconhecedor.acceptWaveForm(audio, audio.length)) {
                        if (liaGUI != null && !liaGUI.isLiaFalando()) {
                            String resultado = reconhecedor.getResult();
                            processarResultado(resultado);
                        } else {
                            // Se ela estiver falando, limpa o buffer agressivamente
                            reconhecedor.getResult();
                        }
                    } else {
                        // Limpa também os resultados parciais enquanto ela fala
                        if (liaGUI != null && liaGUI.isLiaFalando()) {
                            reconhecedor.getPartialResult();
                        }
                    }
                }

                capturador.pararCaptura();
                reconhecedor.close();

            } catch (Exception e) {
                System.err.println("❌ ERRO no reconhecimento: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // GARANTE que o ícone volta ao normal se a thread morrer
                SwingUtilities.invokeLater(() -> {
                    if (liaGUI != null) {
                        liaGUI.setAvatarOuvindo(false);
                    }
                });
            }
        });
        threadEscuta.start();
    }

    public void pararEscuta() {
        ouvindo = false;
        if (threadEscuta != null) {
            threadEscuta.interrupt();
        }
        // NOVO: Avisa a GUI que parou de ouvir
        SwingUtilities.invokeLater(() -> {
            if (liaGUI != null) {
                liaGUI.setAvatarOuvindo(false);
            }
        });
    }

    private void processarResultado(String jsonResultado) {
        // 1. A PRIMEIRA DEFESA: Se a Lia estiver falando, ignoramos tudo imediatamente.
        // Isso evita que o "Eco" chegue a ser processado ou logado.
        if (liaGUI != null && liaGUI.isLiaFalando()) {
            System.out.println("🔇 ECO IGNORADO: Lia está falando.");
            return;
        }

        System.out.println("🔍 processarResultado RECEBEU: " + jsonResultado);

        // 2. Extração e limpeza do texto
        String texto = extrairTextoDoJSON(jsonResultado).toLowerCase().trim();

        // 3. Filtro de Vazio (Para não poluir o console)
        if (texto.isEmpty()) {
            // Removemos o print de "Texto vazio" para o console ficar limpo
            return;
        }

        System.out.println("📝 Texto extraído FINAL: '" + texto + "'");

        // 4. Proteção anti-loop (2 segundos entre comandos)
        long agora = System.currentTimeMillis();
        if (agora - ultimoComando < 2000) {
            System.out.println("⏱️ Ignorando comando repetido ou muito rápido");
            return;
        }

        ultimoComando = agora;

        // 5. Envio para a Interface
        final String textoFinal = texto;
        SwingUtilities.invokeLater(() -> {
            if (liaGUI != null) {
                liaGUI.adicionarLog("🎤 Você disse: " + textoFinal);

                // Note que aqui uso 'processarVoz', certifique-se que 
                // esse método na LiaGUI chama o 'processarComando()'
                liaGUI.processarVoz(textoFinal);
            }
        });
    }

    private void processarComando(String texto) {
        // Palavras de ativação
        String[] ativacoes = {"lia", "lia", "ia"};

        boolean ativado = false;
        String comando = texto;

        // Verifica palavras de ativação
        for (String ativacao : ativacoes) {
            if (texto.contains(ativacao)) {
                comando = texto.replace(ativacao, "").trim();
                ativado = true;
                break;
            }
        }

        // Se não achou ativação mas é comando direto
        if (!ativado) {
            String[] comandosDiretos = {
                "sair", "estado", "limpar", "navegador", "correio",
                "dados", "celular", "planilha", "texto", "som",
                "visual", "servidor", "lixeira", "calculadora", "programa"
            };
            for (String cmd : comandosDiretos) {
                if (texto.contains(cmd)) {
                    comando = texto;
                    ativado = true;
                    break;
                }
            }
        }

        if (ativado) {
            // MAPEAMENTO CORRETO DOS COMANDOS
            if (comando.contains("calculadora") || comando.contains("calc")) {
                comando = "calculadora";
            } else if (comando.contains("estado") || comando.contains("status")) {
                comando = "estado";
            } else if (comando.contains("limpar")) {
                comando = "limpar";
            } else if (comando.contains("sair")) {
                comando = "sair";
            } else if (comando.contains("navegador") || comando.contains("google") || comando.contains("chrome")) {
                comando = "navegador";  // ← CORRIGIDO
            } else if (comando.contains("correio")) {
                comando = "correio";
            } else if (comando.contains("dados")) {
                comando = "dados";
            } else if (comando.contains("celular")) {
                comando = "celular";
            } else if (comando.contains("planilha") || comando.contains("excel")) {
                comando = "planilha";  // ← CORRIGIDO
            } else if (comando.contains("texto") || comando.contains("word")) {
                comando = "texto";
            } else if (comando.contains("som") || comando.contains("aimp")) {
                comando = "som";
            } else if (comando.contains("visual") || comando.contains("code")) {
                comando = "visual";
            } else if (comando.contains("servidor") || comando.contains("wamp")) {
                comando = "servidor";
            } else if (comando.contains("lixeira")) {
                comando = "lixeira";
            } else if (comando.contains("programa") || comando.contains("netbeans")) {
                comando = "programa";
            }

            final String cmdFinal = comando;
            System.out.println("✅ Executando comando: '" + cmdFinal + "'");

            // Executa o comando
            liaGUI.processarComandoVoz(cmdFinal);
        }
    }

    private String extrairTextoDoJSON(String json) {
        try {
            System.out.println("🔧 EXTRAINDO de: " + json);

            // Método 1: Procurar por "text":"conteudo"
            int posText = json.indexOf("\"text\":\"");
            if (posText >= 0) {
                int inicio = posText + 8; // tamanho de "text":" 
                int fim = json.indexOf("\"", inicio);
                if (fim > inicio) {
                    String resultado = json.substring(inicio, fim);
                    System.out.println("✅ Extraído (método 1): '" + resultado + "'");
                    return resultado;
                }
            }

            // Método 2: Formato alternativo com espaços
            posText = json.indexOf("\"text\" : \"");
            if (posText >= 0) {
                int inicio = posText + 10; // tamanho de "text" : "
                int fim = json.indexOf("\"", inicio);
                if (fim > inicio) {
                    String resultado = json.substring(inicio, fim);
                    System.out.println("✅ Extraído (método 2): '" + resultado + "'");
                    return resultado;
                }
            }

            // Método 3: Split simples (último recurso)
            String[] partes = json.split("\"text\"[:\\s]+\"");
            if (partes.length >= 2) {
                String resto = partes[1];
                int fimAspas = resto.indexOf("\"");
                if (fimAspas > 0) {
                    String resultado = resto.substring(0, fimAspas);
                    System.out.println("✅ Extraído (método 3): '" + resultado + "'");
                    return resultado;
                }
            }

            System.out.println("❌ Não conseguiu extrair texto de: " + json);
            return "";

        } catch (Exception e) {
            System.out.println("❌ Erro ao extrair JSON: " + e.getMessage());
            return "";
        }
    }
}
