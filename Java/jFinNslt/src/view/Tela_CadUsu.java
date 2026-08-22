package view;


import utilitarios.Criptografar;

import dao.UsuariosDAO;

import java.io.IOException;

import java.sql.SQLException;

import java.util.List;

import java.util.logging.Level;

import java.util.logging.Logger;

import javax.swing.JOptionPane;

import javax.swing.table.DefaultTableModel;

import model.Usuarios;

import relatorios.RelUsers;

import utilitarios.LimpaTela;



public class Tela_CadUsu extends javax.swing.JFrame {



    public void listar() throws SQLException, ClassNotFoundException {

        UsuariosDAO dao = new UsuariosDAO();

        List<Usuarios> lista = dao.Listar();

        DefaultTableModel dados = (DefaultTableModel) tbDados.getModel();

        tbDados.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

        tbDados.getColumn(tbDados.getColumnName(0)).setPreferredWidth(30);

        tbDados.getColumn(tbDados.getColumnName(1)).setPreferredWidth(130);

        tbDados.getColumn(tbDados.getColumnName(2)).setPreferredWidth(380);

        dados.setNumRows(0);

        for (Usuarios c : lista) {

            dados.addRow(new Object[]{

                c.getId(),

                c.getUsuario(),

                "********" // ou "" ou "********"

            });

        }

    }



    public Tela_CadUsu() {

        initComponents();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

    }



    @SuppressWarnings("unchecked")

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        btSalvar = new javax.swing.JButton();
        btnAtualizar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        btnFechar = new javax.swing.JButton();
        jButtonImprimir = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        tfBusca = new javax.swing.JTextField();
        btAbrir = new javax.swing.JButton();
        btnListarDados = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanelUsuarios = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        tfId = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        tfUsuario = new javax.swing.JTextField();
        pfSenha = new javax.swing.JPasswordField();
        jLabel4 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbDados = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("       Cadastro de Usuários");
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Ações"));

        btSalvar.setText("Salvar");
        btSalvar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSalvarActionPerformed(evt);
            }
        });

        btnAtualizar.setText("Atualizar");
        btnAtualizar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnAtualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtualizarActionPerformed(evt);
            }
        });

        btnExcluir.setText("Excluir");
        btnExcluir.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcluirActionPerformed(evt);
            }
        });

        btnFechar.setText("Fechar");
        btnFechar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnFechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFecharActionPerformed(evt);
            }
        });

        jButtonImprimir.setText("Imprimir");
        jButtonImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonImprimirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(btSalvar)
                .addGap(18, 18, 18)
                .addComponent(btnAtualizar)
                .addGap(18, 18, 18)
                .addComponent(btnExcluir)
                .addGap(18, 18, 18)
                .addComponent(jButtonImprimir)
                .addGap(18, 18, 18)
                .addComponent(btnFechar)
                .addContainerGap(81, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btSalvar, btnAtualizar, btnExcluir, btnFechar});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSalvar)
                    .addComponent(btnAtualizar)
                    .addComponent(btnExcluir)
                    .addComponent(btnFechar)
                    .addComponent(jButtonImprimir))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Abrir Dados"));

        btAbrir.setText("Abrir");
        btAbrir.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAbrirActionPerformed(evt);
            }
        });

        btnListarDados.setText("Listar Dados");
        btnListarDados.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
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
                .addGap(1, 1, 1)
                .addComponent(btAbrir, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfBusca, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnListarDados, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfBusca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btAbrir)
                    .addComponent(btnListarDados))
                .addGap(0, 6, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("CADASTRO DE USUÁRIOS");

        jLabel2.setText("Id");

        tfId.setEditable(false);
        tfId.setEnabled(false);

        jLabel3.setText("Usuário");

        jLabel4.setText("Senha");

        javax.swing.GroupLayout jPanelUsuariosLayout = new javax.swing.GroupLayout(jPanelUsuarios);
        jPanelUsuarios.setLayout(jPanelUsuariosLayout);
        jPanelUsuariosLayout.setHorizontalGroup(
            jPanelUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelUsuariosLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelUsuariosLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(50, 50, 50)
                        .addComponent(tfId, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelUsuariosLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(28, 28, 28)
                        .addComponent(pfSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelUsuariosLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(20, 20, 20)
                        .addComponent(tfUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanelUsuariosLayout.setVerticalGroup(
            jPanelUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelUsuariosLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(tfId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(tfUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pfSenha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap())
        );

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

        jScrollPane1.setViewportView(jScrollPane2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 569, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanelUsuarios, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents



    @SuppressWarnings("deprecation")

    private void btSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSalvarActionPerformed



        if (tfUsuario.getText().trim().isEmpty() || pfSenha.getPassword().length == 0) {

            JOptionPane.showMessageDialog(null, "Preencha usuário e senha!");

            return;

        }



        String usuario = tfUsuario.getText().trim();

        String senhaDigitada = new String(pfSenha.getPassword()); // sem trim aqui



        String hashCalculado = Criptografar.encriptografar(senhaDigitada);

        

        // Debug obrigatório

//        System.out.println("=== DEBUG CADASTRO ===");

//        System.out.println("Usuário: " + usuario);

//        System.out.println("Senha digitada: [" + senhaDigitada + "]");

//        System.out.println("Hash calculado no Java: " + hashCalculado);



        Usuarios obj = new Usuarios();

        obj.setUsuario(usuario);

        obj.setSenha(hashCalculado);

        

//        System.out.println("=== DEBUG DAO ===");

//        System.out.println("Usuario recebido: " + obj.getUsuario());

//        System.out.println("Senha recebida no DAO: " + obj.getSenha());



        try {

            UsuariosDAO dao = new UsuariosDAO();

            dao.Salvar(obj);



            // Verifica o que realmente foi salvo (recarrega o último)

            List<Usuarios> lista = dao.Listar();

            if (!lista.isEmpty()) {

                Usuarios ultimo = lista.get(lista.size() - 1);

                //System.out.println("Hash REAL lido do banco após salvar: " + ultimo.getSenha());

                tfId.setText(String.valueOf(ultimo.getId()));

            }

            ///System.out.println("Tentando inserir com senha: " + obj.getSenha());

            listar();

            new LimpaTela().LimpaTela(jPanelUsuarios);

            JOptionPane.showMessageDialog(null, "Cadastrado! Veja o console para hashes.");

        } catch (Exception ex) {

            System.out.println("Erro ao salvar: " + ex);

            JOptionPane.showMessageDialog(null, "Erro: " + ex.getMessage());

        }

    }//GEN-LAST:event_btSalvarActionPerformed





    private void btAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAbrirActionPerformed

        if (tfBusca.getText().equals("")) {

            JOptionPane.showMessageDialog(null, "Informe um Id válido...");

        } else {

            int mId = Integer.parseInt(tfBusca.getText());

            UsuariosDAO dao;

            try {

                dao = new UsuariosDAO();

                List<Usuarios> lista = dao.Filtrar(mId);

                DefaultTableModel dados = (DefaultTableModel) tbDados.getModel();

                tbDados.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

                tbDados.getColumn(tbDados.getColumnName(0)).setPreferredWidth(30);

                tbDados.getColumn(tbDados.getColumnName(1)).setPreferredWidth(130);

                tbDados.getColumn(tbDados.getColumnName(2)).setPreferredWidth(380);

                dados.setNumRows(0);



                for (Usuarios c : lista) {

                    dados.addRow(new Object[]{

                        c.getId(),

                        c.getUsuario(),

                        c.getSenha(),});



                    tfId.setText(String.valueOf(c.getId()));

                    tfUsuario.setText(c.getUsuario());

                    pfSenha.setText(c.getSenha());

                }

                btSalvar.setEnabled(false);

            } catch (SQLException | ClassNotFoundException ex) {

                Logger.getLogger(Tela_CadUsu.class.getName()).log(Level.SEVERE, null, ex);

            }

        }

    }//GEN-LAST:event_btAbrirActionPerformed



    private void btnListarDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnListarDadosActionPerformed

        try {

            UsuariosDAO dao;

            dao = new UsuariosDAO();

            List<Usuarios> lista = dao.Listar();

            DefaultTableModel dados = (DefaultTableModel) tbDados.getModel();

            tbDados.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

            tbDados.getColumn(tbDados.getColumnName(0)).setPreferredWidth(30);

            tbDados.getColumn(tbDados.getColumnName(1)).setPreferredWidth(130);

            tbDados.getColumn(tbDados.getColumnName(2)).setPreferredWidth(380);

            dados.setNumRows(0);

            for (Usuarios c : lista) {

                dados.addRow(new Object[]{

                    c.getId(),

                    c.getUsuario(),

                    "********" // ou "" ou "********" \\c.getSenha()

                });

            }

        } catch (ClassNotFoundException | SQLException ex) {

            Logger.getLogger(Tela_CadUsu.class.getName()).log(Level.SEVERE, null, ex);

        }

    }//GEN-LAST:event_btnListarDadosActionPerformed



    @SuppressWarnings({"deprecation", "deprecation", "deprecation", "deprecation"})

    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarActionPerformed

        if (tfId.getText().equals("")) {

            JOptionPane.showMessageDialog(null, "Informe o Id");

        } else {

            try {

                Usuarios obj = new Usuarios();

                obj.setUsuario(tfUsuario.getText());

                obj.setSenha(pfSenha.getText());

                

                obj.setId(Integer.parseInt(tfId.getText()));

                try {

                    UsuariosDAO dao = new UsuariosDAO();

                    dao.Editar(obj);

                    LimpaTela util = new LimpaTela();

                    util.LimpaTela(jPanelUsuarios);

                } catch (SQLException | ClassNotFoundException ex) {

                    Logger.getLogger(Tela_CadUsu.class.getName()).log(Level.SEVERE, null, ex);

                }

            } catch (NumberFormatException ex) {

                Logger.getLogger(Tela_CadUsu.class.getName()).log(Level.SEVERE, null, ex);

            }

        }

    }//GEN-LAST:event_btnAtualizarActionPerformed



    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed

        if (tfId.getText().equals("")) {

            JOptionPane.showMessageDialog(null, "Informe o registro(id) à ser excluído.");

        } else {

            Usuarios obj = new Usuarios();

            obj.setId(Integer.parseInt(tfId.getText()));

            UsuariosDAO dao;

            try {

                dao = new UsuariosDAO();

                dao.Excluir(obj);

                LimpaTela util = new LimpaTela();

                util.LimpaTela(jPanelUsuarios);

            } catch (SQLException | ClassNotFoundException ex) {

                Logger.getLogger(Tela_CadUsu.class.getName()).log(Level.SEVERE, null, ex);

            }

        }

    }//GEN-LAST:event_btnExcluirActionPerformed



    private void btnFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFecharActionPerformed



        try {

            Tela_Principal exibir;

            exibir = new Tela_Principal();

            exibir.setVisible(true);

            setVisible(false);

        } catch (IOException ex) {

            Logger.getLogger(Tela_CadUsu.class.getName()).log(Level.SEVERE, null, ex);

        }



    }//GEN-LAST:event_btnFecharActionPerformed



    private void tbDadosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDadosMouseClicked

        // TODO add your handling code here:

    }//GEN-LAST:event_tbDadosMouseClicked



    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated

        try {

            listar();

        } catch (SQLException | ClassNotFoundException ex) {

            Logger.getLogger(Tela_CadUsu.class.getName()).log(Level.SEVERE, null, ex);

        }

    }//GEN-LAST:event_formWindowActivated



    private void jButtonImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonImprimirActionPerformed

        RelUsers usu = new RelUsers();

        JOptionPane.showMessageDialog(null, "Relatório de Usuários gerado com sucesso!");

        dispose();

        new Tela_CadUsu().setVisible(true);

    }//GEN-LAST:event_jButtonImprimirActionPerformed



    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(() -> {

            new Tela_CadUsu().setVisible(true);

        });

    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAbrir;
    private javax.swing.JButton btSalvar;
    private javax.swing.JButton btnAtualizar;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnFechar;
    private javax.swing.JButton btnListarDados;
    private javax.swing.JButton jButtonImprimir;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelUsuarios;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPasswordField pfSenha;
    private javax.swing.JTable tbDados;
    private javax.swing.JTextField tfBusca;
    private javax.swing.JTextField tfId;
    private javax.swing.JTextField tfUsuario;
    // End of variables declaration//GEN-END:variables

}

