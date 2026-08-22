package utilitarios;
/**
 *
 * @author CARLOS
 */
import java.nio.file.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;


public class Backup extends JFrame {

    private JButton btnBackup;
    private JTextField txtCaminho;

    public Backup() {
        setTitle("Backup SQLite");
        setSize(300, 150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        btnBackup = new JButton("Realizar Backup");
        txtCaminho = new JTextField(20);

        btnBackup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarBackup();
            }
        });

        JPanel panel = new JPanel();
        panel.add(new JLabel("Caminho Destino:"));
        panel.add(txtCaminho);
        panel.add(btnBackup);

        add(panel);
        setVisible(true);
    }

    private void realizarBackup() {
        String caminhoDestino = txtCaminho.getText();
        if (caminhoDestino.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe um caminho de destino.");
            return;
        }

        String origemSQLite = "C:/Users/USER/Dropbox/NFin/dist_slt/jfinslt.db";
        String nomeArquivoBackup = "backup_sqlite-" + System.currentTimeMillis() + ".db";
        String destinoBackup = caminhoDestino + File.separator + nomeArquivoBackup;

        if (backupSQLite(origemSQLite, destinoBackup)) {
            JOptionPane.showMessageDialog(this, "Backup realizado com sucesso: " + nomeArquivoBackup);
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao realizar o backup.");
        }
    }

    public static boolean backupSQLite(String origem, String destino) {
        try {
            File origemFile = new File(origem);
            File destinoFile = new File(destino);

            Files.copy(origemFile.toPath(), destinoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Backup();
            }
        });
    }
}    

