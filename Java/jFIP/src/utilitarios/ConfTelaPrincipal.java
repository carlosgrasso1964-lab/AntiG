package utilitarios;

import classes.ImageDBHandler;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.swing.filechooser.FileNameExtensionFilter;
import view.Tela_Principal;

public class ConfTelaPrincipal extends JInternalFrame {

    private final Tela_Principal telaPrincipal;
    private final JLabel previewLabel;

    public ConfTelaPrincipal(Tela_Principal telaPrincipal) {
        super("Configurar Imagem de Fundo",
                true, // resizable
                true, // closable
                true, // maximizable
                true);   // iconifiable

        this.telaPrincipal = telaPrincipal;

        // Configurações básicas
        setSize(600, 500);
        // NÃO chamar setVisible(true) aqui!

        // Define o que acontece ao clicar no X (botão fechar)
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

        // Layout principal
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        previewLabel = new JLabel("Selecione uma imagem", SwingConstants.CENTER);
        previewLabel.setBorder(BorderFactory.createEtchedBorder());
        previewLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        previewLabel.setVerticalTextPosition(SwingConstants.CENTER);
        panel.add(previewLabel, BorderLayout.CENTER);

        JButton btnSelecionar = new JButton("Selecionar Imagem...");
        btnSelecionar.addActionListener(e -> selecionarESalvarImagem());
        panel.add(btnSelecionar, BorderLayout.SOUTH);

        add(panel);

        // Opcional: carregar preview inicial se já existir imagem salva
        // carregarPreviewInicial();
    }

    private void selecionarESalvarImagem() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Imagens", "jpg", "jpeg", "png", "gif", "bmp"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            try {
                String dir = System.getProperty("user.dir");
                String destino = dir + File.separator + "temp" + File.separator + "temp_image.png";

                // Cria pasta temp se não existir
                Files.createDirectories(Paths.get(dir, "temp"));

                // Copia para o local fixo
                Files.copy(selected.toPath(), Paths.get(destino),
                        StandardCopyOption.REPLACE_EXISTING);

                // Salva no banco (seu método)
                ImageDBHandler.saveImageToDatabase(new File(destino));

                // Atualiza preview e tela principal na EDT
                SwingUtilities.invokeLater(() -> {
                    atualizarPreview(destino);
                    telaPrincipal.atualizarImagemDeFundo();
                });

                JOptionPane.showMessageDialog(this, "Imagem atualizada com sucesso!");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao copiar/salvar imagem:\n" + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void atualizarPreview(String caminhoImagem) {
        int w = previewLabel.getWidth() > 0 ? previewLabel.getWidth() : 400;
        int h = previewLabel.getHeight() > 0 ? previewLabel.getHeight() : 300;

        ImageIcon icon = new ImageIcon(caminhoImagem);
        if (icon.getIconWidth() <= 0) {
            return; // imagem inválida
        }
        Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        previewLabel.setIcon(new ImageIcon(scaled));
        previewLabel.setText(null);
        previewLabel.revalidate();
        previewLabel.repaint();
    }

    // Opcional: se quiser mostrar preview da imagem atual ao abrir
    /*
    private void carregarPreviewInicial() {
        // Se você tem um método que retorna o caminho da imagem atual
        String caminhoAtual = ImageDBHandler.getCurrentBackgroundPath();
        if (caminhoAtual != null && new File(caminhoAtual).exists()) {
            SwingUtilities.invokeLater(() -> atualizarPreview(caminhoAtual));
        }
    }
     */
}