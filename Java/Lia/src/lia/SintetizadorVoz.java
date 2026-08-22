package lia;

import java.util.concurrent.LinkedBlockingQueue;

public class SintetizadorVoz {
    private LiaGUI liaGUI;
    private boolean ocupado = false;
    // A fila de mensagens
    private final LinkedBlockingQueue<String> filaMensagens = new LinkedBlockingQueue<>();
    
    public SintetizadorVoz(LiaGUI gui) {
        this.liaGUI = gui;
        iniciarConsumidorDeFila();
    }

    public boolean estaFalando() {
        return ocupado || !filaMensagens.isEmpty();
    }

    // Agora o método falar apenas adiciona na fila
    public void falar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return;
        }
        filaMensagens.add(texto);
    }

    // Esta Thread fica rodando em segundo plano esperando mensagens chegarem
    private void iniciarConsumidorDeFila() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    // O take() faz a thread "dormir" até aparecer algo na fila
                    String textoParaFalar = filaMensagens.take();
                    executarFalaReal(textoParaFalar);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        t.setDaemon(true); // A thread morre junto com o programa
        t.start();
    }

    private void executarFalaReal(String texto) {
        try {
            ocupado = true;
            // 1. Bloqueia o microfone e muda o avatar
            if (liaGUI != null) {
                liaGUI.setAvatarFalando(true);
            }
            System.out.println("🗣️ FALANDO: " + texto);
            texto = texto.replace("\n", "... ")
                    .replace("\r", " ")
                    .replace("'", "''");

            String comando = "powershell -Command \"Add-Type -AssemblyName System.Speech; "
                    + "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; "
                    + "$synth.SelectVoice('Microsoft Maria Desktop'); "
                    + "$synth.Rate = 2; " //controle da velocidade da fala
                    + "$synth.Volume = 100; "
                    + "$synth.Speak('" + texto + "');\"";
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", comando);
            Process processo = pb.start();
            processo.waitFor();

        } catch (Exception e) {
            System.out.println("❌ Erro ao falar: " + e.getMessage());
        } finally {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
            ocupado = false;

            if (liaGUI != null) {
                liaGUI.setAvatarFalando(false);
            }
        }
    }

    public void limparFila() {
        filaMensagens.clear(); // Esvazia a lista de frases pendentes
        ocupado = false;
    }

    public void interromperLimparFila() {
        filaMensagens.clear(); // Esvazia o que estava por vir
        ocupado = false;
        // Opcional: você pode tentar matar o processo aqui também, 
        // mas vamos fazer na GUI que é mais garantido.
    }

    public void falarNoticias(String texto) {
        try {
            String[] linhas = texto.split("\n");
            for (String linha : linhas) {
                if (!linha.trim().isEmpty()) {
                    falar(linha);
                    if (linha.contains("Jornal")) {
                        Thread.sleep(2000); // pausa maior entre jornais
                    } else {
                        Thread.sleep(1200); // pausa entre manchetes
                    }
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}