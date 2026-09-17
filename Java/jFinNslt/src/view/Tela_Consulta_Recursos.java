package view;


import utilitarios.Conexao;

import utilitarios.CustomTableCellRenderer;

import java.io.IOException;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.text.DecimalFormat;

import java.text.MessageFormat;

import java.text.ParseException;

import java.text.SimpleDateFormat;

import java.util.Date;

import java.util.logging.Level;

import java.util.logging.Logger;

import javax.swing.JComboBox;

import javax.swing.JOptionPane;

import javax.swing.JTable;

import javax.swing.SwingConstants;

import javax.swing.table.DefaultTableCellRenderer;

import javax.swing.table.DefaultTableModel;

import javax.swing.table.TableRowSorter;



public class Tela_Consulta_Recursos extends javax.swing.JFrame {



    Conexao conexao = new Conexao();



    CarregarCbx2 r2 = new CarregarCbx2(); //Carrega combobox de Recursos



    public Tela_Consulta_Recursos() throws SQLException {

        initComponents();

        jButtonAtualizaApr.setVisible(false);

        jFormattedTextFieldBxCart.setVisible(false);

        r2.CarregarCbx2("tbrecursos", "nomebco", jComboBoxPesq);

    }



    @SuppressWarnings("unchecked")

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxPesq = new javax.swing.JComboBox<>();
        jFormattedTextFieldDataIni = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        jFormattedTextFieldDataFim = new javax.swing.JFormattedTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTablePesquisa = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldSaldoAnterior = new javax.swing.JTextField();
        tfRecurso = new javax.swing.JTextField();
        tfNrRecurso = new javax.swing.JTextField();
        tfRecNome = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jButtonPesquisar = new javax.swing.JButton();
        jButtonPImprimir = new javax.swing.JButton();
        jButtonPLimpar = new javax.swing.JButton();
        jButtonPFechar = new javax.swing.JButton();
        jButtonAtualizaApr = new javax.swing.JButton();
        jFormattedTextFieldBxCart = new javax.swing.JFormattedTextField();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        jLabel1.setText("Conta para pesquisa:");

        jLabel2.setText("Período de Vencim. à pesquisar");
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jComboBoxPesq.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione" }));
        jComboBoxPesq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxPesqActionPerformed(evt);
            }
        });

        try {
            jFormattedTextFieldDataIni.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##/##/####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        jLabel3.setText("até");

        try {
            jFormattedTextFieldDataFim.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##/##/####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        jTablePesquisa.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Reg.", "Recurso", "vRecurso", "Favorecido", "vFavorecido", "Lancto.", "Emissão", "Vencimento", "Documento", "Classif.", "Descrição", "Valor", "Apresent.", "Status", "Prev.", "Saldo"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTablePesquisa.setSelectionBackground(new java.awt.Color(255, 255, 204));
        jTablePesquisa.setSelectionForeground(new java.awt.Color(51, 51, 51));
        jScrollPane1.setViewportView(jTablePesquisa);

        jLabel4.setText("Saldo Anterior:");

        jTextFieldSaldoAnterior.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldSaldoAnterior.setEnabled(false);

        tfRecurso.setEnabled(false);
        tfRecurso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfRecursoActionPerformed(evt);
            }
        });

        tfNrRecurso.setEnabled(false);

        tfRecNome.setEnabled(false);
        tfRecNome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfRecNomeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxPesq, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfNrRecurso, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfRecurso, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfRecNome, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFormattedTextFieldDataIni, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFormattedTextFieldDataFim, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldSaldoAnterior, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1134, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfRecNome)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2)
                        .addComponent(jComboBoxPesq, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jFormattedTextFieldDataIni, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jFormattedTextFieldDataFim, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4)
                        .addComponent(jTextFieldSaldoAnterior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tfRecurso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tfNrRecurso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
                .addContainerGap())
        );

        jButtonPesquisar.setText("Pesquisar");
        jButtonPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonPesquisar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPesquisarActionPerformed(evt);
            }
        });

        jButtonPImprimir.setText("Imprimir");
        jButtonPImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonPImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPImprimirActionPerformed(evt);
            }
        });

        jButtonPLimpar.setText("Limpar");
        jButtonPLimpar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonPLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPLimparActionPerformed(evt);
            }
        });

        jButtonPFechar.setText("Fechar");
        jButtonPFechar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonPFechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPFecharActionPerformed(evt);
            }
        });

        jButtonAtualizaApr.setText("Baixar Cartões");
        jButtonAtualizaApr.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonAtualizaApr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAtualizaAprActionPerformed(evt);
            }
        });

        try {
            jFormattedTextFieldBxCart.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##/##/####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jFormattedTextFieldBxCart, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonAtualizaApr, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(96, 96, 96)
                .addComponent(jButtonPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jButtonPImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jButtonPLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jButtonPFechar, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonPesquisar)
                    .addComponent(jButtonPImprimir)
                    .addComponent(jButtonPLimpar)
                    .addComponent(jButtonPFechar)
                    .addComponent(jButtonAtualizaApr)
                    .addComponent(jFormattedTextFieldBxCart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("CONSULTA - RECURSOS");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents



    private void tfRecursoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfRecursoActionPerformed

        // TODO add your handling code here:

    }//GEN-LAST:event_tfRecursoActionPerformed



    private void jComboBoxPesqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxPesqActionPerformed

        if (jComboBoxPesq.getSelectedIndex() > 0) {

            String selectedValue = jComboBoxPesq.getSelectedItem().toString();

            var texto = selectedValue.length();

            String completo = selectedValue;

            selectedValue = jComboBoxPesq.getSelectedItem().toString().substring(texto - 14);

            if (!selectedValue.isEmpty()) {

                selectedValue = jComboBoxPesq.getSelectedItem().toString().substring(texto - 14);

                String frase1 = selectedValue.substring(0, 4);

                tfNrRecurso.setText(frase1);

                String frase2 = selectedValue.substring(5);

                tfRecurso.setText(frase2);

                String frase3 = completo.substring(0, (texto - 15));

                tfRecNome.setText(frase3);

            }

        }

    }//GEN-LAST:event_jComboBoxPesqActionPerformed



    private void jButtonPFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPFecharActionPerformed



        try {

            Tela_Principal exibir;

            exibir = new Tela_Principal();

            exibir.setVisible(true);

            setVisible(false);

        } catch (IOException ex) {

            Logger.getLogger(Tela_Consulta_Recursos.class.getName()).log(Level.SEVERE, null, ex);

        }

    }//GEN-LAST:event_jButtonPFecharActionPerformed



    @SuppressWarnings("unchecked")

    private void jButtonPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPesquisarActionPerformed

        if (jFormattedTextFieldDataIni.getText().trim().length() == 4) {

            JOptionPane.showMessageDialog(null, "Atenção! Favor preencher o campo de data inicial.");

        } else if (jFormattedTextFieldDataFim.getText().trim().length() == 4) {

            JOptionPane.showMessageDialog(null, "Atenção! Favor preencher o campo de data final.");

        } else {

            PegaSaldoAnterior();

            String ssa = jTextFieldSaldoAnterior.getText();

            double sa = Double.valueOf(ssa).doubleValue();

            DecimalFormat dec = new DecimalFormat("#,##0.00");

            jTextFieldSaldoAnterior.setText(dec.format(sa));



            String Comparar = tfRecurso.getText();

            if (Comparar.equals("2.001.003")) {

                jButtonAtualizaApr.setVisible(true);

                jFormattedTextFieldBxCart.setVisible(true);

            }



            try {

                SimpleDateFormat formatoBanco = new SimpleDateFormat("yyyy-MM-dd");

                DecimalFormat df = new DecimalFormat("#,##0.00");

                conexao.abrirConexao(); // Abre a conexão

                Connection con = conexao.getConexao(); // Obtém a conexão

                String sql;

                if (tfRecurso.getText().equals("1.002.001")) {

                    sql = "SELECT * FROM tbmovimento WHERE dtVcto BETWEEN ? AND ? AND recurso = ? AND statusMov <> 'RC' ORDER BY dtVcto";

                } else if (tfRecurso.getText().equals("2.001.002")) {

                    sql = "SELECT * FROM tbmovimento WHERE dtVcto BETWEEN ? AND ? AND recurso = ? AND statusMov <> 'PG' ORDER BY dtVcto";

                } else {

                    sql = "SELECT * FROM tbmovimento WHERE dtVcto BETWEEN ? AND ? AND recurso = ? AND statusMov <> 'PG' ORDER BY dtVcto";

                }

                PreparedStatement stmt = con.prepareStatement(sql);

                String dataTextoi = jFormattedTextFieldDataIni.getText();

                SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy");

                Date data = formatoEntrada.parse(dataTextoi);

                String data_i = formatoBanco.format(data);

                stmt.setString(1, data_i);

                String dataTextof = jFormattedTextFieldDataFim.getText();

                Date dataf = formatoEntrada.parse(dataTextof);

                String data_f = formatoBanco.format(dataf);

                stmt.setString(2, data_f);

                stmt.setString(3, tfNrRecurso.getText());

                try (ResultSet rs = stmt.executeQuery()) {

                    DefaultTableModel modelo = (DefaultTableModel) jTablePesquisa.getModel();

                    modelo.setNumRows(0);

                    jTablePesquisa.setRowSorter(new TableRowSorter(modelo));

                    jTablePesquisa.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

                    // Configuração das colunas da tabela

                    int[] colWidths = {40, 60, 0, 80, 0, 75, 75, 75, 110, 0, 245, 80, 100, 50, 40, 80};

                    for (int i = 0; i < colWidths.length; i++) {

                        jTablePesquisa.getColumn(jTablePesquisa.getColumnName(i)).setPreferredWidth(colWidths[i]);

                    }

                    DefaultTableCellRenderer tab = new DefaultTableCellRenderer();

                    tab.setHorizontalAlignment(SwingConstants.RIGHT);

                    jTablePesquisa.getColumnModel().getColumn(11).setCellRenderer(tab);

                    jTablePesquisa.getColumnModel().getColumn(15).setCellRenderer(tab);

                    int[] hiddenCols = {2, 4, 9};

                    for (int col : hiddenCols) {

                        jTablePesquisa.getColumnModel().getColumn(col).setMinWidth(0);

                        jTablePesquisa.getColumnModel().getColumn(col).setMaxWidth(0);

                    }

                    SimpleDateFormat sdfSaida = new SimpleDateFormat("dd/MM/yyyy");

                    SimpleDateFormat sdfEntrada = new SimpleDateFormat("yyyy-MM-dd");



                    while (rs.next()) {

                        String lancapr = formatarData(rs.getString("dtApr"), sdfSaida, sdfEntrada);

                        String lanclancto = formatarData(rs.getString("dtlancto"), sdfSaida, sdfEntrada);

                        String lancEmi = formatarData(rs.getString("dtEmi"), sdfSaida, sdfEntrada);

                        String lancVcto = formatarData(rs.getString("dtVcto"), sdfSaida, sdfEntrada);



                        sa += rs.getDouble("Valor");

                        modelo.addRow(new Object[]{

                            rs.getString("idMov"),

                            rs.getString("recurso"),

                            rs.getString("vrecurso"),

                            rs.getString("clifor"),

                            rs.getString("vCliFor"),

                            lanclancto,

                            lancEmi,

                            lancVcto,

                            rs.getString("documento"),

                            rs.getString("classif"),

                            rs.getString("Descr"),

                            df.format(rs.getDouble("Valor")),

                            lancapr,

                            rs.getString("statusMov"),

                            rs.getString("Prev"),

                            df.format(sa),});

                    }

                    jTablePesquisa.getColumnModel().getColumn(11).setCellRenderer(new CustomTableCellRenderer(11, 11, false));

                    jTablePesquisa.getColumnModel().getColumn(15).setCellRenderer(new CustomTableCellRenderer(15, 11, false));

                }

                con.close();

            } catch (SQLException | ParseException e) {

                Logger.getLogger(Tela_Consulta_Recursos.class.getName()).log(Level.SEVERE, null, e);

            } finally {

                conexao.fecharConexao(); // Fecha a conexão no final

            }

        }

    }



    private String formatarData(String dataString, SimpleDateFormat sdfSaida, SimpleDateFormat sdfEntrada) {

        if (dataString == null) {

            return "";

        }

        try {

            Date dataUtil = sdfEntrada.parse(dataString);

            return sdfSaida.format(dataUtil);

        } catch (ParseException e) {

            return "Data inválida";

        }

    }//GEN-LAST:event_jButtonPesquisarActionPerformed



    private void jButtonPLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPLimparActionPerformed

        ((DefaultTableModel) jTablePesquisa.getModel()).setRowCount(0);

        tfNrRecurso.setText("");

        tfRecurso.setText("");

        jFormattedTextFieldDataIni.setText("");

        jFormattedTextFieldDataFim.setText("");

        jTextFieldSaldoAnterior.setText("0");

        jComboBoxPesq.setSelectedIndex(0);

        jButtonAtualizaApr.setVisible(false);

        jFormattedTextFieldBxCart.setVisible(false);

        tfRecNome.setText("");

    }//GEN-LAST:event_jButtonPLimparActionPerformed



    private void jButtonPImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPImprimirActionPerformed

        String nomeRec = tfRecNome.getText();

        MessageFormat cabecalho = new MessageFormat("Recursos- Extrato- " + nomeRec);

        MessageFormat rodape = new MessageFormat("Página {0,number,integer}");

        try {

            jTablePesquisa.print(JTable.PrintMode.FIT_WIDTH, cabecalho, rodape);

        } catch (java.awt.print.PrinterException e) {

            System.err.format("Erro ao imprimir!!! %s%n", e.getMessage());

        }

    }//GEN-LAST:event_jButtonPImprimirActionPerformed



    private void jButtonAtualizaAprActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAtualizaAprActionPerformed



        DefaultTableModel model = (DefaultTableModel) jTablePesquisa.getModel();

        SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");

        SimpleDateFormat formato_ISO = new SimpleDateFormat("yyyy-MM-dd");



        Connection conn = null;

        try {

            conexao.abrirConexao();

            conn = conexao.getConexao();

            conn.setAutoCommit(false);



            // Parse da data - com validação

            String textoData = jFormattedTextFieldBxCart.getText().trim();

            if (textoData.isEmpty() || textoData.equals("  /  /    ")) {

                throw new IllegalArgumentException("Informe a data de baixa corretamente (dd/MM/yyyy)");

            }

            

            java.util.Date data_v = formato_data.parse(jFormattedTextFieldBxCart.getText());

            java.sql.Date tdtApr = new java.sql.Date(data_v.getTime());

            

            PreparedStatement pstmtUpdate = conn.prepareStatement(

                    "UPDATE tbmovimento SET dtApr = ?, statusMov = 'PG' WHERE idMov = ?"

            );



            int totalAtualizados = 0;

            int idsProcessados = 0;



            // Processa cada linha da tabela

            for (int row = 0; row < model.getRowCount(); row++) {

                int idAtual = Integer.parseInt(model.getValueAt(row, 0).toString().trim());

                // So baixa registros cujo vencimento seja <= data de baixa digitada.
                // Impede que compras futuras (ex: fatura que vence em 25/09) sejam marcadas como
                // pagas na data da transferencia/baixa do cartao (ex: 20/08, 25/08).
                String vctoTexto = model.getValueAt(row, 7).toString().trim();
                if (vctoTexto.isEmpty()) {
                    continue; // sem vencimento: ignora (ex.: SALDO INICIAL)
                }
                java.util.Date dataVctoTemp;
                try {
                    dataVctoTemp = formato_data.parse(vctoTexto);
                } catch (java.text.ParseException pe) {
                    continue; // data invalida: ignora a linha
                }
                if (dataVctoTemp.after(data_v)) {
                    continue; // vencimento depois da data de baixa: NAO baixa
                }

                // Atualiza APENAS este registro (linha da tabela)

                pstmtUpdate.setString(1, formato_ISO.format(tdtApr));

                pstmtUpdate.setInt(2, idAtual);

                pstmtUpdate.addBatch();

                idsProcessados++;

            }



            // Executa updates

            int[] updResults = pstmtUpdate.executeBatch();



            for (int r : updResults) {

                totalAtualizados += r;

            }

            conn.commit();



            JOptionPane.showMessageDialog(null,

                    "Baixa realizada com sucesso!\n"
                    + "Data de baixa: " + formato_ISO.format(tdtApr) + "\n"
                    + "Registros atualizados: " + totalAtualizados + "\n"
                    + "Linhas processadas: " + idsProcessados,
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);



            // Limpezas da tela

            tfNrRecurso.setText("");

            tfRecurso.setText("");

            jFormattedTextFieldDataIni.setText("");

            jFormattedTextFieldDataFim.setText("");

            jTextFieldSaldoAnterior.setText("0");

            jComboBoxPesq.setSelectedIndex(0);

            jButtonAtualizaApr.setVisible(false);

            jFormattedTextFieldBxCart.setVisible(false);

            tfRecNome.setText("");



        } catch (Exception ex) {  // SQLException | ParseException | NumberFormatException etc.

            Logger.getLogger(Tela_Consulta_Recursos.class.getName()).log(Level.SEVERE, null, ex);

            if (conn != null) {

                try {

                    conn.rollback();

                } catch (SQLException ignored) {

                }

            }

            JOptionPane.showMessageDialog(null,

                    "Erro ao processar a baixa:\n" + ex.getMessage(),

                    "Erro", JOptionPane.ERROR_MESSAGE);

        } finally {

            conexao.fecharConexao();

        }

    }//GEN-LAST:event_jButtonAtualizaAprActionPerformed



    private void tfRecNomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfRecNomeActionPerformed

        // TODO add your handling code here:

    }//GEN-LAST:event_tfRecNomeActionPerformed



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

            java.util.logging.Logger.getLogger(Tela_Consulta_Recursos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {

            java.util.logging.Logger.getLogger(Tela_Consulta_Recursos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {

            java.util.logging.Logger.getLogger(Tela_Consulta_Recursos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {

            java.util.logging.Logger.getLogger(Tela_Consulta_Recursos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

        }

        //</editor-fold>

        //</editor-fold>

        //</editor-fold>

        //</editor-fold>

        //</editor-fold>

        //</editor-fold>

        //</editor-fold>

        //</editor-fold>



        /* Create and display the form */

        java.awt.EventQueue.invokeLater(() -> {

            try {

                new Tela_Consulta_Recursos().setVisible(true);

            } catch (SQLException ex) {

                Logger.getLogger(Tela_Consulta_Recursos.class.getName()).log(Level.SEVERE, null, ex);

            }

        });

    }



    private void PegaSaldoAnterior() {

        String sql = "SELECT SUM(Valor) FROM tbmovimento WHERE dtVcto < ? AND recurso = ?";

        try {

            conexao.abrirConexao(); // Abre a conexão

            Connection conn = conexao.getConexao(); // Obtém a conexão

            ResultSet rsoma;

            // Obtém a data do campo de texto e converte para formato adequado para o banco de dados

            try (PreparedStatement stt = conn.prepareStatement(sql)) {

                // Obtém a data do campo de texto e converte para formato adequado para o banco de dados

                String dataTexto = jFormattedTextFieldDataIni.getText(); // Supondo que jFormattedTextFieldDataIni é o campo de texto onde o usuário insere a data

                SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy");

                Date data = formatoEntrada.parse(dataTexto);

                SimpleDateFormat formatoBanco = new SimpleDateFormat("yyyy-MM-dd");

                String dataFormatada = formatoBanco.format(data);

                stt.setString(1, dataFormatada);

                stt.setString(2, tfNrRecurso.getText());

                rsoma = stt.executeQuery();

                double somaval = 0;

                while (rsoma.next()) {

                    somaval = rsoma.getDouble(1);

                }

                jTextFieldSaldoAnterior.setText(String.valueOf(somaval));

            } // Supondo que jFormattedTextFieldDataIni é o campo de texto onde o usuário insere a data

            rsoma.close();

            conn.close();

        } catch (SQLException | ParseException ex) {

            Logger.getLogger(Tela_Consulta_Recursos.class.getName()).log(Level.SEVERE, null, ex);

        } finally {

            conexao.fecharConexao(); // Fecha a conexão no final

        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAtualizaApr;
    private javax.swing.JButton jButtonPFechar;
    private javax.swing.JButton jButtonPImprimir;
    private javax.swing.JButton jButtonPLimpar;
    private javax.swing.JButton jButtonPesquisar;
    private javax.swing.JComboBox<String> jComboBoxPesq;
    private javax.swing.JFormattedTextField jFormattedTextFieldBxCart;
    private javax.swing.JFormattedTextField jFormattedTextFieldDataFim;
    private javax.swing.JFormattedTextField jFormattedTextFieldDataIni;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTablePesquisa;
    private javax.swing.JTextField jTextFieldSaldoAnterior;
    private javax.swing.JTextField tfNrRecurso;
    private javax.swing.JTextField tfRecNome;
    private javax.swing.JTextField tfRecurso;
    // End of variables declaration//GEN-END:variables



    private static class CarregarCbx2 {



        @SuppressWarnings({"empty-statement", "unchecked"})

        public void CarregarCbx2(String tabela, String valor, JComboBox combo) throws SQLException {

            String sql = "Select * FROM " + tabela + " ORDER BY nomebco";

            // Cria uma instância da classe Conexao

            Conexao conexao = new Conexao();

            try {

                // Abre a conexão

                conexao.abrirConexao();

                // Obtém a conexão

                Connection con = conexao.getConexao();

                // Prepara e executa a consulta

                PreparedStatement stmt = con.prepareStatement(sql);

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {

                    combo.addItem(rs.getString(valor) + "-" + (rs.getString("codigo") + "-" + (rs.getString("fk_gpprinc"))));

                }

                rs.close();

                con.close();

            } catch (SQLException ex) {

                Logger.getLogger(Tela_Consulta_Recursos.class

                        .getName()).log(Level.SEVERE, null, ex);

            } finally {

                // Fecha a conexão no final

                conexao.fecharConexao();

            }

        }

    }

}

