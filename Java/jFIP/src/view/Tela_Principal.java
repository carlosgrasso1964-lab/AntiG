package view;

import AgCompr.Alart_Body;
import BlocoDeNotas.BlocNotas;
import Mcalendar.MainCalendApp;
import utilitarios.Conexao;
import utilitarios.ConfTelaPrincipal;
import classes.ImageDBHandler;
import java.awt.Dimension;
import java.awt.Image;
import relatorios.RelCliFor;
import relatorios.RelPlano;
import relatorios.RelRecursos;
import relatorios.RelUsers;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Tela_Principal extends javax.swing.JFrame {

    private VMov vMov;
    public static boolean isCloudAccess;
    private final String tempImagePath = System.getProperty("user.dir")
            + File.separator + "temp" + File.separator + "temp_image.png";
    private Object jdpPD;

    // Construtor padrão
    public Tela_Principal() throws IOException {
        initComponents();

        // Configura lblfondo como fundo (sem ícone fixo!)
        lblfondo.setHorizontalAlignment(JLabel.CENTER);
        lblfondo.setVerticalAlignment(JLabel.CENTER);
        lblfondo.setOpaque(false);  // permite ver os componentes por cima

        // Adiciona como fundo (índice 0)
        getContentPane().add(lblfondo, 0);

        // Recupera do banco e aplica
        ImageDBHandler.retrieveImageFromDatabase(tempImagePath);

        //atualizarImagemDeFundo();
        setSize(1360, 850); //Horizontal x Vertical
        setLocationRelativeTo(null);
        setVisible(true);

        // Listener de redimensionamento
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                atualizarImagemDeFundo();
            }
        });

        setVisible(true);

        // Aqui: agenda a primeira atualização DEPOIS da tela aparecer
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

        jdpPrincipal = new javax.swing.JDesktopPane();
        lblfondo = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLUsu = new javax.swing.JLabel();
        jLData = new javax.swing.JLabel();
        jLHora = new javax.swing.JLabel();
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
        jMenuItemConsClassificacao = new javax.swing.JMenuItem();
        jMenuItemSaldos = new javax.swing.JMenuItem();
        jMenuItemFlxD = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItemRD = new javax.swing.JMenuItem();
        jMenuItemRC = new javax.swing.JMenuItem();
        jMenuItemBalance = new javax.swing.JMenuItem();
        jMenuItemPainelDiario = new javax.swing.JMenuItem();
        jMenuItemAuditoria = new javax.swing.JMenuItem();
        jMenuRelatorios = new javax.swing.JMenu();
        jMenuItemRelPlano = new javax.swing.JMenuItem();
        jMenuItemRelRecurso = new javax.swing.JMenuItem();
        jMenuItemRelCliFor = new javax.swing.JMenuItem();
        jMenuItemRelUsuarios = new javax.swing.JMenuItem();
        jMenuManutencao = new javax.swing.JMenu();
        jMenuItemBackups = new javax.swing.JMenuItem();
        jMenuItemConfTela = new javax.swing.JMenuItem();
        Cadastros = new javax.swing.JMenu();
        jMenuPCadastros = new javax.swing.JMenu();
        jMenuItemProjetos = new javax.swing.JMenuItem();
        jMenuItemEtapas = new javax.swing.JMenuItem();
        jMenuPConsulta = new javax.swing.JMenu();
        jMenuItemProjEtapas = new javax.swing.JMenuItem();
        jMenuCVEstoque = new javax.swing.JMenu();
        jMenuCategorias = new javax.swing.JMenu();
        jMenuItemFCateg = new javax.swing.JMenuItem();
        jMenuProdutos = new javax.swing.JMenu();
        jMenuItemFProds = new javax.swing.JMenuItem();
        jMenuItemCEstq = new javax.swing.JMenuItem();
        jMenuCompras = new javax.swing.JMenu();
        jMenuItemFCompras = new javax.swing.JMenuItem();
        jMenuItemPDComp = new javax.swing.JMenuItem();
        jMenuItemHComp = new javax.swing.JMenuItem();
        jMenuVendas = new javax.swing.JMenu();
        jMenuItemPDV = new javax.swing.JMenuItem();
        jMenuItemPDVendas = new javax.swing.JMenuItem();
        jMenuItemHVendas = new javax.swing.JMenuItem();
        jMenuItemPrecif = new javax.swing.JMenuItem();
        jMenuUtilitarios = new javax.swing.JMenu();
        jMenuItemAbrirPDF = new javax.swing.JMenuItem();
        jMenuItemCalendario = new javax.swing.JMenuItem();
        jMenuItemAgenda = new javax.swing.JMenuItem();
        jMenuItemBlocoNotas = new javax.swing.JMenuItem();
        jMenuSair = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jdpPrincipal.setBackground(new java.awt.Color(255, 255, 255));

        lblfondo.setBackground(new java.awt.Color(255, 255, 255));
        lblfondo.setPreferredSize(new java.awt.Dimension(900, 586));

        jLUsu.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLUsu.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLUsu.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        jLData.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLData.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLData.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        jLHora.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLHora.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLHora.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLUsu, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLData, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(jLHora, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jLData, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
            .addComponent(jLUsu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLHora, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jdpPrincipal.setLayer(lblfondo, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jdpPrincipal.setLayer(jPanel1, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jdpPrincipalLayout = new javax.swing.GroupLayout(jdpPrincipal);
        jdpPrincipal.setLayout(jdpPrincipalLayout);
        jdpPrincipalLayout.setHorizontalGroup(
            jdpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jdpPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jdpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblfondo, javax.swing.GroupLayout.DEFAULT_SIZE, 1359, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jdpPrincipalLayout.setVerticalGroup(
            jdpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jdpPrincipalLayout.createSequentialGroup()
                .addComponent(lblfondo, javax.swing.GroupLayout.DEFAULT_SIZE, 738, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jMenuBar1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jMenuBar1.setPreferredSize(new java.awt.Dimension(559, 32));

        jMenuCadastros.setText("Cadastros");
        jMenuCadastros.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuCadastros.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuCadastrosActionPerformed(evt);
            }
        });

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
        jMenuItemMovParcelamCart.setText("Parcelam.Cartões");
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

        jMenuItem2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItem2.setText("Inflação Pessoal");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItem2);

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
        jMenuItemRC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRCActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItemRC);

        jMenuItemBalance.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemBalance.setText("Balanço");
        jMenuItemBalance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemBalanceActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItemBalance);

        jMenuItemPainelDiario.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemPainelDiario.setText("Painel Diário");
        jMenuItemPainelDiario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPainelDiarioActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItemPainelDiario);

        jMenuItemAuditoria.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemAuditoria.setText("Auditoria");
        jMenuItemAuditoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAuditoriaActionPerformed(evt);
            }
        });
        jMenuConsultas.add(jMenuItemAuditoria);

        jMenuBar1.add(jMenuConsultas);

        jMenuRelatorios.setText("Relatórios");
        jMenuRelatorios.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItemRelPlano.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemRelPlano.setText("Plano de Contas");
        jMenuItemRelPlano.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRelPlanoActionPerformed(evt);
            }
        });
        jMenuRelatorios.add(jMenuItemRelPlano);

        jMenuItemRelRecurso.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemRelRecurso.setText("Recursos");
        jMenuItemRelRecurso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRelRecursoActionPerformed(evt);
            }
        });
        jMenuRelatorios.add(jMenuItemRelRecurso);

        jMenuItemRelCliFor.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemRelCliFor.setText("Favorecidos");
        jMenuItemRelCliFor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRelCliForActionPerformed(evt);
            }
        });
        jMenuRelatorios.add(jMenuItemRelCliFor);

        jMenuItemRelUsuarios.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemRelUsuarios.setText("Usuários");
        jMenuItemRelUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRelUsuariosActionPerformed(evt);
            }
        });
        jMenuRelatorios.add(jMenuItemRelUsuarios);

        jMenuBar1.add(jMenuRelatorios);

        jMenuManutencao.setText("Manutenção");
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

        jMenuBar1.add(jMenuManutencao);

        Cadastros.setText("Projetos");
        Cadastros.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        Cadastros.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CadastrosActionPerformed(evt);
            }
        });

        jMenuPCadastros.setText("Cadastros");
        jMenuPCadastros.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItemProjetos.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemProjetos.setText("Formulário Projetos");
        jMenuItemProjetos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemProjetosActionPerformed(evt);
            }
        });
        jMenuPCadastros.add(jMenuItemProjetos);

        jMenuItemEtapas.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemEtapas.setText("Formulário Etapas");
        jMenuItemEtapas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemEtapasActionPerformed(evt);
            }
        });
        jMenuPCadastros.add(jMenuItemEtapas);

        Cadastros.add(jMenuPCadastros);

        jMenuPConsulta.setText("Consulta");
        jMenuPConsulta.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItemProjEtapas.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemProjEtapas.setText("Projetos/Etapas");
        jMenuItemProjEtapas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemProjEtapasActionPerformed(evt);
            }
        });
        jMenuPConsulta.add(jMenuItemProjEtapas);

        Cadastros.add(jMenuPConsulta);

        jMenuBar1.add(Cadastros);

        jMenuCVEstoque.setText("Mercantil");
        jMenuCVEstoque.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuCategorias.setText("Categorias");
        jMenuCategorias.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItemFCateg.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemFCateg.setText("Cadastro de Categorias");
        jMenuItemFCateg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFCategActionPerformed(evt);
            }
        });
        jMenuCategorias.add(jMenuItemFCateg);

        jMenuCVEstoque.add(jMenuCategorias);

        jMenuProdutos.setText("Produtos");
        jMenuProdutos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItemFProds.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemFProds.setText("Cadastro de Produtos");
        jMenuItemFProds.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFProdsActionPerformed(evt);
            }
        });
        jMenuProdutos.add(jMenuItemFProds);

        jMenuItemCEstq.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemCEstq.setText("Controle de Estoque");
        jMenuItemCEstq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCEstqActionPerformed(evt);
            }
        });
        jMenuProdutos.add(jMenuItemCEstq);

        jMenuCVEstoque.add(jMenuProdutos);

        jMenuCompras.setText("Compras");
        jMenuCompras.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItemFCompras.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemFCompras.setText("Formulário de Compras");
        jMenuItemFCompras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFComprasActionPerformed(evt);
            }
        });
        jMenuCompras.add(jMenuItemFCompras);

        jMenuItemPDComp.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemPDComp.setText("Posição do dia - Compras");
        jMenuItemPDComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPDCompActionPerformed(evt);
            }
        });
        jMenuCompras.add(jMenuItemPDComp);

        jMenuItemHComp.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemHComp.setText("Histórico de Compras");
        jMenuItemHComp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemHCompActionPerformed(evt);
            }
        });
        jMenuCompras.add(jMenuItemHComp);

        jMenuCVEstoque.add(jMenuCompras);

        jMenuVendas.setText("Vendas");
        jMenuVendas.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jMenuItemPDV.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemPDV.setText("PDV");
        jMenuItemPDV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPDVActionPerformed(evt);
            }
        });
        jMenuVendas.add(jMenuItemPDV);

        jMenuItemPDVendas.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemPDVendas.setText("Posição do dia - Vendas");
        jMenuItemPDVendas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPDVendasActionPerformed(evt);
            }
        });
        jMenuVendas.add(jMenuItemPDVendas);

        jMenuItemHVendas.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItemHVendas.setText("Histórico de Vendas");
        jMenuItemHVendas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemHVendasActionPerformed(evt);
            }
        });
        jMenuVendas.add(jMenuItemHVendas);

        jMenuCVEstoque.add(jMenuVendas);

        jMenuItemPrecif.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuItemPrecif.setText("Precificação");
        jMenuItemPrecif.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPrecifActionPerformed(evt);
            }
        });
        jMenuCVEstoque.add(jMenuItemPrecif);

        jMenuBar1.add(jMenuCVEstoque);

        jMenuUtilitarios.setText("Utilitários");
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
            .addGroup(layout.createSequentialGroup()
                .addComponent(jdpPrincipal)
                .addGap(1, 1, 1))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jdpPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setSize(new java.awt.Dimension(1388, 826));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        Object[] options = {"Sim", "Não"};
        int i = JOptionPane.showOptionDialog(null, "Deseja sair do Sistema? !!", "Encerrar Sistema", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (i == JOptionPane.YES_OPTION) { //opçao SIM selecionada !!}
            Deslogado();
            System.exit(0);
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItemUsuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUsuActionPerformed
        VCadUsu obj = new VCadUsu();
        jdpPrincipal.add(obj);
        obj.setVisible(true);
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
        VCadRecursos obj;
        try {
            obj = new VCadRecursos();
            jdpPrincipal.add(obj);
            obj.setVisible(true);
        } catch (SQLException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemRecursosActionPerformed

    private void jMenuItemPlaConActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPlaConActionPerformed
        VCadPlaCon obj = new VCadPlaCon();
        jdpPrincipal.add(obj);
        obj.setVisible(true);
    }//GEN-LAST:event_jMenuItemPlaConActionPerformed

    //-- *** AQUI CRIAMOS AS CHAMADAS DE TODAS AS TELAS *** --
    //========================================================
    private void abrirCadCliFor() {
        new VCadCliFor(this, true).setVisible(true);
    }

    private void abrirCategorias() {
        new Form_Categoria(this, true).setVisible(true);
    }

    private void abrirProdutos() throws SQLException, ClassNotFoundException {
        new Form_Produtos(this, true).setVisible(true);
    }

    private void abrirEstoque() {
        new Form_Estoque(this, true).setVisible(true);
    }

    private void abrirCompras() {
        new Form_Compra(this, true).setVisible(true);
    }

    private void abrirTotalDiaCompra() throws SQLException {
        new Form_TotalDoDiaCompras(this, true).setVisible(true);
    }

    private void abrirHistoricoCompras() {
        new Form_HistoricoCompras(this, true).setVisible(true);
    }

    private void abrirVendas() throws SQLException {
        new Form_Vendas(this, true).setVisible(true);
    }

    private void abrirTotalDoDia() {
        new Form_TotalDoDiaVendas(this, true).setVisible(true);
    }

    private void abrirHistoricoVendas() {
        new Form_HistoricoVendas(this, true).setVisible(true);
    }

    private void abrirPrecif() {
        new Form_Precif(this, true).setVisible(true);
    }


    private void jMenuItemFavorecidosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFavorecidosActionPerformed
        new VCadCliFor(this, true).setVisible(true);
    }//GEN-LAST:event_jMenuItemFavorecidosActionPerformed

    private void jMenuItemRelPlanoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRelPlanoActionPerformed
        RelPlano gen = new RelPlano();
        JOptionPane.showMessageDialog(null, "Relatório Plano de Contas gerado com sucesso!");
        dispose();
        try {
            new Tela_Principal().setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemRelPlanoActionPerformed

    private void jMenuItemRelRecursoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRelRecursoActionPerformed
        RelRecursos recur = new RelRecursos();
        JOptionPane.showMessageDialog(null, "Relatório de Recursos gerado com sucesso!");
        dispose();
        try {
            new Tela_Principal().setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemRelRecursoActionPerformed

    private void jMenuItemRelCliForActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRelCliForActionPerformed
        try {
            RelCliFor cf = new RelCliFor();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        JOptionPane.showMessageDialog(null, "Relatório de Favorecidos gerado com sucesso!");
        dispose();
        try {
            new Tela_Principal().setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemRelCliForActionPerformed

    private void jMenuItemRelUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRelUsuariosActionPerformed
        RelUsers usu = new RelUsers();
        JOptionPane.showMessageDialog(null, "Relatório de Favorecidos gerado com sucesso!");
        dispose();
        try {
            new Tela_Principal().setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemRelUsuariosActionPerformed

    private void jMenuItemMovLancamentosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMovLancamentosActionPerformed
        try {
            if (vMov == null || vMov.isClosed()) {
                vMov = new VMov();
                jdpPrincipal.add(vMov);
            }
            vMov.setVisible(true);
            vMov.toFront(); // Traz a tela VMov para frente
        } catch (SQLException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemMovLancamentosActionPerformed

    private void jMenuMovimentacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuMovimentacaoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuMovimentacaoActionPerformed

    private void jMenuItemAbrirPDFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAbrirPDFActionPerformed
        TelaJFileChosse exibir = null;
        exibir = new TelaJFileChosse();
        exibir.setVisible(true);
        setVisible(false);
    }//GEN-LAST:event_jMenuItemAbrirPDFActionPerformed

    private void jMenuItemMovTransfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMovTransfActionPerformed
        VTransf obj;
        try {
            obj = new VTransf();
            jdpPrincipal.add(obj);
            obj.setVisible(true);
        } catch (SQLException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemMovTransfActionPerformed

    private void jMenuItemMovParcelamentosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMovParcelamentosActionPerformed
        VParcel obj;
        try {
            obj = new VParcel();
            jdpPrincipal.add(obj);
            obj.setVisible(true);
        } catch (SQLException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemMovParcelamentosActionPerformed

    private void jMenuItemConsRecursosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemConsRecursosActionPerformed
        VConsRecursos obj;
        try {
            obj = new VConsRecursos();
            jdpPrincipal.add(obj);
            obj.setVisible(true);
        } catch (SQLException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemConsRecursosActionPerformed

    private void menuItemComprasActionPerformed(java.awt.event.ActionEvent evt) {
        Form_Compra tela = new Form_Compra(this, true);
        tela.setVisible(true);
    }

    private void jMenuItemPFornecedoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPFornecedoresActionPerformed
        VConsFornecedores obj;
        try {
            obj = new VConsFornecedores();
            jdpPrincipal.add(obj);
            obj.setVisible(true);
        } catch (SQLException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemPFornecedoresActionPerformed

    private void jMenuItemConsClassificacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemConsClassificacaoActionPerformed
        VConsClass obj;
        try {
            obj = new VConsClass();
            jdpPrincipal.add(obj);
            obj.setVisible(true);
        } catch (SQLException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemConsClassificacaoActionPerformed

    private void jMenuItemBackupsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemBackupsActionPerformed
        VBackup obj;
        obj = new VBackup();
        jdpPrincipal.add(obj);
        obj.setVisible(true);
    }//GEN-LAST:event_jMenuItemBackupsActionPerformed

    private void jMenuItemPClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPClientesActionPerformed
        VConsClientes obj;
        try {
            obj = new VConsClientes();
            jdpPrincipal.add(obj);
            obj.setVisible(true);
        } catch (SQLException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemPClientesActionPerformed

    private void jMenuItemSaldosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaldosActionPerformed
        VConsSaldos obj;
        obj = new VConsSaldos();
        jdpPrincipal.add(obj);
        obj.setVisible(true);
    }//GEN-LAST:event_jMenuItemSaldosActionPerformed

    private void jMenuItemFlxDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFlxDActionPerformed
        VConsFlxCxDiario obj;
        obj = new VConsFlxCxDiario();
        jdpPrincipal.add(obj);
        obj.setVisible(true);
    }//GEN-LAST:event_jMenuItemFlxDActionPerformed

    private void jMenuItemRDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRDActionPerformed
        VConsRD obj;
        obj = new VConsRD();
        jdpPrincipal.add(obj);
        obj.setVisible(true);
    }//GEN-LAST:event_jMenuItemRDActionPerformed

    private void jMenuItemRCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRCActionPerformed
        VConsRefCruz obj;
        obj = new VConsRefCruz();
        jdpPrincipal.add(obj);
        obj.setVisible(true);
    }//GEN-LAST:event_jMenuItemRCActionPerformed

    private void jMenuItemCalendarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCalendarioActionPerformed
        MainCalendApp exibir = null;
        exibir = new MainCalendApp();
        exibir.setVisible(true);
        setVisible(false);
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

    private void menuItemTotalComprasDiaActionPerformed(java.awt.event.ActionEvent evt) throws SQLException {
        Form_TotalDoDiaCompras tela = new Form_TotalDoDiaCompras(this, true);
        tela.setVisible(true);
    }

    private void jMenuItemBalanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemBalanceActionPerformed
        VConsBalanc obj;
        obj = new VConsBalanc();
        jdpPrincipal.add(obj);
        obj.setVisible(true);
    }//GEN-LAST:event_jMenuItemBalanceActionPerformed

    private void jMenuItemMovParcelamCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMovParcelamCartActionPerformed
        VParcelCart obj;
        try {
            obj = new VParcelCart();
            jdpPrincipal.add(obj);
            obj.setVisible(true);
        } catch (SQLException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemMovParcelamCartActionPerformed

    private void jMenuItemConfTelaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemConfTelaActionPerformed

    // Evita abrir múltiplas instâncias
    for (JInternalFrame frame : jdpPrincipal.getAllFrames()) {
            if (frame instanceof ConfTelaPrincipal) {
                try {
                    frame.setSelected(true);
                    frame.moveToFront();
                    frame.setVisible(true);
                } catch (Exception ex) {
                }
                return;
            }
        }

        ConfTelaPrincipal conf = new ConfTelaPrincipal(this);
        jdpPrincipal.add(conf);

        // Centraliza
        Dimension desktopSize = jdpPrincipal.getSize();
        Dimension frameSize = conf.getSize();
        conf.setLocation(
                (desktopSize.width - frameSize.width) / 2,
                (desktopSize.height - frameSize.height) / 2
        );

        try {
            conf.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
            // ignora
        }

        conf.setVisible(true);
    }//GEN-LAST:event_jMenuItemConfTelaActionPerformed

    private void jMenuCadastrosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuCadastrosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuCadastrosActionPerformed

    private void jMenuItemEtapasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemEtapasActionPerformed
        Form_Etapas form = null;
        try {
            form = new Form_Etapas();
        } catch (SQLException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!isFormAlreadyOpen(form)) {
            jdpPrincipal.add(form);
            form.setVisible(true);
            centralizarInternalFrame(form);
            try {
                form.setSelected(true);
            } catch (Exception e) {
            }
        }

    }//GEN-LAST:event_jMenuItemEtapasActionPerformed

    private void jMenuItemProjEtapasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemProjEtapasActionPerformed
        try {
            Tela_Consulta_Projetos tela = new Tela_Consulta_Projetos();
            if (!isFormAlreadyOpen(tela)) {
                jdpPrincipal.add(tela);
                tela.setVisible(true);
                centralizarInternalFrame(tela);
                try {
                    tela.setSelected(true);
                } catch (PropertyVetoException ex) {
                    Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar consulta: " + ex.getMessage(),
                    "Erro no Banco", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemProjEtapasActionPerformed

    private void jMenuItemProjetosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemProjetosActionPerformed
        Form_Projetos tela = null;
        try {
            tela = new Form_Projetos();
        } catch (SQLException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!isFormAlreadyOpen(tela)) {
            jdpPrincipal.add(tela);           // ? agora aceita!
            tela.setVisible(true);
            centralizarInternalFrame(tela);
            try {
                tela.setSelected(true);
            } catch (Exception e) {
            }
        }
    }//GEN-LAST:event_jMenuItemProjetosActionPerformed

    private void CadastrosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CadastrosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_CadastrosActionPerformed

    private void jMenuItemPDCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPDCompActionPerformed
        try {
            abrirTotalDiaCompra();
        } catch (SQLException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemPDCompActionPerformed

    private void jMenuItemFComprasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFComprasActionPerformed
        abrirCompras();
    }//GEN-LAST:event_jMenuItemFComprasActionPerformed

    private void jMenuItemHCompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHCompActionPerformed
        abrirHistoricoCompras();
    }//GEN-LAST:event_jMenuItemHCompActionPerformed

    private void jMenuItemPDVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPDVActionPerformed
        try {
            abrirVendas();
        } catch (SQLException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemPDVActionPerformed

    private void jMenuItemPDVendasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPDVendasActionPerformed
        new Form_TotalDoDiaVendas(this, true).setVisible(true);
    }//GEN-LAST:event_jMenuItemPDVendasActionPerformed

    private void jMenuItemHVendasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHVendasActionPerformed
        new Form_HistoricoVendas(this, true).setVisible(true);
    }//GEN-LAST:event_jMenuItemHVendasActionPerformed

    private void jMenuItemFCategActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFCategActionPerformed
        abrirCategorias();        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItemFCategActionPerformed

    private void jMenuItemFProdsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFProdsActionPerformed
        try {
            abrirProdutos();
        } catch (SQLException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemFProdsActionPerformed

    private void jMenuItemCEstqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCEstqActionPerformed
        abrirEstoque();
    }//GEN-LAST:event_jMenuItemCEstqActionPerformed

    private void jMenuItemPrecifActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPrecifActionPerformed
        abrirPrecif();
    }//GEN-LAST:event_jMenuItemPrecifActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        Tela_Consulta_Inflacao obj;
        obj = new Tela_Consulta_Inflacao();
        jdpPrincipal.add(obj);
        obj.setVisible(true);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItemPainelDiarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPainelDiarioActionPerformed

        Tela_PainelDiario obj;
        obj = new Tela_PainelDiario();
        obj.setVisible(true);
    }//GEN-LAST:event_jMenuItemPainelDiarioActionPerformed

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

    // Método para evitar abrir o mesmo formulário várias vezes
    private boolean isFormAlreadyOpen(javax.swing.JInternalFrame frame) {
        javax.swing.JInternalFrame[] ativos = jdpPrincipal.getAllFrames();
        for (javax.swing.JInternalFrame ativo : ativos) {
            if (ativo.getClass() == frame.getClass()) {
                try {
                    ativo.setSelected(true);
                    ativo.moveToFront();
                } catch (java.beans.PropertyVetoException e) {
                }
                return true;
            }
        }
        return false;
    }

// Método para centralizar o JInternalFrame no JDesktopPane
    private void centralizarInternalFrame(javax.swing.JInternalFrame frame) {
        java.awt.Dimension desktopSize = jdpPrincipal.getSize();
        java.awt.Dimension frameSize = frame.getSize();

        frame.setLocation(
                (desktopSize.width - frameSize.width) / 2,
                (desktopSize.height - frameSize.height) / 2
        );
    }

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
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tela_Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        SwingUtilities.invokeLater(() -> {
            try {
                // Iniciar a conexão com o banco de dados
                Connection conexao = Conexao.faz_conexao();

                // Verificar se a conexão foi bem-sucedida
                if (conexao != null) {
                    // Debug: Imprimir o valor do tipo de conexão para verificar se está correto
                    System.out.println("O tipo de conexão é: " + Conexao.tipoDeConexao);

                    // Criar e exibir a Tela Principal com base no tipo de conexão
                    new Tela_Principal().setVisible(true);
                } else {
                    System.out.println("Erro ao conectar com o banco de dados.");
                }
            } catch (IOException | SQLException ex) {
                Logger.getLogger(Tela_Principal.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu Cadastros;
    private javax.swing.JLabel jLData;
    private javax.swing.JLabel jLHora;
    private javax.swing.JLabel jLUsu;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuCVEstoque;
    private javax.swing.JMenu jMenuCadastros;
    private javax.swing.JMenu jMenuCategorias;
    private javax.swing.JMenu jMenuCompras;
    private javax.swing.JMenu jMenuConsultas;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItemAbrirPDF;
    private javax.swing.JMenuItem jMenuItemAgenda;
    private javax.swing.JMenuItem jMenuItemAuditoria;
    private javax.swing.JMenuItem jMenuItemBackups;
    private javax.swing.JMenuItem jMenuItemBalance;
    private javax.swing.JMenuItem jMenuItemBlocoNotas;
    private javax.swing.JMenuItem jMenuItemCEstq;
    private javax.swing.JMenuItem jMenuItemCalendario;
    private javax.swing.JMenuItem jMenuItemConfTela;
    private javax.swing.JMenuItem jMenuItemConsClassificacao;
    private javax.swing.JMenuItem jMenuItemConsRecursos;
    private javax.swing.JMenuItem jMenuItemEtapas;
    private javax.swing.JMenuItem jMenuItemFCateg;
    private javax.swing.JMenuItem jMenuItemFCompras;
    private javax.swing.JMenuItem jMenuItemFProds;
    private javax.swing.JMenuItem jMenuItemFavorecidos;
    private javax.swing.JMenuItem jMenuItemFlxD;
    private javax.swing.JMenuItem jMenuItemHComp;
    private javax.swing.JMenuItem jMenuItemHVendas;
    private javax.swing.JMenuItem jMenuItemMovLancamentos;
    private javax.swing.JMenuItem jMenuItemMovParcelamCart;
    private javax.swing.JMenuItem jMenuItemMovParcelamentos;
    private javax.swing.JMenuItem jMenuItemMovTransf;
    private javax.swing.JMenuItem jMenuItemPClientes;
    private javax.swing.JMenuItem jMenuItemPDComp;
    private javax.swing.JMenuItem jMenuItemPDV;
    private javax.swing.JMenuItem jMenuItemPDVendas;
    private javax.swing.JMenuItem jMenuItemPFornecedores;
    private javax.swing.JMenuItem jMenuItemPainelDiario;
    private javax.swing.JMenuItem jMenuItemPlaCon;
    private javax.swing.JMenuItem jMenuItemPrecif;
    private javax.swing.JMenuItem jMenuItemProjEtapas;
    private javax.swing.JMenuItem jMenuItemProjetos;
    private javax.swing.JMenuItem jMenuItemRC;
    private javax.swing.JMenuItem jMenuItemRD;
    private javax.swing.JMenuItem jMenuItemRecursos;
    private javax.swing.JMenuItem jMenuItemRelCliFor;
    private javax.swing.JMenuItem jMenuItemRelPlano;
    private javax.swing.JMenuItem jMenuItemRelRecurso;
    private javax.swing.JMenuItem jMenuItemRelUsuarios;
    private javax.swing.JMenuItem jMenuItemSaldos;
    private javax.swing.JMenuItem jMenuItemUsu;
    private javax.swing.JMenu jMenuManutencao;
    private javax.swing.JMenu jMenuMovimentacao;
    private javax.swing.JMenu jMenuPCadastros;
    private javax.swing.JMenu jMenuPConsulta;
    private javax.swing.JMenu jMenuProdutos;
    private javax.swing.JMenu jMenuRelatorios;
    private javax.swing.JMenu jMenuSair;
    private javax.swing.JMenu jMenuUtilitarios;
    private javax.swing.JMenu jMenuVendas;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JDesktopPane jdpPrincipal;
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

    public void Deslogado() {
        try {
            Connection con = Conexao.faz_conexao();
            String sql = "INSERT INTO logs (usul,data,action) VALUES (?,?,?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, jLUsu.getText());
            Date dataSistema = new Date();
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy - HH:mm.ss");
            stmt.setString(2, fmt.format(dataSistema));
            stmt.setString(3, "logoff");
            stmt.execute();
            stmt.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} // Fim da Classe Principal
