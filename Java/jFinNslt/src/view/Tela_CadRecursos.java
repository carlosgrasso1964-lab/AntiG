package view;


import utilitarios.Conexao;

import dao.RecursosDAO;

import java.io.IOException;

import java.sql.Connection;

import java.sql.ResultSet;

import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.text.DecimalFormat;

import java.text.ParseException;

import java.text.SimpleDateFormat;

import java.util.logging.Level;

import java.util.logging.Logger;

import javax.swing.JOptionPane;

import javax.swing.table.DefaultTableModel;

import javax.swing.JComboBox;

import java.util.List;

import model.Placon;

import model.Recursos;

import relatorios.RelRecursos;

import utilitarios.LimpaTela;



public class Tela_CadRecursos extends javax.swing.JFrame {



    private Conexao conexao;



    public void listar() throws SQLException, ClassNotFoundException {

        RecursosDAO dao = new RecursosDAO();

        List<Recursos> lista = dao.Listar();

        DefaultTableModel dados = (DefaultTableModel) tbDados.getModel();

        tbDados.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

        tbDados.getColumn(tbDados.getColumnName(0)).setPreferredWidth(70); //Cod

        tbDados.getColumn(tbDados.getColumnName(1)).setPreferredWidth(300);//Fonte

        tbDados.getColumn(tbDados.getColumnName(2)).setPreferredWidth(90);//Agencia

        tbDados.getColumn(tbDados.getColumnName(3)).setPreferredWidth(60);//Fluxo

        tbDados.getColumn(tbDados.getColumnName(4)).setPreferredWidth(90);//Limite

        tbDados.getColumn(tbDados.getColumnName(5)).setPreferredWidth(90);//Abertura

        tbDados.getColumn(tbDados.getColumnName(6)).setPreferredWidth(90);//Encerramento

        tbDados.getColumn(tbDados.getColumnName(7)).setPreferredWidth(50);//Status

        tbDados.getColumn(tbDados.getColumnName(8)).setPreferredWidth(80);//Vinculo

        // Limpar a tabela antes de adicionar os novos dados

        dados.setNumRows(0);

        for (Recursos c : lista) {

            // Converter a data de abertura e encerramento para exibição

            String dataAberturaExibir = "";

            String dataEncerramentoExibir = "";

            try {

                SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd"); // Formato no banco

                SimpleDateFormat formatoSaida = new SimpleDateFormat("dd/MM/yyyy"); // Formato para exibição



                if (c.getAbertura() != null && !c.getAbertura().isEmpty()) {

                    java.util.Date dataAberturaUtil = formatoEntrada.parse(c.getAbertura());

                    dataAberturaExibir = formatoSaida.format(dataAberturaUtil); // Converter para dd/MM/yyyy

                }

                if (c.getEncerramento() != null && !c.getEncerramento().isEmpty()) {

                    java.util.Date dataEncerramentoUtil = formatoEntrada.parse(c.getEncerramento());

                    dataEncerramentoExibir = formatoSaida.format(dataEncerramentoUtil); // Converter para dd/MM/yyyy

                }

            } catch (ParseException e) {

                e.printStackTrace(); // Em caso de erro de formatação

            }

            DecimalFormat df = new DecimalFormat("#,##0.00");

            // Adicionar nova linha na tabela

            dados.addRow(new Object[]{

                c.getCodigo(),

                c.getNomebco(),

                c.getAgencia(),

                c.getFluxo(),

                df.format(c.getLimite()),

                dataAberturaExibir, // Data de abertura formatada para exibição

                dataEncerramentoExibir, // Data de encerramento formatada para exibição

                c.getStatus(),

                c.getFk_gpprinc() != null ? c.getFk_gpprinc().getCod_Geral() : "Sem vínculo"

            });

        }

    }



    CarregarCbx re = new CarregarCbx();



    public Tela_CadRecursos() throws SQLException {

        initComponents();

        conexao = new Conexao(); // Certifique-se de inicializar aqui

        re.CarregarCbx("gpprincipal", "nome_C", cbxVinculo);

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
        jLabel1 = new javax.swing.JLabel();
        jPanelRecursos = new javax.swing.JPanel();
        tfCod = new javax.swing.JTextField();
        tfNomeBco = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        tfNomeAgencia = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        tfFlx = new javax.swing.JTextField();
        tfStatus = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        tfLimite = new javax.swing.JTextField();
        cbxVinculo = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        tfAbertura = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        tfEncerramento = new javax.swing.JTextField();
        tfVinculo = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbDados = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btAbrir = new javax.swing.JButton();
        btnListarDados = new javax.swing.JButton();
        tfBusca = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("       Cadastro Recursos");
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
                .addGap(254, 254, 254)
                .addComponent(btSalvar)
                .addGap(18, 18, 18)
                .addComponent(btnAtualizar)
                .addGap(18, 18, 18)
                .addComponent(btnExcluir)
                .addGap(18, 18, 18)
                .addComponent(jButtonImprimir)
                .addGap(18, 18, 18)
                .addComponent(btnFechar)
                .addContainerGap(261, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFechar)
                    .addComponent(btnExcluir)
                    .addComponent(btnAtualizar)
                    .addComponent(btSalvar)
                    .addComponent(jButtonImprimir))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("CADASTRO DE RECURSOS");

        jLabel2.setText("Cod");

        jLabel3.setText("Fonte");

        jLabel5.setText("Agência");

        tfNomeAgencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfNomeAgenciaActionPerformed(evt);
            }
        });

        jLabel6.setText("Fluxo");

        jLabel10.setText("Status");

        jLabel7.setText("Limite");

        jLabel4.setText("Vínculo");

        cbxVinculo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione" }));
        cbxVinculo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxVinculoActionPerformed(evt);
            }
        });

        jLabel8.setText("Abertura");

        jLabel9.setText("Encerramento");

        tfEncerramento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfEncerramentoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelRecursosLayout = new javax.swing.GroupLayout(jPanelRecursos);
        jPanelRecursos.setLayout(jPanelRecursosLayout);
        jPanelRecursosLayout.setHorizontalGroup(
            jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRecursosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelRecursosLayout.createSequentialGroup()
                        .addGroup(jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelRecursosLayout.createSequentialGroup()
                                .addComponent(tfFlx, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfLimite, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfAbertura, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(tfEncerramento, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanelRecursosLayout.createSequentialGroup()
                                .addComponent(tfStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxVinculo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(tfVinculo, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanelRecursosLayout.createSequentialGroup()
                        .addGroup(jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfCod, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tfNomeBco, javax.swing.GroupLayout.PREFERRED_SIZE, 398, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(tfNomeAgencia, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(65, 65, 65)))
                .addContainerGap())
        );
        jPanelRecursosLayout.setVerticalGroup(
            jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRecursosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfCod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(tfNomeAgencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(tfNomeBco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel9)
                        .addComponent(tfEncerramento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tfAbertura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8))
                    .addGroup(jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7)
                        .addComponent(tfFlx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tfLimite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelRecursosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxVinculo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel10)
                    .addComponent(tfVinculo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        tbDados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Cod", "Fonte", "Agência", "Fluxo", "Limite", "Abertura", "Encerramento", "Status", "Vìnculo"
            }
        ));
        tbDados.setSelectionBackground(new java.awt.Color(255, 255, 204));
        tbDados.setSelectionForeground(new java.awt.Color(51, 51, 51));
        jScrollPane2.setViewportView(tbDados);

        jScrollPane1.setViewportView(jScrollPane2);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 962, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
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
                .addContainerGap()
                .addComponent(btAbrir, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tfBusca, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 623, Short.MAX_VALUE)
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
                .addGap(0, 9, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelRecursos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 950, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelRecursos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents



    private void btSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSalvarActionPerformed

        if (tfCod.getText().equals("")) {

            JOptionPane.showMessageDialog(null, "Conta em branco!");

        } else {



            // Criar o objeto GrupoPrincipal com o valor do campo tfVinculo

            Placon placon = buscarGrupoPrincipal(tfVinculo.getText());



            // Verificar se o grupo foi encontrado

            if (placon == null) {

                JOptionPane.showMessageDialog(null, "Grupo Principal inválido!");

                return;

            }

            Recursos obj = new Recursos();

            obj.setCodigo(tfCod.getText());

            obj.setNomebco(tfNomeBco.getText());

            obj.setAgencia(tfNomeAgencia.getText());

            obj.setFluxo(tfFlx.getText());

            

            // Remover todos os pontos, exceto o decimal

            String valorTexto = tfLimite.getText().replaceAll("\\.", "");

            // Substituir a vírgula decimal por ponto decimal

            valorTexto = valorTexto.replaceAll(",", ".");

            // Parse para Double

            obj.setLimite(Double.parseDouble(valorTexto));

            obj.setAbertura(tfAbertura.getText());

            obj.setEncerramento(tfEncerramento.getText());

            obj.setStatus(tfStatus.getText());

            obj.setFk_gpprinc(placon);

            try {

                RecursosDAO dao = new RecursosDAO();

                dao.Salvar(obj);

                LimpaTela util = new LimpaTela();

                util.LimpaTela(jPanelRecursos);

            } catch (SQLException | ClassNotFoundException | ParseException ex) {

                Logger.getLogger(Tela_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);

            }

        }

    }//GEN-LAST:event_btSalvarActionPerformed



    public Placon buscarGrupoPrincipal(String cod_Geral) {

        try {

            Placon placon;

            conexao.abrirConexao(); // Abre a conexão

            Connection conn = conexao.getConexao(); // Obtém a conexão 

            String sql = "SELECT * FROM gpprincipal WHERE cod_Geral=?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, cod_Geral);

                try (ResultSet rs = stmt.executeQuery()) {

                    placon = null;

                    if (rs.next()) {

                        placon = new Placon();

                        placon.setCod_Geral(rs.getString("cod_Geral"));

                    }

                }

            }

            return placon;

        } catch (SQLException erro) {

            JOptionPane.showMessageDialog(null, "Erro ao buscar Grupo Principal! " + erro.getMessage());

        } finally {

            conexao.fecharConexao(); // Fecha a conexão no final

        }

        return null;

    }



    private void btAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAbrirActionPerformed

        if (tfBusca.getText().equals("")) {

            JOptionPane.showMessageDialog(null, "Informe um Id válido...");

        } else {

            try {

                String nome = tfBusca.getText();

                Recursos obj = new Recursos();

                RecursosDAO dao = new RecursosDAO();

                obj = dao.BuscarRecursos(nome);

                if (obj.getCodigo() != null) {

                    tfCod.setText(obj.getCodigo());

                    tfNomeBco.setText(obj.getNomebco());

                    tfNomeAgencia.setText(obj.getAgencia());

                    tfFlx.setText(obj.getFluxo());

                    tfLimite.setText(String.valueOf(obj.getLimite()));

                    // Converte e exibe as datas

                    SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd"); // Formato do banco

                    SimpleDateFormat formatoSaida = new SimpleDateFormat("dd/MM/yyyy"); // Formato para exibição

                    // Data de Abertura

                    if (obj.getAbertura() != null && !obj.getAbertura().isEmpty()) {

                        java.util.Date dataAberturaUtil = formatoEntrada.parse(obj.getAbertura());

                        String dataAberturaExibir = formatoSaida.format(dataAberturaUtil);

                        tfAbertura.setText(dataAberturaExibir); // Exibe no formato dd/MM/yyyy

                    } else {

                        tfAbertura.setText(""); // Caso a data de abertura esteja vazia

                    }

                    // Data de Encerramento

                    if (obj.getEncerramento() != null && !obj.getEncerramento().isEmpty()) {

                        java.util.Date dataEncerramentoUtil = formatoEntrada.parse(obj.getEncerramento());

                        String dataEncerramentoExibir = formatoSaida.format(dataEncerramentoUtil);

                        tfEncerramento.setText(dataEncerramentoExibir); // Exibe no formato dd/MM/yyyy

                    } else {

                        tfEncerramento.setText(""); // Caso a data de encerramento esteja vazia

                    }

                    tfStatus.setText(obj.getStatus());

                    // Aqui, pegue o nome ou ID do GrupoPrincipal e exiba no campo tfVinculo

                    if (obj.getFk_gpprinc() != null) {

                        tfVinculo.setText(obj.getFk_gpprinc().getCod_Geral()); // Exibe o nome do grupo vinculado

                    } else {

                        tfVinculo.setText("Sem vínculo"); // Caso não tenha um GrupoPrincipal vinculado

                    }

                } else {

                    JOptionPane.showMessageDialog(null, "Conta não encontrada!");

                }

            } catch (SQLException | ClassNotFoundException | ParseException ex) {

                Logger.getLogger(Tela_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);

            }

        }

    }//GEN-LAST:event_btAbrirActionPerformed



    private void btnListarDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnListarDadosActionPerformed

        //RecursosDao dao;

        try {

            RecursosDAO dao = new RecursosDAO();

            List<Recursos> lista = dao.Listar();

            DefaultTableModel dados = (DefaultTableModel) tbDados.getModel();

            dados.setNumRows(0);

            tbDados.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

            tbDados.getColumn(tbDados.getColumnName(0)).setPreferredWidth(70); //Cod

            tbDados.getColumn(tbDados.getColumnName(1)).setPreferredWidth(300);//Fonte

            tbDados.getColumn(tbDados.getColumnName(2)).setPreferredWidth(90);//Agencia

            tbDados.getColumn(tbDados.getColumnName(3)).setPreferredWidth(60);//Fluxo

            tbDados.getColumn(tbDados.getColumnName(4)).setPreferredWidth(90);//Limite

            tbDados.getColumn(tbDados.getColumnName(5)).setPreferredWidth(90);//Abertura

            tbDados.getColumn(tbDados.getColumnName(6)).setPreferredWidth(90);//Encerramento

            tbDados.getColumn(tbDados.getColumnName(7)).setPreferredWidth(50);//Status

            tbDados.getColumn(tbDados.getColumnName(8)).setPreferredWidth(80);//Vinculo

            for (Recursos c : lista) {

                String dataAberturaExibir = "";

                String dataEncerramentoExibir = "";

                try {

                    SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd"); // Formato no banco

                    SimpleDateFormat formatoSaida = new SimpleDateFormat("dd/MM/yyyy"); // Formato para exibição

                    if (c.getAbertura() != null && !c.getAbertura().isEmpty()) {

                        java.util.Date dataAberturaUtil = formatoEntrada.parse(c.getAbertura());

                        dataAberturaExibir = formatoSaida.format(dataAberturaUtil); // Converter para dd/MM/yyyy

                    }

                    if (c.getEncerramento() != null && !c.getEncerramento().isEmpty()) {

                        java.util.Date dataEncerramentoUtil = formatoEntrada.parse(c.getEncerramento());

                        dataEncerramentoExibir = formatoSaida.format(dataEncerramentoUtil); // Converter para dd/MM/yyyy

                    }

                } catch (ParseException e) {

                    e.printStackTrace(); // Em caso de erro de formatação

                }

                DecimalFormat df = new DecimalFormat("#,##0.00");

                dados.addRow(new Object[]{

                    c.getCodigo(),

                    c.getNomebco(),

                    c.getAgencia(),

                    c.getFluxo(),

                    df.format(c.getLimite()),

                    dataAberturaExibir, // Data de abertura formatada para exibição

                    dataEncerramentoExibir, // Data de encerramento formatada para exibição

                    c.getStatus(),

                    c.getFk_gpprinc() != null ? c.getFk_gpprinc().getCod_Geral() : "Sem vínculo"

                });

            }

        } catch (SQLException | ClassNotFoundException ex) {

            Logger.getLogger(Tela_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);

        }

    }//GEN-LAST:event_btnListarDadosActionPerformed



    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarActionPerformed

        if (tfBusca.getText().equals("")) {

            JOptionPane.showMessageDialog(null, "Informe o Recurso");

        } else {

            try {

                // Criar o objeto GrupoPrincipal com o valor do campo tfVinculo

                Placon placon = buscarGrupoPrincipal(tfVinculo.getText());

                // Verificar se o grupo foi encontrado

                if (placon == null) {

                    JOptionPane.showMessageDialog(null, "Recurso inválido!");

                    return;

                }

                Recursos obj = new Recursos();

                obj.setNomebco(tfNomeBco.getText());

                obj.setAgencia(tfNomeAgencia.getText());

                obj.setFluxo(tfFlx.getText());

                obj.setLimite(Double.parseDouble(tfLimite.getText().replaceAll(",", ".")));

                obj.setAbertura(tfAbertura.getText());

                obj.setEncerramento(tfEncerramento.getText());

                obj.setStatus(tfStatus.getText());

                obj.setFk_gpprinc(placon);

                obj.setCodigo(tfCod.getText());

                try {

                    RecursosDAO dao = new RecursosDAO();

                    dao.Editar(obj);

                    LimpaTela util = new LimpaTela();

                    util.LimpaTela(jPanelRecursos);

                } catch (SQLException | ClassNotFoundException | ParseException ex) {

                    Logger.getLogger(Tela_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);

                }

            } catch (NumberFormatException ex) {

                Logger.getLogger(Tela_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);

            }

        }

    }//GEN-LAST:event_btnAtualizarActionPerformed



    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed

        if (tfCod.getText().equals("")) {

            JOptionPane.showMessageDialog(null, "Informe o Recurso à ser excluído.");

        } else {

            Recursos obj = new Recursos();

            obj.setCodigo(tfCod.getText());

            RecursosDAO dao;

            try {

                dao = new RecursosDAO();

                dao.Excluir(obj);

                LimpaTela util = new LimpaTela();

                util.LimpaTela(jPanelRecursos);

            } catch (SQLException | ClassNotFoundException ex) {

                Logger.getLogger(Tela_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);

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

            Logger.getLogger(Tela_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);

        }

    }//GEN-LAST:event_btnFecharActionPerformed



    private void cbxVinculoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxVinculoActionPerformed

        String selectedValue = cbxVinculo.getSelectedItem().toString();

        var texto = selectedValue.length();

        texto = texto - 9;

        selectedValue = cbxVinculo.getSelectedItem().toString().substring(texto);

        tfVinculo.setText(selectedValue);

    }//GEN-LAST:event_cbxVinculoActionPerformed



    private void tfNomeAgenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfNomeAgenciaActionPerformed

        // TODO add your handling code here:

    }//GEN-LAST:event_tfNomeAgenciaActionPerformed



    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated

        try {

            listar();

        } catch (SQLException | ClassNotFoundException ex) {

            Logger.getLogger(Tela_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);

        }

    }//GEN-LAST:event_formWindowActivated



    private void jButtonImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonImprimirActionPerformed

        RelRecursos recur = new RelRecursos();

        JOptionPane.showMessageDialog(null, "Relatório de Recursos gerado com sucesso!");

        dispose();

        try {

            new Tela_CadRecursos().setVisible(true);

        } catch (SQLException ex) {

            Logger.getLogger(Tela_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);

        }

    }//GEN-LAST:event_jButtonImprimirActionPerformed



    private void tfEncerramentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfEncerramentoActionPerformed

        // TODO add your handling code here:

    }//GEN-LAST:event_tfEncerramentoActionPerformed



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

            java.util.logging.Logger.getLogger(Tela_CadRecursos.class

                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);



        } catch (InstantiationException ex) {

            java.util.logging.Logger.getLogger(Tela_CadRecursos.class

                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);



        } catch (IllegalAccessException ex) {

            java.util.logging.Logger.getLogger(Tela_CadRecursos.class

                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);



        } catch (javax.swing.UnsupportedLookAndFeelException ex) {

            java.util.logging.Logger.getLogger(Tela_CadRecursos.class

                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        }

        //</editor-fold>

        //</editor-fold>

        //</editor-fold>

        //</editor-fold>



        /* Create and display the form */

        java.awt.EventQueue.invokeLater(() -> {

            try {

                new Tela_CadRecursos().setVisible(true);

            } catch (SQLException ex) {

                Logger.getLogger(Tela_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);

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
    private javax.swing.JButton jButtonImprimir;
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
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelRecursos;
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

            //String sql = "SELECT * FROM " + tabela;
            String sql = "SELECT * FROM " + tabela + " ORDER BY " + "cod_Geral" + " ASC";
            // Cria uma instância da classe Conexao

            Conexao conexao = new Conexao();

            try {

                // Abre a conexão

                conexao.abrirConexao();

                // Obtém a conexão

                Connection con = conexao.getConexao();

                // Prepara e executa a consulta

                PreparedStatement stmt = con.prepareStatement(sql);

                try (ResultSet rs = stmt.executeQuery()) {

                    // Adiciona os resultados no comboBox

                    while (rs.next()) {

                        combo.addItem(rs.getString(valor) + "-" + rs.getString("cod_Geral"));

                    }

                }

            } catch (SQLException ex) {

                Logger.getLogger(Tela_CadRecursos.class.getName()).log(Level.SEVERE, null, ex);

            } finally {

                // Fecha a conexão no final

                conexao.fecharConexao();

            }

        }

    }

}

