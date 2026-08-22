package view;

import utilitarios.Conexao;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.text.MaskFormatter;

public class Tela_de_Mov extends javax.swing.JFrame {

    CarregarCbx re = new CarregarCbx(); //Carrega combobox de Favorecidos
    CarregarCbx2 r2 = new CarregarCbx2(); //Carrega combobox de Recursos
    CarregarCbx3 r3 = new CarregarCbx3(); //Carrega combobox de Classificação por Conta
    MaskFormatter mfData;

    public Tela_de_Mov() throws SQLException {
        try {
            mfData = new MaskFormatter("##/##/####");
        } catch (ParseException ex) {
            System.out.println("Ocorreu um erro ao criar máscara para Apresentação");
        }
        initComponents();
        //Image icon = new ImageIcon(this.getClass().getResource("/IHome.jpg")).getImage();
        //this.setIconImage(icon);
        // Só desabilita o X, mantém a barra normal
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        re.CarregarCbx("tbclifor", "apelidoCliFor", jComboBoxFavorecidos);
        r2.CarregarCbx2("tbrecursos", "nomebco", jComboBoxRecurso);
        r3.CarregarCbx3("gpprincipal", "nome_C", cbxClass);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        //c.add(Calendar.DATE, -60);
        String data = dateFormat.format(c.getTime());
        tfLancto.setText(data);

    }
    public static String textoidMov = "";
    public static String textoLancto = "";
    public static String textoEmissao = "";
    public static String textoVencimento = "";
    public static String textoApresentacao = "";
    public static String textoDocumento = "";
    public static String textoDescricao = "";
    public static String textoOrigem = "";
    public static String textoDestino = "";
    public static String textoVrPg = "";
    public static String textoFav = "";
    public static String textoNrFav = "";
    public static String textoClass = "";
    public static String textoNrRecurso = "";
    public static String textoRecurso = "";

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem2 = new javax.swing.JMenuItem();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        tfRegistro = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        btSalvar = new javax.swing.JButton();
        btnAtualizar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        btnFechar = new javax.swing.JButton();
        btnListarDados = new javax.swing.JButton();
        jButtonReceb = new javax.swing.JButton();
        jButtonPag = new javax.swing.JButton();
        jButtonLimpar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        btAbrir = new javax.swing.JButton();
        tfBusca = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbDadosMov = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        cbxClass = new javax.swing.JComboBox<>();
        tfDescricao = new javax.swing.JTextField();
        tfClass = new javax.swing.JTextField();
        jCbxTipo = new javax.swing.JComboBox<>();
        jComboBoxRecurso = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        tfRecurso = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jComboBoxFavorecidos = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        tfDocumento = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        tfValor = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        tfStatusMov = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        tfPrev = new javax.swing.JTextField();
        tfFavorecidos = new javax.swing.JTextField();
        tfNrRecurso = new javax.swing.JTextField();
        tfNrFav = new javax.swing.JTextField();
        tfEmissao = new javax.swing.JFormattedTextField();
        tfVencimento = new javax.swing.JFormattedTextField();
        tfApresentacao = new javax.swing.JFormattedTextField(mfData);
        jLabel11 = new javax.swing.JLabel();
        tfLancto = new javax.swing.JFormattedTextField(mfData);
        jLabel1 = new javax.swing.JLabel();
        tfSaldoAnterior = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();

        jMenuItem2.setText("jMenuItem2");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("       Movimentação Financeira");
        setResizable(false);

        jLabel2.setText("Registro");

        jLabel3.setText("Recurso");

        jLabel4.setText("Classificação");

        tfRegistro.setEnabled(false);

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

        btnListarDados.setText("Listar Dados");
        btnListarDados.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnListarDados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnListarDadosActionPerformed(evt);
            }
        });

        jButtonReceb.setBackground(new java.awt.Color(153, 255, 51));
        jButtonReceb.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButtonReceb.setForeground(new java.awt.Color(51, 51, 255));
        jButtonReceb.setText("Recebimentos");
        jButtonReceb.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonReceb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRecebActionPerformed(evt);
            }
        });

        jButtonPag.setBackground(new java.awt.Color(255, 51, 0));
        jButtonPag.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jButtonPag.setForeground(new java.awt.Color(255, 255, 51));
        jButtonPag.setText("Pagamentos");
        jButtonPag.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonPag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPagActionPerformed(evt);
            }
        });

        jButtonLimpar.setText("Limpar");
        jButtonLimpar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLimparActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(147, 147, 147)
                .addComponent(btSalvar)
                .addGap(18, 18, 18)
                .addComponent(btnAtualizar)
                .addGap(18, 18, 18)
                .addComponent(btnExcluir)
                .addGap(18, 18, 18)
                .addComponent(btnListarDados)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonLimpar)
                .addGap(20, 20, 20)
                .addComponent(jButtonReceb, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonPag, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
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
                    .addComponent(btnFechar)
                    .addComponent(btnListarDados)
                    .addComponent(jButtonReceb)
                    .addComponent(jButtonPag)
                    .addComponent(jButtonLimpar))
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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btAbrir, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tfBusca, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btAbrir)
                .addComponent(tfBusca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        tbDadosMov.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Reg.", "Recurso", "vRecruso", "Favorecido", "vFavorecido", "Lancto.", "Emissão", "Vencimento", "Documento", "Classif", "Descrição", "Valor", "Apresentação", "Status", "Prev", "Saldo"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbDadosMov.setSelectionBackground(new java.awt.Color(255, 255, 204));
        tbDadosMov.setSelectionForeground(new java.awt.Color(51, 51, 51));
        tbDadosMov.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDadosMovMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tbDadosMov);

        jScrollPane1.setViewportView(jScrollPane2);

        jLabel6.setText("Emissão");

        jLabel7.setText("Descrição");

        cbxClass.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione" }));
        cbxClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxClassActionPerformed(evt);
            }
        });
        cbxClass.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cbxClassKeyPressed(evt);
            }
        });

        tfDescricao.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tfDescricaoKeyPressed(evt);
            }
        });

        tfClass.setEnabled(false);

        jCbxTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione (\"E\"ntrada - \"S\"aída ou \"N\"ulo)", "E", "S", "N" }));

        jComboBoxRecurso.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione" }));
        jComboBoxRecurso.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jComboBoxRecursoMouseClicked(evt);
            }
        });
        jComboBoxRecurso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxRecursoActionPerformed(evt);
            }
        });
        jComboBoxRecurso.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jComboBoxRecursoKeyPressed(evt);
            }
        });

        jLabel12.setText("Favorecido");

        tfRecurso.setEnabled(false);
        tfRecurso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfRecursoActionPerformed(evt);
            }
        });

        jLabel8.setText("Vencimento");

        jComboBoxFavorecidos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione" }));
        jComboBoxFavorecidos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxFavorecidosActionPerformed(evt);
            }
        });
        jComboBoxFavorecidos.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jComboBoxFavorecidosKeyPressed(evt);
            }
        });

        jLabel9.setText("Apresentação");

        jLabel10.setText("Documento");

        tfDocumento.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tfDocumentoKeyPressed(evt);
            }
        });

        jLabel13.setText("Valor");

        tfValor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tfValorKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfValorKeyTyped(evt);
            }
        });

        jLabel14.setText("Status");

        tfStatusMov.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfStatusMovFocusLost(evt);
            }
        });
        tfStatusMov.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfStatusMovActionPerformed(evt);
            }
        });
        tfStatusMov.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tfStatusMovKeyPressed(evt);
            }
        });

        jLabel15.setText("Previsto");

        tfPrev.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfPrevFocusLost(evt);
            }
        });
        tfPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfPrevActionPerformed(evt);
            }
        });

        tfFavorecidos.setEnabled(false);

        tfNrRecurso.setEnabled(false);
        tfNrRecurso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfNrRecursoActionPerformed(evt);
            }
        });

        tfNrFav.setEnabled(false);

        try {
            tfEmissao.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##/##/####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        tfEmissao.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tfEmissaoKeyPressed(evt);
            }
        });

        try {
            tfVencimento.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##/##/####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        tfVencimento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfVencimentoActionPerformed(evt);
            }
        });
        tfVencimento.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tfVencimentoKeyPressed(evt);
            }
        });

        try {
            tfApresentacao.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##/##/####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        tfApresentacao.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfApresentacaoFocusLost(evt);
            }
        });
        tfApresentacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfApresentacaoActionPerformed(evt);
            }
        });
        tfApresentacao.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tfApresentacaoKeyPressed(evt);
            }
        });

        jLabel11.setText("Lançamento");

        try {
            tfLancto.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##/##/####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        tfLancto.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfLanctoFocusLost(evt);
            }
        });
        tfLancto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfLanctoActionPerformed(evt);
            }
        });
        tfLancto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tfLanctoKeyPressed(evt);
            }
        });

        jLabel1.setText("Saldo Anterior");

        tfSaldoAnterior.setEnabled(false);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("M O V I M E N T A Ç Ã O");
        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tfRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCbxTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jComboBoxRecurso, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(tfNrRecurso, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfRecurso, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(24, 24, 24)
                                .addComponent(jLabel12)
                                .addGap(10, 10, 10)
                                .addComponent(jComboBoxFavorecidos, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfNrFav, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfFavorecidos, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbxClass, javax.swing.GroupLayout.PREFERRED_SIZE, 671, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(tfClass, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfStatusMov, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tfSaldoAnterior)
                        .addGap(10, 10, 10))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(25, 25, 25)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tfLancto, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(48, 48, 48)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfEmissao, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfApresentacao, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfValor, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(tfDescricao))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(tfRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jCbxTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBoxRecurso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(tfRecurso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxFavorecidos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfFavorecidos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfNrRecurso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfNrFav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(tfValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel6)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(tfEmissao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfApresentacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(tfLancto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(tfClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(tfStatusMov, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(tfPrev, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(cbxClass, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfSaldoAnterior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    @SuppressWarnings("deprecation")
    private void btSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSalvarActionPerformed
        //Compara se o tipo é Cartões "2.001.003"
        String Comparar = tfRecurso.getText();
        if (Comparar.equals("2.001.003")) {
            //Se o tipo é i­gual a Cartões
            try {
                //Lança à Pagar
                Connection con;
                con = Conexao.faz_conexao();
                String sql = "INSERT INTO tbmovimento(recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,valor,dtApr,statusMov,Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, "0079");
                    //stmt.setString(2, tfRecurso.getText());
                    stmt.setString(2, "2.001.003");
                    stmt.setString(3, tfNrFav.getText());
                    stmt.setString(4, tfFavorecidos.getText());
                    java.util.Date data_l = formato_data.parse(tfLancto.getText());
                    stmt.setDate(5, new java.sql.Date(data_l.getTime()));
                    java.util.Date data_e = formato_data.parse(tfEmissao.getText());
                    stmt.setDate(6, new java.sql.Date(data_e.getTime()));
                    java.util.Date data_v = formato_data.parse(tfVencimento.getText());
                    stmt.setDate(7, new java.sql.Date(data_v.getTime()));
                    stmt.setString(8, tfDocumento.getText());
                    stmt.setString(9, tfClass.getText());
                    stmt.setString(10, tfDescricao.getText());
                    String t = "S";
                    if (jCbxTipo.getSelectedItem().equals(t)) {
                        tfValor.setText("-" + tfValor.getText());
                    }

                    // Converte o valor para um número, aplicando o sinal de negativo, se necessário
                    //double valor = Double.parseDouble(tfValor.getText().replace(",", "."));
                    // Formata o valor para ter duas casas decimais
                    //DecimalFormat df = new DecimalFormat("###,##0.00");
                    //String valorFormatado = df.format(valor);
                    // Atualiza o campo de texto com o valor formatado
                    //tfValor.setText(valorFormatado);
                    // Define o valor formatado como um double na instrução SQL
                    //stmt.setDouble(11, Double.parseDouble(tfValor.getText().replace(",", ".")));
                    // Remover todos os pontos, exceto o decimal
                    String valorTexto = tfValor.getText().replaceAll("\\.", "");
                    // Substituir a vírgula decimal por ponto decimal
                    valorTexto = valorTexto.replaceAll(",", ".");
                    // Parse para Double
                    stmt.setDouble(11, Double.parseDouble(valorTexto));

                    //stmt.setDouble(11, Double.parseDouble(tfValor.getText().replaceAll(",", ".")));
                    if (tfApresentacao == null) {
                        stmt.setDate(12, null);
                    } else if (tfApresentacao.getText().equals("  /  /    ")) {
                        stmt.setDate(12, null);
                    } else if (tfApresentacao.getText().isEmpty()) {
                        stmt.setDate(12, null);
                    } else {
                        java.util.Date data_a = formato_data.parse(tfApresentacao.getText());
                        stmt.setDate(12, new java.sql.Date(data_a.getTime()));
                    }
                    stmt.setString(13, "PG");
                    stmt.setString(14, "V");
                    stmt.execute();
                    con.close();
                } catch (ParseException ex) {
                    Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
                }
                //---------------------------------------------------------------------------------------------------
                //---------------------------------------------------------------------------------------------------

                //Lança o Pagamento
                Connection con2;
                con2 = Conexao.faz_conexao();
                String sql2 = "INSERT INTO tbmovimento(recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,valor,dtApr,statusMov,Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                SimpleDateFormat formato_data2 = new SimpleDateFormat("dd/MM/yyyy");
                try (PreparedStatement stmt2 = con2.prepareStatement(sql2)) {
                    stmt2.setString(1, "0079");
                    //stmt2.setString(2, tfRecurso.getText());
                    stmt2.setString(2, "2.001.003");
                    stmt2.setString(3, tfNrFav.getText());
                    stmt2.setString(4, tfFavorecidos.getText());
                    java.util.Date data_l2 = formato_data2.parse(tfLancto.getText());
                    stmt2.setDate(5, new java.sql.Date(data_l2.getTime()));
                    java.util.Date data_e2 = formato_data2.parse(tfEmissao.getText());
                    stmt2.setDate(6, new java.sql.Date(data_e2.getTime()));
                    java.util.Date data_v2 = formato_data2.parse(tfVencimento.getText());
                    stmt2.setDate(7, new java.sql.Date(data_v2.getTime()));
                    stmt2.setString(8, tfDocumento.getText());
                    //stmt2.setString(9, tfClass.getText());
                    stmt2.setString(9, "9.001.003");
                    stmt2.setString(10, tfDescricao.getText());
                    //String t2 = "S";
                    //String TempVr;
                    //TempVr = tfValor.getText().substring(1).replaceAll(",", ".");
                    //stmt2.setDouble(11, Double.parseDouble(TempVr));
                    //String TempVr = tfValor.getText().substring(1).replace(",", ".");
                    //double valor = Double.parseDouble(TempVr);

                    // Formata o valor para garantir que tenha duas casas decimais
                    //DecimalFormat df = new DecimalFormat("###,##0.00");
                    //String valorFormatado = df.format(valor);
                    // Remover todos os pontos, exceto o decimal
                    String valorTexto = tfValor.getText().substring(1).replaceAll("\\.", "");
                    // Substituir a vírgula decimal por ponto decimal
                    valorTexto = valorTexto.replaceAll(",", ".");
                    // Parse para Double
                    stmt2.setDouble(11, Double.parseDouble(valorTexto));
                    // Converte o valor formatado de volta para double, substituindo a vírgula por ponto
                    //stmt2.setDouble(11, Double.parseDouble(valorFormatado.replace(",", ".")));

                    if (tfApresentacao == null) {
                        stmt2.setDate(12, null);
                    } else if (tfApresentacao.getText().equals("  /  /    ")) {
                        stmt2.setDate(12, null);
                    } else if (tfApresentacao.getText().isEmpty()) {
                        stmt2.setDate(12, null);
                    } else {
                        java.util.Date data_a = formato_data.parse(tfApresentacao.getText());
                        stmt2.setDate(12, new java.sql.Date(data_a.getTime()));
                    }
                    stmt2.setString(13, "PG");
                    stmt2.setString(14, "V");
                    stmt2.execute();
                    con2.close();
                } catch (ParseException ex) {
                    Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
                }
                //---------------------------------------------------------------------------------------------------
                //---------------------------------------------------------------------------------------------------

                //Lança a saída pelo Cartão de Crédito
                Connection con3;
                con3 = Conexao.faz_conexao();
                String sql3 = "INSERT INTO tbmovimento(recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,valor,dtApr,statusMov,Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                SimpleDateFormat formato_data3 = new SimpleDateFormat("dd/MM/yyyy");
                try (PreparedStatement stmt3 = con3.prepareStatement(sql3)) {
                    stmt3.setString(1, tfNrRecurso.getText());
                    //stmt3.setString(2, tfRecurso.getText());
                    stmt3.setString(2, "2.001.003");
                    stmt3.setString(3, tfNrFav.getText());
                    stmt3.setString(4, tfFavorecidos.getText());
                    java.util.Date data_l3 = formato_data3.parse(tfLancto.getText());
                    stmt3.setDate(5, new java.sql.Date(data_l3.getTime()));
                    java.util.Date data_e3 = formato_data3.parse(tfEmissao.getText());
                    stmt3.setDate(6, new java.sql.Date(data_e3.getTime()));
                    java.util.Date data_v3 = formato_data3.parse(tfVencimento.getText());
                    stmt3.setDate(7, new java.sql.Date(data_v3.getTime()));
                    stmt3.setString(8, tfDocumento.getText());
                    //stmt3.setString(9, tfClass.getText());
                    stmt3.setString(9, "9.001.003");
                    stmt3.setString(10, tfDescricao.getText());
                    //String t3 = "S";
                    //stmt3.setDouble(11, Double.parseDouble(tfValor.getText().replaceAll(",", ".")));

                    // Converte o valor para um número, aplicando o sinal de negativo, se necessário
                    //double valor = Double.parseDouble(tfValor.getText().replace(",", "."));
                    // Formata o valor para ter duas casas decimais
                    //DecimalFormat df = new DecimalFormat("###,##0.00");
                    //String valorFormatado = df.format(valor);
                    // Atualiza o campo de texto com o valor formatado
                    //tfValor.setText(valorFormatado);
                    // Remover todos os pontos, exceto o decimal
                    //String valorTexto = tfValor.getText().replaceAll("\\.", "");
                    // Substituir a vírgula decimal por ponto decimal
                    //valorTexto = valorTexto.replaceAll(",", ".");
                    // Parse para Double
                    //stmt3.setDouble(11, Double.parseDouble("-" + valorTexto));
                    //Remover todos os pontos, exceto o decimal
                    String valorTexto = tfValor.getText().replaceAll("\\.", "");
                    // Substituir a vírgula decimal por ponto decimal
                    valorTexto = valorTexto.replaceAll(",", ".");
                    // Parse para Double
                    stmt3.setDouble(11, Double.parseDouble(valorTexto));
                    // Define o valor formatado como um double na instrução SQL
                    //stmt3.setDouble(11, Double.parseDouble(tfValor.getText().replace(",", ".")));
                    stmt3.setDate(12, null);
                    stmt3.setString(13, "");
                    stmt3.setString(14, "V");
                    stmt3.execute();
                    con3.close();
                } catch (ParseException ex) {
                    Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
                }
                //---------------------------------------------------------------------------------------------------
                new Tela_de_Mov().show();
                dispose();
                ListaDados();
                JOptionPane.showMessageDialog(null, "Cartão de Crédito lançado com sucesso!");
                //Limpar dados dos campos
                tfRegistro.setText("");
                //tfNrRecurso.setText("");
                //tfRecurso.setText("");
                tfNrFav.setText("");
                tfFavorecidos.setText("");
                tfLancto.setText("");
                tfEmissao.setText("");
                tfApresentacao.setText("");
                tfDocumento.setText("");
                tfClass.setText("");
                tfDescricao.setText("");
                tfValor.setText("");
                tfApresentacao.setText("");
                tfStatusMov.setText("");
                tfPrev.setText("");
                //jCbxTipo.setSelectedIndex(0);
                cbxClass.setSelectedIndex(0);
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            //Faz um lançamento Simples  
            try {
                Connection con;
                con = Conexao.faz_conexao();
                String sql = "INSERT INTO tbmovimento(recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,valor,dtApr,statusMov,Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, tfNrRecurso.getText());
                    stmt.setString(2, tfRecurso.getText());
                    stmt.setString(3, tfNrFav.getText());
                    stmt.setString(4, tfFavorecidos.getText());
                    java.util.Date data_l = formato_data.parse(tfLancto.getText());
                    stmt.setDate(5, new java.sql.Date(data_l.getTime()));
                    java.util.Date data_e = formato_data.parse(tfEmissao.getText());
                    stmt.setDate(6, new java.sql.Date(data_e.getTime()));
                    java.util.Date data_v = formato_data.parse(tfVencimento.getText());
                    stmt.setDate(7, new java.sql.Date(data_v.getTime()));
                    stmt.setString(8, tfDocumento.getText());
                    stmt.setString(9, tfClass.getText());
                    stmt.setString(10, tfDescricao.getText());
                    String t = "S";
                    if (jCbxTipo.getSelectedItem().equals(t)) {
                        tfValor.setText("-" + tfValor.getText());
                    }
                    // Remover todos os pontos, exceto o decimal
                    String valorTexto = tfValor.getText().replaceAll("\\.", "");
                    // Substituir a vírgula decimal por ponto decimal
                    valorTexto = valorTexto.replaceAll(",", ".");
                    // Parse para Double
                    stmt.setDouble(11, Double.parseDouble(valorTexto));
                    // Converte o valor para um número, aplicando o sinal de negativo, se necessário
                    //double valor = Double.parseDouble(tfValor.getText().replace(",", "."));

                    // Formata o valor para ter duas casas decimais
                    //DecimalFormat df = new DecimalFormat("###,##0.00");
                    //String valorFormatado = df.format(valor);
                    // Atualiza o campo de texto com o valor formatado
                    //tfValor.setText(valorFormatado);
                    // Define o valor formatado como um double na instrução SQL
                    //stmt.setDouble(11, Double.parseDouble(tfValor.getText().replace(",", ".")));
                    //stmt.setDouble(11, Double.parseDouble(tfValor.getText().replaceAll(",", ".")));
                    if (tfApresentacao == null) {
                        stmt.setDate(12, null);
                    } else if (tfApresentacao.getText().equals("  /  /    ")) {
                        stmt.setDate(12, null);
                    } else if (tfApresentacao.getText().isEmpty()) {
                        stmt.setDate(12, null);
                    } else {
                        java.util.Date data_a = formato_data.parse(tfApresentacao.getText());
                        stmt.setDate(12, new java.sql.Date(data_a.getTime()));
                    }
                    stmt.setString(13, tfStatusMov.getText());
                    stmt.setString(14, tfPrev.getText());
                    stmt.execute();
                    new Tela_de_Mov().show();
                    dispose();
                    //ListaDados();
                    JRefresh();
                } catch (ParseException ex) {
                    Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
                }
                con.close();
                JOptionPane.showMessageDialog(null, "Movimentação lançada com sucesso!");
                //Limpar dados dos campos
                tfRegistro.setText("");
                //tfNrRecurso.setText("");
                //tfRecurso.setText("");
                tfNrFav.setText("");
                tfFavorecidos.setText("");
                tfLancto.setText("");
                tfEmissao.setText("");
                tfApresentacao.setText("");
                tfDocumento.setText("");
                tfClass.setText("");
                tfDescricao.setText("");
                tfValor.setText("");
                tfApresentacao.setText("");
                tfStatusMov.setText("");
                tfPrev.setText("");
                //jCbxTipo.setSelectedIndex(0);
                cbxClass.setSelectedIndex(0);
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btSalvarActionPerformed

    private void btAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAbrirActionPerformed
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (tfBusca.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Informe um código válido...");
        } else {
            try ( // CÃ³digo para pesquisar um usuario
                    Connection con = Conexao.faz_conexao()) {
                String sql = "SELECT * FROM tbmovimento WHERE idMov=?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, tfBusca.getText());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        tfRegistro.setText(rs.getString("idMov"));
                        tfNrRecurso.setText(rs.getString("recurso"));
                        tfRecurso.setText(rs.getString("vrecurso"));
                        tfNrFav.setText(rs.getString("clifor"));
                        tfFavorecidos.setText(rs.getString("vCliFor"));
                        tfLancto.setText(sdf.format(rs.getDate("dtlancto")));
                        tfEmissao.setText(sdf.format(rs.getDate("dtEmi")));
                        tfVencimento.setText(sdf.format(rs.getDate("dtVcto")));
                        tfDocumento.setText(rs.getString("documento"));
                        tfClass.setText(rs.getString("classif"));
                        tfDescricao.setText(rs.getString("Descr"));
                        tfValor.setText(rs.getString(String.valueOf("Valor")));
                        Double ver = 0.00;
                        ver = Double.valueOf(tfValor.getText().replaceAll(",", "."));
                        if (ver > 0) {
                            jButtonPag.setEnabled(false);
                        } else {
                            jButtonReceb.setEnabled(false);
                        }
                        if (rs.getDate("dtApr") == null) {
                            tfApresentacao.setText(null);
                        } else {
                            tfApresentacao.setText(sdf.format(rs.getDate("dtApr")));
                        }
                        tfStatusMov.setText(rs.getString("statusMov"));
                        tfPrev.setText(rs.getString("Prev"));
                        btSalvar.setEnabled(false);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_Mov.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btAbrirActionPerformed

    private void btnListarDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnListarDadosActionPerformed
        ListaDados();
    }//GEN-LAST:event_btnListarDadosActionPerformed

    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarActionPerformed
        String t = "S";
        jCbxTipo.requestFocus();
        if (jCbxTipo.getSelectedItem().equals(t)) {
            // Verifica se o valor é positivo e precisa ser negativado
            if (Double.parseDouble(tfValor.getText().replaceAll(",", ".")) > 0) {
                tfValor.setText("-" + tfValor.getText());
            }
        } else if (tfRegistro.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Informe o Código");
        } else {
            try (Connection con = Conexao.faz_conexao()) {
                SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");
                String sql = "UPDATE tbmovimento SET recurso=?, vrecurso=?, clifor=?, vclifor=?, dtlancto=?, dtEmi=?, dtVcto=?, documento=?, classif=?, Descr=?, valor=?, dtApr=?, statusMov=?, Prev=? WHERE idMov=?";
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, tfNrRecurso.getText());
                    stmt.setString(2, tfRecurso.getText());
                    stmt.setString(3, tfNrFav.getText());
                    stmt.setString(4, tfFavorecidos.getText());
                    java.util.Date data_l = formato_data.parse(tfLancto.getText());
                    stmt.setDate(5, new java.sql.Date(data_l.getTime()));
                    java.util.Date data_e = formato_data.parse(tfEmissao.getText());
                    stmt.setDate(6, new java.sql.Date(data_e.getTime()));
                    java.util.Date data_v = formato_data.parse(tfVencimento.getText());
                    stmt.setDate(7, new java.sql.Date(data_v.getTime()));
                    stmt.setString(8, tfDocumento.getText());
                    stmt.setString(9, tfClass.getText());
                    stmt.setString(10, tfDescricao.getText());

                    // Remover o separador de milhar e substituir a vírgula decimal por ponto
                    String valorSemMilhar = tfValor.getText().replace(".", "").replace(",", ".");
                    double valor = Double.parseDouble(valorSemMilhar);

                    // Define o valor no campo de texto formatado para exibição
                    DecimalFormat df = new DecimalFormat("###,##0.00");
                    String valorFormatado = df.format(valor);
                    tfValor.setText(valorFormatado);

                    // Define o valor não formatado como um double na instrução SQL
                    if (Double.parseDouble(tfValor.getText().replace(".", "").replace(",", ".")) > 0) {
                        stmt.setDouble(11, valor);
                    } else {
                        valor = Double.parseDouble(tfValor.getText().replace(".", "").replace(",", "."));
                        stmt.setDouble(11, valor);
                    }
                    if (tfApresentacao.getText().isEmpty()) {
                        stmt.setDate(12, null);
                    } else if (tfApresentacao.getText().equals("  /  /    ")) {
                        tfApresentacao.setValue(null);
                        stmt.setDate(12, null);
                    } else {
                        java.util.Date data_a = formato_data.parse(tfApresentacao.getText());
                        stmt.setDate(12, new java.sql.Date(data_a.getTime()));
                    }
                    stmt.setString(13, tfStatusMov.getText());
                    stmt.setString(14, tfPrev.getText());
                    stmt.setString(15, tfRegistro.getText());
                    stmt.execute();
                    DefaultTableModel modelo = (DefaultTableModel) tbDadosMov.getModel();
                    modelo.setRowCount(0);
                }
            } catch (SQLException | ParseException ex) {
                Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
            }
            JOptionPane.showMessageDialog(null, "Movimento atualizado com sucesso!");
            btSalvar.setEnabled(true);
            tfRegistro.setText("");
            tfNrFav.setText("");
            tfFavorecidos.setText("");
            tfEmissao.setText("");
            tfVencimento.setText("");
            tfDocumento.setText("");
            tfClass.setText("");
            tfDescricao.setText("");
            tfValor.setText("");
            tfApresentacao.setText("");
            tfStatusMov.setText("");
            tfPrev.setText("");
            cbxClass.setSelectedIndex(0);
            tfBusca.setText("");
            JRefresh();
        }
    }//GEN-LAST:event_btnAtualizarActionPerformed

    @SuppressWarnings("deprecation")
    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed
//        Object[] options = {"Sim", "Não"};
//        int i = JOptionPane.showOptionDialog(null, "Deseja Excluir o Registro?", "Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
//        if (i == JOptionPane.YES_OPTION) { //opçao SIM selecionada !!}
//            //if (JOptionPane.showConfirmDialog(null, "Deseja Excluir o Registro?", "Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
//            if (tfRegistro.getText().equals("")) {
//                JOptionPane.showMessageDialog(null, "Informe o registro do movimento à ser excluí­do.");
//            } else {
//                try {
//                    try ( // TODO add your handling code here:
//                            Connection con = Conexao.faz_conexao()) {
//                        String sql = "DELETE FROM tbmovimento WHERE idMov=?";
//                        PreparedStatement stmt = con.prepareStatement(sql);
//                        stmt.setString(1, tfRegistro.getText());
//                        stmt.execute();
//                        stmt.close();
//                    }
//                    JOptionPane.showMessageDialog(null, "Movimento excluído com sucesso!");
//                    // Limpando os dados
//                    tfRegistro.setText("");
//                    //tfNrRecurso.setText("");
//                    //tfRecurso.setText("");
//                    tfNrFav.setText("");
//                    tfFavorecidos.setText("");
//                    tfLancto.setText("");
//                    tfEmissao.setText("");
//                    tfApresentacao.setText("");
//                    tfDocumento.setText("");
//                    tfClass.setText("");
//                    tfDescricao.setText("");
//                    tfValor.setText("");
//                    tfApresentacao.setText("");
//                    tfStatusMov.setText("");
//                    tfPrev.setText("");
//                    //jCbxTipo.setSelectedIndex(0);
//                    cbxClass.setSelectedIndex(0);
//                    tfBusca.setText("");
//                    //new Tela_de_Mov().show();
//                    //ListaDados();
//                    JRefresh();
//                } catch (SQLException ex) {
//                    Logger.getLogger(Tela_de_Mov.class
//                            .getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
        int[] rows = tbDadosMov.getSelectedRows();

        int rowCount = rows.length;

        Object[] options = {"Sim", "Não"};

        String message;

        if (rowCount > 1) {

            message = "Deseja excluir os " + rowCount + " registros selecionados?";

        } else if (rowCount == 1 || !tfRegistro.getText().equals("")) {

            message = "Deseja Excluir o Registro?";

        } else {

            JOptionPane.showMessageDialog(null, "Selecione ao menos um registro para excluir.");

            return;

        }

        int i = JOptionPane.showOptionDialog(null, message, "Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (i == JOptionPane.YES_OPTION) {

            try {

                Connection con = Conexao.faz_conexao();
                //conexao.abrirConexao();

                //Connection con = conexao.getConexao();
                String sql = "DELETE FROM tbmovimento WHERE idMov=?";

                PreparedStatement stmt = con.prepareStatement(sql);

                if (rowCount > 0) {

                    for (int row : rows) {

                        // Obtém o ID da coluna 0 (idMov)
                        Object idValue = tbDadosMov.getValueAt(row, 0);

                        if (idValue != null) {

                            stmt.setString(1, idValue.toString());

                            stmt.addBatch();

                        }

                    }

                    stmt.executeBatch();

                } else {

                    stmt.setString(1, tfRegistro.getText());

                    stmt.execute();

                }

                stmt.close();

                con.close();

                if (rowCount > 1) {

                    JOptionPane.showMessageDialog(null, rowCount + " movimentos excluídos com sucesso!");

                } else {

                    JOptionPane.showMessageDialog(null, "Movimento excluído com sucesso!");

                }

                // Limpando os dados e atualizando
                tfRegistro.setText("");

                tfNrFav.setText("");

                tfFavorecidos.setText("");

                tfLancto.setText("");

                tfEmissao.setText("");

                tfApresentacao.setText("");

                tfDocumento.setText("");

                tfClass.setText("");

                tfDescricao.setText("");

                tfValor.setText("");

                tfStatusMov.setText("");

                tfPrev.setText("");

                cbxClass.setSelectedIndex(0);

                tfBusca.setText("");

                JRefresh();

            } catch (SQLException ex) {

                Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);

                JOptionPane.showMessageDialog(null, "Erro ao excluir registros: " + ex.getMessage());

            } finally {

                //conexao.fecharConexao();
            }
        }
    }//GEN-LAST:event_btnExcluirActionPerformed

    private void btnFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFecharActionPerformed
        // TODO add your handling code here:

        try {
            TelaPrincipal exibir;
            exibir = new TelaPrincipal();
            exibir.setVisible(true);
            setVisible(false);
        } catch (IOException ex) {
            Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnFecharActionPerformed

    private void cbxClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxClassActionPerformed
        String selectedValue = cbxClass.getSelectedItem().toString();//.substring(11);
        var texto = selectedValue.length();
        texto = texto - 9;
        selectedValue = cbxClass.getSelectedItem().toString().substring(texto);
        tfClass.setText(selectedValue);
    }//GEN-LAST:event_cbxClassActionPerformed

    private void tfRecursoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfRecursoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfRecursoActionPerformed

    private void tfStatusMovActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfStatusMovActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfStatusMovActionPerformed

    private void tfPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfPrevActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfPrevActionPerformed

    private void jComboBoxRecursoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxRecursoActionPerformed
//        String selectedValue = jComboBoxRecurso.getSelectedItem().toString();//.substring(11);
        //        var texto = selectedValue.length();
        //        selectedValue = jComboBoxRecurso.getSelectedItem().toString().substring(texto - 14);
        //        if (!selectedValue.isEmpty()) {
        //            selectedValue = jComboBoxRecurso.getSelectedItem().toString().substring(texto - 14);
        //            String frase1 = selectedValue.substring(0, 4);
        //            tfNrRecurso.setText(frase1);
        //            String frase2 = selectedValue.substring(5);
        //            tfRecurso.setText(frase2);
        //        }
        // 1. Verifica se há algum item selecionado para evitar NullPointerException

        if (jComboBoxRecurso.getSelectedItem() == null) {
            return;
        }

        String selectedValue = jComboBoxRecurso.getSelectedItem().toString();
        int textoLength = selectedValue.length();

        // 2. Só faz o recorte se a string tiver pelo menos 14 caracteres
        if (textoLength >= 14) {
            // Pega os últimos 14 caracteres
            String parteFinal = selectedValue.substring(textoLength - 14);

            // 3. Garante que a 'parteFinal' tem tamanho suficiente para os próximos substrings
            if (parteFinal.length() >= 5) {
                String frase1 = parteFinal.substring(0, 4);
                tfNrRecurso.setText(frase1);

                String frase2 = parteFinal.substring(5);
                tfRecurso.setText(frase2);
            }
        } else {
            // Opcional: Limpar os campos caso o texto seja menor que o esperado (ex: "Selecione...")
            tfNrRecurso.setText("");
            tfRecurso.setText("");
        }

    }//GEN-LAST:event_jComboBoxRecursoActionPerformed

    private void jComboBoxFavorecidosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxFavorecidosActionPerformed
        String selectedValue = jComboBoxFavorecidos.getSelectedItem().toString();
        if (selectedValue == null || selectedValue.trim().isEmpty() || selectedValue.equals("Selecione")) {
            tfNrFav.setText("");
            tfFavorecidos.setText("");
            return;
        }
        var texto = selectedValue.length();
        selectedValue = jComboBoxFavorecidos.getSelectedItem().toString().substring(texto - 14);
        if (!selectedValue.isEmpty()) {
            String frase1 = selectedValue.substring(0, 4);
            tfNrFav.setText(frase1);
            String frase2 = selectedValue.substring(5);
            tfFavorecidos.setText(frase2);
        }
    }//GEN-LAST:event_jComboBoxFavorecidosActionPerformed

    @SuppressWarnings({"unchecked", "unchecked", "unchecked", "unchecked"})
    private void jComboBoxRecursoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jComboBoxRecursoKeyPressed
        Connection con;
        try {
            con = Conexao.faz_conexao();
            SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");
            DecimalFormat df = new DecimalFormat("###,##0.00");
            Double sa = 0.00;
            if (tfNrRecurso.getText().equals("0079")) {
                //String sql = "SELECT * FROM tbmovimento WHERE recurso = ? AND statusMov not in ('PG') ORDER BY dtVcto";
                String sql = """
                    SELECT *
                    FROM tbmovimento
                    WHERE statusMov NOT IN ('PG', 'TD', 'SI')
                      AND (
                          recurso = ?
                          OR vrecurso = '2.001.003'
                      )
                    ORDER BY dtVcto, recurso, dtEmi
                    """;
                PreparedStatement stt = con.prepareStatement(sql);
                stt.setString(1, tfNrRecurso.getText());
                PreparedStatement stmt = con.prepareStatement(sql);
                try (ResultSet rs = stt.executeQuery()) {
                    double somaval = 0;
                    tfSaldoAnterior.setText(String.valueOf(somaval));
                    DefaultTableModel modelo = (DefaultTableModel) tbDadosMov.getModel();
                    modelo.setNumRows(0);
                    tbDadosMov.setRowSorter(new TableRowSorter(modelo)); // Organiza tabela clicando no tÃ­tulo da coluna
                    tbDadosMov.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(0)).setPreferredWidth(40); //idMov
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(1)).setPreferredWidth(60); //recurso
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(2)).setPreferredWidth(70); //vrecurso
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(3)).setPreferredWidth(50); //clifor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(4)).setPreferredWidth(75); //vCliFor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(5)).setPreferredWidth(75); //dtlancto
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(6)).setPreferredWidth(75); //dtEmi
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(7)).setPreferredWidth(75); //dtVcto
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(8)).setPreferredWidth(110); //documento
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(9)).setPreferredWidth(70);  //classif
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(10)).setPreferredWidth(175); //Descr
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(11)).setPreferredWidth(80);  //Valor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(12)).setPreferredWidth(100); //dtApr
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(13)).setPreferredWidth(40); //statusMov
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(14)).setPreferredWidth(30); //Prev
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(15)).setPreferredWidth(80); //Saldo
                    DefaultTableCellRenderer tab = new DefaultTableCellRenderer();
                    tab.setHorizontalAlignment(SwingConstants.RIGHT);
                    tbDadosMov.getColumnModel().getColumn(11).setCellRenderer(tab);
                    tbDadosMov.getColumnModel().getColumn(15).setCellRenderer(tab);
                    while (rs.next()) {
                        sa = sa + rs.getDouble("Valor");
                        Date Apre = rs.getDate("dtApr");
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
                        modelo.addRow(new Object[]{rs.getString("idMov"),
                            rs.getString("recurso"),
                            rs.getString("vrecurso"),
                            rs.getString("clifor"),
                            rs.getString("vCliFor"),
                            formato_data.format(rs.getDate("dtlancto")),
                            formato_data.format(rs.getDate("dtEmi")),
                            formato_data.format(rs.getDate("dtVcto")),
                            rs.getString("documento"),
                            rs.getString("classif"),
                            rs.getString("Descr"),
                            df.format(rs.getDouble("Valor")),
                            AprFormatada,
                            rs.getString("statusMov"),
                            rs.getString("Prev"),
                            df.format(sa),});
                    }
                }
                con.close();
            } else if (tfNrRecurso.getText().equals("0022")) {
                String sql = "SELECT * FROM tbmovimento WHERE recurso = ? AND statusMov <> 'RC' ORDER BY dtVcto";
                PreparedStatement stt = con.prepareStatement(sql);
                stt.setString(1, tfNrRecurso.getText());
                try (ResultSet rs = stt.executeQuery()) {
                    double somaval = 0;
                    tfSaldoAnterior.setText(String.valueOf(somaval));
                    DefaultTableModel modelo = (DefaultTableModel) tbDadosMov.getModel();
                    modelo.setNumRows(0);
                    tbDadosMov.setRowSorter(new TableRowSorter(modelo)); // Organiza tabela clicando no tÃ­tulo da coluna
                    tbDadosMov.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(0)).setPreferredWidth(40); //idMov
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(1)).setPreferredWidth(60); //recurso
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(2)).setPreferredWidth(70); //vrecurso
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(3)).setPreferredWidth(50); //clifor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(4)).setPreferredWidth(75); //vCliFor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(5)).setPreferredWidth(75); //dtlancto
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(6)).setPreferredWidth(75); //dtEmi
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(7)).setPreferredWidth(75); //dtVcto
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(8)).setPreferredWidth(110); //documento
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(9)).setPreferredWidth(70);  //classif
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(10)).setPreferredWidth(175); //Descr
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(11)).setPreferredWidth(80);  //Valor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(12)).setPreferredWidth(100); //dtApr
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(13)).setPreferredWidth(40); //statusMov
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(14)).setPreferredWidth(30); //Prev
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(15)).setPreferredWidth(80); //Saldo
                    DefaultTableCellRenderer tab = new DefaultTableCellRenderer();
                    tab.setHorizontalAlignment(SwingConstants.RIGHT);
                    tbDadosMov.getColumnModel().getColumn(11).setCellRenderer(tab);
                    tbDadosMov.getColumnModel().getColumn(15).setCellRenderer(tab);
                    while (rs.next()) {
                        sa = sa + rs.getDouble("Valor");
                        Date Apre = rs.getDate("dtApr");
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
                        modelo.addRow(new Object[]{rs.getString("idMov"),
                            rs.getString("recurso"),
                            rs.getString("vrecurso"),
                            rs.getString("clifor"),
                            rs.getString("vCliFor"),
                            formato_data.format(rs.getDate("dtlancto")),
                            formato_data.format(rs.getDate("dtEmi")),
                            formato_data.format(rs.getDate("dtVcto")),
                            rs.getString("documento"),
                            rs.getString("classif"),
                            rs.getString("Descr"),
                            df.format(rs.getDouble("Valor")),
                            AprFormatada,
                            rs.getString("statusMov"),
                            rs.getString("Prev"),
                            df.format(sa),});
                    }
                    tfSaldoAnterior.setText(String.valueOf(somaval));
                }
                con.close();
            } else {
                String sql = "SELECT * FROM tbmovimento WHERE recurso = ?  ORDER BY dtVcto";
                PreparedStatement stt = con.prepareStatement(sql);
                stt.setString(1, tfNrRecurso.getText());
                try (ResultSet rs = stt.executeQuery()) {
                    double somaval = 0;
                    tfSaldoAnterior.setText(String.valueOf(somaval));
                    DefaultTableModel modelo = (DefaultTableModel) tbDadosMov.getModel();
                    modelo.setNumRows(0);
                    tbDadosMov.setRowSorter(new TableRowSorter(modelo)); // Organiza tabela clicando no tÃ­tulo da coluna
                    tbDadosMov.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(0)).setPreferredWidth(40); //idMov
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(1)).setPreferredWidth(60); //recurso
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(2)).setPreferredWidth(70); //vrecurso
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(3)).setPreferredWidth(50); //clifor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(4)).setPreferredWidth(75); //vCliFor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(5)).setPreferredWidth(75); //dtlancto
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(6)).setPreferredWidth(75); //dtEmi
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(7)).setPreferredWidth(75); //dtVcto
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(8)).setPreferredWidth(110); //documento
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(9)).setPreferredWidth(70);  //classif
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(10)).setPreferredWidth(175); //Descr
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(11)).setPreferredWidth(80);  //Valor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(12)).setPreferredWidth(100); //dtApr
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(13)).setPreferredWidth(40); //statusMov
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(14)).setPreferredWidth(30); //Prev
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(15)).setPreferredWidth(80); //Saldo
                    DefaultTableCellRenderer tab = new DefaultTableCellRenderer();
                    tab.setHorizontalAlignment(SwingConstants.RIGHT);
                    tbDadosMov.getColumnModel().getColumn(11).setCellRenderer(tab);
                    tbDadosMov.getColumnModel().getColumn(15).setCellRenderer(tab);
                    while (rs.next()) {
                        sa = sa + rs.getDouble("Valor");
                        Date Apre = rs.getDate("dtApr");
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
                        modelo.addRow(new Object[]{rs.getString("idMov"),
                            rs.getString("recurso"),
                            rs.getString("vrecurso"),
                            rs.getString("clifor"),
                            rs.getString("vCliFor"),
                            formato_data.format(rs.getDate("dtlancto")),
                            formato_data.format(rs.getDate("dtEmi")),
                            formato_data.format(rs.getDate("dtVcto")),
                            rs.getString("documento"),
                            rs.getString("classif"),
                            rs.getString("Descr"),
                            df.format(rs.getDouble("Valor")),
                            AprFormatada,
                            rs.getString("statusMov"),
                            rs.getString("Prev"),
                            df.format(sa),});
                    }
                }
                con.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            jComboBoxFavorecidos.requestFocus();
        }
        DecimalFormat dec = new DecimalFormat("#,##0.00");

    }//GEN-LAST:event_jComboBoxRecursoKeyPressed

    private void jComboBoxRecursoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jComboBoxRecursoMouseClicked
        jComboBoxFavorecidos.requestFocus();
    }//GEN-LAST:event_jComboBoxRecursoMouseClicked

    private void jComboBoxFavorecidosKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jComboBoxFavorecidosKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            tfLancto.requestFocus();
        }
    }//GEN-LAST:event_jComboBoxFavorecidosKeyPressed

    private void tfDocumentoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfDocumentoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            tfValor.requestFocus();
        }
    }//GEN-LAST:event_tfDocumentoKeyPressed

    private void tfValorKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfValorKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            tfDescricao.requestFocus();
        }
    }//GEN-LAST:event_tfValorKeyPressed

    private void tfDescricaoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfDescricaoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            cbxClass.requestFocus();
        }
    }//GEN-LAST:event_tfDescricaoKeyPressed

    private void cbxClassKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cbxClassKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            tfStatusMov.requestFocus();
        }
    }//GEN-LAST:event_cbxClassKeyPressed

    private void tfStatusMovKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfStatusMovKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            tfPrev.requestFocus();
        }
    }//GEN-LAST:event_tfStatusMovKeyPressed

    private void tfEmissaoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfEmissaoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            tfVencimento.requestFocus();
        }
    }//GEN-LAST:event_tfEmissaoKeyPressed

    private void tfVencimentoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfVencimentoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            tfApresentacao.requestFocus();
        }
    }//GEN-LAST:event_tfVencimentoKeyPressed

    private void tfVencimentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfVencimentoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfVencimentoActionPerformed

    private void tfApresentacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfApresentacaoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfApresentacaoActionPerformed

    private void tfApresentacaoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfApresentacaoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            tfDocumento.requestFocus();
        }
    }//GEN-LAST:event_tfApresentacaoKeyPressed

    private void tfApresentacaoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfApresentacaoFocusLost
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = sdf.parse(tfApresentacao.getText().trim());
            tfApresentacao.setValue(sdf.format(date));
        } catch (ParseException e) {
            tfApresentacao.setFocusLostBehavior(JFormattedTextField.PERSIST);
            tfApresentacao.setText("");
            tfApresentacao.setValue(null);
            tfApresentacao.setEditable(true);
        }
        tfApresentacao.setEditable(true);
    }//GEN-LAST:event_tfApresentacaoFocusLost

    private void tfLanctoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfLanctoFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_tfLanctoFocusLost

    private void tfLanctoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfLanctoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfLanctoActionPerformed

    private void tfLanctoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfLanctoKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            tfEmissao.requestFocus();
        }
    }//GEN-LAST:event_tfLanctoKeyPressed

    private void jButtonPagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPagActionPerformed
        if (tfRegistro.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Selecione o registro do movimento a ser Pago");
            return;
        }
        // Validação da data de apresentação
        String dataApresentacao = tfApresentacao.getText().trim();
        if (dataApresentacao.isEmpty() || dataApresentacao.equals("/  /")) {
            JOptionPane.showMessageDialog(null, "Atenção! Favor preencher o campo data de Apresentação.");
            return;
        }
        // Passagem de dados para as variáveis globais (ou estáticas)
        textoidMov = tfRegistro.getText();
        textoLancto = tfLancto.getText();
        textoEmissao = tfEmissao.getText();
        textoVencimento = tfVencimento.getText();
        textoApresentacao = tfApresentacao.getText();
        textoDocumento = tfDocumento.getText();
        textoDescricao = tfDescricao.getText();
        textoOrigem = "";
        textoDestino = "";
        textoVrPg = tfValor.getText();
        textoFav = tfFavorecidos.getText();
        textoNrFav = tfNrFav.getText();
        textoClass = tfClass.getText();
        textoNrRecurso = tfNrRecurso.getText();
        textoRecurso = tfRecurso.getText();
        // === Abre Tela_Pagar ===
        try {
            // Instancia a nova tela (certifique-se de que o construtor existe)
            Tela_Pagar telaPagar = new Tela_Pagar();

            // Se a Tela_Pagar for um JInternalFrame e você estiver usando JDesktopPane:
            // getDesktopPane().add(telaPagar); 
            telaPagar.setVisible(true);
            this.setVisible(false);

        } catch (SQLException ex) {
            Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao abrir tela de pagamento: " + ex.getMessage());
        }
    }//GEN-LAST:event_jButtonPagActionPerformed

    private void tbDadosMovMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDadosMovMouseClicked
        //Ao clicar duas vezes
        tbDadosMov.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    int setar = tbDadosMov.getSelectedRow();
                    tfBusca.setText(tbDadosMov.getModel().getValueAt(setar, 0).toString());
                    AbrirListaSelecionada();
                }
    }//GEN-LAST:event_tbDadosMovMouseClicked
        });
    }


    private void jButtonRecebActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRecebActionPerformed
        if (tfRegistro.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Selecione o registro do movimento à Receber");
        }
        if (tfApresentacao.getText().trim().length() == 4) {
            JOptionPane.showMessageDialog(null, "Atenção! Favor preencher o campo data de Apresentação.");
        } else {
            tfStatusMov.setText("RC");
            tfPrev.setText("V");
            Atualiza();
            textoidMov = tfRegistro.getText();
            textoLancto = tfLancto.getText();
            textoEmissao = tfEmissao.getText();
            textoVencimento = tfVencimento.getText();
            textoApresentacao = tfApresentacao.getText();
            textoDocumento = tfDocumento.getText();
            textoDescricao = tfDescricao.getText();
            textoOrigem = "";
            textoDestino = "";
            textoVrPg = tfValor.getText();
            textoFav = tfFavorecidos.getText();
            textoNrFav = tfNrFav.getText();
            textoClass = tfClass.getText();
            textoNrRecurso = tfNrRecurso.getText();
            Tela_Receber tela = null;
            try {
                tela = new Tela_Receber();
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
            }
            tela.setVisible(true);
            this.setVisible(false);
        }
    }//GEN-LAST:event_jButtonRecebActionPerformed

    private void tfNrRecursoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfNrRecursoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfNrRecursoActionPerformed

    private void tfPrevFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfPrevFocusLost
        validarPrevisto();
    }//GEN-LAST:event_tfPrevFocusLost

    private void jButtonLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLimparActionPerformed
        LimparDados();
    }//GEN-LAST:event_jButtonLimparActionPerformed

    private void tfValorKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfValorKeyTyped
        validarValor();
    }//GEN-LAST:event_tfValorKeyTyped

    private void tfStatusMovFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfStatusMovFocusLost
        validarStatusMov();
    }//GEN-LAST:event_tfStatusMovFocusLost

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
            java.util.logging.Logger.getLogger(Tela_de_Mov.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Tela_de_Mov.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Tela_de_Mov.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tela_de_Mov.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new Tela_de_Mov().setVisible(true);
                } catch (SQLException ex) {
                    Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
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
    private javax.swing.JComboBox<String> cbxClass;
    private javax.swing.JButton jButtonLimpar;
    private javax.swing.JButton jButtonPag;
    private javax.swing.JButton jButtonReceb;
    private javax.swing.JComboBox<String> jCbxTipo;
    private javax.swing.JComboBox<String> jComboBoxFavorecidos;
    private javax.swing.JComboBox<String> jComboBoxRecurso;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tbDadosMov;
    private javax.swing.JFormattedTextField tfApresentacao;
    private javax.swing.JTextField tfBusca;
    private javax.swing.JTextField tfClass;
    private javax.swing.JTextField tfDescricao;
    private javax.swing.JTextField tfDocumento;
    private javax.swing.JFormattedTextField tfEmissao;
    private javax.swing.JTextField tfFavorecidos;
    private javax.swing.JFormattedTextField tfLancto;
    private javax.swing.JTextField tfNrFav;
    private javax.swing.JTextField tfNrRecurso;
    private javax.swing.JTextField tfPrev;
    private javax.swing.JTextField tfRecurso;
    private javax.swing.JTextField tfRegistro;
    private javax.swing.JTextField tfSaldoAnterior;
    private javax.swing.JTextField tfStatusMov;
    private javax.swing.JTextField tfValor;
    private javax.swing.JFormattedTextField tfVencimento;
    // End of variables declaration//GEN-END:variables

    private static class CarregarCbx {

        @SuppressWarnings({"empty-statement", "unchecked"})
        public void CarregarCbx(String tabela, String valor, JComboBox combo) throws SQLException {
            String sql = "Select * FROM " + tabela + " ORDER BY apelidoCliFor";
            Connection con = Conexao.faz_conexao();
            try {
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    combo.addItem(rs.getString(valor) + "-" + (rs.getString("codCliFor") + "-" + rs.getString("fkCliForGp")));
                }
                rs.close();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_Mov.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static class CarregarCbx2 {

        @SuppressWarnings({"empty-statement", "unchecked"})
        public void CarregarCbx2(String tabela, String valor, JComboBox combo) throws SQLException {
            String sql = "Select * FROM " + tabela + " ORDER BY nomebco";
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
                Logger.getLogger(Tela_de_Mov.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static class CarregarCbx3 {

        @SuppressWarnings({"empty-statement", "unchecked"})
        public void CarregarCbx3(String tabela, String valor, JComboBox combo) throws SQLException {
            String sql = "Select cod_Geral, nome_P, nome_S, nome_C FROM " + tabela + " WHERE nome_C <> '-' ";
            Connection con = Conexao.faz_conexao();
            try {
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    combo.addItem(rs.getString(valor) + "-" + (rs.getString("cod_Geral")));
                }
                rs.close();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_Mov.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void ListaDados() {
        try {
            Connection con = Conexao.faz_conexao();
            String sql = null;
            if (tfNrRecurso.getText().isEmpty()) {
                sql = "Select * FROM tbmovimento";
            } else {
                sql = "Select * FROM tbmovimento WHERE recurso=" + tfNrRecurso.getText();
            }
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel modelo = (DefaultTableModel) tbDadosMov.getModel();
            modelo.setNumRows(0);
            tbDadosMov.setRowSorter(new TableRowSorter(modelo)); // Organiza tabela clicando no tÃ­tulo da coluna
            double sa = 0.00;
            tbDadosMov.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
            tbDadosMov.getColumn(tbDadosMov.getColumnName(0)).setPreferredWidth(40); //idMov
            tbDadosMov.getColumn(tbDadosMov.getColumnName(1)).setPreferredWidth(60); //recurso
            tbDadosMov.getColumn(tbDadosMov.getColumnName(2)).setPreferredWidth(70); //vrecurso 
            tbDadosMov.getColumn(tbDadosMov.getColumnName(3)).setPreferredWidth(50); //clifor
            tbDadosMov.getColumn(tbDadosMov.getColumnName(4)).setPreferredWidth(75); //vCliFor
            tbDadosMov.getColumn(tbDadosMov.getColumnName(5)).setPreferredWidth(75); //dtlancto
            tbDadosMov.getColumn(tbDadosMov.getColumnName(6)).setPreferredWidth(75); //dtEmi
            tbDadosMov.getColumn(tbDadosMov.getColumnName(7)).setPreferredWidth(75); //dtVcto
            tbDadosMov.getColumn(tbDadosMov.getColumnName(8)).setPreferredWidth(110); //documento
            tbDadosMov.getColumn(tbDadosMov.getColumnName(9)).setPreferredWidth(70);  //classif
            tbDadosMov.getColumn(tbDadosMov.getColumnName(10)).setPreferredWidth(175); //Descr
            tbDadosMov.getColumn(tbDadosMov.getColumnName(11)).setPreferredWidth(80);  //Valor
            tbDadosMov.getColumn(tbDadosMov.getColumnName(12)).setPreferredWidth(100); //dtApr
            tbDadosMov.getColumn(tbDadosMov.getColumnName(13)).setPreferredWidth(40); //statusMov
            tbDadosMov.getColumn(tbDadosMov.getColumnName(14)).setPreferredWidth(30); //Prev
            tbDadosMov.getColumn(tbDadosMov.getColumnName(15)).setPreferredWidth(80); //Prev
            DefaultTableCellRenderer tab = new DefaultTableCellRenderer();
            tab.setHorizontalAlignment(SwingConstants.RIGHT);
            tbDadosMov.getColumnModel().getColumn(11).setCellRenderer(tab);
            tbDadosMov.getColumnModel().getColumn(15).setCellRenderer(tab);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            DecimalFormat df = new DecimalFormat("#,##0.00");
            while (rs.next()) {
                sa = sa + rs.getDouble("Valor");
                Date Apre = rs.getDate("dtApr");
                String AprFormatada;
                if (Apre == null && !"".equals(Apre)) {
                    AprFormatada = null;
                } else if (Apre.equals("  /  /    ")) {
                    AprFormatada = null;
                } else if (Apre.equals("")) {
                    AprFormatada = null;
                } else {
                    AprFormatada = sdf.format(Apre);
                }
                modelo.addRow(new Object[]{rs.getString("idMov"),
                    rs.getString("recurso"),
                    rs.getString("vrecurso"),
                    rs.getString("clifor"),
                    rs.getString("vCliFor"),
                    sdf.format(rs.getDate("dtlancto")),
                    sdf.format(rs.getDate("dtEmi")),
                    sdf.format(rs.getDate("dtVcto")),
                    rs.getString("documento"),
                    rs.getString("classif"),
                    rs.getString("Descr"),
                    df.format(rs.getDouble("Valor")),
                    AprFormatada,
                    rs.getString("statusMov"),
                    rs.getString("Prev"),
                    df.format(sa),});
            }
            rs.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Tela_de_Mov.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("unchecked")
    public void ListaDadosSelecionados() throws ParseException {
        try {
            SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");
            DecimalFormat df = new DecimalFormat("###,##0.00");
            Connection con;
            con = Conexao.faz_conexao();
            String ssa = tfSaldoAnterior.getText();
            double sa = Double.valueOf(ssa).doubleValue();;
            sa = Double.parseDouble(ssa);
            String sql = "SELECT * FROM tbmovimento WHERE dtlancto >= ? AND recurso = ? ORDER BY dtlancto";
            PreparedStatement stmt = con.prepareStatement(sql);
            java.util.Date data_c = formato_data.parse(tfLancto.getText());
            stmt.setDate(1, new java.sql.Date(data_c.getTime()));
            stmt.setString(2, tfNrRecurso.getText());
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel modelo = (DefaultTableModel) tbDadosMov.getModel();
            modelo.setNumRows(0);
            //----------------------

            //----------------------
            tbDadosMov.setRowSorter(new TableRowSorter(modelo)); // Organiza tabela clicando no tÃ­tulo da coluna
            tbDadosMov.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
            tbDadosMov.getColumn(tbDadosMov.getColumnName(0)).setPreferredWidth(40); //idMov
            tbDadosMov.getColumn(tbDadosMov.getColumnName(1)).setPreferredWidth(60); //recurso
            tbDadosMov.getColumn(tbDadosMov.getColumnName(2)).setPreferredWidth(70); //vrecurso 
            tbDadosMov.getColumn(tbDadosMov.getColumnName(3)).setPreferredWidth(50); //clifor
            tbDadosMov.getColumn(tbDadosMov.getColumnName(4)).setPreferredWidth(75); //vCliFor
            tbDadosMov.getColumn(tbDadosMov.getColumnName(5)).setPreferredWidth(75); //dtlancto
            tbDadosMov.getColumn(tbDadosMov.getColumnName(6)).setPreferredWidth(75); //dtEmi
            tbDadosMov.getColumn(tbDadosMov.getColumnName(7)).setPreferredWidth(75); //dtVcto
            tbDadosMov.getColumn(tbDadosMov.getColumnName(8)).setPreferredWidth(110); //documento
            tbDadosMov.getColumn(tbDadosMov.getColumnName(9)).setPreferredWidth(70);  //classif
            tbDadosMov.getColumn(tbDadosMov.getColumnName(10)).setPreferredWidth(175); //Descr
            tbDadosMov.getColumn(tbDadosMov.getColumnName(11)).setPreferredWidth(80);  //Valor
            tbDadosMov.getColumn(tbDadosMov.getColumnName(12)).setPreferredWidth(100); //dtApr
            tbDadosMov.getColumn(tbDadosMov.getColumnName(13)).setPreferredWidth(40); //statusMov
            tbDadosMov.getColumn(tbDadosMov.getColumnName(14)).setPreferredWidth(30); //Prev
            tbDadosMov.getColumn(tbDadosMov.getColumnName(15)).setPreferredWidth(80); //Saldo
            DefaultTableCellRenderer tab = new DefaultTableCellRenderer();
            tab.setHorizontalAlignment(SwingConstants.RIGHT);
            tbDadosMov.getColumnModel().getColumn(11).setCellRenderer(tab);
            tbDadosMov.getColumnModel().getColumn(15).setCellRenderer(tab);
            while (rs.next()) {
                sa = sa + rs.getDouble("Valor");
                Date Apre = rs.getDate("dtApr");
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
                modelo.addRow(new Object[]{rs.getString("idMov"),
                    rs.getString("recurso"),
                    rs.getString("vrecurso"),
                    rs.getString("clifor"),
                    rs.getString("vCliFor"),
                    formato_data.format(rs.getDate("dtlancto")),
                    formato_data.format(rs.getDate("dtEmi")),
                    formato_data.format(rs.getDate("dtVcto")),
                    rs.getString("documento"),
                    rs.getString("classif"),
                    rs.getString("Descr"),
                    df.format(rs.getDouble("Valor")),
                    AprFormatada,
                    rs.getString("statusMov"),
                    rs.getString("Prev"),
                    df.format(sa),});
            }
            rs.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Tela_de_Mov.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void AbrirListaSelecionada() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try ( // Código para pesquisar um usuario
                Connection con = Conexao.faz_conexao()) {
            String sql = "SELECT * FROM tbmovimento WHERE idMov=?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, tfBusca.getText());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tfRegistro.setText(rs.getString("idMov"));
                    tfNrRecurso.setText(rs.getString("recurso"));
                    selecionarItemComboBox(jComboBoxRecurso, rs.getString("recurso"));
                    tfRecurso.setText(rs.getString("vrecurso"));
                    tfNrFav.setText(rs.getString("clifor"));
                    tfFavorecidos.setText(rs.getString("vCliFor"));
                    selecionarItemComboBox(jComboBoxFavorecidos, rs.getString("clifor"));
                    tfLancto.setText(sdf.format(rs.getDate("dtlancto")));
                    tfEmissao.setText(sdf.format(rs.getDate("dtEmi")));
                    tfVencimento.setText(sdf.format(rs.getDate("dtVcto")));
                    tfDocumento.setText(rs.getString("documento"));
                    tfClass.setText(rs.getString("classif"));
                    tfDescricao.setText(rs.getString("Descr"));
                    double valor = rs.getDouble("Valor");
                    DecimalFormat df = new DecimalFormat("###,##0.00");
                    tfValor.setText(df.format(valor));
                    //Double ver = Double.parseDouble(tfValor.getText().replace(",", "."));
                    // Remover o separador de milhar e substituir a vírgula decimal por ponto
                    String valorSemMilhar = tfValor.getText().replace(".", "").replace(",", ".");
                    Double ver = Double.parseDouble(valorSemMilhar);
                    if (ver > 0) {
                        jButtonPag.setEnabled(false);
                    } else {
                        jButtonReceb.setEnabled(false);
                    }
                    if (rs.getDate("dtApr") == null) {
                        tfApresentacao.setText(null);
                    } else {
                        tfApresentacao.setText(sdf.format(rs.getDate("dtApr")));
                    }
                    tfStatusMov.setText(rs.getString("statusMov"));
                    tfPrev.setText(rs.getString("Prev"));
                    btSalvar.setEnabled(false);
                }
                stmt.close();
                con.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Tela_de_Mov.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void selecionarItemComboBox(JComboBox<String> combo, String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            combo.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).contains("-" + codigo + "-")) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(0);
    }

    private void validarValor() {
        // Trata o campo jTextField para que receba apenas números
        tfValor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                String textoAtual = tfValor.getText();

                // Permite apenas números, vírgula, ponto, sinal de negativo e backspace
                if (!Character.isDigit(c) && c != ',' && c != '.' && c != '-' && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                    return;
                }

                // Não permite mais de uma vírgula ou ponto no texto
                if ((c == '.' || c == ',') && (textoAtual.contains(".") || textoAtual.contains(","))) {
                    e.consume();
                    return;
                }

                // Não permite o sinal de negativo em posições diferentes do início
                if (c == '-' && !textoAtual.isEmpty()) {
                    e.consume();
                }
            }
        });
    }

    private void validarStatusMov() {
        //Trata o campo para receber apenas caracteres válidos RC, PG, TO ou TD
        String texto = tfStatusMov.getText();
        if (!texto.equals("") && !texto.equals("RC") && !texto.equals("PG") && !texto.equals("TO")
                && !texto.equals("TD") && !texto.equals("rc") && !texto.equals("pg") && !texto.equals("to")
                && !texto.equals("td")) {
            JOptionPane.showMessageDialog(null, "Entrada inválida! Por favor, insira 'RC' ou 'PG'.", "Erro", JOptionPane.ERROR_MESSAGE);
            tfStatusMov.setText("");
            tfStatusMov.requestFocus();
        } else if (texto.equals("rc") || texto.equals("pg") || texto.equals("to") || texto.equals("td")) {
            tfStatusMov.setText(texto.toUpperCase());
        }
    }

    private void validarPrevisto() {
        String texto = tfPrev.getText().trim();
        if (!texto.equals("V") && !texto.equals("F") && !texto.equals("v") && !texto.equals("f")) {
            JOptionPane.showMessageDialog(null, "Entrada inválida! Por favor, insira 'V' ou 'F'.", "Erro", JOptionPane.ERROR_MESSAGE);
            tfPrev.setText("");
            tfPrev.requestFocus();
        } else if (texto.equals("v") || texto.equals("f")) {
            tfPrev.setText(texto.toUpperCase());
        }
    }

    public void Atualiza() {
        try {
            Connection con = Conexao.faz_conexao();
            SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");
            String sql = "UPDATE tbmovimento SET recurso=?,vrecurso=?,clifor=?,vclifor=?,dtlancto=?,dtEmi=?,dtVcto=?,documento=?,classif=?,Descr=?,valor=?,dtApr=?,statusMov=?,Prev=? WHERE idMov=?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, (String) tfNrRecurso.getText());
            stmt.setString(2, (String) tfRecurso.getText());
            stmt.setString(3, tfNrFav.getText());
            stmt.setString(4, tfFavorecidos.getText());
            java.util.Date data_l = formato_data.parse(tfLancto.getText());
            stmt.setDate(5, new java.sql.Date(data_l.getTime()));
            java.util.Date data_e = formato_data.parse(tfEmissao.getText());
            stmt.setDate(6, new java.sql.Date(data_e.getTime()));
            java.util.Date data_v = formato_data.parse(tfVencimento.getText());
            stmt.setDate(7, new java.sql.Date(data_v.getTime()));
            stmt.setString(8, tfDocumento.getText());
            stmt.setString(9, tfClass.getText());
            stmt.setString(10, tfDescricao.getText());
            //stmt.setString(11, tfValor.getText());
            String valorTexto = tfValor.getText().replaceAll("\\.", "");
            // Substituir a vírgula decimal por ponto decimal
            valorTexto = valorTexto.replaceAll(",", ".");
            // Parse para Double
            stmt.setDouble(11, Double.parseDouble(valorTexto));
            //stmt.setDouble(11, Double.parseDouble(tfValor.getText().replaceAll(",", ".")));
            if (tfApresentacao.getText().isEmpty()) {
                stmt.setDate(12, null);
            } else if (tfApresentacao.getText().equals("  /  /    ")) {
                tfApresentacao.setValue(null);
                stmt.setDate(12, null);
            } else {
                java.util.Date data_a = formato_data.parse(tfApresentacao.getText());
                stmt.setDate(12, new java.sql.Date(data_a.getTime()));
            }
            stmt.setString(13, tfStatusMov.getText());
            stmt.setString(14, tfPrev.getText());
            stmt.setString(15, tfRegistro.getText());
            stmt.execute();
            DefaultTableModel modelo = (DefaultTableModel) tbDadosMov.getModel();
            modelo.setRowCount(0);
            stmt.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Tela_de_Mov.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Limpar dados dos campos e tabela
    private void LimparDados() {
        ((DefaultTableModel) tbDadosMov.getModel()).setRowCount(0);
        tfRegistro.setText("");
        tfNrRecurso.setText("");
        tfRecurso.setText("");
        tfNrFav.setText("");
        tfFavorecidos.setText("");
        tfLancto.setText("");
        tfEmissao.setText("");
        tfVencimento.setText("");
        tfApresentacao.setText("");
        tfDocumento.setText("");
        tfClass.setText("");
        tfDescricao.setText("");
        tfValor.setText("");
        tfApresentacao.setText("");
        tfStatusMov.setText("");
        tfPrev.setText("");
        jCbxTipo.setSelectedIndex(0);
        cbxClass.setSelectedIndex(0);
        jComboBoxRecurso.setSelectedIndex(0);
        jComboBoxFavorecidos.setSelectedIndex(0);
    }

    private void JRefresh() {
        Connection con;
        try {
            con = Conexao.faz_conexao();
            SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");
            DecimalFormat df = new DecimalFormat("#,##0.00");
            //String ssa = tfSaldoAnterior.getText();
            Double sa = 0.00;
            if (tfLancto.getText().trim().length() == 4) {
                JOptionPane.showMessageDialog(null, "Atenção! Favor preenche o campo de data Lançamento.");
            } else {
                if (tfNrRecurso.getText().equals("0079")) {
                    String sql = "SELECT * FROM tbmovimento WHERE recurso = ? AND statusMov not in ('PG') ORDER BY dtVcto";
                    PreparedStatement stt = con.prepareStatement(sql);
                    stt.setString(1, tfNrRecurso.getText());
                    PreparedStatement stmt = con.prepareStatement(sql);
                    ResultSet rs = stt.executeQuery();
                    double somaval = 0;
                    tfSaldoAnterior.setText(String.valueOf(somaval));
                    DefaultTableModel modelo = (DefaultTableModel) tbDadosMov.getModel();
                    modelo.setNumRows(0);
                    //tbDadosMov.setRowSorter(new TableRowSorter(modelo)); // Organiza tabela clicando no tí­tulo da coluna
                    tbDadosMov.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(0)).setPreferredWidth(40); //idMov
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(1)).setPreferredWidth(60); //recurso
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(2)).setPreferredWidth(70); //vrecurso 
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(3)).setPreferredWidth(50); //clifor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(4)).setPreferredWidth(75); //vCliFor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(5)).setPreferredWidth(75); //dtlancto
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(6)).setPreferredWidth(75); //dtEmi
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(7)).setPreferredWidth(75); //dtVcto
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(8)).setPreferredWidth(110); //documento
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(9)).setPreferredWidth(70);  //classif
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(10)).setPreferredWidth(175); //Descr
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(11)).setPreferredWidth(80);  //Valor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(12)).setPreferredWidth(100); //dtApr
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(13)).setPreferredWidth(40); //statusMov
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(14)).setPreferredWidth(30); //Prev
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(15)).setPreferredWidth(80); //Saldo
                    DefaultTableCellRenderer tab = new DefaultTableCellRenderer();
                    tab.setHorizontalAlignment(SwingConstants.RIGHT);
                    tbDadosMov.getColumnModel().getColumn(11).setCellRenderer(tab);
                    tbDadosMov.getColumnModel().getColumn(15).setCellRenderer(tab);
                    while (rs.next()) {
                        sa = sa + rs.getDouble("Valor");
                        Date Apre = rs.getDate("dtApr");
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
                        modelo.addRow(new Object[]{rs.getString("idMov"),
                            rs.getString("recurso"),
                            rs.getString("vrecurso"),
                            rs.getString("clifor"),
                            rs.getString("vCliFor"),
                            formato_data.format(rs.getDate("dtlancto")),
                            formato_data.format(rs.getDate("dtEmi")),
                            formato_data.format(rs.getDate("dtVcto")),
                            rs.getString("documento"),
                            rs.getString("classif"),
                            rs.getString("Descr"),
                            df.format(rs.getDouble("Valor")),
                            AprFormatada,
                            rs.getString("statusMov"),
                            rs.getString("Prev"),
                            df.format(sa),});
                    }
                    rs.close();
                    con.close();
                } else if (tfNrRecurso.getText().equals("0022")) {
                    String sql = "SELECT * FROM tbmovimento WHERE recurso = ? AND statusMov <> 'RC' ORDER BY dtVcto";
                    PreparedStatement stt = con.prepareStatement(sql);
                    stt.setString(1, tfNrRecurso.getText());
                    ResultSet rs = stt.executeQuery();
                    double somaval = 0;
                    tfSaldoAnterior.setText(String.valueOf(somaval));
                    DefaultTableModel modelo = (DefaultTableModel) tbDadosMov.getModel();
                    modelo.setNumRows(0);
                    //tbDadosMov.setRowSorter(new TableRowSorter(modelo)); // Organiza tabela clicando no tÃ­tulo da coluna
                    tbDadosMov.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(0)).setPreferredWidth(40); //idMov
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(1)).setPreferredWidth(60); //recurso
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(2)).setPreferredWidth(70); //vrecurso 
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(3)).setPreferredWidth(50); //clifor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(4)).setPreferredWidth(75); //vCliFor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(5)).setPreferredWidth(75); //dtlancto
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(6)).setPreferredWidth(75); //dtEmi
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(7)).setPreferredWidth(75); //dtVcto
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(8)).setPreferredWidth(110); //documento
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(9)).setPreferredWidth(70);  //classif
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(10)).setPreferredWidth(175); //Descr
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(11)).setPreferredWidth(80);  //Valor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(12)).setPreferredWidth(100); //dtApr
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(13)).setPreferredWidth(40); //statusMov
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(14)).setPreferredWidth(30); //Prev
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(15)).setPreferredWidth(80); //Saldo
                    DefaultTableCellRenderer tab = new DefaultTableCellRenderer();
                    tab.setHorizontalAlignment(SwingConstants.RIGHT);
                    tbDadosMov.getColumnModel().getColumn(11).setCellRenderer(tab);
                    tbDadosMov.getColumnModel().getColumn(15).setCellRenderer(tab);
                    while (rs.next()) {
                        sa = sa + rs.getDouble("Valor");
                        Date Apre = rs.getDate("dtApr");
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
                        modelo.addRow(new Object[]{rs.getString("idMov"),
                            rs.getString("recurso"),
                            rs.getString("vrecurso"),
                            rs.getString("clifor"),
                            rs.getString("vCliFor"),
                            formato_data.format(rs.getDate("dtlancto")),
                            formato_data.format(rs.getDate("dtEmi")),
                            formato_data.format(rs.getDate("dtVcto")),
                            rs.getString("documento"),
                            rs.getString("classif"),
                            rs.getString("Descr"),
                            df.format(rs.getDouble("Valor")),
                            AprFormatada,
                            rs.getString("statusMov"),
                            rs.getString("Prev"),
                            df.format(sa),});
                    }
                    tfSaldoAnterior.setText(String.valueOf(somaval));
                    rs.close();
                    con.close();
                } else {
                    String sql = "SELECT * FROM tbmovimento WHERE recurso = ?  ORDER BY dtVcto";
                    PreparedStatement stt = con.prepareStatement(sql);
                    stt.setString(1, tfNrRecurso.getText());
                    ResultSet rs = stt.executeQuery();
                    double somaval = 0;
                    tfSaldoAnterior.setText(String.valueOf(somaval));
                    DefaultTableModel modelo = (DefaultTableModel) tbDadosMov.getModel();
                    modelo.setNumRows(0);
                    //tbDadosMov.setRowSorter(new TableRowSorter(modelo)); // Organiza tabela clicando no tÃ­tulo da coluna
                    tbDadosMov.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(0)).setPreferredWidth(40); //idMov
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(1)).setPreferredWidth(60); //recurso
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(2)).setPreferredWidth(70); //vrecurso 
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(3)).setPreferredWidth(50); //clifor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(4)).setPreferredWidth(75); //vCliFor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(5)).setPreferredWidth(75); //dtlancto
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(6)).setPreferredWidth(75); //dtEmi
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(7)).setPreferredWidth(75); //dtVcto
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(8)).setPreferredWidth(110); //documento
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(9)).setPreferredWidth(70);  //classif
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(10)).setPreferredWidth(175); //Descr
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(11)).setPreferredWidth(80);  //Valor
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(12)).setPreferredWidth(100); //dtApr
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(13)).setPreferredWidth(40); //statusMov
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(14)).setPreferredWidth(30); //Prev
                    tbDadosMov.getColumn(tbDadosMov.getColumnName(15)).setPreferredWidth(80); //Saldo
                    DefaultTableCellRenderer tab = new DefaultTableCellRenderer();
                    tab.setHorizontalAlignment(SwingConstants.RIGHT);
                    tbDadosMov.getColumnModel().getColumn(11).setCellRenderer(tab);
                    tbDadosMov.getColumnModel().getColumn(15).setCellRenderer(tab);
                    while (rs.next()) {
                        sa = sa + rs.getDouble("Valor");
                        Date Apre = rs.getDate("dtApr");
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
                        modelo.addRow(new Object[]{rs.getString("idMov"),
                            rs.getString("recurso"),
                            rs.getString("vrecurso"),
                            rs.getString("clifor"),
                            rs.getString("vCliFor"),
                            formato_data.format(rs.getDate("dtlancto")),
                            formato_data.format(rs.getDate("dtEmi")),
                            formato_data.format(rs.getDate("dtVcto")),
                            rs.getString("documento"),
                            rs.getString("classif"),
                            rs.getString("Descr"),
                            df.format(rs.getDouble("Valor")),
                            AprFormatada,
                            rs.getString("statusMov"),
                            rs.getString("Prev"),
                            df.format(sa),});
                    }
                    rs.close();
                    con.close();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
        }
        jComboBoxFavorecidos.requestFocus();
    }
}
