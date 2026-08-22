package view;


import utilitarios.Algarismo;
import utilitarios.Conexao;
import utilitarios.Criptografar;
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



public class Tela_Acesso extends javax.swing.JFrame {

    private Conexao conexao;

    private static final Logger LOGGER = Logger.getLogger(Tela_Acesso.class.getName());

    Connection conectado = null;

    PreparedStatement pst = null;

    ResultSet rs = null;



    public Tela_Acesso() throws SQLException {

        initComponents();

        Color minhaCor = new Color(139, 69, 19);

        getContentPane().setBackground(minhaCor);

        

        // Desativar os logs

        LOGGER.setLevel(Level.OFF); // Desativa todos os logs

        /*

        try {

            FileHandler fileHandler = new FileHandler("login_logs.log", true);

            fileHandler.setFormatter(new SimpleFormatter());

            LOGGER.addHandler(fileHandler);

            LOGGER.setLevel(Level.ALL);

        } catch (IOException e) {

            LOGGER.log(Level.SEVERE, "Erro ao configurar o logger", e);

        }

        */



        // Inicializar a conexão

        conexao = new Conexao();



        try {

            conexao.abrirConexao();

            Connection conn = conexao.getConexao();

            if (conn != null) {

                jLabelStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/conec.png")));

                //LOGGER.info("Conexão com o banco de dados estabelecida com sucesso.");

            } else {

                jLabelStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/noconec.png")));

                //LOGGER.warning("Falha ao estabelecer conexão com o banco de dados.");

            }

        } catch (SQLException e) {

            //LOGGER.log(Level.SEVERE, "Erro ao abrir conexão no construtor", e);

        } finally {

            conexao.fecharConexao();

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
        jLabel1.setToolTipText("");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(84, 110, 60, 20));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("SENHA");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 170, -1, 20));

        jLabelStatus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabelStatus.setForeground(new java.awt.Color(255, 255, 255));
        jLabelStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/noconec.png"))); // NOI18N
        jLabelStatus.setToolTipText("");
        getContentPane().add(jLabelStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 250, -1, -1));

        tfUsuario.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tfUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfUsuarioActionPerformed(evt);
            }
        });
        getContentPane().add(tfUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 110, 281, -1));

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
        getContentPane().add(pfSenha, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 170, 281, 29));

        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setText("ENTRAR");
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 250, -1, -1));

        jButtonCancelar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButtonCancelar.setText("CANCELAR");
        jButtonCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelarActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 250, -1, -1));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("SISTEMA FINANCEIRO (JAVA-SQLite)");
        jLabel3.setToolTipText("");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 20, 490, 37));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagens/TSplash.png"))); // NOI18N
        jLabel4.setText("jLabel4");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -10, 510, 330));

        setSize(new java.awt.Dimension(510, 323));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents



    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        try {

            conexao.abrirConexao();

            Connection conn = conexao.getConexao();

            String sql = "SELECT * FROM dados_senhas WHERE usuario = ? AND senha = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            String usuario = tfUsuario.getText().trim();

            String senha = String.valueOf(pfSenha.getPassword()).trim();

            

            //LOGGER.info("Tentativa de login - Usuário: " + usuario);

            //LOGGER.info("Senha digitada (sem criptografia): " + senha);



            stmt.setString(1, usuario);

            String dv = senha.substring(senha.length() - 1);

            //LOGGER.info("Dígito verificador extraído: " + dv);

            String senhaSemDV = senha.substring(0, senha.length() - 1).trim();

            //LOGGER.info("Senha sem dígito verificador: " + senhaSemDV);



            // Calcular o dígito verificador com base na data atual

            int comparardv = Algarismo.Alga();

            //LOGGER.info("Dígito verificador calculado por Algarismo.Alga: " + comparardv);



            if (Integer.parseInt(dv) == comparardv) {

                String senhaCriptografada = Criptografar.encriptografar(senhaSemDV).trim();

                //LOGGER.info("Senha criptografada: " + senhaCriptografada);



                stmt.setString(2, senhaCriptografada);

                ResultSet rs = stmt.executeQuery();



                if (rs.next()) {

                    String nome_bd = rs.getString("usuario").trim();

                    //LOGGER.info("Login bem-sucedido para o usuário: " + nome_bd);

                    Tela_Principal exibir = new Tela_Principal();

                    exibir.setNome(nome_bd);

                    exibir.setVisible(true);

                    setVisible(false);

                    BoasVindas();

                } else {

                    //LOGGER.warning("Falha no login: Usuário ou senha incorretos no banco.");

                    JOptionPane.showMessageDialog(null, "Usuário e/ou senha incorretos");

                }

            } else {

                //LOGGER.warning("Falha no login: Dígito verificador inválido. Digitado: " + dv + ", Esperado: " + comparardv);

                JOptionPane.showMessageDialog(null, "Usuário e/ou senha incorretos");

            }

            stmt.close();

            conn.close();

            Logado();

        } catch (SQLException e) {

            //LOGGER.log(Level.SEVERE, "Erro SQL durante o login", e);

        } catch (IOException ex) {

            //LOGGER.log(Level.SEVERE, "Erro de IO durante o login", ex);

        } catch (Exception ex) {

            //LOGGER.log(Level.SEVERE, "Erro inesperado durante o login", ex);

        } finally {

            conexao.fecharConexao();

        }

    }//GEN-LAST:event_jButton1ActionPerformed



    // Método Acesso simplificado

    public void Acesso() {

        jButton1ActionPerformed(null);

    }



    public void Logado() {

        try {

            conexao.abrirConexao();

            Connection conn = conexao.getConexao();

            String sql = "INSERT INTO logs(usul,data,action) VALUES (?,?,?)";

            PreparedStatement stmt = conn.prepareStatement(sql);

            String usuario = tfUsuario.getText().trim();

            stmt.setString(1, usuario);

            Date dataSistema = new Date();

            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy - HH:mm.ss");

            String dataFormatada = fmt.format(dataSistema);

            stmt.setString(2, dataFormatada);

            stmt.setString(3, "logon");

            //LOGGER.info("Registrando log de acesso - Usuário: " + usuario + ", Data: " + dataFormatada);

            stmt.execute();

            stmt.close();

            conn.close();

            //LOGGER.info("Log de acesso registrado com sucesso.");

        } catch (SQLException e) {

            //LOGGER.log(Level.SEVERE, "Erro ao registrar log de acesso", e);

            JOptionPane.showMessageDialog(null, "Não registrou o Log do Usuário!!! " + e);

        } finally {

            conexao.fecharConexao();

        }

    }





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

            java.util.logging.Logger.getLogger(Tela_Acesso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {

            java.util.logging.Logger.getLogger(Tela_Acesso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {

            java.util.logging.Logger.getLogger(Tela_Acesso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {

            java.util.logging.Logger.getLogger(Tela_Acesso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

        }

        //</editor-fold>

        //</editor-fold>

        //</editor-fold>

        //</editor-fold>



        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override

            public void run() {

                try {

                    new Tela_Acesso().setVisible(true);

                } catch (SQLException ex) {

                    Logger.getLogger(Tela_Acesso.class.getName()).log(Level.SEVERE, null, ex);

                }

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

            JOptionPane.showMessageDialog(null, "\n Bom Dia!  \n \n Boas vindas ao\n\n Sistema Financeiro Pessoal!\n");

        } else if (hora >= 12 && hora < 18) {

            JOptionPane.showMessageDialog(null, "\n Boa Tarde!\n \n Boas vindas ao\n\n Sistema Financeiro Pessoal!\n");

        } else {

            JOptionPane.showMessageDialog(null, "\n Boa Noite!\n \n Boas vindas ao\n\n Sistema Financeiro Pessoal!\n");

        }

    }

}

