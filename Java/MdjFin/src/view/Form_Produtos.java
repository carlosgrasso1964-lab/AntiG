package view;

import utilitarios.Conexao;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import dao.CategoriaDAO;
import dao.CliForDAO;
import dao.ProdutosDAO;
import java.awt.Desktop;
import model.Produtos;
import utilitarios.LimpaTela;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import model.Categoria;
import model.CliFor;

/**
 *
 * @author CARLOS
 */
public class Form_Produtos extends javax.swing.JDialog {

    /**
     * Creates new form FormularioClientes
     *
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     */
    private Conexao conexao;
    private CategoriaDAO dao; // Declaração do DAO para categorias
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
//    private final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
//
    // Use em qualquer lugar assim:
    private String formatarMoeda(double valor) {
        return nf.format(valor);
    }
//
//    private double parseMoeda(String valor) {
//        try {
//            return nf.parse(valor).doubleValue();
//        } catch (Exception e) {
//            return 0.0;
//        }
//    }
    

    /**
     * Converte QUALQUER texto de preço para double
     * Aceita: R$ 15,00 / 15,00 / 15.00 / 1500 / R$1500,99 ? tudo vira número perfeito
     */
    private double parseMoeda(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return 0.0;
        }

        try {
            // Primeiro tenta com o NumberFormat (funciona com R$ 1.234,56)
            return nf.parse(texto.trim()).doubleValue();
        } catch (Exception e1) {
            try {
                // Se falhar, remove tudo que não é número, vírgula ou ponto
                String limpo = texto.replaceAll("[^0-9,\\.]", ""); // remove R$, espaços, etc.
                limpo = limpo.replace(".", "");                    // remove separador de milhar
                limpo = limpo.replace(",", ".");                   // vírgula vira ponto
                return Double.parseDouble(limpo);
            } catch (Exception e2) {
                return 0.0;
            }
        }
    }

    public Form_Produtos(java.awt.Frame parent, boolean modal) throws SQLException, ClassNotFoundException {
        super(parent, modal);
        initComponents(); // Inicializa os componentes

        conexao = new Conexao();
        dao = new CategoriaDAO();

        carregarCategoriasNoComboBox();
        carregarFornecedores();
        listar();
    }

    private void carregarCategoriasNoComboBox() throws SQLException {
        if (cbCategorias == null) {
            cbCategorias = new JComboBox<>();
        }

        List<Categoria> lista = dao.listar(); // Busca as categorias no DAO
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

        for (Categoria c : lista) {
            model.addElement(c.getNome()); // Adiciona os nomes ao ComboBox
        }
        cbCategorias.setModel(model);
    }

    private void carregarProdutoParaEdicao(int produtoId) {
        try {
            ProdutosDAO dao = new ProdutosDAO();
            Produtos produto = dao.buscarPorId(produtoId);
            // Preenche os campos com os dados do produto
            txtCodigo.setText(String.valueOf(produto.getId()));
            txtDescricao.setText(produto.getDescricao());
            txtPreco.setText(String.valueOf(produto.getPreco()));
            txtQtdEstoque.setText(String.valueOf(produto.getQtd_estoque()));
            txtQtdEstoqueMinimo.setText(String.valueOf(produto.getQtd_estoqueMinimo()));
            txtQtdEstoqueMaximo.setText(String.valueOf(produto.getQtd_estoqueMaximo()));

            // Preenche o combo box de fornecedor
            cbFornecedor.setSelectedItem(produto.getCliFor().getNomeCliFor());

            // Preenche o combo box de categorias
            Categoria categoria = produto.getCategoria();
            if (categoria != null) {
                for (int i = 0; i < cbCategorias.getItemCount(); i++) {
                    if (cbCategorias.getItemAt(i).equals(categoria.getNome())) {
                        cbCategorias.setSelectedIndex(i);  // Selecione a categoria correta
                        break;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Form_Produtos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("deprecation")
    public void listar() throws SQLException, ClassNotFoundException {
        ProdutosDAO dao = new ProdutosDAO();
        List<Produtos> lista = dao.listarTodos();
        DefaultTableModel dados = (DefaultTableModel) tabela.getModel();
        dados.setNumRows(0); // Limpa a tabela

        // Define formatos
        @SuppressWarnings("deprecation")
        NumberFormat moedaFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        DecimalFormat quantidadeFormat = new DecimalFormat("#,##0.000");
        quantidadeFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(new Locale("pt", "BR")));

        for (Produtos p : lista) {
            dados.addRow(new Object[]{
                p.getId(), // Código
                p.getDescricao(), // Descrição
                quantidadeFormat.format(p.getQtd_estoque()), // Qtd. Estoque
                p.getCliFor().getNomeCliFor(), // Fornecedor
                quantidadeFormat.format(p.getQtd_estoqueMinimo()), // Est. Mínimo
                quantidadeFormat.format(p.getQtd_estoqueMaximo()), // Est. Máximo
                moedaFormat.format(p.getPrecoMedioCusto()), // Preço Médio Custo
                moedaFormat.format(p.getPrecoVenda()), // Preço Venda
                p.getCategoria().getId() // Categoria ID
            });
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
        painel_guias = new javax.swing.JTabbedPane();
        painel_dados_pessoais = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtDescricao = new javax.swing.JTextField();
        btnPesquisar = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        txtPreco = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtQtdEstoque = new javax.swing.JTextField();
        cbFornecedor = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtQtdEstoqueMinimo = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtQtdEstoqueMaximo = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        cbCategorias = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        txtPrecoVd = new javax.swing.JTextField();
        painel_consulta = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        txtPesquisaDescricao = new javax.swing.JTextField();
        btnPesquisa = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        btnNovo = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Formulário de Produtos");
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
        jLabel1.setText("Cadastro de Produtos");

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

        jLabel2.setText("Código: ");

        txtCodigo.setEditable(false);

        jLabel3.setText("Descrição");

        txtDescricao.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtDescricaoKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtDescricaoKeyReleased(evt);
            }
        });

        btnPesquisar.setText("Pesquisar");
        btnPesquisar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPesquisarActionPerformed(evt);
            }
        });

        jLabel4.setText("Custo Médio");

        jLabel9.setText("Qtd. Estoque: ");

        txtQtdEstoque.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtQtdEstoqueActionPerformed(evt);
            }
        });

        cbFornecedor.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                cbFornecedorAncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        cbFornecedor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cbFornecedorMouseClicked(evt);
            }
        });

        jLabel16.setText("Fornecedor");

        jLabel10.setText("Estoque Mínimo");

        jLabel11.setText("Estoque Máximo");

        jLabel5.setText("Categoria");

        jLabel6.setText("Preço Venda");

        javax.swing.GroupLayout painel_dados_pessoaisLayout = new javax.swing.GroupLayout(painel_dados_pessoais);
        painel_dados_pessoais.setLayout(painel_dados_pessoaisLayout);
        painel_dados_pessoaisLayout.setHorizontalGroup(
            painel_dados_pessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painel_dados_pessoaisLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painel_dados_pessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painel_dados_pessoaisLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painel_dados_pessoaisLayout.createSequentialGroup()
                        .addGroup(painel_dados_pessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, painel_dados_pessoaisLayout.createSequentialGroup()
                                .addGroup(painel_dados_pessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(painel_dados_pessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(painel_dados_pessoaisLayout.createSequentialGroup()
                                        .addComponent(txtPreco, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtPrecoVd, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(cbCategorias, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(painel_dados_pessoaisLayout.createSequentialGroup()
                                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(24, 24, 24)
                                .addComponent(cbFornecedor, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(painel_dados_pessoaisLayout.createSequentialGroup()
                                .addGroup(painel_dados_pessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel3))
                                .addGap(19, 19, 19)
                                .addGroup(painel_dados_pessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnPesquisar, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painel_dados_pessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, 379, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(painel_dados_pessoaisLayout.createSequentialGroup()
                                            .addComponent(txtQtdEstoque, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(txtQtdEstoqueMinimo, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addComponent(jLabel11)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(txtQtdEstoqueMaximo, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(216, 216, 216))))
        );
        painel_dados_pessoaisLayout.setVerticalGroup(
            painel_dados_pessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painel_dados_pessoaisLayout.createSequentialGroup()
                .addContainerGap(41, Short.MAX_VALUE)
                .addGroup(painel_dados_pessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painel_dados_pessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPesquisar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painel_dados_pessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtQtdEstoque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(txtQtdEstoqueMinimo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(txtQtdEstoqueMaximo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(painel_dados_pessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(cbFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(painel_dados_pessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtPreco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtPrecoVd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(painel_dados_pessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(cbCategorias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        painel_guias.addTab("Dados do produto", painel_dados_pessoais);

        jLabel15.setText("Descrição: ");

        txtPesquisaDescricao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPesquisaDescricaoActionPerformed(evt);
            }
        });
        txtPesquisaDescricao.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesquisaDescricaoKeyReleased(evt);
            }
        });

        btnPesquisa.setText("Pesquisar");
        btnPesquisa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPesquisaActionPerformed(evt);
            }
        });

        tabela.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "codigo", "descrição", "qtd. estoque", "fornecedor", "est.Minimo", "est.Máximo", "preço médio cst", "preço venda", "categoria id"
            }
        ));
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabela);

        javax.swing.GroupLayout painel_consultaLayout = new javax.swing.GroupLayout(painel_consulta);
        painel_consulta.setLayout(painel_consultaLayout);
        painel_consultaLayout.setHorizontalGroup(
            painel_consultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painel_consultaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painel_consultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 768, Short.MAX_VALUE)
                    .addGroup(painel_consultaLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPesquisaDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPesquisa)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        painel_consultaLayout.setVerticalGroup(
            painel_consultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painel_consultaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painel_consultaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(txtPesquisaDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPesquisa))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        painel_guias.addTab("Consulta de Produtos", painel_consulta);

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
        btnImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImprimirActionPerformed(evt);
            }
        });

        jButton1.setText("FECHAR");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(painel_guias)
            .addGroup(layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(btnNovo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSalvar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEditar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnExcluir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnImprimir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(painel_guias, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, Short.MAX_VALUE)
                    .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(btnExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(btnSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(btnNovo, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarActionPerformed
        try {
            Produtos obj = new Produtos();

            // Descrição
            obj.setDescricao(txtDescricao.getText().trim());
            if (obj.getDescricao().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Descrição é obrigatória!");
                return;
            }

            // Preço de custo
            double precoCusto = parseMoeda(txtPreco.getText());
            if (precoCusto <= 0) {
                JOptionPane.showMessageDialog(this, "Preço de custo inválido!");
                txtPreco.requestFocus();
                return;
            }
            obj.setPrecoMedioCusto(precoCusto);

            // Quantidades
            obj.setQtd_estoque(parseDoubleOuZero(txtQtdEstoque.getText()));
            obj.setQtd_estoqueMinimo(parseDoubleOuZero(txtQtdEstoqueMinimo.getText()));
            obj.setQtd_estoqueMaximo(parseDoubleOuZero(txtQtdEstoqueMaximo.getText()));

            // Fornecedor
            CliFor fornecedor = (CliFor) cbFornecedor.getSelectedItem();
            if (fornecedor == null) {
                JOptionPane.showMessageDialog(this, "Selecione um fornecedor!");
                return;
            }
            obj.setCliFor(fornecedor);

            // Categoria
            String nomeCat = (String) cbCategorias.getSelectedItem();
            Categoria categoria = new CategoriaDAO().buscarPorNome(nomeCat);
            if (categoria == null) {
                JOptionPane.showMessageDialog(this, "Categoria inválida!");
                return;
            }
            obj.setCategoria(categoria);

            // CALCULA PREÇO DE VENDA AUTOMATICAMENTE
            obj.calcularPrecoVenda();

            // Atualiza o campo visual
            txtPrecoVd.setText(formatarMoeda(obj.getPrecoVenda()));

            // Salva no banco
            new ProdutosDAO().salvar(obj);

            JOptionPane.showMessageDialog(this, "Produto cadastrado com sucesso!\nPreço de Venda: " + formatarMoeda(obj.getPrecoVenda()));
            new LimpaTela().LimpaTela(painel_dados_pessoais);
            listar();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnSalvarActionPerformed

    private double parseDoubleOuZero(String texto) {
        try {
            return Double.parseDouble(texto.replace(".", "").replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String formatarValor(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "0";
        }

        // Remove espaços extras e símbolos inválidos
        valor = valor.trim().replaceAll("[^0-9,.-]", "");

        // Remove pontos de milhar e substitui a vírgula decimal por ponto
        valor = valor.replace(".", "").replace(",", ".");

        return valor;
    }

    private void btnPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPesquisarActionPerformed
        try {
            String nome = txtDescricao.getText();
            Produtos obj = new Produtos();
            ProdutosDAO dao;
            dao = new ProdutosDAO();
            CliFor f = new CliFor();
            CliForDAO daof = new CliForDAO();
            obj = (Produtos) dao.filtrarPorNome(nome); //BuscarProdutos(nome)

            if (obj.getDescricao() != null) {
                txtCodigo.setText(String.valueOf(obj.getId()));
                txtDescricao.setText(obj.getDescricao());
                txtPreco.setText(String.valueOf(obj.getPreco()));

                // Exibe a quantidade formatada com 3 casas decimais
                txtQtdEstoque.setText(obj.getQtd_Formatada());
                txtQtdEstoqueMinimo.setText(obj.getQtd_Formatada());
                txtQtdEstoqueMaximo.setText(obj.getQtd_Formatada());

                f = daof.BuscarFornecedor(obj.getCliFor().getNomeCliFor());
                cbFornecedor.getModel().setSelectedItem(f);
            } else {
                JOptionPane.showMessageDialog(null, "Produto não encontrado!");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Form_Produtos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnPesquisarActionPerformed

    private void btnNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNovoActionPerformed
        //Utilitarios util = new Utilitarios();
        //util.LimpaTela(painel_dados_pessoais);

        LimpaTela util = new LimpaTela();
        util.LimpaTela(painel_dados_pessoais);

        // Definir categoria padrão para evitar o erro
        if (cbCategorias.getItemCount() > 0) {
            cbCategorias.setSelectedIndex(0);  // Seleciona a primeira categoria
        }
    }//GEN-LAST:event_btnNovoActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        try {
            listar();
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(Form_Produtos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_formWindowActivated

    private void btnPesquisaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPesquisaActionPerformed
        String nome = "%" + txtPesquisaDescricao.getText() + "%";
        ProdutosDAO dao;
        try {
            dao = new ProdutosDAO();
            List<Produtos> lista = dao.filtrarPorNome(nome); //filtrar(nome);
            DefaultTableModel dados = (DefaultTableModel) tabela.getModel();
            dados.setNumRows(0);

            // Define o formato para moeda no padrão brasileiro
            @SuppressWarnings("deprecation")
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

            for (Produtos p : lista) {
                // Formata o preço
                String precoFormatado = nf.format(p.getPreco());

                dados.addRow(new Object[]{
                    p.getId(),
                    p.getDescricao(),
                    precoFormatado, // Exibe o preço formatado
                    p.getQtd_Formatada(), // Agora usa o método do Model para formatar a quantidade
                    p.getQtd_FormatadaMinimo(),
                    p.getQtd_FormatadaMaximo(),
                    p.getCliFor().getNomeCliFor()
                });
            }
        } catch (SQLException ex) {
            Logger.getLogger(Form_Produtos.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_btnPesquisaActionPerformed

    private void txtPesquisaDescricaoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesquisaDescricaoKeyReleased
        String nome = "%" + txtPesquisaDescricao.getText() + "%";
        ProdutosDAO dao;
        try {
            dao = new ProdutosDAO();
            List<Produtos> lista = dao.filtrarPorNome(nome); //Filtrar(nome);
            DefaultTableModel dados = (DefaultTableModel) tabela.getModel();
            dados.setNumRows(0);
            for (Produtos p : lista) {
                dados.addRow(new Object[]{
                    p.getId(),
                    p.getDescricao(),
                    p.getPreco(),
                    p.getQtd_Formatada(), // Agora usa o método do Model para formatar a quantidade
                    p.getQtd_FormatadaMinimo(),
                    p.getQtd_FormatadaMaximo(),
                    p.getCliFor().getNomeCliFor()
                });
            }
        } catch (SQLException ex) {
            Logger.getLogger(Form_Produtos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_txtPesquisaDescricaoKeyReleased

    private void txtDescricaoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDescricaoKeyReleased

    }//GEN-LAST:event_txtDescricaoKeyReleased

    @SuppressWarnings("unchecked")
    private void carregarFornecedores() {
        CliForDAO dao;
        try {
            dao = new CliForDAO();
            List<CliFor> lista = dao.listarTodos(); //ListarF();
            cbFornecedor.removeAllItems();
            for (CliFor f : lista) {
                cbFornecedor.addItem(f); // Adiciona a instância de Fornecedores
            }
        } catch (SQLException ex) {
            Logger.getLogger(Form_Produtos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void tabelaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelaMouseClicked

        int linha = tabela.getSelectedRow();
        if (linha >= 0) {
            try {
                int idProduto = (int) tabela.getValueAt(linha, 0);
                ProdutosDAO dao = new ProdutosDAO();
                Produtos p = dao.buscarPorId(idProduto);  // ? AQUI VOCÊ BUSCA O PRODUTO

                if (p != null) {
                    txtCodigo.setText(String.valueOf(p.getId()));
                    txtDescricao.setText(p.getDescricao());
                    txtPreco.setText(formatarMoeda(p.getPrecoMedioCusto()));
                    txtPrecoVd.setText(formatarMoeda(p.getPrecoVenda())); // ? AGORA FUNCIONA!

                    txtQtdEstoque.setText(String.format("%.3f", p.getQtd_estoque()));
                    txtQtdEstoqueMinimo.setText(String.format("%.3f", p.getQtd_estoqueMinimo()));
                    txtQtdEstoqueMaximo.setText(String.format("%.3f", p.getQtd_estoqueMaximo()));

                    // Fornecedor
                    for (int i = 0; i < cbFornecedor.getItemCount(); i++) {
                        CliFor f = (CliFor) cbFornecedor.getItemAt(i);
                        if (f.getId() == p.getCliFor().getId()) {
                            cbFornecedor.setSelectedIndex(i);
                            break;
                        }
                    }

                    // Categoria
                    if (p.getCategoria() != null) {
                        for (int i = 0; i < cbCategorias.getItemCount(); i++) {
                            if (cbCategorias.getItemAt(i).equals(p.getCategoria().getNome())) {
                                cbCategorias.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar produto: " + ex.getMessage());
            }
        }

    }//GEN-LAST:event_tabelaMouseClicked

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed

        try {
            if (txtCodigo.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selecione um produto para editar!");
                return;
            }

            Produtos obj = new Produtos();
            obj.setId(Integer.parseInt(txtCodigo.getText()));
            obj.setDescricao(txtDescricao.getText().trim());

            // PREÇO DE CUSTO
            double precoCusto = parseMoeda(txtPreco.getText());
            if (precoCusto <= 0) {
                JOptionPane.showMessageDialog(this, "Preço de custo inválido!");
                txtPreco.requestFocus();
                return;
            }
            obj.setPrecoMedioCusto(precoCusto);

            // QUANTIDADES
            obj.setQtd_estoque(parseDoubleOuZero(txtQtdEstoque.getText()));
            obj.setQtd_estoqueMinimo(parseDoubleOuZero(txtQtdEstoqueMinimo.getText()));
            obj.setQtd_estoqueMaximo(parseDoubleOuZero(txtQtdEstoqueMaximo.getText()));

            // FORNECEDOR
            CliFor fornecedor = (CliFor) cbFornecedor.getSelectedItem();
            if (fornecedor == null) {
                JOptionPane.showMessageDialog(this, "Selecione um fornecedor!");
                return;
            }
            obj.setCliFor(fornecedor);

            // CATEGORIA
            String nomeCat = (String) cbCategorias.getSelectedItem();
            Categoria categoria = new CategoriaDAO().buscarPorNome(nomeCat);
            if (categoria == null) {
                JOptionPane.showMessageDialog(this, "Categoria inválida!");
                return;
            }
            obj.setCategoria(categoria);

            // AQUI É O SEGREDO: VOCÊ PODE ESCOLHER ENTRE:
            // 1. Usar preço de venda MANUAL (do txtPrecoVd) ? prioridade
            // 2. Se estiver vazio ou zero ? calcular automático
//            double precoVendaManual = parseMoeda(txtPrecoVd.getText());
//
//            if (precoVendaManual > 0) {
//                // USUÁRIO DIGITOU UM VALOR ? RESPEITA ELE!
//                obj.setPrecoVenda(precoVendaManual);
//            } else {
//                // CAMPO VAZIO OU ZERO ? CALCULA AUTOMÁTICO
//                obj.calcularPrecoVenda();
//                txtPrecoVd.setText(formatarMoeda(obj.getPrecoVenda()));
//            }
            // PREÇO DE VENDA ? AGORA ACEITA TUDO!
            double precoVendaDigitado = parseMoeda(txtPrecoVd.getText());

            if (precoVendaDigitado > 0) {
                // Usuário digitou algo ? usa o que ele quer
                obj.setPrecoVenda(precoVendaDigitado);
                txtPrecoVd.setText(formatarMoeda(precoVendaDigitado)); // formata bonitinho
            } else {
                // Campo vazio ou inválido ? calcula automático
                obj.calcularPrecoVenda();
                txtPrecoVd.setText(formatarMoeda(obj.getPrecoVenda()));
            }

            // SALVA NO BANCO (agora com o preço de venda CORRETO)
            new ProdutosDAO().atualizar(obj);

            JOptionPane.showMessageDialog(this,
                    "Produto atualizado com sucesso!\nPreço de Venda: " + formatarMoeda(obj.getPrecoVenda()),
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            new LimpaTela().LimpaTela(painel_dados_pessoais);
            listar();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao editar: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnEditarActionPerformed

    public static double parseStringToDouble(String valor) throws ParseException {
        // Usado tanto em quantidade como preço.
        // Remove o símbolo de moeda e os espaços
        String valorLimpo = valor.replace("R$", "").replace("\u00A0", "").trim();
        // Substitui a vírgula decimal por ponto e remove separadores de milhar
        valorLimpo = valorLimpo.replace(".", "").replace(",", ".");
        // Converte para double
        return Double.parseDouble(valorLimpo);
    }

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed
        if (txtCodigo.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para excluir!");
            return;
        }

        int id = Integer.parseInt(txtCodigo.getText());

        try {
            ProdutosDAO dao = new ProdutosDAO();

            // Busca descrição só pra confirmar na mensagem
            String descricao = dao.buscarDescricaoPorId(id);

            int confirma = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja excluir o produto:\n" + id + " - " + descricao + "?",
                    "Confirmação", JOptionPane.YES_NO_OPTION);

            if (confirma == JOptionPane.YES_OPTION) {
                dao.excluir(id);  // ? CORRIGIDO: agora passa só o ID!

                JOptionPane.showMessageDialog(this, "Produto excluído com sucesso!");
                new LimpaTela().LimpaTela(painel_dados_pessoais);
                try {
                    listar(); // atualiza a tabela
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Form_Produtos.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnExcluirActionPerformed

    private void cbFornecedorAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_cbFornecedorAncestorAdded
    }//GEN-LAST:event_cbFornecedorAncestorAdded

    @SuppressWarnings("unchecked")
    private void cbFornecedorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cbFornecedorMouseClicked
        CliForDAO dao;
        try {
            dao = new CliForDAO();
            List<CliFor> lista = dao.listarTodos(); //Listar();
            cbFornecedor.removeAllItems();
            for (CliFor f : lista) {
                cbFornecedor.addItem(f);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Form_Produtos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_cbFornecedorMouseClicked

    private void txtDescricaoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDescricaoKeyPressed

        if (evt.getKeyCode() == KeyEvent.VK_ENTER) { // Verifica se a tecla ENTER foi pressionada
            String nome = txtDescricao.getText(); // Obtém o texto digitado no campo descrição
            Produtos obj = new Produtos();
            ProdutosDAO dao;

            try {
                dao = new ProdutosDAO(); // Inicializa o DAO para buscar o produto
                obj = dao.buscarPorNome(nome); //BuscarProdutos(nome); // Busca o produto pelo nome

                if (obj.getDescricao() != null) { // Se o produto foi encontrado
                    txtCodigo.setText(String.valueOf(obj.getId())); // Preenche o campo código
                    txtDescricao.setText(obj.getDescricao()); // Preenche o campo descrição

                    // Formata a quantidade em estoque com 3 casas decimais
                    String qtdEstoqueFormatada = String.format("%.3f", obj.getQtd_estoque());
                    txtQtdEstoque.setText(qtdEstoqueFormatada);
                    txtQtdEstoqueMinimo.setText(qtdEstoqueFormatada);
                    txtQtdEstoqueMaximo.setText(qtdEstoqueFormatada);

                    // ? Seleção na ComboBox diretamente pelo nome
                    String fornecedorNome = obj.getCliFor().getNomeCliFor();
                    System.out.println("Fornecedor buscado: " + fornecedorNome);

                    boolean fornecedorEncontrado = false;
                    for (int i = 0; i < cbFornecedor.getItemCount(); i++) {
                        String itemNome = cbFornecedor.getItemAt(i).toString();
                        System.out.println("Fornecedor na ComboBox: " + itemNome); // Depuração
                        if (itemNome.equalsIgnoreCase(fornecedorNome)) {
                            cbFornecedor.setSelectedIndex(i);
                            fornecedorEncontrado = true;
                            break;
                        }
                    }

                    if (!fornecedorEncontrado) {
                        System.out.println("Fornecedor não encontrado na ComboBox: " + fornecedorNome);
                        cbFornecedor.setSelectedIndex(-1); // Nenhum item selecionado
                    }

                    // Formata o preço como moeda
                    @SuppressWarnings("deprecation")
                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                    String precoFormatado = nf.format(obj.getPreco());
                    txtPreco.setText(precoFormatado);
                    txtPrecoVd.setText(precoFormatado);
                }
            } catch (SQLException ex) {
                Logger.getLogger(Form_Produtos.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NumberFormatException ex) {
                System.out.println("Erro ao converter número: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_txtDescricaoKeyPressed

    private void txtPesquisaDescricaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPesquisaDescricaoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPesquisaDescricaoActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose(); // Fecha o formulário
    }//GEN-LAST:event_jButton1ActionPerformed

    private void txtQtdEstoqueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQtdEstoqueActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtQtdEstoqueActionPerformed

    @SuppressWarnings("deprecation")
    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        try {
            // FileChooser para definir o local de salvamento
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Salvar Relatório");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));

            // Define Área de Trabalho como diretório padrão
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return; // Usuário cancelou
            }

            // Caminho do arquivo
            String caminho = fileChooser.getSelectedFile().getAbsolutePath();
            if (!caminho.toLowerCase().endsWith(".pdf")) {
                caminho += ".pdf";
            }

            // Formatação de moeda e quantidade
            @SuppressWarnings("deprecation")
            NumberFormat moedaFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            DecimalFormat quantidadeFormat = new DecimalFormat("#,##0.000");
            quantidadeFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(new Locale("pt", "BR")));

            // Criação do Documento
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(caminho));
            document.open();

            // Adiciona Imagem
            try {
                Image imagem = Image.getInstance("src\\Imagens\\Home.png");
                imagem.scaleAbsolute(45f, 50f);
                imagem.setAlignment(Element.ALIGN_LEFT);
                document.add(imagem);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar imagem: " + e.getMessage(), "Erro de Imagem", JOptionPane.WARNING_MESSAGE);
            }

            // Título
            Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph titulo = new Paragraph("Listagem de Produtos em Estoque", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(10);
            document.add(titulo);

            // Subtítulo com Data e Hora
            Font fontSubtitulo = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC);
            String dataHoraAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            Paragraph subtitulo = new Paragraph("Emitido em: " + dataHoraAtual, fontSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(20);
            document.add(subtitulo);

            // Criação da Tabela
            PdfPTable tabela = new PdfPTable(9); // 9 colunas
            tabela.setWidthPercentage(100);
            tabela.setSpacingBefore(10);
            tabela.setSpacingAfter(10);
            tabela.setWidths(new float[]{1f, 3f, 1.5f, 2f, 1.5f, 1.5f, 2f, 2f, 1f});

            // Cabeçalhos
            Font fontCabecalho = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            String[] colunas = {"Código", "Descrição", "Qtd. Estoque", "Fornecedor", "Est. Mínimo", "Est. Máximo", "Preço Médio Custo", "Preço Venda", "Categoria ID"};
            for (String coluna : colunas) {
                PdfPCell cell = new PdfPCell(new Phrase(coluna, fontCabecalho));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                tabela.addCell(cell);
            }

            // Preenchimento da Tabela
            ProdutosDAO dao = new ProdutosDAO();
            List<Produtos> lista = dao.listarTodos(); //Listar();

            for (Produtos p : lista) {
                tabela.addCell(String.valueOf(p.getId())); // Código
                tabela.addCell(p.getDescricao()); // Descrição
                tabela.addCell(quantidadeFormat.format(p.getQtd_estoque())); // Qtd. Estoque
                tabela.addCell(p.getCliFor().getNomeCliFor()); // Fornecedor
                tabela.addCell(quantidadeFormat.format(p.getQtd_estoqueMinimo())); // Est. Mínimo
                tabela.addCell(quantidadeFormat.format(p.getQtd_estoqueMaximo())); // Est. Máximo
                tabela.addCell(moedaFormat.format(p.getPrecoMedioCusto())); // Preço Médio Custo
                tabela.addCell(moedaFormat.format(p.getPrecoVenda())); // Preço Venda
                tabela.addCell(String.valueOf(p.getCategoria().getId())); // Categoria ID
            }

            document.add(tabela);

            // Rodapé
            Font fontRodape = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
            Paragraph rodape = new Paragraph("Relatório gerado automaticamente pelo sistema.", fontRodape);
            rodape.setAlignment(Element.ALIGN_CENTER);
            document.add(rodape);

            document.close();

            // Mensagem de Sucesso
            JOptionPane.showMessageDialog(this, "PDF gerado com sucesso!\nSalvo em: " + caminho, "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            // Abre o PDF automaticamente
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(new File(caminho));
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Erro ao abrir PDF: " + e.getMessage(), "Erro ao Abrir", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao gerar PDF: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnImprimirActionPerformed

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
            java.util.logging.Logger.getLogger(Form_Produtos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Form_Produtos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Form_Produtos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Form_Produtos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            //new FormularioProdutos().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnNovo;
    private javax.swing.JButton btnPesquisa;
    private javax.swing.JButton btnPesquisar;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JComboBox<String> cbCategorias;
    private javax.swing.JComboBox cbFornecedor;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel painel_consulta;
    private javax.swing.JPanel painel_dados_pessoais;
    public javax.swing.JTabbedPane painel_guias;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtDescricao;
    private javax.swing.JTextField txtPesquisaDescricao;
    private javax.swing.JTextField txtPreco;
    private javax.swing.JTextField txtPrecoVd;
    private javax.swing.JTextField txtQtdEstoque;
    private javax.swing.JTextField txtQtdEstoqueMaximo;
    private javax.swing.JTextField txtQtdEstoqueMinimo;
    // End of variables declaration//GEN-END:variables
}
