package view;

import utilitarios.AgCompr.Alart_Body;
import utilitarios.BlocNotas;
import Mcalendar.MainCalendApp;
import utilitarios.Conexao;
import utilitarios.ConfTelaPrincipal;
import utilitarios.ImageDBHandler;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Tela_Principal extends JFrame {

    Conexao conexao = new Conexao();

    private final String tempImagePath = System.getProperty("user.dir")
            + File.separator + "temp" + File.separator + "temp_image.png";

    public Tela_Principal() throws IOException {
        initComponents();
        // Configura lblfondo como fundo (sem ícone fixo!)
        lblfondo.setHorizontalAlignment(JLabel.CENTER);
        lblfondo.setVerticalAlignment(JLabel.CENTER);
        lblfondo.setOpaque(false);  // permite ver os componentes por cima
        // Adiciona como fundo (ï¿½ndice 0)
        getContentPane().add(lblfondo, 0);
        // Recupera do banco e aplica
        ImageDBHandler.retrieveImageFromDatabase(tempImagePath);
        //atualizarImagemDeFundo();
        setSize(887, 720); //Horizontal x Vertical
        setLocationRelativeTo(null);
        setVisible(true);
        // Listener de redimensionamento
        addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentResized(java.awt.event.ComponentEvent evt) {

                atualizarImagemDeFundo();

            }

        });
        setVisible(true);

        // ? Aqui: agenda a primeira atualização depois da tela aparecer
        SwingUtilities.invokeLater(() -> {
            atualizarImagemDeFundo();
        });
    }

    public void atualizarImagemDeFundo() {
        File arquivo = new File(tempImagePath);
        if (!arquivo.exists() || arquivo.length() == 0) {
            lblfondo.setIcon(null);
            return;
        }

        try {
            ImageIcon icon = new ImageIcon(tempImagePath);
            if (icon.getIconWidth() <= 0) {
                return;
            }

            // Redimensiona para o tamanho atual do label/fundo
            Image scaled = icon.getImage().getScaledInstance(
                    lblfondo.getWidth(),
                    lblfondo.getHeight(),
                    Image.SCALE_SMOOTH
            );
            lblfondo.setIcon(new ImageIcon(scaled));
            lblfondo.revalidate();
            lblfondo.repaint();
        } catch (Exception e) {
            e.printStackTrace();
            lblfondo.setIcon(null);
        }
    }

    // Método chamado pela ConfTelaPrincipal após copiar nova imagem
    public void definirTelaPrincipal(String caminhoSelecionado, String destinoPath) {
        try {
            // Copia para o caminho fixo que usamos sempre
            Files.copy(
                    Paths.get(caminhoSelecionado),
                    Paths.get(destinoPath), // deve ser o mesmo tempImagePath
                    StandardCopyOption.REPLACE_EXISTING
            );
            // Opcional: salva no banco se ainda precisar
            ImageDBHandler.saveImageToDatabase(new File(destinoPath));
            // Atualiza imediatamente
            atualizarImagemDeFundo();
            JOptionPane.showMessageDialog(this, "Fundo atualizado!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jPanel1 = new javax.swing.JPanel();
        jLHora = new javax.swing.JLabel();
        jLUsu = new javax.swing.JLabel();
        jLData = new javax.swing.JLabel();
        lblfondo = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuCadastros = new javax.swing.JMenu();
        jMenuItemUsu = new javax.swing.JMenuItem();
        jMenuItemPlaCon = new javax.swing.JMenuItem();
        jMenuItemRecursos = new javax.swing.JMenuItem();
        jMenuItemFavorecidos = new javax.swing.JMenuItem();
        jMenuMovimentacao = new javax.swing.JMenu();
        jMenuItemMovLancamentos = new javax.swing.JMenuItem();
        jMenuItemMovTransf = new javax.swing.JMenuItem();
        jMenuItemMovParcelamentos = new javax.swing.JMenuItem();
        jMenuItemMovParcelamCart = new javax.swing.JMenuItem();
        jMenuConsultas = new javax.swing.JMenu();
        jMenuItemConsRecursos = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemPFornecedores = new javax.swing.JMenuItem();
        jMenuItemPClientes = new javax.swing.JMenuItem();
        jMenuItemInv = new javax.swing.JMenuItem();
        jMenuItemConsClassificacao = new javax.swing.JMenuItem();
        jMenuItemSaldos = new javax.swing.JMenuItem();
        jMenuItemFlxD = new javax.swing.JMenuItem();
        jMenuItemRD = new javax.swing.JMenuItem();
        jMenuItemRC = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItemBalance = new javax.swing.JMenuItem();
        jMenuItemPainel = new javax.swing.JMenuItem();
        jMenuItemAuditoria = new javax.swing.JMenuItem();
        jMenuRelatorios = new javax.swing.JMenu();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuCompras = new javax.swing.JMenu();
        jMenuItemFormCompras = new javax.swing.JMenuItem();
        jMenuItemPosDiaCompras = new javax.swing.JMenuItem();
        jMenuItemHistComrpas = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItemPosVds = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuManutencao = new javax.swing.JMenu();
        jMenuItemBackups = new javax.swing.JMenuItem();
        jMenuItemConfTela = new javax.swing.JMenuItem();
        jMenuItemImportarSQL = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenu7 = new javax.swing.JMenu();
        jMenuItemProjtos = new javax.swing.JMenuItem();
        jMenuItemEtapas = new javax.swing.JMenuItem();
        jMenu8 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuUtilitarios = new javax.swing.JMenu();
        jMenuItemAbrirPDF = new javax.swing.JMenuItem();
        jMenuItemCalendario = new javax.swing.JMenuItem();
        jMenuItemAgenda = new javax.swing.JMenuItem();
        jMenuItemBlocoNotas = new javax.swing.JMenuItem();
        jMenuSair = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        jMenuItem2.setText("jMenuItem2");

        jMenu2.setText("jMenu2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sistema Financeiro Java SQLite");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jLHora.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLHora.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLHora.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLUsu.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLUsu.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLUsu.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLData.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLData.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLData.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 530, Short.MAX_VALUE)
                .addComponent(jLData, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jLHora, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jLHora, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
            .addComponent(jLData, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLUsu, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        lblfondo.setPreferredSize(new java.awt.Dimension(900, 586));

        jMenuBar1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jMenuBar1.setPreferredSize(new java.awt.Dimension(559, 32));

        jMenuCadastros.setText("Cadastros");
        jMenuCadastros.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItemUsu.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemUsu.setText("Usuários");
        jMenuItemUsu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemUsuActionPerformed(evt);
            }
        });
        jMenuCadastros.add(jMenuItemUsu);

        jMenuItemPlaCon.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemPlaCon.setText("Plano de Contas");
        jMenuItemPlaCon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPlaConActionPerformed(evt);
            }
        });
        jMenuCadastros.add(jMenuItemPlaCon);

        jMenuItemRecursos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemRecursos.setText("Recursos");
        jMenuItemRecursos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRecursosActionPerformed(evt);
            }
        });
        jMenuCadastros.add(jMenuItemRecursos);

        jMenuItemFavorecidos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemFavorecidos.setText("Favorecidos");
        jMenuItemFavorecidos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFavorecidosActionPerformed(evt);
            }
        });
        jMenuCadastros.add(jMenuItemFavorecidos);

        jMenuBar1.add(jMenuCadastros);

        jMenuMovimentacao.setText("Movimentação");
        jMenuMovimentacao.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuMovimentacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuMovimentacaoActionPerformed(evt);
            }
        });

        jMenuItemMovLancamentos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemMovLancamentos.setText("Lançamentos");
        jMenuItemMovLancamentos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMovLancamentosActionPerformed(evt);
            }
        });
        jMenuMovimentacao.add(jMenuItemMovLancamentos);

        jMenuItemMovTransf.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemMovTransf.setText("Transferências");
        jMenuItemMovTransf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMovTransfActionPerformed(evt);
            }
        });
        jMenuMovimentacao.add(jMenuItemMovTransf);

        jMenuItemMovParcelamentos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemMovParcelamentos.setText("Parcelamentos");
        jMenuItemMovParcelamentos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMovParcelamentosActionPerformed(evt);
            }
        });
        jMenuMovimentacao.add(jMenuItemMovParcelamentos);

        jMenuItemMovParcelamCart.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemMovParcelamCart.setText("Parecelamento de Cartões");
        jMenuItemMovParcelamCart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMovParcelamCartActionPerformed(evt);
            }
        });
        jMenuMovimentacao.add(jMenuItemMovParcelamCart);

        jMenuBar1.add(jMenuMovimentacao);

        jMenuConsultas.setText("Consultas");
        jMenuConsultas.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItemConsRecursos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemConsRecursos.setText("Recursos");
        jMenuItemConsRecursos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemConsRecursosActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItemConsRecursos);

        jMenu1.setText("Favorecidos");
        jMenu1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItemPFornecedores.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemPFornecedores.setText("Fornecedores");
        jMenuItemPFornecedores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPFornecedoresActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemPFornecedores);

        jMenuItemPClientes.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemPClientes.setText("Clientes");
        jMenuItemPClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPClientesActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemPClientes);

        jMenuConsultas.add(jMenu1);

        jMenuItemInv.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemInv.setText("Investimentos");
        jMenuItemInv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemInvActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItemInv);

        jMenuItemConsClassificacao.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemConsClassificacao.setText("Classificação");
        jMenuItemConsClassificacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemConsClassificacaoActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItemConsClassificacao);

        jMenuItemSaldos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemSaldos.setText("Saldos");
        jMenuItemSaldos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaldosActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItemSaldos);

        jMenuItemFlxD.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemFlxD.setText("Fluxo Diário");
        jMenuItemFlxD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFlxDActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItemFlxD);

        jMenuItemRD.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemRD.setText("Receitas X Despesas");
        jMenuItemRD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRDActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItemRD);

        jMenuItemRC.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemRC.setText("Referência Cruzada");
        jMenuItemRC.setActionCommand("Referênciaa Cruzada");
        jMenuItemRC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRCActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItemRC);

        jMenuItem11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItem11.setText("Inflação pessoal");
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItem11);

        jMenuItemBalance.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemBalance.setText("Balanço");
        jMenuItemBalance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemBalanceActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItemBalance);

        jMenuItemPainel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemPainel.setText("Painel Diário");
        jMenuItemPainel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPainelActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItemPainel);

        jMenuItemAuditoria.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemAuditoria.setText("Auditoria");
        jMenuItemAuditoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAuditoriaActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItemAuditoria);

        jMenuBar1.add(jMenuConsultas);

        jMenuRelatorios.setText("Estoque");
        jMenuRelatorios.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenu6.setText("Categorias");
        jMenu6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItem9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem9.setText("Formulario de Categorias");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem9);

        jMenuRelatorios.add(jMenu6);

        jMenu3.setText("Produtos");
        jMenu3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItem7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem7.setText("Formulário de Produtos");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem7);

        jMenuItem5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem5.setText("Consulta de Produtos");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem5);

        jMenuItem6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem6.setText("Controle de Estoque");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem6);

        jMenuRelatorios.add(jMenu3);

        jMenuCompras.setText("Compras");
        jMenuCompras.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItemFormCompras.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemFormCompras.setText("Formulário de Compras");
        jMenuItemFormCompras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFormComprasActionPerformed(evt);
            }
        });
        jMenuCompras.add(jMenuItemFormCompras);

        jMenuItemPosDiaCompras.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemPosDiaCompras.setText("Posição do dia - Compras");
        jMenuItemPosDiaCompras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPosDiaComprasActionPerformed(evt);
            }
        });
        jMenuCompras.add(jMenuItemPosDiaCompras);

        jMenuItemHistComrpas.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemHistComrpas.setText("Histórico de Compras");
        jMenuItemHistComrpas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemHistComrpasActionPerformed(evt);
            }
        });
        jMenuCompras.add(jMenuItemHistComrpas);

        jMenuRelatorios.add(jMenuCompras);

        jMenu4.setText("Vendas");
        jMenu4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItem8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem8.setText("Abrir o PDV");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem8);

        jMenuItemPosVds.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemPosVds.setText("Posição do Dia -Vendas");
        jMenuItemPosVds.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPosVdsActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItemPosVds);

        jMenuItem10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem10.setText("Histórico de Vendas");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem10);

        jMenuRelatorios.add(jMenu4);

        jMenuItem3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItem3.setText("Precificação");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenuRelatorios.add(jMenuItem3);

        jMenuBar1.add(jMenuRelatorios);

        jMenuManutencao.setText("Manutenção");
        jMenuManutencao.setToolTipText("");
        jMenuManutencao.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItemBackups.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemBackups.setText("Backups do Sistema");
        jMenuItemBackups.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemBackupsActionPerformed(evt);
            }
        });
        jMenuManutencao.add(jMenuItemBackups);

        jMenuItemConfTela.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemConfTela.setText("Configura Tela Principal");
        jMenuItemConfTela.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemConfTelaActionPerformed(evt);
            }
        });
        jMenuManutencao.add(jMenuItemConfTela);

        jMenuItemImportarSQL.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemImportarSQL.setText("Importar SQL (FinAS)");
        jMenuItemImportarSQL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemImportarSQLActionPerformed(evt);
            }
        });
        jMenuManutencao.add(jMenuItemImportarSQL);

        jMenuBar1.add(jMenuManutencao);

        jMenu5.setText("Projetos");
        jMenu5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenu7.setText("Cadastros");
        jMenu7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItemProjtos.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemProjtos.setText("Formulário Projetos");
        jMenuItemProjtos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemProjtosActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItemProjtos);

        jMenuItemEtapas.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemEtapas.setText("Formulário Etapas");
        jMenuItemEtapas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemEtapasActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItemEtapas);

        jMenu5.add(jMenu7);

        jMenu8.setText("Consulta ");
        jMenu8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItem4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem4.setText("Projetos/Etapas");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu8.add(jMenuItem4);

        jMenu5.add(jMenu8);

        jMenuBar1.add(jMenu5);

        jMenuUtilitarios.setText("Utilitários");
        jMenuUtilitarios.setToolTipText("");
        jMenuUtilitarios.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItemAbrirPDF.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemAbrirPDF.setText("Abrir PDF");
        jMenuItemAbrirPDF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAbrirPDFActionPerformed(evt);
            }
        });
        jMenuUtilitarios.add(jMenuItemAbrirPDF);

        jMenuItemCalendario.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemCalendario.setText("Calendário");
        jMenuItemCalendario.setToolTipText("");
        jMenuItemCalendario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCalendarioActionPerformed(evt);
            }
        });
        jMenuUtilitarios.add(jMenuItemCalendario);

        jMenuItemAgenda.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemAgenda.setText("Agenda");
        jMenuItemAgenda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAgendaActionPerformed(evt);
            }
        });
        jMenuUtilitarios.add(jMenuItemAgenda);

        jMenuItemBlocoNotas.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemBlocoNotas.setText("Bloco de Notas");
        jMenuItemBlocoNotas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemBlocoNotasActionPerformed(evt);
            }
        });
        jMenuUtilitarios.add(jMenuItemBlocoNotas);

        jMenuBar1.add(jMenuUtilitarios);

        jMenuSair.setText("Sair");
        jMenuSair.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuSairActionPerformed(evt);
            }
        });

        jMenuItem1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItem1.setText("Encerra Sistema");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenuSair.add(jMenuItem1);

        jMenuBar1.add(jMenuSair);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblfondo, javax.swing.GroupLayout.PREFERRED_SIZE, 860, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblfondo, javax.swing.GroupLayout.PREFERRED_SIZE, 609, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setSize(new java.awt.Dimension(886, 684));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents


    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed

        Object[] options = {"Sim", "Não"};

        int i = JOptionPane.showOptionDialog(null, "Deseja sair do Sistema? !!", "Encerrar Sistema", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (i == JOptionPane.YES_OPTION) {

            try {

                //opção SIM selecionada !!}
                Deslogado();

            } catch (SQLException ex) {

                Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);

            }

            System.exit(0);

        }

    }//GEN-LAST:event_jMenuItem1ActionPerformed


    private void jMenuItemUsuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUsuActionPerformed

        // TODO add your handling code here:
        Tela_CadUsu exibir = new Tela_CadUsu();

        exibir.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemUsuActionPerformed


    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

        // Data
        Date dataSistema = new Date();

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");

        jLData.setText(formato.format(dataSistema));

        // Hora 
        Timer timer = new Timer(1000, new hora());

        timer.start();

        // Usuario
        jLUsu.setText(usuarioLogado);

    }//GEN-LAST:event_formWindowOpened


    private void jMenuItemRecursosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRecursosActionPerformed

        Tela_CadRecursos exibir = null;

        try {

            exibir = new Tela_CadRecursos();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        exibir.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemRecursosActionPerformed


    private void jMenuItemPlaConActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPlaConActionPerformed

        Tela_CadPlaCon exibir = new Tela_CadPlaCon();

        exibir.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemPlaConActionPerformed


    private void jMenuItemFavorecidosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFavorecidosActionPerformed

        Tela_CadCliFor exibir = null;

        try {

            exibir = new Tela_CadCliFor();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        exibir.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemFavorecidosActionPerformed


    private void jMenuItemMovLancamentosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMovLancamentosActionPerformed

        Tela_Mov lanc = null;

        try {

            lanc = new Tela_Mov();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        lanc.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemMovLancamentosActionPerformed


    private void jMenuMovimentacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuMovimentacaoActionPerformed

        // TODO add your handling code here:

    }//GEN-LAST:event_jMenuMovimentacaoActionPerformed


    private void jMenuItemAbrirPDFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAbrirPDFActionPerformed

        Tela_JFileChosse exibir = null;

        exibir = new Tela_JFileChosse();

        exibir.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemAbrirPDFActionPerformed


    private void jMenuItemMovTransfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMovTransfActionPerformed

        Tela_Transf transf = null;

        try {

            transf = new Tela_Transf();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        transf.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemMovTransfActionPerformed


    private void jMenuItemMovParcelamentosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMovParcelamentosActionPerformed

        Tela_Parcelamento parcel = null;

        try {

            parcel = new Tela_Parcelamento();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        parcel.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemMovParcelamentosActionPerformed


    private void jMenuItemConsRecursosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemConsRecursosActionPerformed

        Tela_Consulta_Recursos pesq = null;

        try {

            pesq = new Tela_Consulta_Recursos();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        pesq.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemConsRecursosActionPerformed


    private void jMenuItemPFornecedoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPFornecedoresActionPerformed

        Tela_Consulta_Fornecedores pesqf = null;

        try {

            pesqf = new Tela_Consulta_Fornecedores();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        pesqf.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemPFornecedoresActionPerformed


    private void jMenuItemConsClassificacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemConsClassificacaoActionPerformed

        Tela_Consulta_Invest pesqc = null;

        try {

            pesqc = new Tela_Consulta_Invest();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        pesqc.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemConsClassificacaoActionPerformed


    private void jMenuItemBackupsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemBackupsActionPerformed

        Tela_Backup bkp = null;

        bkp = new Tela_Backup();

        bkp.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemBackupsActionPerformed

    private void jMenuItemImportarSQLActionPerformed(java.awt.event.ActionEvent evt) {
        Tela_ImportarSQL importarSQL = new Tela_ImportarSQL();
        importarSQL.executarImportacao(this);
    }


    private void jMenuItemPClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPClientesActionPerformed

        Tela_Consulta_Clientes pesqf = null;

        try {

            pesqf = new Tela_Consulta_Clientes();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        pesqf.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemPClientesActionPerformed


    private void jMenuItemSaldosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaldosActionPerformed

        Tela_Consulta_Saldos pesqs = null;

        try {

            pesqs = new Tela_Consulta_Saldos();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        pesqs.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemSaldosActionPerformed


    private void jMenuItemFlxDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFlxDActionPerformed

        Tela_Consulta_FlxCxD pesqfd = null;

        try {

            pesqfd = new Tela_Consulta_FlxCxD();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        pesqfd.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemFlxDActionPerformed


    private void jMenuItemRDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRDActionPerformed

        Tela_Consulta_RD pesqRD = null;

        try {

            pesqRD = new Tela_Consulta_RD();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        pesqRD.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemRDActionPerformed


    private void jMenuItemRCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRCActionPerformed

        Tela_Consulta_RC pesqRC = null;

        try {

            pesqRC = new Tela_Consulta_RC();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        pesqRC.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemRCActionPerformed


    private void jMenuItemCalendarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCalendarioActionPerformed
        try {
            MainCalendApp exibir = new MainCalendApp();
            exibir.setVisible(true);
            this.setExtendedState(ICONIFIED); // Minimiza ao invÃ©s de esconder
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir calendário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_jMenuItemCalendarioActionPerformed


    private void jMenuItemAgendaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAgendaActionPerformed

        Alart_Body obj = new Alart_Body();

        obj.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemAgendaActionPerformed


    private void jMenuItemBlocoNotasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemBlocoNotasActionPerformed

        BlocNotas exibir = null;

        exibir = new BlocNotas();

        exibir.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemBlocoNotasActionPerformed


    private void jMenuItemBalanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemBalanceActionPerformed

        Tela_Consulta_Balance pesqBl = null;

        try {

            pesqBl = new Tela_Consulta_Balance();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        pesqBl.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemBalanceActionPerformed


    private void jMenuItemMovParcelamCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMovParcelamCartActionPerformed

        Tela_ParcCart parcelCart = null;

        try {

            parcelCart = new Tela_ParcCart();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        parcelCart.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemMovParcelamCartActionPerformed


    private void jMenuItemConfTelaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemConfTelaActionPerformed

        ConfTelaPrincipal ctela = new ConfTelaPrincipal(this);

        ctela.setVisible(true);

        // Fechar o menu
        jMenuManutencao.setVisible(false);

        // Fechar a Tela_Principal
        setVisible(false);

    }//GEN-LAST:event_jMenuItemConfTelaActionPerformed


    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed

        Form_Produtos consultaProdutos;

        try {

            consultaProdutos = new Form_Produtos(this, true);

            consultaProdutos.painel_guias.setSelectedIndex(1);

            consultaProdutos.setModal(rootPaneCheckingEnabled);

            consultaProdutos.setVisible(true);

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);

        } catch (ClassNotFoundException ex) {

            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);

        }


    }//GEN-LAST:event_jMenuItem5ActionPerformed


    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed

        Form_Estoque fe = new Form_Estoque(this, true);

        fe.setModal(rootPaneCheckingEnabled);

        fe.setVisible(true);

    }//GEN-LAST:event_jMenuItem6ActionPerformed


    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed

        Form_Produtos fp;

        try {

            fp = new Form_Produtos(this, true);

            fp.setModal(rootPaneCheckingEnabled);

            fp.setVisible(true);

        } catch (SQLException | ClassNotFoundException ex) {

            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);

        }


    }//GEN-LAST:event_jMenuItem7ActionPerformed


    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed

        Form_Vendas fv;

        try {

            fv = new Form_Vendas();

            fv.setVisible(true);

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);

        }


    }//GEN-LAST:event_jMenuItem8ActionPerformed


    private void jMenuItemPosVdsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPosVdsActionPerformed

        Form_TotalDoDiaVendas fd = new Form_TotalDoDiaVendas(this, true);

        fd.setModal(rootPaneCheckingEnabled);

        fd.setVisible(true);

    }//GEN-LAST:event_jMenuItemPosVdsActionPerformed


    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed

        Form_HistoricoVendas fh = new Form_HistoricoVendas();

        fh.setVisible(true);

    }//GEN-LAST:event_jMenuItem10ActionPerformed


    private void jMenuItemFormComprasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFormComprasActionPerformed

        try {

            Form_Compra fc;

            fc = new Form_Compra();

            fc.setVisible(true);

        } catch (ClassNotFoundException ex) {

            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);

        }

    }//GEN-LAST:event_jMenuItemFormComprasActionPerformed


    private void jMenuItemPosDiaComprasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPosDiaComprasActionPerformed

        Form_TotalDoDiaCompras fdc;

        try {

            fdc = new Form_TotalDoDiaCompras(this, true);

            fdc.setModal(rootPaneCheckingEnabled);

            fdc.setVisible(true);

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);

        }


    }//GEN-LAST:event_jMenuItemPosDiaComprasActionPerformed


    private void jMenuItemHistComrpasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHistComrpasActionPerformed

        Form_HistoricoCompras fhc;

        try {

            fhc = new Form_HistoricoCompras();

            fhc.setVisible(true);

        } catch (SQLException | ParseException ex) {

            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);

        }

    }//GEN-LAST:event_jMenuItemHistComrpasActionPerformed


    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed

        Form_Categoria fc = new Form_Categoria();

        fc.setVisible(true);  // Exibe o formulário de forma não modal

    }//GEN-LAST:event_jMenuItem9ActionPerformed


    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed

        Form_Precif precif = null;

        precif = new Form_Precif();

        precif.setVisible(true);

        setVisible(false);        // TODO add your handling code here:

    }//GEN-LAST:event_jMenuItem3ActionPerformed


    private void jMenuItemProjtosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemProjtosActionPerformed

        Form_Projetos proj = null;

        proj = new Form_Projetos();

        proj.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemProjtosActionPerformed


    private void jMenuItemEtapasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemEtapasActionPerformed

        Form_Etapas etp = null;

        etp = new Form_Etapas();

        etp.setVisible(true);

        setVisible(false);

    }//GEN-LAST:event_jMenuItemEtapasActionPerformed


    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed

        Tela_Consulta_Projetos tcproj = null;

        tcproj = new Tela_Consulta_Projetos();

        tcproj.setVisible(true);

        setVisible(false);        // TODO add your handling code here:

    }//GEN-LAST:event_jMenuItem4ActionPerformed


    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed

        Tela_Consulta_Inflacao pesqInfla = null;

        pesqInfla = new Tela_Consulta_Inflacao();

        pesqInfla.setVisible(true);

        setVisible(false);        // TODO add your handling code here:

    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jMenuItemPainelDiarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed

    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenuItemPainelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPainelActionPerformed
        new Tela_PainelDiario(this).setVisible(true);
        setVisible(false);
    }//GEN-LAST:event_jMenuItemPainelActionPerformed

    private void jMenuItemAuditoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAuditoriaActionPerformed
        Tela_Consulta_Auditoria pesqB2 = null;

        try {

            pesqB2 = new Tela_Consulta_Auditoria();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        pesqB2.setVisible(true);

        setVisible(false);
    }//GEN-LAST:event_jMenuItemAuditoriaActionPerformed

    private void jMenuSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSairActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuSairActionPerformed

    private void jMenuItemInvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemInvActionPerformed
        Tela_Consulta_Invest pesqI = null;

        try {

            pesqI = new Tela_Consulta_Invest();

        } catch (SQLException ex) {

            Logger.getLogger(Tela_Principal.class
                    .getName()).log(Level.SEVERE, null, ex);

        }

        pesqI.setVisible(true);

        setVisible(false);
    }//GEN-LAST:event_jMenuItemInvActionPerformed

    // Método para carregar uma imagem de fundo na JLabel lblfondo
    public void carregarImagemFundo(String imagePath) {

        try {

            ImageIcon icon = new ImageIcon(imagePath);

            Image imagemOriginal = icon.getImage();

            // Obter o tamanho da JLabel (lblfondo)
            int larguraLabel = lblfondo.getWidth();

            int alturaLabel = lblfondo.getHeight();

            // Redimensionar a imagem apenas se a largura e a altura forem maiores que zero
            if (larguraLabel > 0 && alturaLabel > 0) {

                // Redimensionar a imagem para caber na JLabel (lblfondo)
                Image imagemRedimensionada = imagemOriginal.getScaledInstance(larguraLabel, alturaLabel, Image.SCALE_SMOOTH);

                ImageIcon imagemRedimensionadaIcon = new ImageIcon(imagemRedimensionada);

                // Definir a imagem redimensionada na JLabel (lblfondo)
                lblfondo.setIcon(imagemRedimensionadaIcon);

            }

        } catch (Exception e) {

            e.printStackTrace();

            JOptionPane.showMessageDialog(this, "Erro ao carregar imagem de fundo.");

        }

    }

    /**
     *
     * @param args the command line arguments
     *
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

            java.util.logging.Logger.getLogger(Tela_Principal.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {

            java.util.logging.Logger.getLogger(Tela_Principal.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {

            java.util.logging.Logger.getLogger(Tela_Principal.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {

            java.util.logging.Logger.getLogger(Tela_Principal.class
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
        SwingUtilities.invokeLater(() -> {

            try {

                new Tela_Principal();

            } catch (IOException ex) {

                Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);

            }

        });

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                try {

                    new Tela_Principal().setVisible(true);

                } catch (IOException ex) {

                    Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);

                }

            }

        });

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLData;
    private javax.swing.JLabel jLHora;
    private javax.swing.JLabel jLUsu;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenu jMenu8;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuCadastros;
    private javax.swing.JMenu jMenuCompras;
    private javax.swing.JMenu jMenuConsultas;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JMenuItem jMenuItemAbrirPDF;
    private javax.swing.JMenuItem jMenuItemAgenda;
    private javax.swing.JMenuItem jMenuItemAuditoria;
    private javax.swing.JMenuItem jMenuItemBackups;
    private javax.swing.JMenuItem jMenuItemImportarSQL;
    private javax.swing.JMenuItem jMenuItemBalance;
    private javax.swing.JMenuItem jMenuItemBlocoNotas;
    private javax.swing.JMenuItem jMenuItemCalendario;
    private javax.swing.JMenuItem jMenuItemConfTela;
    private javax.swing.JMenuItem jMenuItemConsClassificacao;
    private javax.swing.JMenuItem jMenuItemConsRecursos;
    private javax.swing.JMenuItem jMenuItemEtapas;
    private javax.swing.JMenuItem jMenuItemFavorecidos;
    private javax.swing.JMenuItem jMenuItemFlxD;
    private javax.swing.JMenuItem jMenuItemFormCompras;
    private javax.swing.JMenuItem jMenuItemHistComrpas;
    private javax.swing.JMenuItem jMenuItemInv;
    private javax.swing.JMenuItem jMenuItemMovLancamentos;
    private javax.swing.JMenuItem jMenuItemMovParcelamCart;
    private javax.swing.JMenuItem jMenuItemMovParcelamentos;
    private javax.swing.JMenuItem jMenuItemMovTransf;
    private javax.swing.JMenuItem jMenuItemPClientes;
    private javax.swing.JMenuItem jMenuItemPFornecedores;
    private javax.swing.JMenuItem jMenuItemPainel;
    private javax.swing.JMenuItem jMenuItemPlaCon;
    private javax.swing.JMenuItem jMenuItemPosDiaCompras;
    private javax.swing.JMenuItem jMenuItemPosVds;
    private javax.swing.JMenuItem jMenuItemProjtos;
    private javax.swing.JMenuItem jMenuItemRC;
    private javax.swing.JMenuItem jMenuItemRD;
    private javax.swing.JMenuItem jMenuItemRecursos;
    private javax.swing.JMenuItem jMenuItemSaldos;
    private javax.swing.JMenuItem jMenuItemUsu;
    private javax.swing.JMenu jMenuManutencao;
    private javax.swing.JMenu jMenuMovimentacao;
    private javax.swing.JMenu jMenuRelatorios;
    private javax.swing.JMenu jMenuSair;
    private javax.swing.JMenu jMenuUtilitarios;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblfondo;
    // End of variables declaration//GEN-END:variables
    class hora implements ActionListener {

        @Override

        public void actionPerformed(ActionEvent e) {

            Calendar now = Calendar.getInstance();

            jLHora.setText(String.format("%1$tH:%1$tM:%1$tS", now));

        }

    }

    private static String usuarioLogado;

    public static void setNome(String nome) {

        usuarioLogado = nome;

    }

    public void Deslogado() throws SQLException {

        try {

            int retries = 5; // número de tentativas

            while (retries > 0) {

                conexao.abrirConexao(); // Abre a conexão

                Connection con = conexao.getConexao(); // Obtém a conexão

                PreparedStatement stmt = null;

                String sql = "INSERT INTO logs (usul, data, action) VALUES (?, ?, ?)";

                stmt = con.prepareStatement(sql);

                stmt.setString(1, jLUsu.getText());

                // Usa o formato ISO 8601 para a data e hora
                Date dataSistema = new Date();

                SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");

                String dataFormatada = fmt.format(dataSistema);

                stmt.setString(2, dataFormatada);

                stmt.setString(3, "logoff");

                stmt.execute();

                break; // se a execução for bem-sucedida, sai do loop

            }

        } catch (SQLException e) {

            e.printStackTrace();

        } finally {

            conexao.fecharConexao(); // Fecha a conexão no final

        }

    }

} // Fim da Classe Principal

