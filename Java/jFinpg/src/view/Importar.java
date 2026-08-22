package view;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Importar extends javax.swing.JFrame {

    String path = null;

    public Importar() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextFieldCaminho = new javax.swing.JTextField();
        jButtonSelecionar = new javax.swing.JButton();
        jButtonImportar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTextFieldCaminho.setEditable(false);

        jButtonSelecionar.setText("Selecionar pasta");
        jButtonSelecionar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonSelecionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelecionarActionPerformed(evt);
            }
        });

        jButtonImportar.setText("Importar o Banco de Dados PostgreSQL");
        jButtonImportar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonImportar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonImportarActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Backup - Importar Arquivo");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButtonImportar, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTextFieldCaminho)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonSelecionar)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonSelecionar)
                    .addComponent(jTextFieldCaminho, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonImportar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSelecionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelecionarActionPerformed
    /*    FileNameExtensionFilter filter = new FileNameExtensionFilter("SQL", "sql");
        File diretorio = new File("Backup");
        JFileChooser fc = new JFileChooser(diretorio);
        fc.setFileFilter(filter);
        fc.showOpenDialog(this);
        try {
            File f = fc.getSelectedFile();
            path = f.getAbsolutePath();
            path = path.replace('\\', '/');
            jTextFieldCaminho.setText(path);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }//GEN-LAST:event_jButtonSelecionarActionPerformed

    private void jButtonImportarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonImportarActionPerformed
        
        
        /*String user = System.getenv("PGLUSER");
        String pass = System.getenv("PGLSENHA");
        String dbName = "jfin";
        String host = System.getenv("PGLURL");
        String port = "5432";  // porta padrão do PostgreSQL

        // Remover o prefixo "jdbc:postgresql://" se estiver presente
        if (host.startsWith("jdbc:postgresql://")) {
            host = host.substring(18);
            int colonIndex = host.indexOf(':');
            if (colonIndex != -1) {
                host = host.substring(0, colonIndex);
            }
        }

        String pgRestorePath = "C:/Program Files/PostgreSQL/16/bin/pg_restore.exe"; // Caminho para pg_restore no Windows
        // Ou "/usr/bin/pg_restore" para Linux

        String caminhoArquivo = jTextFieldCaminho.getText();

        if (caminhoArquivo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um arquivo de backup para restaurar.");
            return;
        }

        String[] restoreCmd = {
            pgRestorePath,
            "--host=" + host,
            "--port=" + port,
            "--username=" + user,
            "--dbname=" + dbName,
            "--clean", // Remove objetos existentes antes de restaurar
            "--create", // Cria o banco de dados antes de restaurar
            caminhoArquivo
        };

        ProcessBuilder pb = new ProcessBuilder(restoreCmd);

        // Configura a variável de ambiente PGPASSWORD para evitar prompt de senha
        Map<String, String> env = pb.environment();
        env.put("PGPASSWORD", pass);

        try {
            Process process = pb.start();

            // Monitoramento do processo
            Thread monitorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            monitorThread.start();

            // Temporizador para imprimir progresso
            long startTime = System.currentTimeMillis();
            while (process.isAlive()) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                System.out.println("Processo em andamento... " + (elapsedTime / 1000) + " segundos");
                Thread.sleep(5000); // Atualiza a cada 5 segundos
            }

            int procCom = process.waitFor();
            monitorThread.join(); // Aguarda o término da thread de monitoramento

            if (procCom == 0) {
                jLabel2.setText("Restaurado com Sucesso!!!");
            } else {
                jLabel2.setText("Restauração não realizada!");
                System.err.println("Erro ao restaurar o backup.");
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Importar.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }//GEN-LAST:event_jButtonImportarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Importar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Importar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Importar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Importar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new Importar().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonImportar;
    private javax.swing.JButton jButtonSelecionar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jTextFieldCaminho;
    // End of variables declaration//GEN-END:variables
}
