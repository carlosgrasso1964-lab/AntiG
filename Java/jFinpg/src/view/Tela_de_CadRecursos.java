package view;

import utilitarios.Conexao;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.JComboBox;
import java.text.DecimalFormat;

public class Tela_de_CadRecursos extends javax.swing.JFrame {

    CarregarCbx re = new CarregarCbx();

    public Tela_de_CadRecursos() throws SQLException {
        initComponents();
        //Image icon = new ImageIcon(this.getClass().getResource("/IHome.jpg")).getImage();
        //this.setIconImage(icon);
        re.CarregarCbx("gpprincipal", "nome_c", cbxVinculo);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        tfCod = new javax.swing.JTextField();
        tfNomeBco = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        btSalvar = new javax.swing.JButton();
        btnAtualizar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        btnFechar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        btAbrir = new javax.swing.JButton();
        btnListarDados = new javax.swing.JButton();
        tfBusca = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbDados = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        tfFlx = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        tfNomeAgencia = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        cbxVinculo = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        tfStatus = new javax.swing.JTextField();
        tfLimite = new javax.swing.JTextField();
        tfAbertura = new javax.swing.JTextField();
        tfEncerramento = new javax.swing.JTextField();
        tfVinculo = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("       Cadastro Recursos");
        setUndecorated(true);
        setResizable(false);

        jLabel2.setText("Cod");

        jLabel3.setText("Fonte");

        jLabel4.setText("Vínculo");

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
                .addContainerGap()
                .addComponent(btSalvar)
                .addGap(40, 40, 40)
                .addComponent(btnAtualizar)
                .addGap(276, 276, 276)
                .addComponent(btnExcluir)
                .addGap(86, 86, 86)
                .addComponent(btnFechar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(btAbrir, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tfBusca, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnListarDados, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btAbrir)
                    .addComponent(btnListarDados)
                    .addComponent(tfBusca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 6, Short.MAX_VALUE))
        );

        tbDados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Cod", "Fonte", "Agência", "Fluxo", "Limite", "Abertura", "Encerramento", "Status", "Vinculo"
            }
        ));
        tbDados.setSelectionBackground(new java.awt.Color(255, 255, 204));
        tbDados.setSelectionForeground(new java.awt.Color(0, 0, 0));
        jScrollPane2.setViewportView(tbDados);

        jScrollPane1.setViewportView(jScrollPane2);

        jLabel5.setText("Agência");

        jLabel6.setText("Fluxo");

        jLabel7.setText("Limite");

        jLabel8.setText("Abertura");

        jLabel9.setText("Encerramento");

        cbxVinculo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione" }));
        cbxVinculo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxVinculoActionPerformed(evt);
            }
        });

        jLabel10.setText("Status");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("CADASTRO DE RECURSOS");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tfCod, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel6))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(tfFlx, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfLimite, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfAbertura, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(31, 31, 31)
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfEncerramento, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(137, 137, 137))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(tfStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbxVinculo, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(tfVinculo, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 32, Short.MAX_VALUE))))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfNomeBco, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfNomeAgencia, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfCod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tfNomeBco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(tfNomeAgencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(tfFlx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfAbertura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfEncerramento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(tfLimite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel6))
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxVinculo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel10)
                    .addComponent(tfVinculo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSalvarActionPerformed
        try {
            Connection con;
            con = Conexao.faz_conexao();
            String sql = "INSERT INTO tbrecursos(\"codigo\",\"nomebco\",\"agencia\",\"fluxo\",limite,\"abertura\",\"encerramento\",\"status\",\"fk_gpprinc\") VALUES (?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");
                stmt.setString(1, tfCod.getText());
                stmt.setString(2, tfNomeBco.getText());
                stmt.setString(3, tfNomeAgencia.getText());
                stmt.setString(4, tfFlx.getText());
                stmt.setDouble(5, Double.parseDouble(tfLimite.getText().replaceAll(",", ".")));
                java.util.Date data_c = formato_data.parse(tfAbertura.getText());
                stmt.setDate(6, new java.sql.Date(data_c.getTime()));
                java.util.Date data_c2 = formato_data.parse(tfEncerramento.getText());
                stmt.setDate(7, new java.sql.Date(data_c2.getTime()));
                stmt.setString(8, tfStatus.getText());
                stmt.setString(9, tfVinculo.getText());
                stmt.execute();
            } catch (ParseException ex) {
                Logger.getLogger(Tela_de_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);
            }
            con.close();
            JOptionPane.showMessageDialog(null, "Recurso cadastrado com sucesso!");
            tfCod.setText("");
            tfNomeBco.setText("");
            tfNomeAgencia.setText("");
            tfFlx.setText("");
            tfLimite.setText("");
            tfAbertura.setText("");
            tfEncerramento.setText("");
            tfStatus.setText("");
            cbxVinculo.setSelectedItem(0);
        } catch (SQLException ex) {
            Logger.getLogger(Tela_de_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btSalvarActionPerformed

    private void btAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAbrirActionPerformed
        if (tfBusca.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Informe um Id válido...");
        } else {
            try (
                    Connection con = Conexao.faz_conexao()) {
                String sql = "SELECT * FROM tbrecursos WHERE \"codigo\"=?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, tfBusca.getText());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        tfCod.setText(rs.getString("codigo"));
                        tfNomeBco.setText(rs.getString("nomebco"));
                        tfNomeAgencia.setText(rs.getString("agencia"));
                        tfFlx.setText(rs.getString("fluxo"));
                        tfLimite.setText(rs.getString(String.valueOf("limite")));
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        tfAbertura.setText(sdf.format(rs.getDate("abertura")));
                        SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
                        tfEncerramento.setText(sdf1.format(rs.getDate("encerramento")));
                        tfStatus.setText(rs.getString("status"));
                        tfVinculo.setText(rs.getString("fk_gpprinc"));
                        btSalvar.setEnabled(false);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_CadRecursos.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btAbrirActionPerformed

    private void btnListarDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnListarDadosActionPerformed
        try {
            Connection con = Conexao.faz_conexao();
            String sql = "Select * FROM tbrecursos ORDER BY \"codigo\"";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel modelo = (DefaultTableModel) tbDados.getModel();
            modelo.setNumRows(0);
            tbDados.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
            tbDados.getColumn(tbDados.getColumnName(0)).setPreferredWidth(40);
            tbDados.getColumn(tbDados.getColumnName(1)).setPreferredWidth(270);
            tbDados.getColumn(tbDados.getColumnName(2)).setPreferredWidth(60);
            tbDados.getColumn(tbDados.getColumnName(3)).setPreferredWidth(40);
            tbDados.getColumn(tbDados.getColumnName(4)).setPreferredWidth(60);
            tbDados.getColumn(tbDados.getColumnName(5)).setPreferredWidth(80);
            tbDados.getColumn(tbDados.getColumnName(6)).setPreferredWidth(100);
            tbDados.getColumn(tbDados.getColumnName(7)).setPreferredWidth(50);
            tbDados.getColumn(tbDados.getColumnName(8)).setPreferredWidth(80);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            DecimalFormat df = new DecimalFormat("#,##0.00");
            while (rs.next()) {
                modelo.addRow(new Object[]{rs.getString("codigo"),
                    rs.getString("nomebco"),
                    rs.getString("agencia"),
                    rs.getString("fluxo"),
                    df.format(rs.getDouble("limite")),
                    sdf.format(rs.getDate("abertura")),
                    sdf.format(rs.getDate("encerramento")),
                    rs.getString("status"),
                    rs.getString("fk_gpprinc"),});
            }
            rs.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Tela_de_CadRecursos.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnListarDadosActionPerformed

    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarActionPerformed

        if (tfCod.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Informe o Código");
        } else {
            /*try {
                Connection con = Conexao.faz_conexao();
                String sql = "UPDATE tbrecursos SET \"nomebco\"=?,\"agencia\"=?,\"fluxo\"=?,limite=?,\"abertura\"=?,\"encerramento\"=?,\"status\"=?,\"fk_gpprinc\"=? WHERE \"codigo\"=?";
                PreparedStatement stmt = con.prepareStatement(sql);
                SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");
                stmt.setString(1, tfNomeBco.getText());
                stmt.setString(2, tfNomeAgencia.getText());
                stmt.setString(3, tfFlx.getText());
                stmt.setString(4, tfLimite.getText());
                java.util.Date data_c = formato_data.parse(tfAbertura.getText());
                stmt.setDate(5, new java.sql.Date(data_c.getTime()));
                java.util.Date data_c2 = formato_data.parse(tfEncerramento.getText());
                stmt.setDate(6, new java.sql.Date(data_c2.getTime()));
                stmt.setString(7, tfStatus.getText());
                stmt.setString(8, (String) tfVinculo.getText());
                stmt.setString(9, tfCod.getText());
                stmt.execute();
                stmt.close();
                con.close();
                JOptionPane.showMessageDialog(null, "Recursos atualizados com sucesso!");
                btSalvar.setEnabled(true);
                tfCod.setText("");
                tfNomeBco.setText("");
                tfNomeAgencia.setText("");
                tfFlx.setText("");
                tfLimite.setText("");
                tfAbertura.setText("");
                tfEncerramento.setText("");
                tfStatus.setText("");
                cbxVinculo.setSelectedIndex(0);
                tfVinculo.setText("");
                tfBusca.setText("");
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_CadRecursos.class
                        .getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(Tela_de_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);
            }*/
            try {
                Connection con = Conexao.faz_conexao();
                String sql = "UPDATE tbrecursos SET \"nomebco\"=?,\"agencia\"=?,\"fluxo\"=?,limite=?,\"abertura\"=?,\"encerramento\"=?,\"status\"=?,\"fk_gpprinc\"=? WHERE \"codigo\"=?";
                PreparedStatement stmt = con.prepareStatement(sql);
                SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");

                stmt.setString(1, tfNomeBco.getText());
                stmt.setString(2, tfNomeAgencia.getText());
                stmt.setString(3, tfFlx.getText());

                // Converta o valor do campo limite para double
                double limite;
                try {
                    limite = Double.parseDouble(tfLimite.getText());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("O valor de limite deve ser um número válido.");
                }
                stmt.setDouble(4, limite);

                java.util.Date data_c = formato_data.parse(tfAbertura.getText());
                stmt.setDate(5, new java.sql.Date(data_c.getTime()));
                java.util.Date data_c2 = formato_data.parse(tfEncerramento.getText());
                stmt.setDate(6, new java.sql.Date(data_c2.getTime()));

                stmt.setString(7, tfStatus.getText());
                stmt.setString(8, tfVinculo.getText());
                stmt.setString(9, tfCod.getText());

                stmt.execute();
                stmt.close();
                con.close();

                JOptionPane.showMessageDialog(null, "Recursos atualizados com sucesso!");
                btSalvar.setEnabled(true);
                tfCod.setText("");
                tfNomeBco.setText("");
                tfNomeAgencia.setText("");
                tfFlx.setText("");
                tfLimite.setText("");
                tfAbertura.setText("");
                tfEncerramento.setText("");
                tfStatus.setText("");
                cbxVinculo.setSelectedIndex(0);
                tfVinculo.setText("");
                //tfBusca.setText();
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(Tela_de_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnAtualizarActionPerformed

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed
        if (tfCod.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Informe o código do recurso à ser excluí­do.");
        } else {
            try {
                Connection con = Conexao.faz_conexao();
                String sql = "DELETE FROM tbrecursos WHERE \"codigo\"=?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, tfCod.getText());
                stmt.execute();
                stmt.close();
                con.close();
                JOptionPane.showMessageDialog(null, "Recurso excluí­do com sucesso!");
                tfCod.setText("");
                tfNomeAgencia.setText("");
                tfFlx.setText("");
                tfLimite.setText("");
                tfAbertura.setText("");
                tfEncerramento.setText("");
                tfStatus.setText("");
                cbxVinculo.setSelectedItem("");
                tfBusca.setText("");
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_CadRecursos.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnExcluirActionPerformed

    private void btnFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFecharActionPerformed

        try {
            TelaPrincipal exibir;
            exibir = new TelaPrincipal();
            exibir.setVisible(true);
            setVisible(false);
        } catch (IOException ex) {
            Logger.getLogger(Tela_de_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_btnFecharActionPerformed

    private void cbxVinculoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxVinculoActionPerformed
        String selectedValue = cbxVinculo.getSelectedItem().toString();
        var texto = selectedValue.length();
        texto = texto - 9;
        selectedValue = cbxVinculo.getSelectedItem().toString().substring(texto);
        tfVinculo.setText(selectedValue);
    }//GEN-LAST:event_cbxVinculoActionPerformed

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
            java.util.logging.Logger.getLogger(Tela_de_CadRecursos.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Tela_de_CadRecursos.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Tela_de_CadRecursos.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tela_de_CadRecursos.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Tela_de_CadRecursos().setVisible(true);
                } catch (SQLException ex) {
                    Logger.getLogger(Tela_de_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAbrir;
    private javax.swing.JButton btSalvar;
    private javax.swing.JButton btnAtualizar;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnFechar;
    private javax.swing.JButton btnListarDados;
    private javax.swing.JComboBox<String> cbxVinculo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tbDados;
    private javax.swing.JTextField tfAbertura;
    private javax.swing.JTextField tfBusca;
    private javax.swing.JTextField tfCod;
    private javax.swing.JTextField tfEncerramento;
    private javax.swing.JTextField tfFlx;
    private javax.swing.JTextField tfLimite;
    private javax.swing.JTextField tfNomeAgencia;
    private javax.swing.JTextField tfNomeBco;
    private javax.swing.JTextField tfStatus;
    private javax.swing.JTextField tfVinculo;
    // End of variables declaration//GEN-END:variables

    private static class CarregarCbx {

        @SuppressWarnings({"empty-statement", "unchecked"})
        public void CarregarCbx(String tabela, String valor, JComboBox combo) throws SQLException {
            String sql = "Select * FROM " + tabela;
            Connection con = Conexao.faz_conexao();
            try {
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    combo.addItem(rs.getString(valor) + "-" + (rs.getString("cod_geral")));
                }
                rs.close();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_CadRecursos.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
