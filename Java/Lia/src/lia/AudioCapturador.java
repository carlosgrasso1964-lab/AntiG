package lia;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

public class AudioCapturador {
    private TargetDataLine linhaAudio;
    private AudioFormat formato;
    private boolean capturando = false;
    private int sampleRate = 16000; // Mudamos para 16000 que é mais compatível

    public AudioCapturador() {
        // Formatos mais compatíveis (tentativa em ordem)
        AudioFormat[] formatosTentar = {
            new AudioFormat(16000, 16, 1, true, false),
            new AudioFormat(22050, 16, 1, true, false),
            new AudioFormat(44100, 16, 1, true, false),
            new AudioFormat(8000, 16, 1, true, false)
        };
        
        for (AudioFormat fmt : formatosTentar) {
            try {
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, fmt);
                if (AudioSystem.isLineSupported(info)) {
                    this.formato = fmt;
                    this.sampleRate = (int) fmt.getSampleRate();
                    System.out.println("✅ Áudio format OK: " + sampleRate + " Hz");
                    return;
                }
            } catch (Exception e) {
                // Tenta próximo
            }
        }
        
        // Se nada funcionar, usa 16000 mesmo (pode falhar)
        this.formato = new AudioFormat(16000, 16, 1, true, false);
        this.sampleRate = 16000;
        System.out.println("⚠️ Usando formato padrão 16000 Hz");
    }
    
    public void iniciarCaptura() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, formato);
        linhaAudio = (TargetDataLine) AudioSystem.getLine(info);
        linhaAudio.open(formato);
        linhaAudio.start();
        capturando = true;
        System.out.println("✅ Captura iniciada em " + sampleRate + " Hz");
    }
    
    public byte[] capturarAudio(int duracaoSegundos) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int totalBytes = 0;
        int bytesPorSegundo = sampleRate * 2;
        int bytesAlvo = duracaoSegundos * bytesPorSegundo;
        
        long inicio = System.currentTimeMillis();
        while (capturando && totalBytes < bytesAlvo && 
               (System.currentTimeMillis() - inicio) < (duracaoSegundos * 1000 + 500)) {
            int bytesRead = linhaAudio.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                out.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
        }
        
        return out.toByteArray();
    }
    
    public void pararCaptura() {
        capturando = false;
        if (linhaAudio != null) {
            linhaAudio.stop();
            linhaAudio.close();
        }
    }
    
    public int getSampleRate() {
        return sampleRate;
    }
}