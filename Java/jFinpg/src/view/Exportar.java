package view;

import utilitarios.Conexao;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

public class Exportar extends javax.swing.JFrame {

    public Exportar() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextFieldCaminho = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButtonExportar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jButton1.setText("Selecionar pasta");
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButtonExportar.setText("Exportar o Banco de Dados PostgreSQL");
        jButtonExportar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonExportar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExportarActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Backup - Exportar Arquivo");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonExportar, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextFieldCaminho)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1)
                    .addComponent(jTextFieldCaminho, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addComponent(jButtonExportar)
                .addGap(19, 19, 19))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        File diretorioAtual = FileSystemView.getFileSystemView().getHomeDirectory();
        JFileChooser ch = new JFileChooser(diretorioAtual.getAbsolutePath());
        ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int se = ch.showSaveDialog(null);
        if (se == JFileChooser.APPROVE_OPTION) {
            String caminho = ch.getSelectedFile().getPath();
            jTextFieldCaminho.setText(caminho);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    @SuppressWarnings({"deprecation", "deprecation"})
    private void jButtonExportarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportarActionPerformed
        String caminho = jTextFieldCaminho.getText();
        if (caminho.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um diretório de destino para o backup.");
            return;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nomeArquivoBackup = "jfinbkpPtg-" + timeStamp + ".sql";
        String destinoBackup = caminho + File.separator + nomeArquivoBackup;
        if (backupPostgres(destinoBackup)) {
            JOptionPane.showMessageDialog(this, "Backup realizado com sucesso: " + nomeArquivoBackup);
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao realizar o backup.");
        }
    }//GEN-LAST:event_jButtonExportarActionPerformed
    private boolean backupPostgres(String destinoBackup) {
        Connection con = null;
        String host = "";
        String port = "5432";
        String dbName = "jfin";
        String user = "";
        String password = "";
        try {
            // Obter a conexão
            con = Conexao.faz_conexao();
            if (con == null) {
                System.err.println("Falha ao obter a conexão com o banco de dados.");
                return false;
            }
            // Obter os detalhes da conexão
            DatabaseMetaData metaData = con.getMetaData();
            String url = metaData.getURL();
            user = metaData.getUserName();
            
            // Determinar se está na nuvem ou localmente
            boolean isCloud = checkIfRunningInCloud();

            // Use um if para definir a senha com base no ambiente
            if (isCloud) {
                // Nuvem
                user = System.getenv("PG_USER");
                password = System.getenv("PG_PASSWORD");
                url = System.getenv("PG_URL");
            } else {
                // Local
                user = System.getenv("PGLUSER");
                password = System.getenv("PGLSENHA");
                url = System.getenv("PGLURL");
            }
            
            // Extraindo host do URL da conexão
            if (url != null && url.startsWith("jdbc:postgresql://")) {
                url = url.substring(18);
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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            // Fechar a conexão
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        ProcessBuilder pb = new ProcessBuilder(
                //"C:/Program Files/PostgreSQL/16/bin/pg_dump",
                "C:/Program Files/PostgreSQL/17/bin/pg_dump",
                "--host=" + host,
                "--port=" + port,
                "--username=" + user,
                "--dbname=" + dbName,
                "--format=custom",
                "--file=" + destinoBackup
        );
        // Configura a variável de ambiente PGPASSWORD para evitar prompt de senha
        Map<String, String> env = pb.environment();
        env.put("PGPASSWORD", password);
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
        String env = System.getenv("PG_PASSWORD");
        return env != null && env.equals("CLOUD");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonExportar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextFieldCaminho;
    // End of variables declaration//GEN-END:variables
}
