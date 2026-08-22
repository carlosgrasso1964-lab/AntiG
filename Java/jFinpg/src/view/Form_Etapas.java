package view;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import dao.EtapaDAO;
import dao.ProjetoDAO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.Etapa;
import model.Projeto;

/**
 *
 * @author Usuário
 */
public class Form_Etapas extends javax.swing.JFrame {

    private final EtapaDAO etapaDAO;
    private final ProjetoDAO projetoDAO;
    private final Map<String, Integer> projetoIdMap = new HashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");

    public Form_Etapas() {
        initComponents();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Cadastro de Etapas");

        projetoDAO = new ProjetoDAO();
        etapaDAO = new EtapaDAO();

        configurarTabela(); // Nova configuração com colunas financeiras
        carregarProjetosNoCombo();
        carregarEtapas();
        configurarSelecaoTabela();
    }

    private void configurarTabela() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Projeto", "Etapa", "Prioridade", "Responsável",
                    "Início Prev.", "Fim Prev.", "Início Real.", "Fim Real.",
                    "Status", "R$ Previsto", "R$ Realizado", "% Executado", "Observações"}, 0
        );
        tabelaEtapas.setModel(model);

        // Alinhar colunas financeiras à direita
        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        tabelaEtapas.getColumnModel().getColumn(10).setCellRenderer(rightRenderer); // R$ Previsto
        tabelaEtapas.getColumnModel().getColumn(11).setCellRenderer(rightRenderer); // R$ Realizado
        tabelaEtapas.getColumnModel().getColumn(12).setCellRenderer(rightRenderer); // % Executado
    }

    private void configurarSelecaoTabela() {
        tabelaEtapas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int linha = tabelaEtapas.getSelectedRow();
                if (linha != -1) {
                    Object idObj = tabelaEtapas.getValueAt(linha, 0);
                    txtIDE.setText(idObj != null ? idObj.toString() : "");
                }
            }
        });
    }

    private void carregarProjetosNoCombo() {
        cbProj.removeAllItems();
        projetoIdMap.clear();
        try {
            List<Projeto> projetos = projetoDAO.listarTodos();
            for (Projeto p : projetos) {
                cbProj.addItem(p.getNome());
                projetoIdMap.put(p.getNome(), p.getId());
            }
            if (cbProj.getItemCount() > 0) {
                cbProj.setSelectedIndex(0);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar projetos: " + ex.getMessage());
        }
    }

    private void carregarEtapas() {
        DefaultTableModel model = (DefaultTableModel) tabelaEtapas.getModel();
        model.setRowCount(0);
        try {
            List<Etapa> etapas = etapaDAO.listarTodos();
            for (Etapa e : etapas) {
                String nomeProjeto = projetoDAO.getNomeProjetoPorId(e.getProjetoId());
                if (nomeProjeto == null) {
                    nomeProjeto = "Projeto excluído";
                }

                BigDecimal previsto = e.getValorPrevisto();
                BigDecimal realizado = e.getValorRealizado();
                String percentual = "0,0%";
                if (previsto != null && previsto.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal perc = realizado.divide(previsto, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(1, RoundingMode.HALF_UP);
                    percentual = perc.toString().replace(".", ",") + "%";
                }

                model.addRow(new Object[]{
                    e.getId(),
                    nomeProjeto,
                    e.getNome(),
                    e.getPrioridade(),
                    e.getResponsavel(),
                    formatarData(e.getDataInicioPrevista()),
                    formatarData(e.getDataFimPrevista()),
                    formatarData(e.getDataInicioRealizada()),
                    formatarData(e.getDataFimRealizada()),
                    e.getStatus(),
                    "R$ " + df.format(previsto),
                    "R$ " + df.format(realizado),
                    percentual,
                    e.getObs() != null ? e.getObs() : ""
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar etapas: " + ex.getMessage());
        }
    }

    private String formatarData(LocalDate data) {
        return data != null ? data.format(formatter) : "";
    }

    private BigDecimal parseMoeda(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            String limpo = texto.replace("R$", "").replace(".", "").replace(",", ".").trim();
            return new BigDecimal(limpo);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void limparCampos() {
        txtIDE.setText("");
        txtEtapa.setText("");
        txtResp.setText("");
        txtInicioEtapaPrev.setText("");
        txtFimEtapaPrev.setText("");
        txtInicioEtapaReal.setText("");
        txtFimEtapaReal.setText("");
        jTextAreaObs.setText("");
        txtVrPrev.setText("0,00");
        txtVrReal.setText("0,00");
        cbPrioridade.setSelectedIndex(0);
        cbStatusEtapa.setSelectedIndex(0);
        if (cbProj.getItemCount() > 0) {
            cbProj.setSelectedIndex(0);
        }
        tabelaEtapas.clearSelection();
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
        jLabel14 = new javax.swing.JLabel();
        txtVrPrev = new javax.swing.JTextField();
        txtVrReal = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
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
        addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                formPropertyChange(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("FORMULÁRIO - CADASTRO DE ETAPAS");

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

        cbStatusEtapa.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Não Iniciado", "Em Andamento", "Pendente", "Atrasada", "Cancelada", "Concluída" }));

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

        jLabel14.setText("R$ Realizado:");

        jLabel13.setText("R$ Previsto:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFimEtapaPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(430, 430, 430))
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
                        .addGap(18, 18, 18)
                        .addComponent(txtIDE, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(723, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(cbProj, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(431, 431, 431))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap(279, Short.MAX_VALUE)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtResp, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(44, 44, 44)
                                .addComponent(jLabel13)
                                .addGap(18, 18, 18)
                                .addComponent(txtVrPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel14))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel8))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addGap(18, 18, 18)
                                        .addComponent(cbStatusEtapa, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                                                .addComponent(txtInicioEtapaPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtFimEtapaReal, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtVrReal, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(55, 55, 55)))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEtapa, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
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
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabelaEtapas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "PROJETO", "ETAPA", "PRIORIDADE", "RESPONSÁVEL", "INÍCIO PREV", "FIM PREV", "INICIO REAL.", "FIM REAL.", "STATUS", "R$ Previsto", "R$ Realizado", "% Executado", "OBS."
            }
        ));
        jScrollPane1.setViewportView(tabelaEtapas);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 904, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE))
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
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jButtonCriar)
                .addComponent(jButtonLer)
                .addComponent(jButtonAtualizar)
                .addComponent(jButtonExcluir)
                .addComponent(jButtonListar)
                .addComponent(jButtonFechar))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCriarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCriarActionPerformed
        try {
            Etapa etapa = new Etapa();

            String nomeProjetoSelecionado = (String) cbProj.getSelectedItem();
            if (nomeProjetoSelecionado == null || !projetoIdMap.containsKey(nomeProjetoSelecionado)) {
                JOptionPane.showMessageDialog(this, "Selecione um projeto válido!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            etapa.setProjetoId(projetoIdMap.get(nomeProjetoSelecionado));

            if (txtEtapa.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "O nome da etapa é obrigatório!", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            etapa.setNome(txtEtapa.getText().trim());
            etapa.setPrioridade(cbPrioridade.getSelectedItem().toString());
            etapa.setResponsavel(txtResp.getText().trim());
            etapa.setStatus(cbStatusEtapa.getSelectedItem().toString());
            etapa.setObs(jTextAreaObs.getText());

            // Datas
            try {
                etapa.setDataInicioPrevista(parseDateOrNull(txtInicioEtapaPrev.getText().trim()));
                etapa.setDataFimPrevista(parseDateOrNull(txtFimEtapaPrev.getText().trim()));
                etapa.setDataInicioRealizada(parseDateOrNull(txtInicioEtapaReal.getText().trim()));
                etapa.setDataFimRealizada(parseDateOrNull(txtFimEtapaReal.getText().trim()));
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Data inválida! Use dd/MM/yyyy", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Valores financeiros
            BigDecimal vrPrev = parseMoeda(txtVrPrev.getText());
            BigDecimal vrReal = parseMoeda(txtVrReal.getText());
            if (vrPrev == null || vrReal == null) {
                JOptionPane.showMessageDialog(this, "Valores financeiros inválidos! Use formato como 1500,00", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            etapa.setValorPrevisto(vrPrev);
            etapa.setValorRealizado(vrReal);

            etapaDAO.criar(etapa);
            JOptionPane.showMessageDialog(this, "Etapa criada com sucesso!");
            limparCampos();
            carregarEtapas();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao criar etapa: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonCriarActionPerformed

    private LocalDate parseDateOrNull(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(texto.trim(), formatter);
    }


    private void jButtonFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFecharActionPerformed
//        try {
//            new TelaPrincipal().setVisible(true);
//            dispose();
//        } catch (IOException ex) {
//            Logger.getLogger(Form_Etapas.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        dispose();
//        dispose();
        try {
            new TelaPrincipal().setVisible(true);
            dispose();
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
        try {
            if (txtIDE.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selecione uma etapa na tabela primeiro.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = Integer.parseInt(txtIDE.getText().trim());
            Etapa e = etapaDAO.ler(id);
            if (e != null) {
                String nomeProjeto = projetoDAO.getNomeProjetoPorId(e.getProjetoId());
                if (nomeProjeto != null && projetoIdMap.containsKey(nomeProjeto)) {
                    cbProj.setSelectedItem(nomeProjeto);
                } else {
                    cbProj.setSelectedIndex(-1);
                }

                txtEtapa.setText(e.getNome());
                cbPrioridade.setSelectedItem(e.getPrioridade());
                txtResp.setText(e.getResponsavel());
                txtInicioEtapaPrev.setText(formatarData(e.getDataInicioPrevista()));
                txtFimEtapaPrev.setText(formatarData(e.getDataFimPrevista()));
                txtInicioEtapaReal.setText(formatarData(e.getDataInicioRealizada()));
                txtFimEtapaReal.setText(formatarData(e.getDataFimRealizada()));
                cbStatusEtapa.setSelectedItem(e.getStatus());
                jTextAreaObs.setText(e.getObs() != null ? e.getObs() : "");

                // Valores financeiros
                txtVrPrev.setText(df.format(e.getValorPrevisto()));
                txtVrReal.setText(df.format(e.getValorRealizado()));
            } else {
                JOptionPane.showMessageDialog(this, "Etapa não encontrada!");
                limparCampos();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao ler etapa: " + ex.getMessage());
        }
    }//GEN-LAST:event_jButtonLerActionPerformed

    private void jButtonAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAtualizarActionPerformed
        try {
            if (txtIDE.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selecione uma etapa para atualizar.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = Integer.parseInt(txtIDE.getText().trim());
            Etapa etapa = etapaDAO.ler(id);
            if (etapa == null) {
                JOptionPane.showMessageDialog(this, "Etapa não encontrada!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String nomeProjetoSelecionado = (String) cbProj.getSelectedItem();
            if (nomeProjetoSelecionado == null || !projetoIdMap.containsKey(nomeProjetoSelecionado)) {
                JOptionPane.showMessageDialog(this, "Selecione um projeto válido!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            etapa.setProjetoId(projetoIdMap.get(nomeProjetoSelecionado));

            if (txtEtapa.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "O nome da etapa é obrigatório!", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            etapa.setNome(txtEtapa.getText().trim());
            etapa.setPrioridade(cbPrioridade.getSelectedItem().toString());
            etapa.setResponsavel(txtResp.getText().trim());
            etapa.setStatus(cbStatusEtapa.getSelectedItem().toString());
            etapa.setObs(jTextAreaObs.getText());

            try {
                etapa.setDataInicioPrevista(parseDateOrNull(txtInicioEtapaPrev.getText().trim()));
                etapa.setDataFimPrevista(parseDateOrNull(txtFimEtapaPrev.getText().trim()));
                etapa.setDataInicioRealizada(parseDateOrNull(txtInicioEtapaReal.getText().trim()));
                etapa.setDataFimRealizada(parseDateOrNull(txtFimEtapaReal.getText().trim()));
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Data inválida! Use dd/MM/yyyy", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal vrPrev = parseMoeda(txtVrPrev.getText());
            BigDecimal vrReal = parseMoeda(txtVrReal.getText());
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
            if (txtIDE.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selecione uma etapa para excluir.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = Integer.parseInt(txtIDE.getText().trim());
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja excluir esta etapa?", "Confirmação", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                etapaDAO.deletar(id);
                JOptionPane.showMessageDialog(this, "Etapa excluída com sucesso!");
                limparCampos();
                carregarEtapas();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID inválido.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
        }
    }//GEN-LAST:event_jButtonExcluirActionPerformed

    private void jButtonListarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonListarActionPerformed
        carregarEtapas();
    }//GEN-LAST:event_jButtonListarActionPerformed

    private void cbPrioridadeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbPrioridadeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbPrioridadeActionPerformed

    private void formPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_formPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_formPropertyChange

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new Form_Etapas().setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Erro crítico ao iniciar:\n" + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
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
