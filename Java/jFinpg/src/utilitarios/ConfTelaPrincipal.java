package utilitarios;

import classes.ImageDBHandler;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import view.TelaPrincipal;

public class ConfTelaPrincipal extends JFrame {

    private final TelaPrincipal telaPrincipal;
    private final JLabel previewLabel;  // renomeei para ficar claro que é preview

    public ConfTelaPrincipal(TelaPrincipal telaPrincipal) {
        this.telaPrincipal = telaPrincipal;
        setTitle("Configurar Imagem de Fundo");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        previewLabel = new JLabel("Selecione uma imagem", SwingConstants.CENTER);
        previewLabel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(previewLabel, BorderLayout.CENTER);

        JButton btnSelecionar = new JButton("Selecionar Imagem...");
        btnSelecionar.addActionListener(e -> selecionarESalvarImagem());
        panel.add(btnSelecionar, BorderLayout.SOUTH);

        add(panel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                telaPrincipal.setVisible(true);
                dispose();
            }
        });

        setVisible(true);
    }

    private void selecionarESalvarImagem() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imagens", "jpg", "jpeg", "png", "gif", "bmp"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            try {
                String dir = System.getProperty("user.dir");
                String destino = dir + File.separator + "temp" + File.separator + "temp_image.png";

                // Cria pasta temp se não existir
                new File(dir + File.separator + "temp").mkdirs();

                // Copia para o local fixo
                Files.copy(selected.toPath(), Paths.get(destino), StandardCopyOption.REPLACE_EXISTING);

                // Salva no banco
                ImageDBHandler.saveImageToDatabase(new File(destino));

                // ? Pequena pausa para garantir que o arquivo temp foi escrito,
                // depois atualiza o preview e a tela principal na EDT
                SwingUtilities.invokeLater(() -> {
                    // Atualiza preview local
                    int w = previewLabel.getWidth() > 0 ? previewLabel.getWidth() : 400;
                    int h = previewLabel.getHeight() > 0 ? previewLabel.getHeight() : 300;
                    ImageIcon icon = new ImageIcon(destino);
                    Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    previewLabel.setIcon(new ImageIcon(scaled));
                    previewLabel.setText(null);
                    previewLabel.revalidate();
                    previewLabel.repaint();

                    // Atualiza a TelaPrincipal
                    telaPrincipal.atualizarImagemDeFundo();
                });

                JOptionPane.showMessageDialog(this, "Imagem atualizada com sucesso!");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao copiar/salvar imagem:\n" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
