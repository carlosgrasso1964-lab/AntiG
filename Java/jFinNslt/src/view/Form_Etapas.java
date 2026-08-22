package view;



import java.io.IOException;

import java.util.logging.Level;

import java.util.logging.Logger;

import dao.EtapaDAO;

import dao.ProjetoDAO;

import java.sql.SQLException;

import java.time.LocalDate;

import java.time.format.DateTimeFormatter;

import java.time.format.DateTimeParseException;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

import javax.swing.JOptionPane;

import javax.swing.event.ListSelectionEvent;

import javax.swing.event.ListSelectionListener;

import javax.swing.table.DefaultTableModel;

import model.Etapa;

import utilitarios.Conexao;

import model.Projeto;

import java.math.BigDecimal;

import java.math.RoundingMode;

import java.text.DecimalFormat;


public class Form_Etapas extends javax.swing.JFrame {

    private EtapaDAO etapaDAO;

    private ProjetoDAO projetoDAO;

    private Map<String, Integer> projetoIdMap = new HashMap<>();


    /**

     * Creates new form Form_Etapas

     */

    public Form_Etapas() {

        initComponents();

        Conexao conexao = new Conexao();

        etapaDAO = new EtapaDAO(conexao);

        projetoDAO = new ProjetoDAO(conexao);

        txtVrPrev.setText("0,00");

        txtVrReal.setText("0,00");



        //tabelaEtapas.getColumnModel().getColumn(1).setHeaderValue("Nome do Projeto");

        carregarProjetos(); // Carrega os projetos no JComboBox

        carregarEtapas();

//        tabelaEtapas.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

//            public void valueChanged(ListSelectionEvent event) {

//                if (!event.getValueIsAdjusting()) {

//                    int selectedRow = tabelaEtapas.getSelectedRow();

//                    if (selectedRow != -1) {

//                        int id = (int) tabelaEtapas.getValueAt(selectedRow, 0); // Assumindo que o ID está na primeira coluna (Índice 0)

//                        txtIDE.setText(String.valueOf(id));

//                    }

//                }

//            }

//        });

        tabelaEtapas.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override

            public void valueChanged(ListSelectionEvent e) {

                if (e.getValueIsAdjusting()) {

                    return; // evita chamar duas vezes

                }

                int linhaSelecionada = tabelaEtapas.getSelectedRow();

                if (linhaSelecionada == -1) {

                    // nenhuma linha selecionada ? limpa o ID

                    txtIDE.setText("");

                    return;

                }



                // A coluna 0 da tabela é o ID da etapa (não do projeto!)

                Object valorId = tabelaEtapas.getValueAt(linhaSelecionada, 0);



                if (valorId != null && !valorId.toString().trim().isEmpty()) {

                    txtIDE.setText(valorId.toString().trim());

                } else {

                    txtIDE.setText("");

                }

            }

        });



        // Alinha colunas financeiras à direita

        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();

        rightRenderer.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);



        tabelaEtapas.getColumnModel().getColumn(10).setCellRenderer(rightRenderer); // R$ Previsto

        tabelaEtapas.getColumnModel().getColumn(11).setCellRenderer(rightRenderer); // R$ Realizado

        tabelaEtapas.getColumnModel().getColumn(12).setCellRenderer(rightRenderer); // % Executado

    }



    private void carregarEtapas() {

//        DefaultTableModel model = (DefaultTableModel) tabelaEtapas.getModel();

//        model.setRowCount(0);

//

//        try {

//            List<Etapa> etapas = etapaDAO.listarTodos();

//            for (Etapa etapa : etapas) {

//                String nomeProjeto = projetoDAO.getNomeProjetoPorId(etapa.getProjetoId()); // Obtém o nome do projeto

//

//                model.addRow(new Object[]{

//                    etapa.getId(),

//                    nomeProjeto, // Exibe o nome do projeto em vez do ID

//                    etapa.getNome(),

//                    etapa.getPrioridade(),

//                    etapa.getResponsavel(),

//                    etapa.getDataInicioPrevista(),

//                    etapa.getDataFimPrevista(),

//                    etapa.getDataInicioRealizada(),

//                    etapa.getDataFimRealizada(),

//                    etapa.getStatus()

//                });

//            }

//        } catch (SQLException ex) {

//            JOptionPane.showMessageDialog(this, "Erro ao carregar etapas: " + ex.getMessage());

//        }

        DefaultTableModel model = (DefaultTableModel) tabelaEtapas.getModel();

        model.setRowCount(0);



        DecimalFormat df = new DecimalFormat("#,##0.00");

        DecimalFormat pf = new DecimalFormat("0.0");



        try {

            List<Etapa> etapas = etapaDAO.listarTodos();

            for (Etapa etapa : etapas) {

                String nomeProjeto = projetoDAO.getNomeProjetoPorId(etapa.getProjetoId());



                BigDecimal previsto = etapa.getValorPrevisto();

                BigDecimal realizado = etapa.getValorRealizado();

                String percentual = "0,0%";



                if (previsto != null && previsto.compareTo(BigDecimal.ZERO) > 0) {

                    double perc = realizado.divide(previsto, 4, RoundingMode.HALF_UP)

                            .multiply(BigDecimal.valueOf(100))

                            .doubleValue();

                    percentual = pf.format(perc) + "%";

                }



                model.addRow(new Object[]{

                    etapa.getId(),

                    nomeProjeto,

                    etapa.getNome(),

                    etapa.getPrioridade(),

                    etapa.getResponsavel(),

                    etapa.getDataInicioPrevista(),

                    etapa.getDataFimPrevista(),

                    etapa.getDataInicioRealizada(),

                    etapa.getDataFimRealizada(),

                    etapa.getStatus(),

                    "R$ " + df.format(previsto),

                    "R$ " + df.format(realizado),

                    percentual,

                    etapa.getObs()

                });

            }

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(this, "Erro ao carregar etapas: " + ex.getMessage());

            ex.printStackTrace();

        }

    }



    /**

     * This method is called from within the constructor to initialize the form.

     * WARNING: Do NOT modify this code. The content of this method is always

     * regenerated by the Form Editor.

     */

    @SuppressWarnings("unchecked")

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents

    private void initComponents() {



        jPanel1 = new javax.swing.JPanel();

        jLabel1 = new javax.swing.JLabel();

        jPanel4 = new javax.swing.JPanel();

        jLabel2 = new javax.swing.JLabel();

        jLabel3 = new javax.swing.JLabel();

        jLabel4 = new javax.swing.JLabel();

        jLabel5 = new javax.swing.JLabel();

        jLabel6 = new javax.swing.JLabel();

        jLabel7 = new javax.swing.JLabel();

        jLabel8 = new javax.swing.JLabel();

        jLabel9 = new javax.swing.JLabel();

        txtIDE = new javax.swing.JTextField();

        txtEtapa = new javax.swing.JTextField();

        txtInicioEtapaPrev = new javax.swing.JTextField();

        txtFimEtapaPrev = new javax.swing.JTextField();

        txtInicioEtapaReal = new javax.swing.JTextField();

        txtFimEtapaReal = new javax.swing.JTextField();

        cbStatusEtapa = new javax.swing.JComboBox<>();

        jLabel10 = new javax.swing.JLabel();

        cbPrioridade = new javax.swing.JComboBox<>();

        jLabel11 = new javax.swing.JLabel();

        jScrollPane2 = new javax.swing.JScrollPane();

        jTextAreaObs = new javax.swing.JTextArea();

        jLabel12 = new javax.swing.JLabel();

        txtResp = new javax.swing.JTextField();

        cbProj = new javax.swing.JComboBox<>();

        jLabel13 = new javax.swing.JLabel();

        jLabel14 = new javax.swing.JLabel();

        txtVrPrev = new javax.swing.JTextField();

        txtVrReal = new javax.swing.JTextField();

        jPanel2 = new javax.swing.JPanel();

        jScrollPane1 = new javax.swing.JScrollPane();

        tabelaEtapas = new javax.swing.JTable();

        jPanel3 = new javax.swing.JPanel();

        jButtonCriar = new javax.swing.JButton();

        jButtonLer = new javax.swing.JButton();

        jButtonAtualizar = new javax.swing.JButton();

        jButtonExcluir = new javax.swing.JButton();

        jButtonListar = new javax.swing.JButton();

        jButtonFechar = new javax.swing.JButton();



        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        setTitle("Formulario Etapas");



        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jLabel1.setText("FORMUL\\u00c1RIO - CADASTRO DE ETAPAS");



        jLabel2.setText("ID");



        jLabel3.setText("Projeto:");



        jLabel4.setText("Etapa:");



        jLabel5.setText("Inicio Previsto:");



        jLabel6.setText("Fim Previsto:");



        jLabel7.setText("Início Realizado:");



        jLabel8.setText("Fim Realizado:");



        jLabel9.setText("Status Etapa:");



        txtEtapa.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {

                txtEtapaActionPerformed(evt);

            }

        });



        txtInicioEtapaPrev.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {

                txtInicioEtapaPrevActionPerformed(evt);

            }

        });



        txtFimEtapaPrev.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {

                txtFimEtapaPrevActionPerformed(evt);

            }

        });



        cbStatusEtapa.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Não Iniciado", "Em Andamento", "Pendente", "Atrasada", "Cancelada", "Conclu\\u00edda" }));



        jLabel10.setText("Prioridade:");



        cbPrioridade.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Baixa", "Média", "Alta" }));

        cbPrioridade.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {

                cbPrioridadeActionPerformed(evt);

            }

        });



        jLabel11.setText("Obs.:");



        jTextAreaObs.setColumns(20);

        jTextAreaObs.setRows(5);

        jScrollPane2.setViewportView(jTextAreaObs);



        jLabel12.setText("Respons.:");



        cbProj.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "nome" }));



        jLabel13.setText("R$ Previsto:");



        jLabel14.setText("R$ Realizado:");



        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);

        jPanel4.setLayout(jPanel4Layout);

        jPanel4Layout.setHorizontalGroup(

            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

            .addGroup(jPanel4Layout.createSequentialGroup()

                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

                    .addGroup(jPanel4Layout.createSequentialGroup()

                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

                            .addGroup(jPanel4Layout.createSequentialGroup()

                                .addGap(28, 28, 28)

                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)

                                    .addComponent(jLabel7)

                                    .addComponent(jLabel5)))

                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()

                                .addContainerGap()

                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

                                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)

                                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))))

                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

                            .addGroup(jPanel4Layout.createSequentialGroup()

                                .addGap(376, 376, 376)

                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE))

                            .addGroup(jPanel4Layout.createSequentialGroup()

                                .addGap(18, 18, 18)

                                .addComponent(txtIDE, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))))

                    .addGroup(jPanel4Layout.createSequentialGroup()

                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()

                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

                                    .addGroup(jPanel4Layout.createSequentialGroup()

                                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

                                        .addComponent(jLabel8))

                                    .addGroup(jPanel4Layout.createSequentialGroup()

                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)

                                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)

                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()

                                                    .addGap(129, 129, 129)

                                                    .addComponent(txtInicioEtapaReal, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))

                                                .addGroup(jPanel4Layout.createSequentialGroup()

                                                    .addContainerGap()

                                                    .addComponent(jLabel10)

                                                    .addGap(18, 18, 18)

                                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

                                                        .addComponent(cbPrioridade, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)

                                                        .addComponent(txtInicioEtapaPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))))

                                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()

                                                    .addGap(6, 6, 6)

                                                    .addComponent(jLabel13)

                                                    .addGap(18, 18, 18)

                                                    .addComponent(txtVrPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))

                                                .addGroup(jPanel4Layout.createSequentialGroup()

                                                    .addComponent(jLabel9)

                                                    .addGap(18, 18, 18)

                                                    .addComponent(cbStatusEtapa, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))))

                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

                                        .addComponent(jLabel14)))

                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

                                    .addGroup(jPanel4Layout.createSequentialGroup()

                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)

                                        .addComponent(txtFimEtapaReal, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))

                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()

                                        .addGap(6, 6, 6)

                                        .addComponent(txtVrReal, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)))

                                .addGap(55, 55, 55))

                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()

                                .addGap(129, 129, 129)

                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()

                                        .addComponent(jLabel6)

                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)

                                        .addComponent(txtFimEtapaPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)

                                        .addGap(55, 55, 55))

                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()

                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)

                                            .addGroup(jPanel4Layout.createSequentialGroup()

                                                .addComponent(jLabel12)

                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)

                                                .addComponent(txtResp, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))

                                            .addComponent(cbProj, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))

                                        .addGap(18, 18, 18)

                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)

                                            .addComponent(jLabel4)

                                            .addComponent(jLabel11))

                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))))

                        .addComponent(txtEtapa, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE)))

                .addGap(19, 19, 19))

        );

        jPanel4Layout.setVerticalGroup(

            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

            .addGroup(jPanel4Layout.createSequentialGroup()

                .addContainerGap()

                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)

                    .addComponent(jLabel2)

                    .addComponent(txtIDE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))

                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)

                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)

                    .addComponent(jLabel3)

                    .addComponent(jLabel4)

                    .addComponent(txtEtapa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)

                    .addComponent(cbProj, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))

                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)

                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

                    .addGroup(jPanel4Layout.createSequentialGroup()

                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)

                            .addComponent(jLabel10)

                            .addComponent(cbPrioridade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)

                            .addComponent(jLabel11)

                            .addComponent(jLabel12)

                            .addComponent(txtResp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))

                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)

                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)

                            .addComponent(jLabel5)

                            .addComponent(jLabel6)

                            .addComponent(txtInicioEtapaPrev, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)

                            .addComponent(txtFimEtapaPrev, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))

                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)

                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)

                            .addComponent(jLabel7)

                            .addComponent(jLabel8)

                            .addComponent(txtInicioEtapaReal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)

                            .addComponent(txtFimEtapaReal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))

                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)

                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)

                            .addComponent(jLabel9)

                            .addComponent(cbStatusEtapa))

                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)

                            .addComponent(jLabel13)

                            .addComponent(jLabel14)

                            .addComponent(txtVrPrev, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)

                            .addComponent(txtVrReal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))

                    .addComponent(jScrollPane2))

                .addContainerGap())

        );



        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);

        jPanel1.setLayout(jPanel1Layout);

        jPanel1Layout.setHorizontalGroup(

            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

            .addGroup(jPanel1Layout.createSequentialGroup()

                .addContainerGap()

                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))

            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

        );

        jPanel1Layout.setVerticalGroup(

            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

            .addGroup(jPanel1Layout.createSequentialGroup()

                .addGap(3, 3, 3)

                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)

                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)

                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

                .addContainerGap())

        );



        tabelaEtapas.setModel(new javax.swing.table.DefaultTableModel(

            new Object [][] {



            },

            new String [] {

                "ID", "PROJETO", "ETAPA", "PRIORIDADE", "RESPONS\\u00c1VEL", "INÍCIO PREV", "FIM PREV", "INICIO REAL.", "FIM REAL.", "STATUS", "R$ Previsto", "R$ Realizado", "% Executado", "OBS."

            }

        ));

        jScrollPane1.setViewportView(tabelaEtapas);



        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);

        jPanel2.setLayout(jPanel2Layout);

        jPanel2Layout.setHorizontalGroup(

            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

            .addComponent(jScrollPane1)

        );

        jPanel2Layout.setVerticalGroup(

            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()

                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 371, javax.swing.GroupLayout.PREFERRED_SIZE)

                .addContainerGap())

        );



        jButtonCriar.setText("Criar");

        jButtonCriar.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {

                jButtonCriarActionPerformed(evt);

            }

        });



        jButtonLer.setText("Ler");

        jButtonLer.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {

                jButtonLerActionPerformed(evt);

            }

        });



        jButtonAtualizar.setText("Atualizar");

        jButtonAtualizar.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {

                jButtonAtualizarActionPerformed(evt);

            }

        });



        jButtonExcluir.setText("Excluir");

        jButtonExcluir.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {

                jButtonExcluirActionPerformed(evt);

            }

        });



        jButtonListar.setText("Listar");

        jButtonListar.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {

                jButtonListarActionPerformed(evt);

            }

        });



        jButtonFechar.setText("Fechar");

        jButtonFechar.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {

                jButtonFecharActionPerformed(evt);

            }

        });



        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);

        jPanel3.setLayout(jPanel3Layout);

        jPanel3Layout.setHorizontalGroup(

            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()

                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

                .addComponent(jButtonCriar)

                .addGap(18, 18, 18)

                .addComponent(jButtonLer)

                .addGap(18, 18, 18)

                .addComponent(jButtonAtualizar)

                .addGap(18, 18, 18)

                .addComponent(jButtonExcluir)

                .addGap(18, 18, 18)

                .addComponent(jButtonListar)

                .addGap(18, 18, 18)

                .addComponent(jButtonFechar)

                .addGap(167, 167, 167))

        );

        jPanel3Layout.setVerticalGroup(

            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

            .addGroup(jPanel3Layout.createSequentialGroup()

                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)

                    .addComponent(jButtonCriar)

                    .addComponent(jButtonLer)

                    .addComponent(jButtonAtualizar)

                    .addComponent(jButtonExcluir)

                    .addComponent(jButtonListar)

                    .addComponent(jButtonFechar))

                .addGap(0, 6, Short.MAX_VALUE))

        );



        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());

        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(

            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

        );

        layout.setVerticalGroup(

            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

            .addGroup(layout.createSequentialGroup()

                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)

                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)

                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)

                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)

                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)

                .addGap(0, 0, Short.MAX_VALUE))

        );



        pack();

        setLocationRelativeTo(null);

    }// </editor-fold>//GEN-END:initComponents



    private void jButtonCriarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCriarActionPerformed

        try {

            Etapa etapa = new Etapa();



            // Projeto

            String nomeProjeto = (String) cbProj.getSelectedItem();

            if (nomeProjeto == null || !projetoIdMap.containsKey(nomeProjeto)) {

                JOptionPane.showMessageDialog(this, "Selecione um projeto válido.", "Erro", JOptionPane.ERROR_MESSAGE);

                return;

            }

            etapa.setProjetoId(projetoIdMap.get(nomeProjeto));



            // Dados básicos

            etapa.setNome(txtEtapa.getText().trim());

            etapa.setPrioridade(cbPrioridade.getSelectedItem().toString());

            etapa.setResponsavel(txtResp.getText().trim());

            etapa.setStatus(cbStatusEtapa.getSelectedItem().toString());

            etapa.setObs(jTextAreaObs.getText());



            // Datas previstas (formato dd/MM/yyyy)

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            try {

                if (!txtInicioEtapaPrev.getText().trim().isEmpty()) {

                    etapa.setDataInicioPrevista(LocalDate.parse(txtInicioEtapaPrev.getText().trim(), formatter));

                }

                if (!txtFimEtapaPrev.getText().trim().isEmpty()) {

                    etapa.setDataFimPrevista(LocalDate.parse(txtFimEtapaPrev.getText().trim(), formatter));

                }

            } catch (DateTimeParseException ex) {

                JOptionPane.showMessageDialog(this, "Data inválida! Use o formato dd/MM/yyyy", "Erro de Data", JOptionPane.ERROR_MESSAGE);

                return;

            }



            // Datas realizadas (pode deixar vazio por enquanto)

            // (você pode adicionar campos depois se quiser)

            // === NOVO: VALORES FINANCEIROS

            BigDecimal vrPrev = parseMoeda(txtVrPrev.getText().trim());

            BigDecimal vrReal = parseMoeda(txtVrReal.getText().trim());



            if (vrPrev == null || vrReal == null) {

                JOptionPane.showMessageDialog(this, "Valor Previsto e Realizado devem ser números válidos (ex: 15000,50)", "Erro Financeiro", JOptionPane.ERROR_MESSAGE);

                return;

            }



            etapa.setValorPrevisto(vrPrev);

            etapa.setValorRealizado(vrReal);



            // Salva no banco

            etapaDAO.criar(etapa);

            JOptionPane.showMessageDialog(this, "Etapa criada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);



            limparCampos();

            carregarEtapas();



        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(this, "Erro ao salvar etapa: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);

            ex.printStackTrace();

        }

    }//GEN-LAST:event_jButtonCriarActionPerformed



    private void jButtonFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFecharActionPerformed

        try {

            Tela_Principal exibir;

            exibir = new Tela_Principal();

            exibir.setVisible(true);

            setVisible(false);

        } catch (IOException ex) {

            Logger.getLogger(Form_Etapas.class.getName()).log(Level.SEVERE, null, ex);

        }

    }//GEN-LAST:event_jButtonFecharActionPerformed



    private void txtEtapaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEtapaActionPerformed

        // TODO add your handling code here:

    }//GEN-LAST:event_txtEtapaActionPerformed



    private void txtFimEtapaPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFimEtapaPrevActionPerformed

        // TODO add your handling code here:

    }//GEN-LAST:event_txtFimEtapaPrevActionPerformed



    private void txtInicioEtapaPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtInicioEtapaPrevActionPerformed

        // TODO add your handling code here:

    }//GEN-LAST:event_txtInicioEtapaPrevActionPerformed



    private void jButtonLerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLerActionPerformed

        String idTexto = txtIDE.getText().trim();

        if (idTexto.isEmpty()) {

            JOptionPane.showMessageDialog(this, "Selecione uma etapa na tabela primeiro.", "Atenção", JOptionPane.WARNING_MESSAGE);

            return;

        }



        try {

            int id = Integer.parseInt(idTexto);

            Etapa etapa = etapaDAO.ler(id);

            if (etapa != null) {

                // preenche os campos (seu código atual está \\u00f3timo!)

                String nomeProjeto = projetoDAO.getNomeProjetoPorId(etapa.getProjetoId());

                cbProj.setSelectedItem(nomeProjeto);



                txtEtapa.setText(etapa.getNome());

                cbPrioridade.setSelectedItem(etapa.getPrioridade());

                txtResp.setText(etapa.getResponsavel());



                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                txtInicioEtapaPrev.setText(etapa.getDataInicioPrevista() != null ? etapa.getDataInicioPrevista().format(fmt) : "");

                txtFimEtapaPrev.setText(etapa.getDataFimPrevista() != null ? etapa.getDataFimPrevista().format(fmt) : "");

                txtInicioEtapaReal.setText(etapa.getDataInicioRealizada() != null ? etapa.getDataInicioRealizada().format(fmt) : "");

                txtFimEtapaReal.setText(etapa.getDataFimRealizada() != null ? etapa.getDataFimRealizada().format(fmt) : "");



                cbStatusEtapa.setSelectedItem(etapa.getStatus());

                jTextAreaObs.setText(etapa.getObs());



                // VALORES FINANCEIROS

                DecimalFormat df = new DecimalFormat("#,##0.00");

                txtVrPrev.setText(df.format(etapa.getValorPrevisto()));

                txtVrReal.setText(df.format(etapa.getValorRealizado()));



            } else {

                JOptionPane.showMessageDialog(this, "Etapa não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);

            }

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(this, "ID inválido.", "Erro", JOptionPane.ERROR_MESSAGE);

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(this, "Erro ao ler etapa: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);

        }

    }//GEN-LAST:event_jButtonLerActionPerformed



    private void jButtonAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAtualizarActionPerformed

        try {

            if (txtIDE.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(this, "Selecione uma etapa para atualizar.", "Atenção", JOptionPane.WARNING_MESSAGE);

                return;

            }



            Etapa etapa = new Etapa();

            etapa.setId(Integer.parseInt(txtIDE.getText().trim()));



            // Projeto

            String nomeProjeto = (String) cbProj.getSelectedItem();

            if (nomeProjeto == null || !projetoIdMap.containsKey(nomeProjeto)) {

                JOptionPane.showMessageDialog(this, "Selecione um projeto válido.", "Erro", JOptionPane.ERROR_MESSAGE);

                return;

            }

            etapa.setProjetoId(projetoIdMap.get(nomeProjeto));



            // Dados básicos

            etapa.setNome(txtEtapa.getText().trim());

            etapa.setPrioridade(cbPrioridade.getSelectedItem().toString());

            etapa.setResponsavel(txtResp.getText().trim());

            etapa.setStatus(cbStatusEtapa.getSelectedItem().toString());

            etapa.setObs(jTextAreaObs.getText());



            // Datas

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            try {

                etapa.setDataInicioPrevista(parseDateOrNull(txtInicioEtapaPrev.getText().trim(), formatter));

                etapa.setDataFimPrevista(parseDateOrNull(txtFimEtapaPrev.getText().trim(), formatter));

                etapa.setDataInicioRealizada(parseDateOrNull(txtInicioEtapaReal.getText().trim(), formatter));

                etapa.setDataFimRealizada(parseDateOrNull(txtFimEtapaReal.getText().trim(), formatter));

            } catch (DateTimeParseException ex) {

                JOptionPane.showMessageDialog(this, "Data inválida! Use dd/MM/yyyy", "Erro", JOptionPane.ERROR_MESSAGE);

                return;

            }



            // VALORES FINANCEIROS

            BigDecimal vrPrev = parseMoeda(txtVrPrev.getText().trim());

            BigDecimal vrReal = parseMoeda(txtVrReal.getText().trim());



            if (vrPrev == null || vrReal == null) {

                JOptionPane.showMessageDialog(this, "Valores financeiros inválidos!", "Erro", JOptionPane.ERROR_MESSAGE);

                return;

            }



            etapa.setValorPrevisto(vrPrev);

            etapa.setValorRealizado(vrReal);



            etapaDAO.atualizar(etapa);

            JOptionPane.showMessageDialog(this, "Etapa atualizada com sucesso!");

            carregarEtapas();



        } catch (SQLException | NumberFormatException ex) {

            JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + ex.getMessage());

        }

    }//GEN-LAST:event_jButtonAtualizarActionPerformed



    private void jButtonExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExcluirActionPerformed

        try {

            int id = Integer.parseInt(txtIDE.getText());

            Conexao conexao = new Conexao(); // Crie uma instância de Conexao

            EtapaDAO etapaDAO = new EtapaDAO(conexao); // Passe a instância de Conexao para o construtor

            etapaDAO.deletar(id);

            conexao.fecharConexao(); // Feche a conexão

            JOptionPane.showMessageDialog(this, "Etapa excluída com sucesso!");

            carregarEtapas();

        } catch (SQLException | NumberFormatException ex) {

            JOptionPane.showMessageDialog(this, "Erro ao excluir etapa: " + ex.getMessage());

        }

    }//GEN-LAST:event_jButtonExcluirActionPerformed



    private void jButtonListarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonListarActionPerformed

        //    carregarEtapas();

    }//GEN-LAST:event_jButtonListarActionPerformed



    private void cbPrioridadeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbPrioridadeActionPerformed

        // TODO add your handling code here:

    }//GEN-LAST:event_cbPrioridadeActionPerformed

    private void carregarProjetos() {

        cbProj.removeAllItems(); // Limpa o JComboBox antes de carregar

        projetoIdMap.clear(); // Limpa o mapa

        try {

            List<Projeto> projetos = projetoDAO.listarTodos(); // Método que lista todos os projetos

            for (Projeto projeto : projetos) {

                cbProj.addItem(projeto.getNome()); // Adiciona apenas o nome (String)

                projetoIdMap.put(projeto.getNome(), projeto.getId()); // Associa o nome ao ID

            }

            if (!projetos.isEmpty()) {

                cbProj.setSelectedIndex(0); // Seleciona o primeiro projeto por padrão

            }

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(this, "Erro ao carregar projetos: " + ex.getMessage());

        }

    }



    private void limparCampos() {

        txtIDE.setText(""); // Limpa o campo ID

        cbProj.setSelectedIndex(-1); // Desseleciona o projeto no JComboBox

        txtEtapa.setText(""); // Limpa o nome da etapa

        cbPrioridade.setSelectedIndex(0); // Seleciona o primeiro item ou -1 se preferir nenhum

        txtResp.setText(""); // Limpa o responsável

        txtInicioEtapaPrev.setText(""); // Limpa a data de início prevista

        txtFimEtapaPrev.setText(""); // Limpa a data de fim prevista

        txtInicioEtapaReal.setText(""); // Limpa a data de início realizada

        txtFimEtapaReal.setText(""); // Limpa a data de fim realizada

        cbStatusEtapa.setSelectedIndex(0); // Seleciona o primeiro item ou -1 se preferir nenhum

        jTextAreaObs.setText(""); // Limpa as observações

        // NOVO: limpa os campos financeiros

        txtVrPrev.setText("");

        txtVrReal.setText("");

    }



    // Converte String com moeda (10.500,75 ou 10500.50) ? BigDecimal

    private BigDecimal parseMoeda(String texto) {

        if (texto == null || texto.trim().isEmpty()) {

            return BigDecimal.ZERO;

        }

        try {

            String limpo = texto.replace(".", "").replace(",", ".");

            return new BigDecimal(limpo);

        } catch (NumberFormatException e) {

            return null; // sinaliza erro

        }

    }



    // Converte String de data ou retorna null se vazia

    private LocalDate parseDateOrNull(String texto, DateTimeFormatter fmt) {

        if (texto == null || texto.trim().isEmpty()) {

            return null;

        }

        try {

            return LocalDate.parse(texto.trim(), fmt);

        } catch (DateTimeParseException e) {

            throw e; // vai ser tratado no catch do botão

        }

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

            java.util.logging.Logger.getLogger(Form_Etapas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {

            java.util.logging.Logger.getLogger(Form_Etapas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {

            java.util.logging.Logger.getLogger(Form_Etapas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {

            java.util.logging.Logger.getLogger(Form_Etapas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

        }

        //</editor-fold>



        /* Create and display the form */

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                new Form_Etapas().setVisible(true);

            }

        });

    }



    // Variables declaration - do not modify//GEN-BEGIN:variables

    private javax.swing.JComboBox<String> cbPrioridade;

    private javax.swing.JComboBox<String> cbProj;

    private javax.swing.JComboBox<String> cbStatusEtapa;

    private javax.swing.JButton jButtonAtualizar;

    private javax.swing.JButton jButtonCriar;

    private javax.swing.JButton jButtonExcluir;

    private javax.swing.JButton jButtonFechar;

    private javax.swing.JButton jButtonLer;

    private javax.swing.JButton jButtonListar;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel10;

    private javax.swing.JLabel jLabel11;

    private javax.swing.JLabel jLabel12;

    private javax.swing.JLabel jLabel13;

    private javax.swing.JLabel jLabel14;

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

    private javax.swing.JPanel jPanel3;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JScrollPane jScrollPane2;

    private javax.swing.JTextArea jTextAreaObs;

    private javax.swing.JTable tabelaEtapas;

    private javax.swing.JTextField txtEtapa;

    private javax.swing.JTextField txtFimEtapaPrev;

    private javax.swing.JTextField txtFimEtapaReal;

    private javax.swing.JTextField txtIDE;

    private javax.swing.JTextField txtInicioEtapaPrev;

    private javax.swing.JTextField txtInicioEtapaReal;

    private javax.swing.JTextField txtResp;

    private javax.swing.JTextField txtVrPrev;

    private javax.swing.JTextField txtVrReal;

    // End of variables declaration//GEN-END:variables

}

