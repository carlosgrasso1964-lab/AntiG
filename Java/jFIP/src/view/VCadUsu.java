
package view;

import utilitarios.Conexao;
import classes.Criptografar;
import java.util.logging.Level;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.sql.PreparedStatement;
import java.util.logging.Logger;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author CARLOS
 */
public class VCadUsu extends javax.swing.JInternalFrame {

    /**
     * Creates new form vCad
     */
    public VCadUsu() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tfId = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        tfUsuario = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        pfSenha = new javax.swing.JPasswordField();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbDados = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        btSalvar = new javax.swing.JButton();
        btnAtualizar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        btnFechar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        tfBusca = new javax.swing.JTextField();
        btAbrir = new javax.swing.JButton();
        btnListarDados = new javax.swing.JButton();

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Cadastro de Usuários");

        jLabel2.setText("Id");

        tfId.setEditable(false);

        jLabel3.setText("Usuário");

        jLabel4.setText("Senha");

        tbDados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Usuário", "Senha"
            }
        ));
        tbDados.setSelectionBackground(new java.awt.Color(255, 255, 204));
        tbDados.setSelectionForeground(new java.awt.Color(51, 51, 51));
        tbDados.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDadosMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tbDados);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Ações"));

        btSalvar.setText("Salvar");
        btSalvar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSalvarActionPerformed(evt);
            }
        });

        btnAtualizar.setText("Atualizar");
        btnAtualizar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAtualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtualizarActionPerformed(evt);
            }
        });

        btnExcluir.setText("Excluir");
        btnExcluir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcluirActionPerformed(evt);
            }
        });

        btnFechar.setText("Fechar");
        btnFechar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFecharActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(btSalvar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAtualizar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnExcluir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnFechar)
                .addGap(0, 14, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSalvar)
                    .addComponent(btnAtualizar)
                    .addComponent(btnExcluir)
                    .addComponent(btnFechar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Abrir Dados"));

        btAbrir.setText("Abrir");
        btAbrir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAbrirActionPerformed(evt);
            }
        });

        btnListarDados.setText("Listar Dados");
        btnListarDados.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnListarDados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnListarDadosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btAbrir, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfBusca, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnListarDados, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(tfBusca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btAbrir)
                .addComponent(btnListarDados))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel2)
                                    .addGap(50, 50, 50)
                                    .addComponent(tfId, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addGap(28, 28, 28)
                                        .addComponent(pfSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addGap(20, 20, 20)
                                        .addComponent(tfUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(111, 111, 111)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(tfId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(tfUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(pfSenha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        setBounds(350, 100, 609, 519);
    }// </editor-fold>//GEN-END:initComponents

    private void tbDadosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDadosMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tbDadosMouseClicked

    @SuppressWarnings("deprecation")
    private void btSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSalvarActionPerformed
        if (tfUsuario.getText().equals("") || pfSenha.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Usuário e/ou Senha em branco!");
        } else {
            try {
                Connection con;
                con = Conexao.faz_conexao();
                String sql = "INSERT INTO dados_senhas(usuario, senha) VALUES (?,?)";
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, tfUsuario.getText());
                    String senha = String.valueOf(pfSenha.getPassword());
                    senha = Criptografar.encriptografar(senha);
                    stmt.setString(2, senha);
                    stmt.execute();
                }
                con.close();
                JOptionPane.showMessageDialog(null, "Usuário cadastrado com sucesso!");
                //Limpar dados dos campos
                tfId.setText("");
                tfUsuario.setText("");
                pfSenha.setText("");
            } catch (SQLException ex) {
                Logger.getLogger(VCadUsu.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btSalvarActionPerformed

    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarActionPerformed
        if (tfId.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Informe o Id");
        } else {
            try {
                Connection con = Conexao.faz_conexao();
                String sql = "UPDATE dados_senhas SET usuario=?, senha=? WHERE id=?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, tfUsuario.getText());
                String senha = String.valueOf(pfSenha.getPassword());
                senha = Criptografar.encriptografar(senha);
                System.out.println(senha);
                stmt.setString(2, senha);
                stmt.setString(3, tfId.getText());
                stmt.execute();
                stmt.close();
                con.close();
                JOptionPane.showMessageDialog(null, "Usuário/Senha atualizados com sucesso!");
                btSalvar.setEnabled(true);
                tfId.setText("");
                tfUsuario.setText("");
                pfSenha.setText("");
                tfBusca.setText("");
            } catch (SQLException ex) {
                Logger.getLogger(VCadUsu.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnAtualizarActionPerformed

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed
        if (tfId.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Informe o registro(id) à ser excluído.");
        } else {
            try {
                Connection con = Conexao.faz_conexao();
                String sql = "DELETE FROM dados_senhas WHERE id=?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, tfId.getText());
                stmt.execute();
                stmt.close();
                con.close();
                JOptionPane.showMessageDialog(null, "Usuario/Senha excluídos com sucesso!");
                tfId.setText("");
                tfUsuario.setText("");
                pfSenha.setText("");
                tfBusca.setText("");
            } catch (SQLException ex) {
                Logger.getLogger(VCadUsu.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnExcluirActionPerformed

    private void btnFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFecharActionPerformed
        dispose();
    }//GEN-LAST:event_btnFecharActionPerformed

    private void btAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAbrirActionPerformed
        if (tfBusca.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Informe um Id válido...");
        } else {
            try {
                try ( // Código para pesquisar um usuario
                    Connection con = Conexao.faz_conexao()) {
                    String sql = "SELECT * FROM dados_senhas WHERE id =?";
                    PreparedStatement stmt = con.prepareStatement(sql);
                    stmt.setString(1, tfBusca.getText());
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            tfId.setText(rs.getString("id"));
                            tfUsuario.setText(rs.getString("usuario"));
                            pfSenha.setText(rs.getString("senha"));
                            btSalvar.setEnabled(false);
                        }
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(VCadUsu.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btAbrirActionPerformed

    private void btnListarDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnListarDadosActionPerformed
        try {
            Connection con = Conexao.faz_conexao();
            String sql = "Select * FROM dados_senhas";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel modelo = (DefaultTableModel) tbDados.getModel();
            tbDados.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
            tbDados.getColumn(tbDados.getColumnName(0)).setPreferredWidth(30);
            tbDados.getColumn(tbDados.getColumnName(1)).setPreferredWidth(130);
            tbDados.getColumn(tbDados.getColumnName(2)).setPreferredWidth(350);
            modelo.setNumRows(0);
            while (rs.next()) {
                //modelo.addRow(new Object[]{rs.getString("id"), rs.getString("usuario"), rs.getString("senha")});
                modelo.addRow(new Object[]{rs.getString("id"), rs.getString("usuario"), "******"});
            }
            rs.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(VCadUsu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnListarDadosActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAbrir;
    private javax.swing.JButton btSalvar;
    private javax.swing.JButton btnAtualizar;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnFechar;
    private javax.swing.JButton btnListarDados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPasswordField pfSenha;
    private javax.swing.JTable tbDados;
    private javax.swing.JTextField tfBusca;
    private javax.swing.JTextField tfId;
    private javax.swing.JTextField tfUsuario;
    // End of variables declaration//GEN-END:variables
}
