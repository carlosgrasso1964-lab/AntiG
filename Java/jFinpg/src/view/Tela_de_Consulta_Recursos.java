package view;

import utilitarios.Conexao;
import classes.CustomTableCellRenderer;
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

public class Tela_de_Consulta_Recursos extends javax.swing.JFrame {

    CarregarCbx2 r2 = new CarregarCbx2(); //Carrega combobox de Recursos

    public Tela_de_Consulta_Recursos() throws SQLException {
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
                "Reg.", "Recurso", "vRecurso", "Favorecido", "vFavorecido", "Lançto.", "Emissão", "Vencimento", "Documento", "Classif.", "Descrição", "Valor", "Apresent.", "Status", "Prev.", "Saldo"
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
        jTablePesquisa.setSelectionForeground(new java.awt.Color(0, 0, 0));
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
        jButtonPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonPesquisar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPesquisarActionPerformed(evt);
            }
        });

        jButtonPImprimir.setText("Imprimir");
        jButtonPImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonPImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPImprimirActionPerformed(evt);
            }
        });

        jButtonPLimpar.setText("Limpar");
        jButtonPLimpar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonPLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPLimparActionPerformed(evt);
            }
        });

        jButtonPFechar.setText("Fechar");
        jButtonPFechar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonPFechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPFecharActionPerformed(evt);
            }
        });

        jButtonAtualizaApr.setText("Baixar Cartões");
        jButtonAtualizaApr.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jFormattedTextFieldBxCart, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonAtualizaApr, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jButtonPImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jButtonPLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jButtonPFechar, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(323, 323, 323))
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
            TelaPrincipal exibir;
            exibir = new TelaPrincipal();
            exibir.setVisible(true);
            setVisible(false);
        } catch (IOException ex) {
            Logger.getLogger(Tela_de_Consulta_Recursos.class.getName()).log(Level.SEVERE, null, ex);
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
            //Compara se o tipo é Cartões "2.001.003"
            String Comparar = tfRecurso.getText();
            if (Comparar.equals("2.001.003")) {
                jButtonAtualizaApr.setVisible(true);
                jFormattedTextFieldBxCart.setVisible(true);
            }
            try {
                SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");
                DecimalFormat df = new DecimalFormat("#,##0.00");
                Connection con;
                con = Conexao.faz_conexao();
                String sql;
                if (tfRecurso.getText().equals("1.002.001")) {
                    sql = "SELECT * FROM \"tbmovimento\" WHERE \"dtvcto\" BETWEEN ? AND ? AND \"recurso\" = ? AND \"statusmov\" <> 'RC' ORDER BY \"dtvcto\"";

                } else if (tfRecurso.getText().equals("2.001.002")) {
                    sql = "SELECT * FROM \"tbmovimento\" WHERE \"dtvcto\" BETWEEN ? AND ? AND \"recurso\" = ? AND \"statusmov\" <> 'PG' ORDER BY \"dtvcto\"";

                } else {
                    sql = "SELECT * FROM \"tbmovimento\" WHERE \"dtvcto\" BETWEEN ? AND ? AND \"recurso\" = ? ORDER BY \"dtvcto\"";
                }
                PreparedStatement stmt = con.prepareStatement(sql);
                java.util.Date data_i = null;
                try {
                    data_i = formato_data.parse(jFormattedTextFieldDataIni.getText());
                } catch (ParseException ex) {
                    System.out.println("Problema ao pegar data inicial: " + ex);
                }
                stmt.setDate(1, new java.sql.Date(data_i.getTime()));
                java.util.Date data_f = null;
                try {
                    data_f = formato_data.parse(jFormattedTextFieldDataFim.getText());
                } catch (ParseException ex) {
                    System.out.println("Problema ao pegar data Final: " + ex);
                }
                stmt.setDate(2, new java.sql.Date(data_f.getTime()));
                stmt.setString(3, tfNrRecurso.getText());
                ResultSet rs = stmt.executeQuery();
                DefaultTableModel modelo = (DefaultTableModel) jTablePesquisa.getModel();
                modelo.setNumRows(0);
                jTablePesquisa.setRowSorter(new TableRowSorter(modelo)); // Organiza tabela clicando no tÃ­tulo da coluna
                jTablePesquisa.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(0)).setPreferredWidth(40); //idMov
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(1)).setPreferredWidth(60); //recurso
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(2)).setPreferredWidth(0); //70 vrecurso 
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(3)).setPreferredWidth(80); //50 clifor
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(4)).setPreferredWidth(0); //75 vCliFor
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(5)).setPreferredWidth(75); //dtlancto
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(6)).setPreferredWidth(75); //dtEmi
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(7)).setPreferredWidth(75); //dtVcto
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(8)).setPreferredWidth(110); //documento
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(9)).setPreferredWidth(0);  //classif
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(10)).setPreferredWidth(245); //Descr
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(11)).setPreferredWidth(80);  //Valor
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(12)).setPreferredWidth(100); //dtApr
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(13)).setPreferredWidth(50); //40 statusMov
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(14)).setPreferredWidth(40); //30 Prev
                jTablePesquisa.getColumn(jTablePesquisa.getColumnName(15)).setPreferredWidth(80); //Saldo
                DefaultTableCellRenderer tab = new DefaultTableCellRenderer();
                tab.setHorizontalAlignment(SwingConstants.RIGHT);
                jTablePesquisa.getColumnModel().getColumn(11).setCellRenderer(tab);//Mostrando no Formato Moeda
                jTablePesquisa.getColumnModel().getColumn(15).setCellRenderer(tab);//Mostrando no Formato Moeda
                jTablePesquisa.getColumnModel().getColumn(2).setMinWidth(0); // Ocultando as colunas
                jTablePesquisa.getColumnModel().getColumn(2).setMaxWidth(0); // Ocultando as colunas
                jTablePesquisa.getColumnModel().getColumn(4).setMinWidth(0); // Ocultando as colunas
                jTablePesquisa.getColumnModel().getColumn(4).setMaxWidth(0); // Ocultando as colunas
                jTablePesquisa.getColumnModel().getColumn(9).setMinWidth(0); // Ocultando as colunas
                jTablePesquisa.getColumnModel().getColumn(9).setMaxWidth(0); // Ocultando as colunas

                while (rs.next()) {
                    sa = sa + rs.getDouble("valor");
                    Date Apre = rs.getDate("dtapr");
                    String AprFormatada;
                    if (Apre == null && !"".equals(Apre)) {
                        AprFormatada = null;
                    } else if (Apre.equals("  /  /    ")) {
                        AprFormatada = null;
                    } else if (Apre.equals("")) {
                        AprFormatada = null;
                    } else {
                        AprFormatada = formato_data.format(Apre);
                    }
                    modelo.addRow(new Object[]{
                        rs.getString("idmov"),
                        rs.getString("recurso"),
                        rs.getString("vrecurso"),
                        rs.getString("clifor"),
                        rs.getString("vclifor"),
                        formato_data.format(rs.getDate("dtlancto")),
                        formato_data.format(rs.getDate("dtemi")),
                        formato_data.format(rs.getDate("dtvcto")),
                        rs.getString("documento"),
                        rs.getString("classif"),
                        rs.getString("descr"),
                        df.format(rs.getDouble("valor")),
                        AprFormatada,
                        rs.getString("statusmov"),
                        rs.getString("prev"),
                        df.format(sa),});
                }

                //Parâmetros Coluna da jTable, Tamanho da Fonte, se é data (true ou false).
                jTablePesquisa.getColumnModel().getColumn(11).setCellRenderer(new CustomTableCellRenderer(11, 11, false));
                jTablePesquisa.getColumnModel().getColumn(15).setCellRenderer(new CustomTableCellRenderer(15, 11, false));

                rs.close();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_Mov.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
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

//        DefaultTableModel model = (DefaultTableModel) jTablePesquisa.getModel();
//        SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");
//
//        Connection con = null;
//        PreparedStatement stmt = null;
//
//        try {
//            con = Conexao.faz_conexao();
//            String sql = "UPDATE \"tbmovimento\" SET \"dtapr\" = ?, \"statusmov\" = ? WHERE \"idmov\" = ?";
//            stmt = con.prepareStatement(sql);
//
//            for (int i = 0; i < model.getRowCount(); i++) {
//                int tid = Integer.valueOf(model.getValueAt(i, 0).toString());
//                java.util.Date data_v = formato_data.parse(jFormattedTextFieldBxCart.getText());
//                java.sql.Date tdtApr = new java.sql.Date(data_v.getTime());
//                String tstatusMov = model.getValueAt(i, 13).toString();
//
//                if (tstatusMov.isEmpty()) {
//                    tstatusMov = "PG";
//                }
//
//                stmt.setDate(1, tdtApr);
//                stmt.setString(2, tstatusMov);
//                stmt.setInt(3, tid);
//                stmt.addBatch();
//            }
//
//            int[] updatedRows = stmt.executeBatch();
//            JOptionPane.showMessageDialog(null, "Lançamento de cartões baixados com sucesso!");
//
//            // Limpar os campos após atualização
//            tfNrRecurso.setText("");
//            tfRecurso.setText("");
//            jFormattedTextFieldDataIni.setText("");
//            jFormattedTextFieldDataFim.setText("");
//            jTextFieldSaldoAnterior.setText("0");
//            jComboBoxPesq.setSelectedIndex(0);
//            jButtonAtualizaApr.setVisible(false);
//            jFormattedTextFieldBxCart.setVisible(false);
//            tfRecNome.setText("");
//
//        } catch (SQLException | ParseException ex) {
//            Logger.getLogger(Tela_de_Consulta_Recursos.class.getName()).log(Level.SEVERE, null, ex);
//            JOptionPane.showMessageDialog(null, "Erro ao atualizar os dados: " + ex.getMessage());
//        } finally {
//            try {
//                if (stmt != null) {
//                    stmt.close();
//                }
//                if (con != null) {
//                    con.close();
//                }
//            } catch (SQLException ex) {
//                Logger.getLogger(Tela_de_Consulta_Recursos.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        DefaultTableModel model = (DefaultTableModel) jTablePesquisa.getModel();
//        SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");
//
//        Connection con = null;
//        PreparedStatement stmt = null;
//
//        try {
//            con = Conexao.faz_conexao();
//            con.setAutoCommit(false);  // Inicia transação
//
//            // Validação básica da data
//            String textoData = jFormattedTextFieldBxCart.getText().trim();
//            if (textoData.isEmpty() || textoData.equals("  /  /    ") || textoData.equals(" / / ")) {
//                throw new IllegalArgumentException("Informe a data de baixa corretamente (dd/MM/yyyy)");
//            }
//
//            // Converte a data uma única vez
//            java.util.Date dataUtil = formato_data.parse(textoData);
//            java.sql.Date dataSql = new java.sql.Date(dataUtil.getTime());
//
//            // PreparedStatement - PostgreSQL usa ? normalmente para DATE
//            String sql = "UPDATE tbmovimento SET dtapr = ?, statusmov = 'PG' WHERE idmov = ?";
//            stmt = con.prepareStatement(sql);
//
//            int totalAtualizados = 0;
//            int linhasProcessadas = 0;
//
//            // Processa cada linha selecionada na tabela
//            for (int i = 0; i < model.getRowCount(); i++) {
//                // Pega o ID da linha atual
//                String idStr = model.getValueAt(i, 0).toString().trim();
//                int idAtual = Integer.parseInt(idStr);
//
//                // Atualiza o registro atual
//                stmt.setDate(1, dataSql);
//                stmt.setInt(2, idAtual);
//                stmt.addBatch();
//
//                // Atualiza até 2 registros anteriores (se existirem)
//                for (int offset = 1; offset <= 2; offset++) {
//                    int idAnterior = idAtual - offset;
//                    if (idAnterior > 0) {
//                        stmt.setDate(1, dataSql);
//                        stmt.setInt(2, idAnterior);
//                        stmt.addBatch();
//                    }
//                }
//
//                linhasProcessadas++;
//            }
//
//            // Executa todos os updates em batch
//            int[] resultados = stmt.executeBatch();
//
//            // Conta quantos registros foram efetivamente atualizados
//            for (int res : resultados) {
//                if (res != PreparedStatement.EXECUTE_FAILED) {
//                    totalAtualizados += Math.max(res, 1); // batch pode retornar -2 em alguns drivers
//                }
//            }
//
//            con.commit();
//
//            // Mensagem de sucesso mais completa (igual ao SQLite)
//            JOptionPane.showMessageDialog(null,
//                    "Baixa realizada com sucesso!\n"
//                    + "Data utilizada: " + formato_data.format(dataUtil) + "\n"
//                    + "Registros atualizados: " + totalAtualizados + "\n"
//                    + "Novo registro de pagamento do cartão criado.\n"
//                    + "Processadas " + linhasProcessadas + " linha(s) da tabela.",
//                    "Sucesso",
//                    JOptionPane.INFORMATION_MESSAGE);
//
//            // Limpeza dos campos e controles visuais
//            tfNrRecurso.setText("");
//            tfRecurso.setText("");
//            jFormattedTextFieldDataIni.setText("");
//            jFormattedTextFieldDataFim.setText("");
//            jTextFieldSaldoAnterior.setText("0");
//            jComboBoxPesq.setSelectedIndex(0);
//            jButtonAtualizaApr.setVisible(false);
//            jFormattedTextFieldBxCart.setVisible(false);
//            tfRecNome.setText("");
//
//        } catch (ParseException e) {
//            JOptionPane.showMessageDialog(null,
//                    "Formato de data inválido.\nUse o padrão dd/MM/yyyy",
//                    "Erro de data", JOptionPane.ERROR_MESSAGE);
//
//        } catch (NumberFormatException e) {
//            JOptionPane.showMessageDialog(null,
//                    "ID de movimento inválido na tabela.",
//                    "Erro nos dados", JOptionPane.ERROR_MESSAGE);
//
//        } catch (Exception ex) {  // SQLException e outros
//            Logger.getLogger(Tela_de_Consulta_Recursos.class.getName()).log(Level.SEVERE, null, ex);
//
//            if (con != null) {
//                try {
//                    con.rollback();
//                    JOptionPane.showMessageDialog(null,
//                            "Erro ao processar a baixa (rollback realizado):\n" + ex.getMessage(),
//                            "Erro", JOptionPane.ERROR_MESSAGE);
//                } catch (SQLException rbEx) {
//                    Logger.getLogger(Tela_de_Consulta_Recursos.class.getName()).log(Level.SEVERE, "Rollback falhou", rbEx);
//                }
//            } else {
//                JOptionPane.showMessageDialog(null,
//                        "Erro ao processar a baixa:\n" + ex.getMessage(),
//                        "Erro", JOptionPane.ERROR_MESSAGE);
//            }
//        } finally {
//            if (stmt != null) {
//                try {
//                    stmt.close();
//                } catch (SQLException ignored) {
//                }
//            }
//            if (con != null) {
//                try {
//                    con.setAutoCommit(true);
//                } catch (SQLException ignored) {
//                }
//                try {
//                    con.close();
//                } catch (SQLException ignored) {
//                }
//            }
//        }
        DefaultTableModel model = (DefaultTableModel) jTablePesquisa.getModel();
        SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");

        Connection con = null;
        PreparedStatement stmtCartao = null;
        PreparedStatement stmtAnteriores = null;

        try {
            con = Conexao.faz_conexao();
            con.setAutoCommit(false);

            String textoData = jFormattedTextFieldBxCart.getText().trim();
            if (textoData.isEmpty() || textoData.matches("\\s*/\\s*/\\s*")) {
                throw new IllegalArgumentException("Informe a data de pagamento corretamente (dd/MM/yyyy)");
            }

            java.util.Date dataUtil = formato_data.parse(textoData);
            java.sql.Date dataSql = new java.sql.Date(dataUtil.getTime());

            // Para o registro do cartão (atualiza status + data)
            String sqlCartao = "UPDATE tbmovimento SET dtapr = ?, statusmov = 'PG' WHERE idmov = ?";
            stmtCartao = con.prepareStatement(sqlCartao);

            // Para os anteriores (só atualiza data)
            String sqlAnteriores = "UPDATE tbmovimento SET dtapr = ? WHERE idmov = ?";
            stmtAnteriores = con.prepareStatement(sqlAnteriores);

            int totalAtualizados = 0;
            int cartoesBaixados = 0;

            for (int i = 0; i < model.getRowCount(); i++) {
                String idStr = model.getValueAt(i, 0).toString().trim();
                int idCartao = Integer.parseInt(idStr);

                // 1. Atualiza o registro do cartão (data + status 'PG')
                stmtCartao.setDate(1, dataSql);
                stmtCartao.setInt(2, idCartao);   // ? corrigido: índice 2 para o WHERE
                stmtCartao.addBatch();

                // 2. Atualiza os dois anteriores (somente dtapr)
                for (int offset = 1; offset <= 2; offset++) {
                    int idAnt = idCartao - offset;
                    if (idAnt > 0) {
                        stmtAnteriores.setDate(1, dataSql);
                        stmtAnteriores.setInt(2, idAnt);
                        stmtAnteriores.addBatch();
                    }
                }

                cartoesBaixados++;
            }

            // Executa batches
            int[] resCartao = stmtCartao.executeBatch();
            int[] resAnteriores = stmtAnteriores.executeBatch();

            // Conta atualizações (PostgreSQL pode retornar 1 por update bem-sucedido)
            for (int r : resCartao) {
                totalAtualizados += (r > 0 ? r : 0);
            }
            for (int r : resAnteriores) {
                totalAtualizados += (r > 0 ? r : 0);
            }

            con.commit();

//            JOptionPane.showMessageDialog(null,
//                    "Pagamento antecipado registrado com sucesso!\n\n"
//                    + "Data utilizada: " + formato_data.format(dataUtil) + "\n"
//                    + "Cartões baixados: " + cartoesBaixados + "\n"
//                    + "Registros atualizados no total: " + totalAtualizados + "\n\n"
//                    + "(Data de pagamento sobrescrita nos registros relacionados)",
//                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            JOptionPane.showMessageDialog(null,
                    "Baixa / pagamento antecipado realizado com sucesso!\n\n"
                    + "Data utilizada: " + formato_data.format(dataUtil) + "\n"
                    + "Cartões processados: " + cartoesBaixados + "\n"
                    + "Registros atualizados: " + totalAtualizados + "\n\n"
                    + "A data de pagamento foi sobrescrita nos registros relacionados.",
                    "Operação concluída", JOptionPane.INFORMATION_MESSAGE);

            // Limpeza da interface
            tfNrRecurso.setText("");
            tfRecurso.setText("");
            jFormattedTextFieldDataIni.setText("");
            jFormattedTextFieldDataFim.setText("");
            jTextFieldSaldoAnterior.setText("0");
            jComboBoxPesq.setSelectedIndex(0);
            jButtonAtualizaApr.setVisible(false);
            jFormattedTextFieldBxCart.setVisible(false);
            tfRecNome.setText("");

        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, "Data inválida. Use dd/MM/yyyy.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "ID inválido na tabela.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            Logger.getLogger(Tela_de_Consulta_Recursos.class.getName()).log(Level.SEVERE, null, ex);
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ignored) {
                }
            }
            JOptionPane.showMessageDialog(null,
                    "Erro ao registrar o pagamento:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (stmtCartao != null) {
                    stmtCartao.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (stmtAnteriores != null) {
                    stmtAnteriores.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException ignored) {
            }
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
            java.util.logging.Logger.getLogger(Tela_de_Consulta_Recursos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Tela_de_Consulta_Recursos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Tela_de_Consulta_Recursos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tela_de_Consulta_Recursos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Tela_de_Consulta_Recursos().setVisible(true);
                } catch (SQLException ex) {
                    Logger.getLogger(Tela_de_Consulta_Recursos.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void PegaSaldoAnterior() {
        String sql = "SELECT SUM(\"valor\") FROM \"tbmovimento\" WHERE \"dtvcto\" < ? AND \"recurso\" = ?";
        Connection con = null;
        PreparedStatement stt = null;
        ResultSet rsoma = null;

        try {
            con = Conexao.faz_conexao();
            stt = con.prepareStatement(sql);
            SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");
            java.util.Date data_c = formato_data.parse(jFormattedTextFieldDataIni.getText());
            stt.setDate(1, new java.sql.Date(data_c.getTime()));
            stt.setString(2, tfNrRecurso.getText());
            rsoma = stt.executeQuery();

            double somaval = 0;
            if (rsoma.next()) {
                somaval = rsoma.getDouble(1);
            }

            jTextFieldSaldoAnterior.setText(String.valueOf(somaval));
        } catch (SQLException | ParseException ex) {
            Logger.getLogger(Tela_de_Consulta_Recursos.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao buscar o saldo anterior: " + ex.getMessage());
        } finally {
            try {
                if (rsoma != null) {
                    rsoma.close();
                }
                if (stt != null) {
                    stt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_Consulta_Recursos.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            String sql = "Select * FROM \"" + tabela + "\" ORDER BY \"nomebco\"";
            Connection con = Conexao.faz_conexao();
            try {
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    combo.addItem(rs.getString(valor) + "-" + (rs.getString("codigo") + "-" + (rs.getString("fk_gpprinc"))));
                }
                rs.close();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_Consulta_Recursos.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
