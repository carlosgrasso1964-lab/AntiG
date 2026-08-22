package view;

import classes.Algarismo;
import utilitarios.Conexao;
import classes.Criptografar;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Tela_de_Acesso extends javax.swing.JFrame {

    Connection conectado = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    public Tela_de_Acesso() throws SQLException {
        initComponents();
        Color minhaCor = new Color(139, 69, 19);
        getContentPane().setBackground(minhaCor);
        conectado = Conexao.faz_conexao();
        if (conectado != null) {
            jLabelStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/conec.png")));
        } else {
            jLabelStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/noconec.png")));
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabelStatus = new javax.swing.JLabel();
        tfUsuario = new javax.swing.JTextField();
        pfSenha = new javax.swing.JPasswordField();
        jButton1 = new javax.swing.JButton();
        jButtonCancelar = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Tela de Login");
        setBackground(new java.awt.Color(255, 255, 255));
        setFont(new java.awt.Font("Agency FB", 1, 10)); // NOI18N
        setUndecorated(true);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("USUÁRIO");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 110, -1, 20));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("SENHA");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 170, -1, 20));

        jLabelStatus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabelStatus.setForeground(new java.awt.Color(255, 255, 255));
        jLabelStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/noconec.png"))); // NOI18N
        jLabelStatus.setToolTipText("");
        getContentPane().add(jLabelStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, -1, -1));

        tfUsuario.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tfUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfUsuarioActionPerformed(evt);
            }
        });
        getContentPane().add(tfUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 110, 281, -1));

        pfSenha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pfSenhaActionPerformed(evt);
            }
        });
        pfSenha.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                pfSenhaKeyPressed(evt);
            }
        });
        getContentPane().add(pfSenha, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 170, 281, 29));

        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setText("ENTRAR");
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 240, -1, -1));

        jButtonCancelar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButtonCancelar.setText("CANCELAR");
        jButtonCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 240, -1, -1));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("         SISTEMA FINANCEIRO (JAVA-MariaDB)");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 20, 490, 37));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/TSplash.png"))); // NOI18N
        jLabel4.setText("jLabel4");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -10, 510, 390));

        setSize(new java.awt.Dimension(510, 318));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            try (Connection con = Conexao.faz_conexao()) {
                String sql = "SELECT * FROM dados_senhas WHERE usuario = ? AND senha = ?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, tfUsuario.getText());
                String senha = String.valueOf(pfSenha.getPassword());
                String dv = senha.substring(senha.length() - 1);
                int pd = 0;
                //int comparardv = Algarismo.Alga(pd);
                // Calcular o dígito verificador com base na data atual
                int comparardv = Algarismo.Alga();
                senha = senha.substring(0, senha.length() - 1);
                if (Integer.parseInt(dv) == comparardv) {
                    senha = Criptografar.encriptografar(senha);
                    stmt.setString(2, senha);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String nome_bd = (rs.getString("usuario"));
                        TelaPrincipal exibir = new TelaPrincipal();
                        exibir.setNome(nome_bd);
                        exibir.setVisible(true);
                        setVisible(false);
                        BoasVindas();
                        Logado();
                    } else {
                        JOptionPane.showMessageDialog(null, "Usuário e/ou senha incorretos");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Usuário e/ou senha incorretos");
                }
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(Tela_de_Acesso.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void tfUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfUsuarioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfUsuarioActionPerformed

    private void pfSenhaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pfSenhaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pfSenhaActionPerformed

    private void pfSenhaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pfSenhaKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            Acesso();
        }
    }//GEN-LAST:event_pfSenhaKeyPressed

    private void jButtonCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelarActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButtonCancelarActionPerformed

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
            java.util.logging.Logger.getLogger(Tela_de_Acesso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Tela_de_Acesso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Tela_de_Acesso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tela_de_Acesso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        java.awt.EventQueue.invokeLater(() -> {
            try {
                new Tela_de_Acesso().setVisible(true);
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_Acesso.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonCancelar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JPasswordField pfSenha;
    private javax.swing.JTextField tfUsuario;
    // End of variables declaration//GEN-END:variables

    private void BoasVindas() {
        Calendar agora = Calendar.getInstance();
        int hora = agora.get(Calendar.HOUR_OF_DAY);
        if (hora > 6 && hora < 12) {
            JOptionPane.showMessageDialog(null, "\n Bom Dia!  \n \n Bem vindo ao\n\n Sistema Financeiro Pessoal!\n");
        } else if (hora >= 12 && hora < 18) {
            JOptionPane.showMessageDialog(null, "\n Boa Tarde!\n \n Bem vindo ao\n\n Sistema Financeiro Pessoal!\n");
        } else {
            JOptionPane.showMessageDialog(null, "\n Boa Noite!\n \n Bem vindo ao\n\n Sistema Financeiro Pessoal!\n");
        }
    }

    public void Acesso() {
        try {
            try (Connection con = Conexao.faz_conexao()) {
                String sql = "SELECT * FROM dados_senhas WHERE usuario = ? AND senha = ?";
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, tfUsuario.getText());
                    String senha = String.valueOf(pfSenha.getPassword());
                    String dv = senha.substring(senha.length() - 1);
                    int pd = 0;
                    //int comparardv = Algarismo.Alga(pd);
                    // Calcular o dígito verificador com base na data atual
                    int comparardv = Algarismo.Alga();
                    senha = senha.substring(0, senha.length() - 1);
                    if (Integer.parseInt(dv) == comparardv) {
                        senha = Criptografar.encriptografar(senha);
                        stmt.setString(2, senha);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            String nome_bd = (rs.getString("usuario"));
                            TelaPrincipal exibir = new TelaPrincipal();
                            exibir.setNome(nome_bd);
                            exibir.setVisible(true);
                            setVisible(false);
                            BoasVindas();
                            Logado();
                        } else {
                            JOptionPane.showMessageDialog(null, "Usuário e/ou senha incorretos");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Usuário e/ou senha incorretos");
                    }
                }
            }
        } catch (SQLException e) {
        } catch (IOException ex) {
            Logger.getLogger(Tela_de_Acesso.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Logado() {
        try {
            try (Connection con = Conexao.faz_conexao()) {
                String sql = "INSERT INTO logs(usul,data,action) VALUES (?,?,?)";
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, tfUsuario.getText());
                    Date dataSistema = new Date();
                    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy - HH:mm.ss");
                    stmt.setString(2, fmt.format(dataSistema));
                    stmt.setString(3, "logon");
                    stmt.execute();
                }
            }
        } catch (SQLException e) {
        }
    }
}
