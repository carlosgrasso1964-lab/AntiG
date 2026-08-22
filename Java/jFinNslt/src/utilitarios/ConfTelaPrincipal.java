package utilitarios;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import view.Tela_Principal;

public class ConfTelaPrincipal extends JFrame {

    private final Tela_Principal telaPrincipal;
    private final JLabel previewLabel;  // renomeei para ficar claro que é preview

    public ConfTelaPrincipal(Tela_Principal telaPrincipal) {
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

                // Salva no banco (se ainda usa)
                ImageDBHandler.saveImageToDatabase(new File(destino));

                // Atualiza o preview (opcional, mas ajuda o usuário)
                ImageIcon icon = new ImageIcon(destino);
                Image scaled = icon.getImage().getScaledInstance(previewLabel.getWidth(), previewLabel.getHeight(), Image.SCALE_SMOOTH);
                previewLabel.setIcon(new ImageIcon(scaled));
                previewLabel.setText(null);

                // Atualiza a tela principal (aqui está o ponto chave!)
                telaPrincipal.atualizarImagemDeFundo();   // ? chama o método que sugeri antes

                JOptionPane.showMessageDialog(this, "Imagem atualizada com sucesso!");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao copiar/salvar imagem:\n" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}