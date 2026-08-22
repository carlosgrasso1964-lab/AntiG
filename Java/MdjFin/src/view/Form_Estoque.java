package view;

import dao.ProdutosDAO;
import model.Produtos;
import utilitarios.LimpaTela;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import utilitarios.Conexao;
import dao.CliForDAO;
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.CliFor;
import static utilitarios.FormataMoeda.formatarMoeda;

public class Form_Estoque extends javax.swing.JDialog {

    private int idProduto = 0;

    public Form_Estoque(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        // CARREGA TODOS OS PRODUTOS AO ABRIR
        try {
            listarTodos(); // Mostra todos logo que abre
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar produtos: " + ex.getMessage());
        }

        // === PLACEHOLDER BONITÃO NO CAMPO DE PESQUISA ===
        txtPesquisa.setText("Digite para buscar em tempo real...");
        txtPesquisa.setForeground(Color.GRAY);

        txtPesquisa.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtPesquisa.getText().equals("Digite para buscar em tempo real...")) {
                    txtPesquisa.setText("");
                    txtPesquisa.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtPesquisa.getText().trim().isEmpty()) {
                    txtPesquisa.setText("Digite para buscar em tempo real...");
                    txtPesquisa.setForeground(Color.GRAY);
                    // Quando sai do campo e tá vazio ? mostra todos os produtos de novo
                    try {
                        listarTodos();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(rootPane, "Erro ao recarregar lista.");
                    }
                }
            }
        });

        // PESQUISA EM TEMPO REAL NO CAMPO DE BUSCA (não no txtDescricao!)
        // Mude para o campo que você usa pra pesquisar (ex: txtPesquisa, txtBusca, etc)
        // VOU ASSUMIR QUE É txtPesquisa (mais comum)
        // Se o seu campo se chama diferente, só troca o nome abaixo!
        txtPesquisa.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrarEmTempoReal();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrarEmTempoReal();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrarEmTempoReal();
            }

            private void filtrarEmTempoReal() {
                String texto = txtPesquisa.getText().trim();

                try {
                    ProdutosDAO dao = new ProdutosDAO();
                    List<Produtos> lista;

                    if (texto.isEmpty()) {
                        lista = dao.listarTodos();
                    } else {
                        lista = dao.filtrarPorNome("%" + texto + "%"); // busca parcial
                    }

                    atualizarTabela(lista);

                    // Se só tiver 1 resultado ? carrega automaticamente nos campos
                    if (lista.size() == 1) {
                        tabela.setRowSelectionInterval(0, 0);
                        carregarProdutoNaTela(lista.get(0));
                    }

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(rootPane, "Erro na busca: " + ex.getMessage());
                }
            }
        });

        // CLIQUE DUPLO NA TABELA ? CARREGA O PRODUTO
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tabela.getSelectedRow();
                    if (row >= 0) {
                        int id = (int) tabela.getValueAt(row, 0);
                        try {
                            Produtos p = new ProdutosDAO().buscarPorId(id);
                            if (p != null) {
                                carregarProdutoNaTela(p);
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(rootPane, "Erro ao carregar produto.");
                        }
                    }
                }
            }
        });
    }

    // MÉTODO PARA ATUALIZAR A TABELA (REUTILIZÁVEL)
    private void atualizarTabela(List<Produtos> lista) {
        DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();
        modelo.setRowCount(0);

        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

        for (Produtos p : lista) {
            modelo.addRow(new Object[]{
                p.getId(), // 0 - Código
                p.getDescricao(), // 1 - Descrição
                nf.format(p.getPrecoMedioCusto()), // 3 - Preço Custo
                String.format("%.3f", p.getQtd_estoque()), // 2 - Qtd Atual
                //nf.format(p.getPrecoVenda()), // 4 - Preço Venda
                String.format("%.3f", p.getQtd_estoqueMinimo()), // 5 - Qtd Mínima
                String.format("%.3f", p.getQtd_estoqueMaximo()), // 6 - Qtd Máxima
                p.getCliFor() != null ? p.getCliFor().getNomeCliFor() : "Sem fornecedor", // 7 - Fornecedor
                p.getCategoria() != null ? p.getCategoria().getNome() : "Sem categoria" // 8 - Categoria
            });
        }
    }

    // CARREGA TODOS OS PRODUTOS (ao abrir)
    private void listarTodos() throws SQLException {
        ProdutosDAO dao = new ProdutosDAO();
        List<Produtos> lista = dao.listarTodos();
        atualizarTabela(lista);
    }

    // CARREGA OS DADOS DO PRODUTO NOS CAMPOS
    private void carregarProdutoNaTela(Produtos p) {
        idProduto = p.getId();
        txtCodigo.setText(String.valueOf(p.getId()));
        txtDescricao.setText(p.getDescricao());
        txtPrecoAtual.setText(formatarMoeda(p.getPrecoMedioCusto()));
        txtQtdAtual.setText(String.format("%.3f", p.getQtd_estoque()));
        //txtPrecoNovo.setText(formatarMoeda(p.getPrecoVenda()));  // se tiver esse campo
        txtEstoqueMinimo.setText(String.format("%.3f", p.getQtd_estoqueMinimo()));
        txtEstoqueMaximo.setText(String.format("%.3f", p.getQtd_estoqueMaximo()));

        // Limpa os campos de entrada (novos)
        txtQtd_Nova.setText("");
        txtPrecoNovo.setText("");
    }

    // FUNÇÃO DE FORMATAÇÃO (reutilizável)
    private String formatarMoeda(double valor) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
        return nf.format(valor);
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
        painel_guias = new javax.swing.JTabbedPane();
        painel_estoque = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtDescricao = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtQtdAtual = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtQtd_Nova = new javax.swing.JTextField();
        btnAdicionar = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        txtEstoqueMaximo = new javax.swing.JTextField();
        txtPrecoAtual = new javax.swing.JTextField();
        txtPrecoNovo = new javax.swing.JTextField();
        txtEstoqueMinimo = new javax.swing.JTextField();
        txtPesquisa = new javax.swing.JTextField();
        btnNovo = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Formulário de Estoque");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));

        jLabel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Estoque");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        painel_guias.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Consulta de Produtos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        jLabel2.setText("Código: ");

        jLabel3.setText("Descrição");

        txtDescricao.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtDescricaoKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtDescricaoKeyReleased(evt);
            }
        });

        jLabel4.setText("Quantidade Atual");

        jLabel9.setText("Quantidade");

        btnAdicionar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAdicionar.setText("Adicionar");
        btnAdicionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarActionPerformed(evt);
            }
        });

        jLabel5.setText("Estoque Mínimo");

        jLabel6.setText("Estoque Máximo");

        jLabel7.setText("Preço Atual");

        jLabel8.setText("Preço Novo");

        txtPesquisa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtPesquisaKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesquisaKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout painel_estoqueLayout = new javax.swing.GroupLayout(painel_estoque);
        painel_estoque.setLayout(painel_estoqueLayout);
        painel_estoqueLayout.setHorizontalGroup(
            painel_estoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painel_estoqueLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painel_estoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painel_estoqueLayout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addGroup(painel_estoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painel_estoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painel_estoqueLayout.createSequentialGroup()
                                .addComponent(txtDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, 426, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                                .addComponent(btnAdicionar))
                            .addGroup(painel_estoqueLayout.createSequentialGroup()
                                .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtPesquisa))))
                    .addGroup(painel_estoqueLayout.createSequentialGroup()
                        .addGroup(painel_estoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painel_estoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel5)
                                .addComponent(jLabel4))
                            .addComponent(jLabel6))
                        .addGap(12, 12, 12)
                        .addGroup(painel_estoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtQtdAtual, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                            .addComponent(txtEstoqueMaximo)
                            .addComponent(txtEstoqueMinimo))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(painel_estoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painel_estoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtPrecoAtual, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                            .addComponent(txtPrecoNovo)
                            .addComponent(txtQtd_Nova))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        painel_estoqueLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {txtEstoqueMaximo, txtQtdAtual});

        painel_estoqueLayout.setVerticalGroup(
            painel_estoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painel_estoqueLayout.createSequentialGroup()
                .addGroup(painel_estoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painel_estoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painel_estoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtQtdAtual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(txtQtd_Nova, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painel_estoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel7)
                    .addComponent(txtPrecoAtual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEstoqueMinimo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painel_estoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel8)
                    .addComponent(txtEstoqueMaximo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPrecoNovo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(38, Short.MAX_VALUE))
        );

        painel_estoqueLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {txtEstoqueMaximo, txtQtdAtual});

        painel_guias.addTab("Dados do produto", painel_estoque);

        btnNovo.setText("NOVO");
        btnNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNovoActionPerformed(evt);
            }
        });

        btnSalvar.setText("SALVAR");
        btnSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalvarActionPerformed(evt);
            }
        });

        btnEditar.setText("ALTERAR");
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarActionPerformed(evt);
            }
        });

        btnExcluir.setText("EXCLUIR");
        btnExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcluirActionPerformed(evt);
            }
        });

        btnImprimir.setText("IMPRIMIR");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Lista de Produtos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        tabela.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "codigo", "descrição", "preço", "qtd. estoque", "qtd.Minima", "qtd.Maxima", "fornecedor"
            }
        ));
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelaMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tabela);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(painel_guias)
            .addGroup(layout.createSequentialGroup()
                .addGap(101, 101, 101)
                .addComponent(btnNovo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSalvar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEditar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnExcluir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnImprimir)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(painel_guias)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNovo)
                    .addComponent(btnSalvar)
                    .addComponent(btnEditar)
                    .addComponent(btnExcluir)
                    .addComponent(btnImprimir)))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarActionPerformed
        Produtos obj = new Produtos();
        obj.setDescricao(txtDescricao.getText());

        // Obtém o preço a partir do campo txtPreco, já que você formatou como moeda
        //String precoTexto = txtPreco.getText().replace("R$", "").replace(".", "").replace(",", ".");
        //obj.setPreco(Double.valueOf(precoTexto)); // Converte o texto formatado para double
        obj.setPreco(Double.valueOf(txtQtdAtual.getText()));

        // Obtém a quantidade de estoque com 3 casas decimais
        String qtdEstoqueTexto = txtQtd_Nova.getText().replace(".", "").replace(",", "."); // Formata para o padrão de double
        obj.setQtd_estoque(Double.valueOf(qtdEstoqueTexto)); // Converte o texto para double

        try {
            ProdutosDAO dao = new ProdutosDAO();
            dao.salvar(obj);
            LimpaTela util = new LimpaTela();
            util.LimpaTela(painel_estoque);
        } catch (SQLException ex) {
            Logger.getLogger(Form_Estoque.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnSalvarActionPerformed

    private void btnNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNovoActionPerformed
        LimpaTela util = new LimpaTela();
        util.LimpaTela(painel_estoque);
    }//GEN-LAST:event_btnNovoActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        try {
            listarTodos();
        } catch (SQLException ex) {
            Logger.getLogger(Form_Estoque.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_formWindowActivated

    private void txtDescricaoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDescricaoKeyReleased

    }//GEN-LAST:event_txtDescricaoKeyReleased

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        JOptionPane.showMessageDialog(this,
                "Para editar descrição, fornecedor ou categoria do produto,\n"
                + "vá até o menu: Produtos ? Cadastro de Produtos.",
                "Informação", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed
        if (txtCodigo.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para excluir!");
            return;
        }

        int id = Integer.parseInt(txtCodigo.getText());

        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja EXCLUIR o produto?\n\n"
                + "ID: " + id + "\n"
                + "Descrição: " + txtDescricao.getText() + "\n\n"
                + "Essa ação NÃO pode ser desfeita!",
                "CONFIRMAR EXCLUSÃO",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                ProdutosDAO dao = new ProdutosDAO();
                dao.excluir(id);  // ? CORRETO: agora passa só o ID (int)

                JOptionPane.showMessageDialog(this, "Produto excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                new LimpaTela().LimpaTela(painel_estoque);
                listarTodos(); // Atualiza a tabela

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Não foi possível excluir o produto.\n"
                        + "Pode haver vendas ou compras vinculadas a ele.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnExcluirActionPerformed

    private void txtDescricaoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDescricaoKeyPressed

        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            String nome = txtDescricao.getText().trim();
            if (nome.isEmpty()) {
                return;
            }

            try {
                ProdutosDAO dao = new ProdutosDAO();
                Produtos obj = dao.buscarPorNome(nome);

                if (obj != null && obj.getDescricao() != null) {
                    // Formata o preço médio de custo atual como moeda brasileira
                    NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
                    String precoFormatado = nf.format(obj.getPrecoMedioCusto());

                    // Preenche os campos
                    txtCodigo.setText(String.valueOf(obj.getId()));
                    txtDescricao.setText(obj.getDescricao());
                    txtQtdAtual.setText(String.format("%.3f", obj.getQtd_estoque()));
                    txtPrecoAtual.setText(precoFormatado);
                    txtEstoqueMinimo.setText(String.format("%.3f", obj.getQtd_estoqueMinimo()));
                    txtEstoqueMaximo.setText(String.format("%.3f", obj.getQtd_estoqueMaximo()));

                    // Limpa os campos de entrada
                    txtQtd_Nova.setText("");
                    txtPrecoNovo.setText("");

                    // Foca no campo de quantidade nova
                    txtQtd_Nova.requestFocus();

                } else {
                    JOptionPane.showMessageDialog(this,
                            "Produto não encontrado com o nome: " + nome,
                            "Atenção", JOptionPane.WARNING_MESSAGE);
                    txtDescricao.selectAll();
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao buscar produto:\n" + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_txtDescricaoKeyPressed

    private void tabelaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelaMouseClicked
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            return; // Nenhuma linha selecionada
        }

        try {
            // Pega o ID da primeira coluna
            idProduto = Integer.parseInt(tabela.getValueAt(linha, 0).toString());

            // Preenche os campos com os dados da tabela
            txtCodigo.setText(tabela.getValueAt(linha, 0).toString());
            txtDescricao.setText(tabela.getValueAt(linha, 1).toString());
            txtPrecoAtual.setText(tabela.getValueAt(linha, 2).toString());
            txtQtdAtual.setText(tabela.getValueAt(linha, 3).toString());
            txtEstoqueMinimo.setText(tabela.getValueAt(linha, 4).toString());
            txtEstoqueMaximo.setText(tabela.getValueAt(linha, 5).toString());

            // Limpa os campos de entrada para nova movimentação
            txtQtd_Nova.setText("");
            txtPrecoNovo.setText("");
            txtQtd_Nova.requestFocus();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar o produto da tabela.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_tabelaMouseClicked

    private String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        // Remove todos os caracteres não numéricos, exceto a vírgula
        return input.replaceAll("[^\\d,]", "").replace(",", ".").trim();
    }

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        if (idProduto <= 0) {
            JOptionPane.showMessageDialog(this, "Selecione um produto primeiro!");
            return;
        }

        double qtdNova, precoNovo;
        try {
            qtdNova = Double.parseDouble(txtQtd_Nova.getText().replace(",", "."));
            precoNovo = Double.parseDouble(txtPrecoNovo.getText().replace("R$", "").replace(".", "").replace(",", ".").trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Verifique quantidade e preço!");
            return;
        }

        if (qtdNova <= 0 || precoNovo <= 0) {
            JOptionPane.showMessageDialog(this, "Quantidade e preço devem ser maiores que zero!");
            return;
        }

        Connection con = null;
        try {
            con = Conexao.faz_conexao();
            con.setAutoCommit(false);

            double custoTotalNovo = qtdNova * precoNovo;

            // CHAMA COM A CONEXÃO ABERTO (agora funciona!)
            new ProdutosDAO().entradaEstoque(con, idProduto, qtdNova, custoTotalNovo);

            con.commit();

            JOptionPane.showMessageDialog(this,
                    "ENTRADA DE ESTOQUE REALIZADA COM SUCESSO!\n"
                    + "Quantidade adicionada: " + String.format("%.3f", qtdNova) + "\n"
                    + "Preço médio recalculado!",
                    "SUCESSO!", JOptionPane.INFORMATION_MESSAGE);

            // limpa e atualiza
            txtQtd_Nova.setText("");
            txtPrecoNovo.setText("");
            txtQtd_Nova.requestFocus();
            listarTodos();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "ERRO NA ENTRADA DE ESTOQUE:\n" + e.getMessage(),
                    "Falha", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            if (con != null) try {
                con.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            if (con != null) try {
                con.setAutoCommit(true);
                con.close();
            } catch (Exception e) {
            }
        }
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void txtPesquisaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesquisaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPesquisaKeyPressed

    private void txtPesquisaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesquisaKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPesquisaKeyReleased

    public static double parseStringToDouble(String valor) throws ParseException {
        // Remove o símbolo de moeda e espaços
        String valorLimpo = valor.replace("R$", "").replace("\u00A0", "").trim();

        // Substitui a vírgula decimal por ponto e remove separadores de milhar
        valorLimpo = valorLimpo.replace(".", "").replace(",", ".");

        // Converte para double
        return Double.parseDouble(valorLimpo);
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
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Form_Estoque.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Form_Estoque.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Form_Estoque.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Form_Estoque.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
        java.awt.EventQueue.invokeLater(() -> {
            //new FormularioEstoque().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnNovo;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JLabel jLabel1;
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
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel painel_estoque;
    private javax.swing.JTabbedPane painel_guias;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtDescricao;
    private javax.swing.JTextField txtEstoqueMaximo;
    private javax.swing.JTextField txtEstoqueMinimo;
    private javax.swing.JTextField txtPesquisa;
    private javax.swing.JTextField txtPrecoAtual;
    private javax.swing.JTextField txtPrecoNovo;
    private javax.swing.JTextField txtQtdAtual;
    private javax.swing.JTextField txtQtd_Nova;
    // End of variables declaration//GEN-END:variables
}
