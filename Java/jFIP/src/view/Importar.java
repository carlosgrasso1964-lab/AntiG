package view;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

        jButtonImportar.setText("Importar o Banco de Dados MySQL");
        jButtonImportar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonImportar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonImportarActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setText("Backup - Importar Arquivo");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(76, 76, 76)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 72, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButtonImportar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTextFieldCaminho)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonSelecionar)))))
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
        FileNameExtensionFilter filter = new FileNameExtensionFilter("SQL", "sql");
        File diretorio = new File("Backup");
        JFileChooser fc = new JFileChooser(diretorio);
        fc.setFileFilter(filter);
        fc.showOpenDialog(this);
        try {
            File f = fc.getSelectedFile();
            path = f.getAbsolutePath();
            path = path.replace('\\','/');
            jTextFieldCaminho.setText(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButtonSelecionarActionPerformed

    private void jButtonImportarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonImportarActionPerformed
        if (path == null || path.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um arquivo de backup para importar.");
            return;
        }

        if (importarMySQL(path)) {
            JOptionPane.showMessageDialog(this, "Importação realizada com sucesso.");
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao realizar a importação.");
        }
    }//GEN-LAST:event_jButtonImportarActionPerformed

    private boolean importarMySQL(String caminhoBackup) {
         String host = "";
        String port = "3306";
        String dbName = "jfin";
        String user = "";
        String password = "";

        try {
            // Determinar se está na nuvem ou localmente
            boolean isCloud = checkIfRunningInCloud();

            // Use um if para definir a senha com base no ambiente
            String url;
            if (isCloud) {
                // Nuvem
                user = "root"; //System.getenv("AW_USER");
                password = System.getenv("PGLSENHA");
                url = System.getenv("AW_URL");
            } else {
                // Local
                user = "root";
                password = System.getenv("PGLSENHA");
                url = System.getenv("DB_URL");
            }

            // Extraindo host, porta e nome do banco de dados do URL da conexão
            if (url != null && url.startsWith("jdbc:mysql://")) {
                url = url.substring(13);
                int queryParamIndex = url.indexOf('?');
                if (queryParamIndex != -1) {
                    url = url.substring(0, queryParamIndex); // Remover parâmetros da URL
                }
                int colonIndex = url.indexOf(':');
                int slashIndex = url.indexOf('/');
                if (colonIndex != -1 && slashIndex != -1) {
                    host = url.substring(0, colonIndex);
                    port = url.substring(colonIndex + 1, slashIndex);
                    dbName = url.substring(slashIndex + 1);
                }
            }

            if (password == null || password.isEmpty()) {
                System.err.println("A senha do banco de dados não está definida nas variáveis de ambiente.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // ProcessBuilder para executar o mysqlimport
        ProcessBuilder pb = new ProcessBuilder(
                "mysql",
                "--host=" + host,
                "--port=" + port,
                "--user=" + user,
                "--password=" + password,
                dbName,
                "-e",
                "source " + caminhoBackup
        );

        try {
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return true;
            } else {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println(line);
                    }
                }
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    
    // Função para determinar se está rodando na nuvem
    private boolean checkIfRunningInCloud() {
        // Implemente sua lógica aqui para determinar se está na nuvem
        // Pode ser baseado no hostname, endereço IP, variáveis de ambiente específicas, etc.
        String env = System.getenv("AW_PASSWORD");
        return env != null && env.equals("CLOUD");
    }

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
