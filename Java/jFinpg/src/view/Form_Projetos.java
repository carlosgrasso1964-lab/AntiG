package view;

import dao.ProjetoDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.Projeto;
import java.math.BigDecimal;

public class Form_Projetos extends javax.swing.JFrame {

    private final ProjetoDAO projetoDAO;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");

    /**
     * Creates new form Form_Projetos
     */
    public Form_Projetos() {
        initComponents();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Cadastro de Projetos");

        projetoDAO = new ProjetoDAO();

        configurarTabela(); // Nova configuração com colunas financeiras
        carregarProjetos();
        configurarSelecaoTabela();

        // Desabilita edição dos campos financeiros (calculados automaticamente)
        txtValorPrevistoTotal.setEditable(false);
        txtValorRealizadoTotal.setEditable(false);
        txtValorPrevistoTotal.setBackground(new java.awt.Color(240, 240, 240));
        txtValorRealizadoTotal.setBackground(new java.awt.Color(240, 240, 240));
    }
    
    private void configurarTabela() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nome", "Início Prev.", "Fim Prev.", "Início Real.", "Fim Real.", "Status", "R$ Previsto Total", "R$ Realizado Total", "% Financeiro"}, 0
        );
        tabelaProjetos.setModel(model);

        // Alinhar colunas financeiras à direita
        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        tabelaProjetos.getColumnModel().getColumn(7).setCellRenderer(rightRenderer); // R$ Previsto
        tabelaProjetos.getColumnModel().getColumn(8).setCellRenderer(rightRenderer); // R$ Realizado
        tabelaProjetos.getColumnModel().getColumn(9).setCellRenderer(rightRenderer); // % Financeiro
    }

    private void configurarSelecaoTabela() {
        tabelaProjetos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int linha = tabelaProjetos.getSelectedRow();
                if (linha != -1) {
                    Object idObj = tabelaProjetos.getValueAt(linha, 0);
                    txtId.setText(idObj != null ? idObj.toString() : "");
                }
            }
        });
    }

    private void carregarProjetos() {
        DefaultTableModel model = (DefaultTableModel) tabelaProjetos.getModel();
        model.setRowCount(0);
        try {
            List<Projeto> projetos = projetoDAO.listarTodos();
            for (Projeto p : projetos) {
                BigDecimal previsto = p.getValorPrevistoTotal();
                BigDecimal realizado = p.getValorRealizadoTotal();
                String percentual = "0,0%";
                if (previsto != null && previsto.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal perc = realizado.divide(previsto, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(1, java.math.RoundingMode.HALF_UP);
                    percentual = perc.toString().replace(".", ",") + "%";
                }

                model.addRow(new Object[]{
                    p.getId(),
                    p.getNome(),
                    formatarData(p.getDataInicioPrevista()),
                    formatarData(p.getDataFimPrevista()),
                    formatarData(p.getDataInicioRealizada()),
                    formatarData(p.getDataFimRealizada()),
                    p.getStatus(),
                    "R$ " + df.format(previsto),
                    "R$ " + df.format(realizado),
                    percentual
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar projetos: " + ex.getMessage());
        }
    }

    private String formatarData(LocalDate data) {
        return data != null ? data.format(formatter) : "";
    }

    private void limparCampos() {
        txtId.setText("");
        txtNome.setText("");
        txtInicioP.setText("");
        txtF.setText("");
        txtInicioR.setText("");
        txtFR.setText("");
        cbStatus.setSelectedIndex(0);
        txtValorPrevistoTotal.setText("0,00");
        txtValorRealizadoTotal.setText("0,00");
        tabelaProjetos.clearSelection();
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
        txtId = new javax.swing.JTextField();
        txtInicioP = new javax.swing.JTextField();
        txtInicioR = new javax.swing.JTextField();
        txtNome = new javax.swing.JTextField();
        txtF = new javax.swing.JTextField();
        txtFR = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cbStatus = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtValorPrevistoTotal = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtValorRealizadoTotal = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaProjetos = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jButtonCriar = new javax.swing.JButton();
        jButtonLer = new javax.swing.JButton();
        jButtonAtualizar = new javax.swing.JButton();
        jButtonExcluir = new javax.swing.JButton();
        jButtonListar = new javax.swing.JButton();
        jButtonFechar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Formulario Projetos");

        txtInicioP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtInicioPActionPerformed(evt);
            }
        });

        jLabel6.setText("Status:");

        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Não Iniciado", "Em Andamento", "Atrasado", "Cancelado", "Concluído" }));

        jLabel2.setText("ID:");

        jLabel4.setText("Início Previsto:");

        jLabel7.setText("Início Realizado:");

        jLabel3.setText("Nome:");

        jLabel5.setText("Fim Previsto:");

        jLabel8.setText("Fim Realizado:");

        jLabel9.setText("Valor Previsto Total:");

        jLabel10.setText("Valor Realizado Total:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(txtInicioR, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                    .addComponent(txtInicioP, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(txtFR, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtF, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, 541, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtValorPrevistoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtValorRealizadoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {txtF, txtInicioP});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {txtFR, txtInicioR});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtInicioP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtInicioR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtFR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10)
                    .addComponent(txtValorPrevistoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtValorRealizadoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(8, Short.MAX_VALUE))
        );

        tabelaProjetos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Nome", "Inicio Prev", "Fim Prev", "Inicio Realiz", "Fim Realiz", "Status"
            }
        ));
        jScrollPane1.setViewportView(tabelaProjetos);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(53, 53, 53))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(93, 93, 93)
                .addComponent(jButtonCriar)
                .addGap(41, 41, 41)
                .addComponent(jButtonLer)
                .addGap(45, 45, 45)
                .addComponent(jButtonAtualizar)
                .addGap(29, 29, 29)
                .addComponent(jButtonExcluir)
                .addGap(27, 27, 27)
                .addComponent(jButtonListar)
                .addGap(18, 18, 18)
                .addComponent(jButtonFechar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonAtualizar, jButtonCriar, jButtonExcluir, jButtonFechar, jButtonLer, jButtonListar});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jButtonCriar)
                    .addComponent(jButtonLer)
                    .addComponent(jButtonAtualizar)
                    .addComponent(jButtonExcluir)
                    .addComponent(jButtonListar)
                    .addComponent(jButtonFechar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButtonAtualizar, jButtonCriar, jButtonExcluir, jButtonFechar, jButtonLer, jButtonListar});

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("FORMULÁRIO CADASTRO DE PROJETOS");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCriarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCriarActionPerformed
        try {
            Projeto projeto = new Projeto();
            if (txtNome.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "O nome do projeto é obrigatório!", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            projeto.setNome(txtNome.getText().trim());

            try {
                projeto.setDataInicioPrevista(parseDateOrNull(txtInicioP.getText().trim()));
                projeto.setDataFimPrevista(parseDateOrNull(txtF.getText().trim()));
                projeto.setDataInicioRealizada(parseDateOrNull(txtInicioR.getText().trim()));
                projeto.setDataFimRealizada(parseDateOrNull(txtFR.getText().trim()));
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Data inválida! Use dd/MM/yyyy", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            projeto.setStatus(cbStatus.getSelectedItem().toString());

            // Totais financeiros são zerados automaticamente (trigger cuida depois)
            projeto.setValorPrevistoTotal(BigDecimal.ZERO);
            projeto.setValorRealizadoTotal(BigDecimal.ZERO);

            projetoDAO.criar(projeto);
            JOptionPane.showMessageDialog(this, "Projeto criado com sucesso!");
            limparCampos();
            carregarProjetos();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao criar projeto: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
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
//            TelaPrincipal exibir;
//            exibir = new TelaPrincipal();
//            exibir.setVisible(true);
//            setVisible(false);
//        } catch (IOException ex) {
//            Logger.getLogger(Form_Projetos.class.getName()).log(Level.SEVERE, null, ex);
//        }
        try {
            new TelaPrincipal().setVisible(true);
            dispose();
        } catch (IOException ex) {
            Logger.getLogger(Form_Projetos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonFecharActionPerformed

    private void jButtonLerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLerActionPerformed
        try {
            if (txtId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selecione um projeto na tabela primeiro.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = Integer.parseInt(txtId.getText().trim());
            Projeto p = projetoDAO.ler(id);
            if (p != null) {
                txtNome.setText(p.getNome());
                txtInicioP.setText(formatarData(p.getDataInicioPrevista()));
                txtF.setText(formatarData(p.getDataFimPrevista()));
                txtInicioR.setText(formatarData(p.getDataInicioRealizada()));
                txtFR.setText(formatarData(p.getDataFimRealizada()));
                cbStatus.setSelectedItem(p.getStatus());

                // Exibe totais financeiros (somente leitura)
                txtValorPrevistoTotal.setText(df.format(p.getValorPrevistoTotal()));
                txtValorRealizadoTotal.setText(df.format(p.getValorRealizadoTotal()));
            } else {
                JOptionPane.showMessageDialog(this, "Projeto não encontrado!");
                limparCampos();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID inválido.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao ler projeto: " + ex.getMessage());
        }
    }//GEN-LAST:event_jButtonLerActionPerformed

    private void jButtonAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAtualizarActionPerformed
        try {
            if (txtId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selecione um projeto para atualizar.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = Integer.parseInt(txtId.getText().trim());
            Projeto projeto = projetoDAO.ler(id);
            if (projeto == null) {
                JOptionPane.showMessageDialog(this, "Projeto não encontrado!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (txtNome.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "O nome do projeto é obrigatório!", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            projeto.setNome(txtNome.getText().trim());

            try {
                projeto.setDataInicioPrevista(parseDateOrNull(txtInicioP.getText().trim()));
                projeto.setDataFimPrevista(parseDateOrNull(txtF.getText().trim()));
                projeto.setDataInicioRealizada(parseDateOrNull(txtInicioR.getText().trim()));
                projeto.setDataFimRealizada(parseDateOrNull(txtFR.getText().trim()));
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Data inválida! Use dd/MM/yyyy", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            projeto.setStatus(cbStatus.getSelectedItem().toString());

            // Totais financeiros NÃO são editados aqui ? o trigger atualiza automaticamente
            projetoDAO.atualizar(projeto);
            JOptionPane.showMessageDialog(this, "Projeto atualizado com sucesso!");
            carregarProjetos();

        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + ex.getMessage());
        }
    }//GEN-LAST:event_jButtonAtualizarActionPerformed

    private void jButtonExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExcluirActionPerformed
        try {
            if (txtId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selecione um projeto para excluir.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = Integer.parseInt(txtId.getText().trim());
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja excluir o projeto ID " + id + "?\nTodas as etapas serão excluídas também!",
                    "Confirmação", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                projetoDAO.deletar(id);
                JOptionPane.showMessageDialog(this, "Projeto excluído com sucesso!");
                limparCampos();
                carregarProjetos();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID inválido.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
        }
    }//GEN-LAST:event_jButtonExcluirActionPerformed

    private void jButtonListarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonListarActionPerformed
        carregarProjetos();
    }//GEN-LAST:event_jButtonListarActionPerformed

    private void txtInicioPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtInicioPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtInicioPActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new Form_Projetos().setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Erro ao iniciar o formulário:\n" + ex.getMessage(),
                        "Erro Crítico", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cbStatus;
    private javax.swing.JButton jButtonAtualizar;
    private javax.swing.JButton jButtonCriar;
    private javax.swing.JButton jButtonExcluir;
    private javax.swing.JButton jButtonFechar;
    private javax.swing.JButton jButtonLer;
    private javax.swing.JButton jButtonListar;
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
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabelaProjetos;
    private javax.swing.JTextField txtF;
    private javax.swing.JTextField txtFR;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtInicioP;
    private javax.swing.JTextField txtInicioR;
    private javax.swing.JTextField txtNome;
    private javax.swing.JTextField txtValorPrevistoTotal;
    private javax.swing.JTextField txtValorRealizadoTotal;
    // End of variables declaration//GEN-END:variables

}
