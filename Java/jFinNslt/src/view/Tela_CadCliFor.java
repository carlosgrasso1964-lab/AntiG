package view;


import utilitarios.Conexao;

import dao.CliForDAO;

import java.awt.event.ItemEvent;

import java.awt.event.ItemListener;

import java.io.FileNotFoundException;

import java.io.IOException;

import java.sql.Connection;

import java.sql.ResultSet;

import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.text.ParseException;

import java.util.List;

import java.util.logging.Level;

import java.util.logging.Logger;

import javax.swing.JOptionPane;

import javax.swing.table.DefaultTableModel;

import javax.swing.JComboBox;

import javax.swing.event.ListSelectionEvent;

import javax.swing.event.ListSelectionListener;

import javax.swing.table.TableRowSorter;

import model.CliFor;

import model.Placon;

import relatorios.RelCliFor;

import utilitarios.LimpaTela;



public class Tela_CadCliFor extends javax.swing.JFrame {



    private Conexao conexao = new Conexao();

    private String editingCodCliFor = null; // Para rastrear se estamos editando



    @SuppressWarnings({"unchecked", "unchecked"})

    public void listar() throws SQLException, ClassNotFoundException {

        CliForDAO dao = new CliForDAO();

        List<CliFor> lista = dao.Listar();

        DefaultTableModel dados = (DefaultTableModel) tbDadosCliFor.getModel();

        tbDadosCliFor.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(0)).setPreferredWidth(40);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(1)).setPreferredWidth(40);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(2)).setPreferredWidth(300);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(3)).setPreferredWidth(200);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(4)).setPreferredWidth(0);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(5)).setPreferredWidth(150);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(6)).setPreferredWidth(0);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(7)).setPreferredWidth(0);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(8)).setPreferredWidth(0);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(9)).setPreferredWidth(0);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(10)).setPreferredWidth(0);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(11)).setPreferredWidth(0);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(12)).setPreferredWidth(150);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(13)).setPreferredWidth(30);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(14)).setPreferredWidth(0);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(15)).setPreferredWidth(0);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(16)).setPreferredWidth(150);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(17)).setPreferredWidth(0);

        tbDadosCliFor.getColumn(tbDadosCliFor.getColumnName(18)).setPreferredWidth(70);

        tbDadosCliFor.getColumnModel().getColumn(4).setMinWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(4).setMaxWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(6).setMinWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(6).setMaxWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(7).setMinWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(7).setMaxWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(8).setMinWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(8).setMaxWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(9).setMinWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(9).setMaxWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(10).setMinWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(10).setMaxWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(11).setMinWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(11).setMaxWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(14).setMinWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(14).setMaxWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(15).setMinWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(15).setMaxWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(17).setMinWidth(0);

        tbDadosCliFor.getColumnModel().getColumn(17).setMaxWidth(0);

        tbDadosCliFor.setRowSorter(new TableRowSorter(dados));

        dados.setNumRows(0);

        for (CliFor c : lista) {

            dados.addRow(new Object[]{

                c.getCodCliFor(),

                c.getTipo(),

                c.getNomeCliFor(),

                c.getApelidoCliFor(),

                c.getEmail(),

                c.getCelular(),

                c.getTelefone(),

                c.getCep(),

                c.getEndereco(),

                c.getNumero(),

                c.getComplemento(),

                c.getBairro(),

                c.getCidade(),

                c.getEstado(),

                c.getRg(),

                c.getCpf(),

                c.getContatoCliFor(),

                c.getObs(),

                c.getFkCliForGp() != null ? c.getFkCliForGp().getCod_Geral() : "Sem vínculo"

            });

        }

    }



    //CarregarCbx re = new CarregarCbx();

    public Tela_CadCliFor() throws SQLException {

        initComponents();

        CarregarCbx re = new CarregarCbx();

        re.CarregarCbx("gpprincipal", "nome_C", cbxConta);

        // Adicionar listener para jCbxTipo

        jCbxTipo.addItemListener(new ItemListener() {

            @Override

            public void itemStateChanged(ItemEvent e) {

                if (e.getStateChange() == ItemEvent.SELECTED && editingCodCliFor == null) {

                    try {

                        CliForDAO dao = new CliForDAO();

                        String selectedType = (String) jCbxTipo.getSelectedItem();

                        String suggestedCode;

                        if ("CLI".equals(selectedType)) {

                            suggestedCode = dao.suggestClienteCode();

                        } else if ("FOR".equals(selectedType)) {

                            suggestedCode = dao.suggestFornecedorCode();

                        } else {

                            suggestedCode = "";

                        }

                        tfCodCliFor.setText(suggestedCode);

                    } catch (SQLException | ClassNotFoundException ex) {

                        Logger.getLogger(Tela_CadCliFor.class.getName()).log(Level.SEVERE, null, ex);

                        JOptionPane.showMessageDialog(null, "Erro ao sugerir código: " + ex.getMessage());

                    }

                }

            }

        });

        // Adicionar listener para seleção na tabela

        tbDadosCliFor.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override

            public void valueChanged(ListSelectionEvent e) {

                if (!e.getValueIsAdjusting() && tbDadosCliFor.getSelectedRow() != -1) {

                    int selectedRow = tbDadosCliFor.getSelectedRow();

                    tfCodCliFor.setText(tbDadosCliFor.getValueAt(selectedRow, 0).toString());

                    jCbxTipo.setSelectedItem(tbDadosCliFor.getValueAt(selectedRow, 1).toString());

                    tfNomeCliFor.setText(tbDadosCliFor.getValueAt(selectedRow, 2).toString());

                    tfNomeFant.setText(tbDadosCliFor.getValueAt(selectedRow, 3) != null ? tbDadosCliFor.getValueAt(selectedRow, 3).toString() : "");

                    txtEmail.setText(tbDadosCliFor.getValueAt(selectedRow, 4) != null ? tbDadosCliFor.getValueAt(selectedRow, 4).toString() : "");

                    txtCelular.setText(tbDadosCliFor.getValueAt(selectedRow, 5) != null ? tbDadosCliFor.getValueAt(selectedRow, 5).toString() : "");

                    txtTelefone.setText(tbDadosCliFor.getValueAt(selectedRow, 6) != null ? tbDadosCliFor.getValueAt(selectedRow, 6).toString() : "");

                    txtCep.setText(tbDadosCliFor.getValueAt(selectedRow, 7) != null ? tbDadosCliFor.getValueAt(selectedRow, 7).toString() : "");

                    txtEndereco.setText(tbDadosCliFor.getValueAt(selectedRow, 8) != null ? tbDadosCliFor.getValueAt(selectedRow, 8).toString() : "");

                    txtNumero.setText(tbDadosCliFor.getValueAt(selectedRow, 9) != null ? tbDadosCliFor.getValueAt(selectedRow, 9).toString() : "");

                    txtComplemento.setText(tbDadosCliFor.getValueAt(selectedRow, 10) != null ? tbDadosCliFor.getValueAt(selectedRow, 10).toString() : "");

                    txtBairro.setText(tbDadosCliFor.getValueAt(selectedRow, 11) != null ? tbDadosCliFor.getValueAt(selectedRow, 11).toString() : "");

                    txtCidade.setText(tbDadosCliFor.getValueAt(selectedRow, 12) != null ? tbDadosCliFor.getValueAt(selectedRow, 12).toString() : "");

                    cbUF.setSelectedItem(tbDadosCliFor.getValueAt(selectedRow, 13) != null ? tbDadosCliFor.getValueAt(selectedRow, 13).toString() : "");

                    txtRG.setText(tbDadosCliFor.getValueAt(selectedRow, 14) != null ? tbDadosCliFor.getValueAt(selectedRow, 14).toString() : "");

                    txtCpf.setText(tbDadosCliFor.getValueAt(selectedRow, 15) != null ? tbDadosCliFor.getValueAt(selectedRow, 15).toString() : "");

                    tfContato.setText(tbDadosCliFor.getValueAt(selectedRow, 16) != null ? tbDadosCliFor.getValueAt(selectedRow, 16).toString() : "");

                    tfObs.setText(tbDadosCliFor.getValueAt(selectedRow, 17) != null ? tbDadosCliFor.getValueAt(selectedRow, 17).toString() : "");

                    tfConta.setText(tbDadosCliFor.getValueAt(selectedRow, 18) != null ? tbDadosCliFor.getValueAt(selectedRow, 18).toString() : "");

                    editingCodCliFor = tbDadosCliFor.getValueAt(selectedRow, 0).toString();

                    btSalvar.setVisible(false);

                }

            }

        });

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
        btSalvar = new javax.swing.JButton();
        btnAtualizar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        btnFechar = new javax.swing.JButton();
        jButtonImprimir = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanelCadFav = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        tfCodCliFor = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jCbxTipo = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        tfNomeCliFor = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        tfNomeFant = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        tfContato = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        tfObs = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cbxConta = new javax.swing.JComboBox<>();
        tfConta = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtCelular = new javax.swing.JFormattedTextField();
        jLabel10 = new javax.swing.JLabel();
        txtTelefone = new javax.swing.JFormattedTextField();
        jLabel12 = new javax.swing.JLabel();
        txtCep = new javax.swing.JFormattedTextField();
        jLabel13 = new javax.swing.JLabel();
        txtEndereco = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txtNumero = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txtBairro = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        txtComplemento = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        txtCidade = new javax.swing.JTextField();
        cbUF = new javax.swing.JComboBox<>();
        jLabel18 = new javax.swing.JLabel();
        txtRG = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        txtCpf = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbDadosCliFor = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btAbrir = new javax.swing.JButton();
        tfBusca = new javax.swing.JTextField();
        btnListarDados = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("       Cadastro de Clientes e Fornecedores");
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnAtualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonImprimir)
                .addGap(18, 18, 18)
                .addComponent(btnFechar, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(219, 219, 219))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btSalvar, btnAtualizar, btnExcluir, btnFechar, jButtonImprimir});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSalvar)
                    .addComponent(btnAtualizar)
                    .addComponent(btnExcluir)
                    .addComponent(btnFechar)
                    .addComponent(jButtonImprimir))
                .addGap(0, 8, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("CADASTRO DE FAVORECIDOS");

        jLabel2.setText("Cód");

        jLabel11.setText("Tipo");

        jCbxTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione", "CLI", "FOR", "BCO", "CAR", "PER", "INV" }));

        jLabel3.setText("Nome");

        jLabel5.setText("Nome Fantasia");

        jLabel6.setText("Contato");

        jLabel7.setText("Observação:");

        jLabel4.setText("Vínculo:");

        cbxConta.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione" }));
        cbxConta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxContaActionPerformed(evt);
            }
        });

        tfConta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfContaActionPerformed(evt);
            }
        });

        jLabel8.setText("E-mail:");

        jLabel9.setText("Celular:");

        try {
            txtCelular.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("(##)# ####-####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        jLabel10.setText("Telefone:");

        try {
            txtTelefone.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("(##) ####-####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        jLabel12.setText("CEP");

        try {
            txtCep.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.###-###")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        jLabel13.setText("Endereço:");

        jLabel14.setText("Número:");

        jLabel15.setText("Bairro :");

        jLabel16.setText("Complemento:");

        jLabel17.setText("Cidade:");

        cbUF.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO" }));

        jLabel18.setText("RG/IE :");

        jLabel19.setText("CPF/CNPJ :");

        jLabel20.setText("UF:");

        javax.swing.GroupLayout jPanelCadFavLayout = new javax.swing.GroupLayout(jPanelCadFav);
        jPanelCadFav.setLayout(jPanelCadFavLayout);
        jPanelCadFavLayout.setHorizontalGroup(
            jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCadFavLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelCadFavLayout.createSequentialGroup()
                        .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel4)
                            .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel2)
                                .addComponent(jLabel3))
                            .addComponent(jLabel8)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelCadFavLayout.createSequentialGroup()
                                .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tfNomeCliFor, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfCodCliFor, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelCadFavLayout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(jLabel11))
                                    .addGroup(jPanelCadFavLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel5)))
                                .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelCadFavLayout.createSequentialGroup()
                                        .addGap(7, 7, 7)
                                        .addComponent(jCbxTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanelCadFavLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanelCadFavLayout.createSequentialGroup()
                                                .addComponent(txtCelular, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel10)
                                                .addGap(45, 45, 45)
                                                .addComponent(txtTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(tfNomeFant, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(jPanelCadFavLayout.createSequentialGroup()
                                .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtCep, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(txtRG, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                                        .addComponent(txtBairro, javax.swing.GroupLayout.Alignment.LEADING)))
                                .addGap(134, 134, 134)
                                .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addGroup(jPanelCadFavLayout.createSequentialGroup()
                                        .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addGroup(jPanelCadFavLayout.createSequentialGroup()
                                                .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                    .addGroup(jPanelCadFavLayout.createSequentialGroup()
                                                        .addComponent(jLabel19)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(txtCpf, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(jPanelCadFavLayout.createSequentialGroup()
                                                        .addComponent(jLabel16)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(txtComplemento, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel17)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtCidade))
                                            .addGroup(jPanelCadFavLayout.createSequentialGroup()
                                                .addComponent(jLabel13)
                                                .addGap(34, 34, 34)
                                                .addComponent(txtEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 381, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanelCadFavLayout.createSequentialGroup()
                                                .addComponent(jLabel14)
                                                .addGap(18, 18, 18)
                                                .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanelCadFavLayout.createSequentialGroup()
                                                .addComponent(jLabel20)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cbUF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(jPanelCadFavLayout.createSequentialGroup()
                                        .addComponent(jLabel7)
                                        .addGap(26, 26, 26)
                                        .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(tfConta, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(tfObs, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addComponent(tfContato, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbxConta, javax.swing.GroupLayout.PREFERRED_SIZE, 443, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel18))
                .addGap(129, 130, Short.MAX_VALUE))
        );
        jPanelCadFavLayout.setVerticalGroup(
            jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCadFavLayout.createSequentialGroup()
                .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfCodCliFor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(jCbxTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfNomeCliFor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5)
                    .addComponent(tfNomeFant, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(txtCelular, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(txtTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel12)
                    .addComponent(txtCep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(txtEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelCadFavLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                        .addComponent(txtBairro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtRG, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanelCadFavLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel17)
                                .addComponent(txtCidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cbUF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel20))
                            .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel15)
                                .addComponent(jLabel16)
                                .addComponent(txtComplemento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel19)
                            .addComponent(txtCpf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfObs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfContato, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanelCadFavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(tfConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanelCadFavLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {cbUF, cbxConta, jCbxTipo, tfCodCliFor, tfConta, tfContato, tfNomeCliFor, tfNomeFant, tfObs, txtBairro, txtCelular, txtCep, txtCidade, txtComplemento, txtCpf, txtEmail, txtEndereco, txtNumero, txtRG, txtTelefone});

        tbDadosCliFor.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Cod", "Tipo", "Razão Social", "Nome Fantasia", "E-mail", "Celular", "Telefone", "CEP", "Endereço", "Numero", "Bairro", "Complemento", "Cidade", "UF", "RG/IE", "CPF/CNPJ", "Contato", "Observação", "Vinculo"
            }
        ));
        tbDadosCliFor.setSelectionBackground(new java.awt.Color(0, 0, 0));
        tbDadosCliFor.setSelectionForeground(new java.awt.Color(255, 255, 0));
        jScrollPane1.setViewportView(tbDadosCliFor);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addContainerGap()
                .addComponent(btAbrir, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tfBusca, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnListarDados, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btAbrir)
                .addComponent(tfBusca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnListarDados))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelCadFav, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelCadFav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents



    private void btSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSalvarActionPerformed

        if (tfCodCliFor.getText().equals("")) {

            JOptionPane.showMessageDialog(null, "Código em branco!");

        } else {

            // Criar o objeto GrupoPrincipal com o valor do campo tfVinculo

            Placon placon = buscarGrupoPrincipal(tfConta.getText());

            // Verificar se o grupo foi encontrado

            if (placon == null) {

                JOptionPane.showMessageDialog(null, "Grupo Principal inválido!");

                return;

            }

            CliFor obj = new CliFor();

            obj.setCodCliFor(tfCodCliFor.getText());

            obj.setTipo((String) jCbxTipo.getSelectedItem());

            obj.setNomeCliFor(tfNomeCliFor.getText());

            obj.setApelidoCliFor(tfNomeFant.getText());

            obj.setEmail(txtEmail.getText());

            obj.setTelefone(txtTelefone.getText());

            obj.setCelular(txtCelular.getText());

            obj.setCep(txtCep.getText());

            obj.setEndereco(txtEndereco.getText());

            String numeroTexto = txtNumero.getText().trim();

            int numero = numeroTexto.isEmpty() ? 0 : Integer.parseInt(numeroTexto);

            obj.setNumero(numero);

            obj.setComplemento(txtComplemento.getText());

            obj.setBairro(txtBairro.getText());

            obj.setCidade(txtCidade.getText());

            obj.setEstado(cbUF.getSelectedItem().toString());

            obj.setRg(txtRG.getText());

            obj.setCpf(txtCpf.getText());

            obj.setContatoCliFor(tfContato.getText());

            obj.setObs(tfObs.getText());

            obj.setFkCliForGp(placon);

            try {

                CliForDAO dao = new CliForDAO();

                dao.Salvar(obj);

                LimpaTela util = new LimpaTela();

                util.LimpaTela(jPanelCadFav);

            } catch (SQLException | ClassNotFoundException | ParseException ex) {

                Logger.getLogger(Tela_CadCliFor.class.getName()).log(Level.SEVERE, null, ex);

            }

        }

    }//GEN-LAST:event_btSalvarActionPerformed



    public Placon buscarGrupoPrincipal(String cod_Geral) {

        try {

            conexao.abrirConexao();

            Connection con = conexao.getConexao();

            String sql = "SELECT * FROM gpprincipal WHERE cod_Geral=?";

            try (PreparedStatement stmt = con.prepareStatement(sql)) {

                stmt.setString(1, cod_Geral);

                try (ResultSet rs = stmt.executeQuery()) {

                    if (rs.next()) {

                        Placon placon = new Placon();

                        placon.setCod_Geral(rs.getString("cod_Geral"));

                        return placon;

                    }

                }

            }

            return null;

        } catch (SQLException ex) {

            Logger.getLogger(Tela_CadCliFor.class.getName()).log(Level.SEVERE, null, ex);

            JOptionPane.showMessageDialog(null, "Erro ao buscar Grupo Principal: " + ex.getMessage());

            return null;

        } finally {

            conexao.fecharConexao();

        }

    }



    private void btAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAbrirActionPerformed

        if (tfBusca.getText().isEmpty()) {

            JOptionPane.showMessageDialog(null, "Informe um Código válido...");

            return;

        }

        try {

            String codCliFor = tfBusca.getText();

            CliForDAO dao = new CliForDAO();

            CliFor obj = dao.BuscarCliFor(codCliFor);

            if (obj != null && obj.getCodCliFor() != null) {

                tfCodCliFor.setText(obj.getCodCliFor());

                jCbxTipo.setSelectedItem(obj.getTipo());

                tfNomeCliFor.setText(obj.getNomeCliFor());

                tfNomeFant.setText(obj.getApelidoCliFor() != null ? obj.getApelidoCliFor() : "");

                txtEmail.setText(obj.getEmail() != null ? obj.getEmail() : "");

                txtTelefone.setText(obj.getTelefone() != null ? obj.getTelefone() : "");

                txtCelular.setText(obj.getCelular() != null ? obj.getCelular() : "");

                txtCep.setText(obj.getCep() != null ? obj.getCep() : "");

                txtEndereco.setText(obj.getEndereco() != null ? obj.getEndereco() : "");

                txtNumero.setText(String.valueOf(obj.getNumero()));

                txtComplemento.setText(obj.getComplemento() != null ? obj.getComplemento() : "");

                txtBairro.setText(obj.getBairro() != null ? obj.getBairro() : "");

                txtCidade.setText(obj.getCidade() != null ? obj.getCidade() : "");

                cbUF.setSelectedItem(obj.getEstado() != null ? obj.getEstado() : "");

                txtRG.setText(obj.getRg() != null ? obj.getRg() : "");

                txtCpf.setText(obj.getCpf() != null ? obj.getCpf() : "");

                tfContato.setText(obj.getContatoCliFor() != null ? obj.getContatoCliFor() : "");

                tfObs.setText(obj.getObs() != null ? obj.getObs() : "");

                tfConta.setText(obj.getFkCliForGp() != null ? obj.getFkCliForGp().getCod_Geral() : "");

                editingCodCliFor = obj.getCodCliFor();

                btSalvar.setVisible(false);

            } else {

                JOptionPane.showMessageDialog(null, "Favorecido não encontrado!");

            }

        } catch (SQLException | ClassNotFoundException ex) {

            Logger.getLogger(Tela_CadCliFor.class.getName()).log(Level.SEVERE, null, ex);

            JOptionPane.showMessageDialog(null, "Erro ao abrir favorecido: " + ex.getMessage());

        }

    }//GEN-LAST:event_btAbrirActionPerformed



    @SuppressWarnings("unchecked")

    private void btnListarDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnListarDadosActionPerformed

        try {

            listar();

        } catch (SQLException | ClassNotFoundException ex) {

            Logger.getLogger(Tela_CadCliFor.class.getName()).log(Level.SEVERE, null, ex);

            JOptionPane.showMessageDialog(null, "Erro ao listar dados: " + ex.getMessage());

        }

    }//GEN-LAST:event_btnListarDadosActionPerformed



    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarActionPerformed

        if (tfCodCliFor.getText().isEmpty() || tfNomeCliFor.getText().isEmpty() || jCbxTipo.getSelectedItem() == null) {

            JOptionPane.showMessageDialog(null, "Campos obrigatórios em branco!");

            return;

        }

        try {

            Placon placon = buscarGrupoPrincipal(tfConta.getText());

            if (placon == null && !tfConta.getText().isEmpty()) {

                JOptionPane.showMessageDialog(null, "Código inválido!");

                return;

            }

            CliFor obj = new CliFor();

            obj.setCodCliFor(tfCodCliFor.getText());

            obj.setTipo((String) jCbxTipo.getSelectedItem());

            obj.setNomeCliFor(tfNomeCliFor.getText());

            obj.setApelidoCliFor(tfNomeFant.getText().isEmpty() ? null : tfNomeFant.getText());

            obj.setEmail(txtEmail.getText().isEmpty() ? null : txtEmail.getText());

            obj.setTelefone(txtTelefone.getText().isEmpty() ? null : txtTelefone.getText());

            obj.setCelular(txtCelular.getText().isEmpty() ? null : txtCelular.getText());

            obj.setCep(txtCep.getText().isEmpty() ? null : txtCep.getText());

            obj.setEndereco(txtEndereco.getText().isEmpty() ? null : txtEndereco.getText());

            String numeroTexto = txtNumero.getText().trim();

            int numero = numeroTexto.isEmpty() ? 0 : Integer.parseInt(numeroTexto);

            obj.setNumero(numero);

            obj.setComplemento(txtComplemento.getText().isEmpty() ? null : txtComplemento.getText());

            obj.setBairro(txtBairro.getText().isEmpty() ? null : txtBairro.getText());

            obj.setCidade(txtCidade.getText().isEmpty() ? null : txtCidade.getText());

            obj.setEstado(cbUF.getSelectedItem() != null ? cbUF.getSelectedItem().toString() : null);

            obj.setRg(txtRG.getText().isEmpty() ? null : txtRG.getText());

            obj.setCpf(txtCpf.getText().isEmpty() ? null : txtCpf.getText());

            obj.setContatoCliFor(tfContato.getText().isEmpty() ? null : tfContato.getText());

            obj.setObs(tfObs.getText().isEmpty() ? null : tfObs.getText());

            obj.setFkCliForGp(placon);

            CliForDAO dao = new CliForDAO();

            dao.Editar(obj);

            LimpaTela util = new LimpaTela();

            util.LimpaTela(jPanelCadFav);

            editingCodCliFor = null;

            btSalvar.setVisible(true);

            listar();

        } catch (SQLException | ClassNotFoundException | ParseException ex) {

            Logger.getLogger(Tela_CadCliFor.class.getName()).log(Level.SEVERE, null, ex);

            JOptionPane.showMessageDialog(null, "Erro ao atualizar: " + ex.getMessage());

        }

    }//GEN-LAST:event_btnAtualizarActionPerformed



    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed

        if (tfCodCliFor.getText().isEmpty()) {

            JOptionPane.showMessageDialog(null, "Informe o Favorecido a ser excluído.");

            return;

        }

        try {

            CliFor obj = new CliFor();

            obj.setCodCliFor(tfCodCliFor.getText());

            CliForDAO dao = new CliForDAO();

            dao.Excluir(obj);

            LimpaTela util = new LimpaTela();

            util.LimpaTela(jPanelCadFav);

            editingCodCliFor = null;

            btSalvar.setVisible(true);

            listar();

        } catch (SQLException | ClassNotFoundException ex) {

            Logger.getLogger(Tela_CadCliFor.class.getName()).log(Level.SEVERE, null, ex);

            JOptionPane.showMessageDialog(null, "Erro ao excluir: " + ex.getMessage());

        }

    }//GEN-LAST:event_btnExcluirActionPerformed



    private void btnFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFecharActionPerformed

        try {

            Tela_Principal exibir = new Tela_Principal();

            exibir.setVisible(true);

            setVisible(false);

        } catch (IOException ex) {

            Logger.getLogger(Tela_CadCliFor.class.getName()).log(Level.SEVERE, null, ex);

        }

    }//GEN-LAST:event_btnFecharActionPerformed



    private void cbxContaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxContaActionPerformed

        String selectedValue = cbxConta.getSelectedItem().toString();

        int texto = selectedValue.length() - 9;

        String codGeral = selectedValue.substring(texto);

        tfConta.setText(codGeral);

    }//GEN-LAST:event_cbxContaActionPerformed



    private void tfContaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfContaActionPerformed

        // TODO add your handling code here:

    }//GEN-LAST:event_tfContaActionPerformed



    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated

        try {

            listar();

        } catch (SQLException | ClassNotFoundException ex) {

            Logger.getLogger(Tela_CadCliFor.class.getName()).log(Level.SEVERE, null, ex);

        }

    }//GEN-LAST:event_formWindowActivated



    private void jButtonImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonImprimirActionPerformed

        try {

            RelCliFor CliFor = new RelCliFor();

            JOptionPane.showMessageDialog(null, "Relatório de Favorecidos gerado com sucesso!");

            dispose();

            new Tela_CadCliFor().setVisible(true);

        } catch (FileNotFoundException | SQLException ex) {

            Logger.getLogger(Tela_CadCliFor.class.getName()).log(Level.SEVERE, null, ex);

        }

    }//GEN-LAST:event_jButtonImprimirActionPerformed



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

            java.util.logging.Logger.getLogger(Tela_CadCliFor.class

                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);



        } catch (InstantiationException ex) {

            java.util.logging.Logger.getLogger(Tela_CadCliFor.class

                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);



        } catch (IllegalAccessException ex) {

            java.util.logging.Logger.getLogger(Tela_CadCliFor.class

                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);



        } catch (javax.swing.UnsupportedLookAndFeelException ex) {

            java.util.logging.Logger.getLogger(Tela_CadCliFor.class

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

        try {

            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {

                if ("Nimbus".equals(info.getName())) {

                    javax.swing.UIManager.setLookAndFeel(info.getClassName());

                    break;

                }

            }

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {

            java.util.logging.Logger.getLogger(Tela_CadCliFor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);

        }

        java.awt.EventQueue.invokeLater(() -> {

            try {

                new Tela_CadCliFor().setVisible(true);

            } catch (SQLException ex) {

                Logger.getLogger(Tela_CadCliFor.class.getName()).log(Level.SEVERE, null, ex);

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
    private javax.swing.JComboBox<String> cbUF;
    private javax.swing.JComboBox<String> cbxConta;
    private javax.swing.JButton jButtonImprimir;
    private javax.swing.JComboBox<String> jCbxTipo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
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
    private javax.swing.JPanel jPanelCadFav;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tbDadosCliFor;
    private javax.swing.JTextField tfBusca;
    private javax.swing.JTextField tfCodCliFor;
    private javax.swing.JTextField tfConta;
    private javax.swing.JTextField tfContato;
    private javax.swing.JTextField tfNomeCliFor;
    private javax.swing.JTextField tfNomeFant;
    private javax.swing.JTextField tfObs;
    private javax.swing.JTextField txtBairro;
    private javax.swing.JFormattedTextField txtCelular;
    private javax.swing.JFormattedTextField txtCep;
    private javax.swing.JTextField txtCidade;
    private javax.swing.JTextField txtComplemento;
    private javax.swing.JTextField txtCpf;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtEndereco;
    private javax.swing.JTextField txtNumero;
    private javax.swing.JTextField txtRG;
    private javax.swing.JFormattedTextField txtTelefone;
    // End of variables declaration//GEN-END:variables



    private static class CarregarCbx {

        @SuppressWarnings({"empty-statement", "unchecked"})

        public void CarregarCbx(String tabela, String valor, JComboBox combo) throws SQLException {

            String sql = "SELECT * FROM " + tabela;

            Conexao conexao = new Conexao();

            try {

                conexao.abrirConexao();

                Connection con = conexao.getConexao();

                PreparedStatement stmt = con.prepareStatement(sql);

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {

                    combo.addItem(rs.getString(valor) + "-" + rs.getString("cod_Geral"));

                }

                rs.close();

                stmt.close();

                con.close();

            } catch (SQLException ex) {

                Logger.getLogger(Tela_CadCliFor.class.getName()).log(Level.SEVERE, null, ex);

            } finally {

                conexao.fecharConexao();

            }

        }

    }



}

