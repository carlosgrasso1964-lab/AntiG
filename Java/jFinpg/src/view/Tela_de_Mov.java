package view;

import utilitarios.Conexao;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.math.BigDecimal;
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
    CarregarCbx3 r3 = new CarregarCbx3(); //Carrega combobox de ClassificaÃ§Ã£o por Conta
    CarregarCbxBancos r4 = new CarregarCbxBancos();
    MaskFormatter mfData;

    public Tela_de_Mov() throws SQLException {
        initComponents();  // ? OBRIGATÓRIO PRIMEIRO!

        // === 1. CARREGAR TODAS AS COMBOBOXES ===
        // Carregar comboboxes
        // Favorecidos
        re.CarregarCbx("tbclifor", "apelidoclifor", jComboBoxFavorecidos);
        // Recursos
        r2.CarregarCbx2("tbrecursos", "nomebco", jComboBoxRecurso);
        // Classificação
        r3.CarregarCbx3("gpprincipal", "nome_c", cbxClass);
        // Bancos (Destino)

        // === 2. MÁSCARA DE DATA ===
        try {
            mfData = new MaskFormatter("##/##/####");
            JFormattedTextField ftfApresentacao = new JFormattedTextField(mfData);
            // Aplique onde precisar (ex: tfApresentacao.setFormatterFactory(...))
        } catch (ParseException ex) {
            System.out.println("Erro na máscara de data");
        }

        // === 3. ACTION LISTENER DO BANCO DESTINO ===
        jComboBoxBancoDestino.addActionListener(evt -> jComboBoxBancoDestinoActionPerformed(evt));

        // === 4. DATA PADRÃO (-60 dias) ===
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        //c.add(Calendar.DATE, -60);
        tfLancto.setText(df.format(c.getTime()));

        // === 5. OCULTAR CAMPOS DO BANCO DESTINO ===
//        jLabelBancoDestino.setVisible(false);
//        jComboBoxBancoDestino.setVisible(false);
//        tfNrBancoDestino.setVisible(false);
//        tfBancoDestino.setVisible(false);
        // === AGORA SIM: jComboBoxBancoDestino EXISTE! ===
        try {
            mfData = new MaskFormatter("##/##/####");
        } catch (ParseException ex) {
            System.out.println("Ocorreu um erro ao criar máscara para Apresentação");
        }

        // === CARREGAR BANCOS ===
        jComboBoxBancoDestino.removeAllItems();
        jComboBoxBancoDestino.addItem(""); // Opção vazia

        String sqlBancos = "SELECT nomebco, codigo, fk_gpprinc FROM tbrecursos WHERE fk_gpprinc LIKE '1.%' ORDER BY nomebco";
        try (Connection con = Conexao.faz_conexao(); PreparedStatement stmt = con.prepareStatement(sqlBancos); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String item = rs.getString("nomebco") + "-" + rs.getString("codigo") + "-" + rs.getString("fk_gpprinc");
                jComboBoxBancoDestino.addItem(item);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao carregar bancos: " + ex.getMessage());
            ex.printStackTrace();
        }

        // === ACTION LISTENER ===
        jComboBoxBancoDestino.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxBancoDestinoActionPerformed(evt);
            }
        });

        // === DATA PADRÃO ===
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        //    Calendar c = Calendar.getInstance();
        //c.add(Calendar.DATE, -60);
        tfLancto.setText(dateFormat.format(c.getTime()));

        // === OCULTAR CAMPOS ===
        jLabelBancoDestino.setVisible(false);
        jComboBoxBancoDestino.setVisible(false);
        tfNrBancoDestino.setVisible(false);
        tfBancoDestino.setVisible(false);
        try {
            mfData = new MaskFormatter("##/##/####");
        } catch (ParseException ex) {
            System.out.println("Erro na máscara");
        }
    }

    private void jComboBoxBancoDestinoActionPerformed(java.awt.event.ActionEvent evt) {

        String selected = (String) jComboBoxBancoDestino.getSelectedItem();

        if (selected == null || selected.trim().isEmpty() || selected.equals("")) {
            tfNrBancoDestino.setText("");
            tfBancoDestino.setText("");
            return;
        }

        try {
            // Formato: "BRADESCO-0237-1.001.001"
            int lastDash = selected.lastIndexOf('-');
            int secondLastDash = selected.lastIndexOf('-', lastDash - 1);

            String codigo = selected.substring(secondLastDash + 1, lastDash);
            String vrecurso = selected.substring(lastDash + 1);

            tfNrBancoDestino.setText(codigo);
            tfBancoDestino.setText(vrecurso);

            //System.out.println("Banco selecionado: " + codigo + " | " + vrecurso);
        } catch (Exception e) {
            tfNrBancoDestino.setText("");
            tfBancoDestino.setText("");
            System.out.println("Erro ao processar banco: " + e.getMessage());
        }
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
        jPanelE = new javax.swing.JPanel();
        jLabelBancoDestino = new javax.swing.JLabel();
        jComboBoxBancoDestino = new javax.swing.JComboBox<>();
        tfNrBancoDestino = new javax.swing.JTextField();
        tfBancoDestino = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();

        jMenuItem2.setText("jMenuItem2");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("       Movimentação Financeira - PostgreSQL");
        setUndecorated(true);
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
                .addGap(172, 172, 172)
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

        tfValor.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfValorFocusLost(evt);
            }
        });
        tfValor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tfValorKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfValorKeyTyped(evt);
            }
        });

        jLabel14.setText("Status");

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

        jLabelBancoDestino.setText("Bco. Destino");

        tfNrBancoDestino.setEnabled(false);

        tfBancoDestino.setEnabled(false);

        javax.swing.GroupLayout jPanelELayout = new javax.swing.GroupLayout(jPanelE);
        jPanelE.setLayout(jPanelELayout);
        jPanelELayout.setHorizontalGroup(
            jPanelELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelELayout.createSequentialGroup()
                .addComponent(jLabelBancoDestino, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxBancoDestino, javax.swing.GroupLayout.PREFERRED_SIZE, 481, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfNrBancoDestino, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfBancoDestino, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelELayout.setVerticalGroup(
            jPanelELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jComboBoxBancoDestino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabelBancoDestino)
                .addComponent(tfNrBancoDestino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(tfBancoDestino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addComponent(tfRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCbxTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(290, 290, 290)
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
                    .addGroup(layout.createSequentialGroup()
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
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(25, 25, 25)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfDescricao)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jComboBoxRecurso, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(tfLancto, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfEmissao, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfApresentacao, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(30, 30, 30)
                                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfValor, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCbxTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
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

        try {
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy");

            // === DATAS ===
            java.util.Date dtLanctoUtil = formatoEntrada.parse(tfLancto.getText());
            java.sql.Date dtLancto = new java.sql.Date(dtLanctoUtil.getTime());

            java.util.Date dtEmiUtil = formatoEntrada.parse(tfEmissao.getText());
            java.sql.Date dtEmi = new java.sql.Date(dtEmiUtil.getTime());

            java.util.Date dtVctoUtil = formatoEntrada.parse(tfVencimento.getText());
            java.sql.Date dtVcto = new java.sql.Date(dtVctoUtil.getTime());

            // === DATA DE APRESENTAÇÃO (OPCIONAL) ===
            java.sql.Date dtApr = null;
            String dataApStr = tfApresentacao != null ? tfApresentacao.getText().trim() : "";

            // === DATA DE APRESENTAÇÃO (OPCIONAL) ===
            //java.sql.Date dtApr = null;
            //String dataApStr = tfApresentacao != null ? tfApresentacao.getText() : "";
            // Remove espaços e caracteres de máscara
            dataApStr = dataApStr.replaceAll("[^0-9/]", "");  // Só deixa números e /

            if (dataApStr.isEmpty()
                    || dataApStr.equals("//")
                    || dataApStr.length() < 8
                    || dataApStr.matches(".*_.*")) {  // ainda tem _ da máscara

                dtApr = null; // Ignora campo vazio ou inválido
            } else {
                try {
                    // Garante formato dd/MM/yyyy
                    if (dataApStr.length() == 8 && !dataApStr.contains("/")) {
                        dataApStr = dataApStr.substring(0, 2) + "/"
                                + dataApStr.substring(2, 4) + "/"
                                + dataApStr.substring(4);
                    }

                    java.util.Date dtAprUtil = formatoEntrada.parse(dataApStr);
                    dtApr = new java.sql.Date(dtAprUtil.getTime());
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(null,
                            "Data de Apresentação inválida: '" + tfApresentacao.getText() + "'\n"
                            + "Use o formato: dd/MM/yyyy (ex: 15/03/2025)");
                    return;
                }
            }

            // === VALOR ===
            String valorTexto = tfValor.getText().replaceAll("[^0-9,-]", "").replace(".", "").replace(",", ".");
            double valorOriginal = Double.parseDouble(valorTexto);
            double valor = valorOriginal;
            String tipo = (String) jCbxTipo.getSelectedItem();
            if ("S".equals(tipo) && valor > 0) {
                valor = -valorOriginal;
            }
            String recurso = tfRecurso.getText().trim();

            if ("2.001.004".equals(recurso) || "2.002.001".equals(recurso)) {
                jLabelBancoDestino.setVisible(true);
                jComboBoxBancoDestino.setVisible(true);
                tfNrBancoDestino.setVisible(true);
                tfBancoDestino.setVisible(true);
            } else {
                jLabelBancoDestino.setVisible(false);
                jComboBoxBancoDestino.setVisible(false);
                tfNrBancoDestino.setVisible(false);
                tfBancoDestino.setVisible(false);
            }

            Connection con = Conexao.faz_conexao();
            String sql = "INSERT INTO tbmovimento(recurso, vrecurso, \"clifor\", \"vclifor\", dtlancto, \"dtemi\", \"dtvcto\", documento, classif, \"descr\", \"valor\", \"dtapr\", \"statusmov\", \"prev\") "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // ===================================================================
            // 1. CARTÃO DE CRÉDITO (2.001.003)
            // ===================================================================
            if ("2.001.003".equals(recurso)) {
                // --- 1. Lançamento a Pagar (0079) ---
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, "0079");
                    stmt.setString(2, "2.001.003");
                    stmt.setString(3, tfNrFav.getText());
                    stmt.setString(4, tfFavorecidos.getText());
                    stmt.setDate(5, dtLancto);
                    stmt.setDate(6, dtEmi);
                    stmt.setDate(7, dtVcto);
                    stmt.setString(8, tfDocumento.getText());
                    stmt.setString(9, tfClass.getText());
                    stmt.setString(10, tfDescricao.getText());
                    stmt.setDouble(11, -valorOriginal); // Sempre negativo
                    stmt.setDate(12, dtApr);
                    stmt.setString(13, "PG");
                    stmt.setString(14, "V");
                    stmt.execute();
                }

                // --- 2. Pagamento (baixa da dívida) ---
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, "0079");
                    stmt.setString(2, "2.001.003");
                    stmt.setString(3, tfNrFav.getText());
                    stmt.setString(4, tfFavorecidos.getText());
                    stmt.setDate(5, dtLancto);
                    stmt.setDate(6, dtEmi);
                    stmt.setDate(7, dtVcto);
                    stmt.setString(8, tfDocumento.getText());
                    stmt.setString(9, "9.001.003");
                    stmt.setString(10, tfDescricao.getText());
                    stmt.setDouble(11, valorOriginal); // Positivo
                    stmt.setDate(12, dtApr);
                    stmt.setString(13, "PG");
                    stmt.setString(14, "V");
                    stmt.execute();
                }

                // --- 3. Saída pelo Cartão ---
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, tfNrRecurso.getText());
                    stmt.setString(2, "2.001.003");
                    stmt.setString(3, tfNrFav.getText());
                    stmt.setString(4, tfFavorecidos.getText());
                    stmt.setDate(5, dtLancto);
                    stmt.setDate(6, dtEmi);
                    stmt.setDate(7, dtVcto);
                    stmt.setString(8, tfDocumento.getText());
                    stmt.setString(9, "9.001.003");
                    stmt.setString(10, tfDescricao.getText());
                    stmt.setDouble(11, valor);
                    stmt.setDate(12, null);
                    stmt.setString(13, "");
                    stmt.setString(14, "V");
                    stmt.execute();
                }

                JOptionPane.showMessageDialog(null, "Cartão de Crédito lançado com sucesso!");

                // ===================================================================
                // 2. EMPRÉSTIMO (2.001.004 ou 2.002.001)
                // ===================================================================
            } else if ("2.001.004".equals(recurso) || "2.002.001".equals(recurso)) {
                double valorAbs = Math.abs(valorOriginal);

                // --- 1. Entrada no Banco ---
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, tfNrBancoDestino.getText());
                    stmt.setString(2, tfBancoDestino.getText());
                    stmt.setString(3, tfNrFav.getText());
                    stmt.setString(4, tfFavorecidos.getText());
                    stmt.setDate(5, dtLancto);
                    stmt.setDate(6, dtEmi);
                    stmt.setDate(7, dtLancto);
                    stmt.setString(8, tfDocumento.getText());
                    stmt.setString(9, "3.003.005");
                    stmt.setString(10, "Empréstimo Obtido - " + tfDescricao.getText());
                    stmt.setDouble(11, valorAbs);
                    stmt.setDate(12, dtLancto);
                    stmt.setString(13, "RC");
                    stmt.setString(14, "V");
                    stmt.execute();
                }

                // --- 2. Passivo (0080) ---
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, "0080");
                    stmt.setString(2, recurso);
                    stmt.setString(3, tfNrFav.getText());
                    stmt.setString(4, tfFavorecidos.getText());
                    stmt.setDate(5, dtLancto);
                    stmt.setDate(6, dtEmi);
                    stmt.setDate(7, dtVcto);
                    stmt.setString(8, tfDocumento.getText());
                    stmt.setString(9, "4.008.011");
                    stmt.setString(10, "Obrigação Empréstimo - " + tfDescricao.getText());
                    stmt.setDouble(11, -valorAbs);
                    stmt.setDate(12, dtApr);
                    stmt.setString(13, dtApr != null ? "PG" : "");
                    stmt.setString(14, "V");
                    stmt.execute();
                }

                // --- 3. Contas a Pagar (0079) ---
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, "0079");
                    stmt.setString(2, "2.001.004");
                    stmt.setString(3, tfNrFav.getText());
                    stmt.setString(4, tfFavorecidos.getText());
                    stmt.setDate(5, dtLancto);
                    stmt.setDate(6, dtEmi);
                    stmt.setDate(7, dtVcto);
                    stmt.setString(8, tfDocumento.getText());
                    stmt.setString(9, "9.001.003");
                    stmt.setString(10, "Empréstimo à Pagar - " + tfDescricao.getText());
                    stmt.setDouble(11, -valorAbs);
                    stmt.setDate(12, dtApr);
                    stmt.setString(13, dtApr != null ? "PG" : "");
                    stmt.setString(14, "V");
                    stmt.execute();
                }

                // --- 4. Se já foi pago (dtApr preenchida) ---
                if (dtApr != null) {
                    // Baixa da dívida (0079)
                    try (PreparedStatement stmt = con.prepareStatement(sql)) {
                        stmt.setString(1, "0079");
                        stmt.setString(2, "2.001.005");
                        stmt.setString(3, tfNrFav.getText());
                        stmt.setString(4, tfFavorecidos.getText());
                        stmt.setDate(5, dtLancto);
                        stmt.setDate(6, dtEmi);
                        stmt.setDate(7, dtVcto);
                        stmt.setString(8, tfDocumento.getText());
                        stmt.setString(9, "9.001.003");
                        stmt.setString(10, "Pagamento Empréstimo - " + tfDescricao.getText());
                        stmt.setDouble(11, valorAbs);
                        stmt.setDate(12, dtApr);
                        stmt.setString(13, "PG");
                        stmt.setString(14, "V");
                        stmt.execute();
                    }

                    // Saída do banco
                    try (PreparedStatement stmt = con.prepareStatement(sql)) {
                        stmt.setString(1, tfNrBancoDestino.getText());
                        stmt.setString(2, tfBancoDestino.getText());
                        stmt.setString(3, tfNrFav.getText());
                        stmt.setString(4, tfFavorecidos.getText());
                        stmt.setDate(5, dtLancto);
                        stmt.setDate(6, dtEmi);
                        stmt.setDate(7, dtApr);
                        stmt.setString(8, tfDocumento.getText());
                        stmt.setString(9, "9.001.003");
                        stmt.setString(10, "Saída Pagto Empréstimo - " + tfDescricao.getText());
                        stmt.setDouble(11, -valorAbs);
                        stmt.setDate(12, dtApr);
                        stmt.setString(13, "PG");
                        stmt.setString(14, "V");
                        stmt.execute();
                    }

                    // Baixa do Passivo
                    try (PreparedStatement stmt = con.prepareStatement(sql)) {
                        stmt.setString(1, "0080");
                        stmt.setString(2, recurso);
                        stmt.setString(3, tfNrFav.getText());
                        stmt.setString(4, tfFavorecidos.getText());
                        stmt.setDate(5, dtLancto);
                        stmt.setDate(6, dtEmi);
                        stmt.setDate(7, dtApr);
                        stmt.setString(8, tfDocumento.getText());
                        stmt.setString(9, "9.001.003");
                        stmt.setString(10, "Baixa Obrig. Empréstimo - " + tfDescricao.getText());
                        stmt.setDouble(11, valorAbs);
                        stmt.setDate(12, dtApr);
                        stmt.setString(13, "PG");
                        stmt.setString(14, "V");
                        stmt.execute();
                    }
                }

                JOptionPane.showMessageDialog(null, "Empréstimo lançado com sucesso!");

                // ===================================================================
                // 3. LANÇAMENTO SIMPLES
                // ===================================================================
            } else {
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, tfNrRecurso.getText());
                    stmt.setString(2, tfRecurso.getText());
                    stmt.setString(3, tfNrFav.getText());
                    stmt.setString(4, tfFavorecidos.getText());
                    stmt.setDate(5, dtLancto);
                    stmt.setDate(6, dtEmi);
                    stmt.setDate(7, dtVcto);
                    stmt.setString(8, tfDocumento.getText());
                    stmt.setString(9, tfClass.getText());
                    stmt.setString(10, tfDescricao.getText());
                    stmt.setDouble(11, valor);
                    stmt.setDate(12, dtApr);
                    stmt.setString(13, tfStatusMov.getText());
                    stmt.setString(14, tfPrev.getText());
                    stmt.execute();
                }
                JOptionPane.showMessageDialog(null, "Movimentação lançada com sucesso!");
            }

            // === ATUALIZA TELA ===
            new Tela_de_Mov().setVisible(true);
            dispose();
            JRefresh();
            ListaDados();

            // === LIMPA CAMPOS ===
            limparCampos();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao salvar: " + ex.getMessage());
        } finally {
            try {
                if (Conexao.faz_conexao() != null) {
                    Conexao.faz_conexao().close();
                }
            } catch (Exception e) {
            }
        }
    }

// === MÉTODO AUXILIAR PARA LIMPAR CAMPOS ===
    private void limparCampos() {
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
        tfStatusMov.setText("");
        tfPrev.setText("");
        tfNrBancoDestino.setText("");
        tfBancoDestino.setText("");
        jCbxTipo.setSelectedIndex(0);
        cbxClass.setSelectedIndex(0);
        jComboBoxBancoDestino.setSelectedIndex(0);
    }//GEN-LAST:event_btSalvarActionPerformed

    private void btAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAbrirActionPerformed
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        if (tfBusca.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Informe um código válido...");
        } else {
            try ( // Código para pesquisar um usuario
                    Connection con = Conexao.faz_conexao()) {
                String sql = "SELECT * FROM tbmovimento WHERE \"idmov\"=?";
                PreparedStatement stmt = con.prepareStatement(sql);
                //stmt.setString(1, tfBusca.getText());
                stmt.setInt(1, Integer.parseInt(tfBusca.getText()));
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        tfRegistro.setText(rs.getString("idmov"));
                        tfNrRecurso.setText(rs.getString("recurso"));
                        tfRecurso.setText(rs.getString("vrecurso"));
                        tfNrFav.setText(rs.getString("clifor"));
                        tfFavorecidos.setText(rs.getString("vclifor"));
                        tfLancto.setText(sdf.format(rs.getDate("dtlancto")));
                        tfEmissao.setText(sdf.format(rs.getDate("dtemi")));
                        tfVencimento.setText(sdf.format(rs.getDate("dtvcto")));
                        tfDocumento.setText(rs.getString("documento"));
                        tfClass.setText(rs.getString("classif"));
                        tfDescricao.setText(rs.getString("descr"));
                        tfValor.setText(rs.getString(String.valueOf("valor")));
                        Double ver = 0.00;
                        ver = Double.valueOf(tfValor.getText().replaceAll(",", "."));
                        if (ver > 0) {
                            jButtonPag.setEnabled(false);
                        } else {
                            jButtonReceb.setEnabled(false);
                        }
                        if (rs.getDate("dtapr") == null) {
                            tfApresentacao.setText(null);
                        } else {
                            tfApresentacao.setText(sdf.format(rs.getDate("dtapr")));
                        }
                        tfStatusMov.setText(rs.getString("statusmov"));
                        tfPrev.setText(rs.getString("prev"));
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
        if (jCbxTipo.getSelectedItem().equals(t)) {
            if (Double.parseDouble(tfValor.getText().replaceAll(",", ".")) >= 0) {
                tfValor.setText("-" + tfValor.getText());
            }
        } else if (tfRegistro.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Informe o Código");
        } else {
            try {
                Connection con = Conexao.faz_conexao();
                SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");
                String sql = "UPDATE tbmovimento SET \"recurso\"=?,\"vrecurso\"=?,\"clifor\"=?,\"vclifor\"=?,dtlancto=?,\"dtemi\"=?,\"dtvcto\"=?,documento=?,classif=?,\"descr\"=?,\"valor\"=?,\"dtapr\"=?,\"statusmov\"=?,\"prev\"=? WHERE idmov=?";
                PreparedStatement stmt = con.prepareStatement(sql);
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

                // Obter o valor do JTextField
                String valorTexto = tfValor.getText();

                // Remover os pontos de separação de milhares, mas manter o sinal negativo
                valorTexto = valorTexto.replaceAll("[^\\d,\\-]", "");

                // Substituir a vírgula decimal por ponto decimal
                valorTexto = valorTexto.replace(",", ".");

                // Parse para Double
                double valor = Double.parseDouble(valorTexto);

                // Definir o valor no PreparedStatement
                stmt.setDouble(11, valor);

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
                // Convert tfRegistro para Integer
                int idmov = Integer.parseInt(tfRegistro.getText());
                stmt.setInt(15, idmov);
                stmt.execute();
                DefaultTableModel modelo = (DefaultTableModel) tbDadosMov.getModel();
                modelo.setRowCount(0);
                stmt.close();
                con.close();
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
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnAtualizarActionPerformed

    @SuppressWarnings("deprecation")
    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed
        int[] rows = tbDadosMov.getSelectedRows();
        int rowCount = rows.length;
        Object[] options = {"Sim", "Não"};
        String message;

        // Definição da mensagem de confirmação
        if (rowCount > 1) {
            message = "Deseja excluir os " + rowCount + " registros selecionados?";
        } else if (rowCount == 1 || !tfRegistro.getText().trim().equals("")) {
            message = "Deseja Excluir o Registro?";
        } else {
            JOptionPane.showMessageDialog(null, "Selecione ao menos um registro para excluir.");
            return;
        }

        int i = JOptionPane.showOptionDialog(null, message, "Exclusão",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (i == JOptionPane.YES_OPTION) {
            try {
                Connection con = Conexao.faz_conexao();
                String sql = "DELETE FROM tbmovimento WHERE \"idmov\"=?";
                PreparedStatement stmt = con.prepareStatement(sql);

                // DICA DE SEGURANÇA: Validar se a conversão para número é possível
                try {
                    if (rowCount > 0) {
                        for (int row : rows) {
                            Object idValue = tbDadosMov.getValueAt(row, 0);
                            if (idValue != null) {
                                // Converte para int antes de enviar ao Postgres
                                int id = Integer.parseInt(idValue.toString());
                                stmt.setInt(1, id);
                                stmt.addBatch();
                            }
                        }
                        stmt.executeBatch();
                    } else {
                        // Converte o texto do campo para int
                        int id = Integer.parseInt(tfRegistro.getText().trim());
                        stmt.setInt(1, id);
                        stmt.execute();
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Erro: O ID do registro deve ser um número válido.");
                    return; // Interrompe a execução se o ID for inválido
                }

                stmt.close();
                con.close();

                // Mensagem de sucesso e atualização da tela
                if (rowCount > 1) {
                    JOptionPane.showMessageDialog(null, rowCount + " movimentos excluídos com sucesso!");
                } else {
                    JOptionPane.showMessageDialog(null, "Movimento excluído com sucesso!");
                }

                limparCamposExcluir(); // Chame um método de limpeza para organizar o código
                JRefresh();

            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "Erro de Banco de Dados: " + ex.getMessage());
            }
        }
    }

    // Método auxiliar para manter o código limpo
    private void limparCamposExcluir() {
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
        String selectedValue = (String) jComboBoxRecurso.getSelectedItem();
        if (selectedValue == null || selectedValue.trim().isEmpty() || selectedValue.equals("Selecione")) {
            tfNrRecurso.setText("");
            tfRecurso.setText("");
            ocultarCamposBancoDestino();
            return;
        }

        try {
            // Formato: "EMPRÉSTIMOS OBTIDOS C.PRAZO-0080-2.001.004"
            int lastDash = selectedValue.lastIndexOf('-');
            int secondLastDash = selectedValue.lastIndexOf('-', lastDash - 1);
            String codigo = selectedValue.substring(secondLastDash + 1, lastDash);
            String vrecurso = selectedValue.substring(lastDash + 1);
            tfNrRecurso.setText(codigo);
            tfRecurso.setText(vrecurso);
            //System.out.println("Recurso: " + codigo + " | " + vrecurso);
            if (vrecurso.equals("2.001.004") || vrecurso.equals("2.002.001")) {
                mostrarCamposBancoDestino();
            } else {
                ocultarCamposBancoDestino();
            }
        } catch (Exception e) {
            tfNrRecurso.setText("");
            tfRecurso.setText("");
            ocultarCamposBancoDestino();
            System.out.println("Erro no recurso: " + e.getMessage());
        }
    }//GEN-LAST:event_jComboBoxRecursoActionPerformed

    private void ocultarCamposBancoDestino() {
        jLabelBancoDestino.setVisible(false);
        jComboBoxBancoDestino.setVisible(false);
        tfNrBancoDestino.setText("");
        tfBancoDestino.setText("");
        if (jComboBoxBancoDestino.getItemCount() > 0) {
            jComboBoxBancoDestino.setSelectedIndex(0);
        }
    }

    private void mostrarCamposBancoDestino() {
        jLabelBancoDestino.setVisible(true);
        jComboBoxBancoDestino.setVisible(true);
        jComboBoxBancoDestino.requestFocus();
    }

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

    @SuppressWarnings("unchecked")
    private void jComboBoxRecursoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jComboBoxRecursoKeyPressed
        //DecimalFormat dec = new DecimalFormat("#,##0.00");
        Connection con;
        try {
            con = Conexao.faz_conexao();
            SimpleDateFormat formato_data = new SimpleDateFormat("dd/MM/yyyy");
            DecimalFormat df = new DecimalFormat("#,##0.00");
            //String ssa = tfSaldoAnterior.getText();
            Double sa = 0.00;
            /*if (tfLancto.getText().trim().length() == 4) {
                JOptionPane.showMessageDialog(null, "Atenção! Favor preenche o campo de data Lançamento.");
            } else {*/
            if (tfNrRecurso.getText().equals("0079")) {
                //String sql = "SELECT * FROM tbmovimento WHERE recurso = ? AND \"statusmov\" not in ('PG') ORDER BY \"dtvcto\"";
                String sql = """
                    SELECT *
                    FROM tbmovimento
                    WHERE statusmov NOT IN ('PG', 'TD', 'SI')
                      AND (
                          recurso = ?
                          OR vrecurso = '2.001.003'
                      )
                    ORDER BY dtvcto, recurso, dtemi
                    """;
                PreparedStatement stt = con.prepareStatement(sql);
                stt.setString(1, tfNrRecurso.getText());
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stt.executeQuery();
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
                rs.close();
                con.close();

            } else if (tfNrRecurso.getText().equals("0080")) {
                jLabelBancoDestino.setVisible(true);
                jComboBoxBancoDestino.setVisible(true);
                tfNrBancoDestino.setVisible(true);
                tfBancoDestino.setVisible(true);

            } else if (tfNrRecurso.getText().equals("0022")) {
                String sql = "SELECT * FROM tbmovimento WHERE recurso = ? AND \"statusmov\" <> 'RC' ORDER BY \"dtvcto\"";
                PreparedStatement stt = con.prepareStatement(sql);
                stt.setString(1, tfNrRecurso.getText());
                ResultSet rs = stt.executeQuery();
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
                tfSaldoAnterior.setText(String.valueOf(somaval));
                rs.close();
                con.close();
            } else {
                String sql = "SELECT * FROM tbmovimento WHERE recurso = ?  ORDER BY \"dtvcto\"";
                PreparedStatement stt = con.prepareStatement(sql);
                stt.setString(1, tfNrRecurso.getText());
                ResultSet rs = stt.executeQuery();
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
                    modelo.addRow(new Object[]{rs.getString("idmov"),
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
                rs.close();
                con.close();
            }
            //}
        } catch (SQLException ex) {
            Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            jComboBoxFavorecidos.requestFocus();
        }
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
        // Validação: data de apresentação deve estar preenchida
        String dataApresentacao = tfApresentacao.getText().trim();
        if (dataApresentacao.isEmpty() || dataApresentacao.equals("  /  /    ")) {
            JOptionPane.showMessageDialog(null, "Atenção! Favor preencher o campo data de Apresentação.");
            return;
        }
        // === APENAS PASSA OS DADOS PARA Tela_Pagar ===
        // Não ALTERA NADA NO BANCO AINDA!
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
        Tela_Pagar tela = null;
        try {
            tela = new Tela_Pagar();
            tela.setVisible(true);
            this.setVisible(false); // Esconde Tela_Mov
        } catch (SQLException ex) {
            Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Erro ao abrir tela de pagamento.");
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
        //validarValor();
    }//GEN-LAST:event_tfValorKeyTyped

    private void tfValorFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfValorFocusLost
    }//GEN-LAST:event_tfValorFocusLost

    private void tfStatusMovFocusLost(java.awt.event.FocusEvent evt) {
        validarStatusMov();
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
    private javax.swing.JComboBox<String> jComboBoxBancoDestino;
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
    private javax.swing.JLabel jLabelBancoDestino;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelE;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tbDadosMov;
    private javax.swing.JFormattedTextField tfApresentacao;
    private javax.swing.JTextField tfBancoDestino;
    private javax.swing.JTextField tfBusca;
    private javax.swing.JTextField tfClass;
    private javax.swing.JTextField tfDescricao;
    private javax.swing.JTextField tfDocumento;
    private javax.swing.JFormattedTextField tfEmissao;
    private javax.swing.JTextField tfFavorecidos;
    private javax.swing.JFormattedTextField tfLancto;
    private javax.swing.JTextField tfNrBancoDestino;
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
            String sql = "SELECT * FROM \"" + tabela + "\" ORDER BY \"apelidoclifor\"";
            Connection con = Conexao.faz_conexao();
            try {
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    combo.addItem(rs.getString(valor) + "-" + rs.getString("codclifor") + "-" + rs.getString("fkcliforgp"));
                }
                rs.close();
                stmt.close();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

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
                Logger.getLogger(Tela_de_Mov.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static class CarregarCbx3 {

        @SuppressWarnings({"empty-statement", "unchecked"})
        public void CarregarCbx3(String tabela, String valor, JComboBox<String> combo) throws SQLException {
            String sql = "SELECT \"cod_geral\", \"nome_p\", \"nome_s\", \"nome_c\" FROM \"" + tabela + "\" WHERE \"nome_c\" <> '-' ORDER BY \"nome_c\"";
            Connection con = Conexao.faz_conexao();
            try {
                PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    combo.addItem(rs.getString(valor) + "-" + (rs.getString("cod_Geral")));
                }
                rs.close();
                stmt.close();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static class CarregarCbxBancos {

        @SuppressWarnings({"empty-statement", "unchecked"})
        public void CarregarCbxBancos(String tabela, String valor, JComboBox combo, String fk_gpprinc) throws SQLException {
            String sql = "SELECT * FROM \"" + tabela + "\" WHERE \"fk_gpprinc\" = ? ORDER BY \"nomebco\"";
            //Conexao conexao = new Conexao();
            Connection con = Conexao.faz_conexao();
            try {
                //conexao.abrirConexao();
                //Connection con = conexao.getConexao();
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, fk_gpprinc);
                ResultSet rs = stmt.executeQuery();
                combo.addItem(""); // Opção vazia
                while (rs.next()) {
                    combo.addItem(rs.getString(valor) + "-" + rs.getString("codigo") + "-" + rs.getString("fk_gpprinc"));
                }
                rs.close();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
            }
//            } finally {
//                conexao.fecharConexao();
//            }
        }
    }

    @SuppressWarnings("unchecked")
    public void ListaDados() {
        try {
            Connection con = Conexao.faz_conexao();
            String sql = null;
            String recursoTexto = tfNrRecurso.getText();
            if (recursoTexto.isEmpty()) {
                sql = "Select * FROM tbmovimento ORDER BY idmov";
            } else {
                int pega = Integer.parseInt(recursoTexto);
                sql = "Select * FROM tbmovimento WHERE recurso = '" + pega + "'" + "ORDER BY idmov";
            }
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel modelo = (DefaultTableModel) tbDadosMov.getModel();
            modelo.setNumRows(0);
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
                    AprFormatada = sdf.format(Apre);
                }
                modelo.addRow(new Object[]{rs.getString("idmov"),
                    rs.getString("recurso"),
                    rs.getString("vrecurso"),
                    rs.getString("clifor"),
                    rs.getString("vclifor"),
                    sdf.format(rs.getDate("dtlancto")),
                    sdf.format(rs.getDate("dtemi")),
                    sdf.format(rs.getDate("dtvcto")),
                    rs.getString("documento"),
                    rs.getString("classif"),
                    rs.getString("descr"),
                    df.format(rs.getDouble("valor")),
                    AprFormatada,
                    rs.getString("statusmov"),
                    rs.getString("prev"),
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
            DecimalFormat df = new DecimalFormat("#,##0.00");
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
                modelo.addRow(new Object[]{rs.getString("idmov"),
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
                    rs.getString("statusMov"),
                    rs.getString("prev"),
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
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

        try (Connection con = Conexao.faz_conexao()) {
            String sql = "SELECT * FROM tbmovimento WHERE \"idmov\"=?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(tfBusca.getText()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tfRegistro.setText(rs.getString("idmov"));
                    tfNrRecurso.setText(rs.getString("recurso"));
                    selecionarItemComboBox(jComboBoxRecurso, rs.getString("recurso"));
                    tfRecurso.setText(rs.getString("vrecurso"));
                    tfNrFav.setText(rs.getString("clifor"));
                    tfFavorecidos.setText(rs.getString("vclifor"));
                    selecionarItemComboBox(jComboBoxFavorecidos, rs.getString("clifor"));
                    tfLancto.setText(sdf.format(rs.getDate("dtlancto")));
                    tfEmissao.setText(sdf.format(rs.getDate("dtemi")));
                    tfVencimento.setText(sdf.format(rs.getDate("dtvcto")));
                    tfDocumento.setText(rs.getString("documento"));
                    tfClass.setText(rs.getString("classif"));
                    tfDescricao.setText(rs.getString("descr"));

                    // Obter o valor como string
                    String valorString = rs.getString("valor");

                    // Verificar se o valor não é nulo e formatar
                    if (valorString != null) {
                        // Converter string para BigDecimal para evitar problemas de precisão
                        BigDecimal valor = new BigDecimal(valorString);

                        // Formatar o valor
                        String valorFormatado = decimalFormat.format(valor);

                        // Definir o valor formatado na TextField
                        tfValor.setText(valorFormatado);

                        // Converter o valor formatado de volta para Double para a lógica
                        Double ver = Double.valueOf(valorString.replaceAll(",", "."));
                        if (ver > 0) {
                            jButtonPag.setEnabled(false);
                        } else {
                            jButtonReceb.setEnabled(false);
                        }
                    } else {
                        // Se o valor for nulo, definir a TextField como vazia
                        tfValor.setText("");
                    }

                    if (rs.getDate("dtapr") == null) {
                        tfApresentacao.setText(null);
                    } else {
                        tfApresentacao.setText(sdf.format(rs.getDate("dtapr")));
                    }
                    tfStatusMov.setText(rs.getString("statusmov"));
                    tfPrev.setText(rs.getString("prev"));
                    btSalvar.setEnabled(false);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
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
                if (!Character.isDigit(c) && c != ',' && c != '.' && c != '-' && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
                if ((c == '.' || c == ',') && (textoAtual.contains(".") || textoAtual.contains(","))) {
                    e.consume();
                }
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
            String sql = "UPDATE tbmovimento SET \"recurso\"=?, \"vrecurso\"=?, \"clifor\"=?, \"vclifor\"=?, \"dtlancto\"=?, \"dtemi\"=?, \"dtvcto\"=?, \"documento\"=?, \"classif\"=?, \"descr\"=?, \"valor\"=?, \"dtapr\"=?, \"statusmov\"=?, \"prev\"=? WHERE \"idmov\"=?";
            PreparedStatement stmt = con.prepareStatement(sql);
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

            // Obter o valor do JTextField
            String valorTexto = tfValor.getText();

            // Remover os pontos de separação de milhares, mas manter o sinal negativo
            valorTexto = valorTexto.replaceAll("[^\\d,\\-]", "");

            // Substituir a vírgula decimal por ponto decimal
            valorTexto = valorTexto.replace(",", ".");

            // Parse para Double
            double valor = Double.parseDouble(valorTexto);

            // Definir o valor no PreparedStatement
            stmt.setDouble(11, valor);

            //stmt.setDouble(11, Double.parseDouble(tfValor.getText()));
            if (tfApresentacao.getText().isEmpty() || tfApresentacao.getText().equals("  /  /    ")) {
                stmt.setDate(12, null);
            } else {
                java.util.Date data_a = formato_data.parse(tfApresentacao.getText());
                stmt.setDate(12, new java.sql.Date(data_a.getTime()));
            }
            stmt.setString(13, tfStatusMov.getText());
            stmt.setString(14, tfPrev.getText());
            stmt.setInt(15, Integer.parseInt(tfRegistro.getText()));
            stmt.execute();
            DefaultTableModel modelo = (DefaultTableModel) tbDadosMov.getModel();
            modelo.setRowCount(0);
            stmt.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
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
            Double sa = 0.00;
            //if (tfLancto.getText().trim().length() == 4) {
            //    JOptionPane.showMessageDialog(null, "Atenção! Favor preenche o campo de data Lançamento.");
            //} else {
            if (tfNrRecurso.getText().equals("0079")) {
                String sql = "SELECT * FROM tbmovimento WHERE recurso = ? AND \"statusmov\" not in ('PG') ORDER BY \"dtvcto\"";
                PreparedStatement stt = con.prepareStatement(sql);
                stt.setString(1, tfNrRecurso.getText());
                PreparedStatement stmt = con.prepareStatement(sql);
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
                    modelo.addRow(new Object[]{rs.getString("idmov"),
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
                rs.close();
                con.close();
            } else if (tfNrRecurso.getText().equals("0022")) {
                String sql = "SELECT * FROM tbmovimento WHERE recurso = ? AND \"statusmov\" <> 'RC' ORDER BY \"dtvcto\"";
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
                    modelo.addRow(new Object[]{rs.getString("idmov"),
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
                tfSaldoAnterior.setText(String.valueOf(somaval));
                rs.close();
                con.close();
            } else {
                String sql = "SELECT * FROM tbmovimento WHERE recurso = ?  ORDER BY \"dtvcto\"";
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
                    modelo.addRow(new Object[]{rs.getString("idmov"),
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
                rs.close();
                con.close();
            }
            //}
        } catch (SQLException ex) {
            Logger.getLogger(Tela_de_Mov.class.getName()).log(Level.SEVERE, null, ex);
        }
        jComboBoxFavorecidos.requestFocus();
    }
}
