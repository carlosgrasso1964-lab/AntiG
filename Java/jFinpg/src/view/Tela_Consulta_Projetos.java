package view;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import modelos.ProjetoEtapaHierarquico;
import modelos.ProjetosEtapasHierarquicoTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.ui.RectangleEdge;
import java.math.BigDecimal;
import org.jfree.data.time.TimePeriod;

public class Tela_Consulta_Projetos extends javax.swing.JFrame {

    //private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    /**
     * Creates new form Tela_Cons_Projetos
     */
    public Tela_Consulta_Projetos() throws SQLException {
        initComponents();
        setLocationRelativeTo(null);
        setTitle("Consulta de Projetos e Etapas");
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        // Carrega todos os projetos ao abrir
        tabelaProj.setModel(new ProjetosEtapasHierarquicoTableModel(""));
    }

    // ===================== GRÁFICO GANTT CORRIGIDO =====================
    private void exibirGrafico() {
        TaskSeriesCollection dataset = createDatasetFromTable(tabelaProj.getModel());

        JFreeChart chart = ChartFactory.createGanttChart(
                "Gráfico de Gantt - Cronograma de Projetos e Etapas",
                "Projeto/Etapa",
                "Tempo",
                dataset,
                true, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        DateAxis rangeAxis = (DateAxis) plot.getRangeAxis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        rangeAxis.setDateFormatOverride(dateFormat);

        // Margens mínimas para evitar "invasão" visual
        rangeAxis.setLowerMargin(0.02);
        rangeAxis.setUpperMargin(0.05);

        // Cálculo do range com margem dinâmica
        Date minDate = null;
        Date maxDate = null;
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            TaskSeries series = dataset.getSeries(i);
            for (int j = 0; j < series.getTasks().size(); j++) {
                Task task = (Task) series.getTasks().get(j);
                TimePeriod period = task.getDuration();
                Date start = period.getStart();
                Date end = period.getEnd();

                if (minDate == null || start.before(minDate)) {
                    minDate = start;
                }
                if (maxDate == null || end.after(maxDate)) {
                    maxDate = end;
                }
            }
        }

        if (minDate != null && maxDate != null) {
            long duration = maxDate.getTime() - minDate.getTime();
            long margin = duration / 20; // 5% de margem dinâmica

            rangeAxis.setRange(
                    new Date(minDate.getTime() - margin),
                    new Date(maxDate.getTime() + margin)
            );
        }

        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setPosition(RectangleEdge.BOTTOM);
        }

        // Aparência
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setRangeGridlinePaint(java.awt.Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(java.awt.Color.LIGHT_GRAY);

        ChartFrame frame = new ChartFrame("Gráfico de Gantt", chart);
        frame.pack();
        frame.setVisible(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelTitulo = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanelCorpo = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaProj = new javax.swing.JTable();
        jPanelBotoes = new javax.swing.JPanel();
        btnConcluidos = new javax.swing.JButton();
        btnNaoConcluidos = new javax.swing.JButton();
        btnGant = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        txt_proj = new javax.swing.JTextField();
        btn_Abrir = new javax.swing.JButton();
        jButtonFechar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Projetos e Etapas");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Projetos e Etapas");

        javax.swing.GroupLayout jPanelTituloLayout = new javax.swing.GroupLayout(jPanelTitulo);
        jPanelTitulo.setLayout(jPanelTituloLayout);
        jPanelTituloLayout.setHorizontalGroup(
            jPanelTituloLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTituloLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelTituloLayout.setVerticalGroup(
            jPanelTituloLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTituloLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabelaProj.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "PROJETO", "ID ETAPA", "ETAPA", "PRIORIDADE", "INIC_PREV", "INIC_REAL", "FIM_PREV", "FIM_REAL", "STATUS", "OBS", "R$ Previsto", "R$ Realizado", "% Realizado"
            }
        ));
        jScrollPane1.setViewportView(tabelaProj);

        javax.swing.GroupLayout jPanelCorpoLayout = new javax.swing.GroupLayout(jPanelCorpo);
        jPanelCorpo.setLayout(jPanelCorpoLayout);
        jPanelCorpoLayout.setHorizontalGroup(
            jPanelCorpoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCorpoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanelCorpoLayout.setVerticalGroup(
            jPanelCorpoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelCorpoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnConcluidos.setText("Concluídos");
        btnConcluidos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConcluidosActionPerformed(evt);
            }
        });

        btnNaoConcluidos.setText("Não Concluídos");
        btnNaoConcluidos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNaoConcluidosActionPerformed(evt);
            }
        });

        btnGant.setText("Gant");
        btnGant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGantActionPerformed(evt);
            }
        });

        btnImprimir.setText("Imprimir");
        btnImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImprimirActionPerformed(evt);
            }
        });

        btn_Abrir.setText("Abrir Projeto");
        btn_Abrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_AbrirActionPerformed(evt);
            }
        });

        jButtonFechar.setText("Fechar");
        jButtonFechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFecharActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelBotoesLayout = new javax.swing.GroupLayout(jPanelBotoes);
        jPanelBotoes.setLayout(jPanelBotoesLayout);
        jPanelBotoesLayout.setHorizontalGroup(
            jPanelBotoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBotoesLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(btn_Abrir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_proj, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(143, 143, 143)
                .addComponent(btnConcluidos)
                .addGap(18, 18, 18)
                .addComponent(btnNaoConcluidos)
                .addGap(18, 18, 18)
                .addComponent(btnGant)
                .addGap(18, 18, 18)
                .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonFechar)
                .addContainerGap(350, Short.MAX_VALUE))
        );
        jPanelBotoesLayout.setVerticalGroup(
            jPanelBotoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBotoesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelBotoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnConcluidos)
                    .addComponent(btnNaoConcluidos)
                    .addComponent(btnGant)
                    .addComponent(btnImprimir)
                    .addComponent(txt_proj, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_Abrir)
                    .addComponent(jButtonFechar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanelCorpo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelBotoes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelCorpo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelBotoes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private TaskSeriesCollection createDatasetFromTable(TableModel model) {
        TaskSeriesCollection dataset = new TaskSeriesCollection();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); // <-- AQUI ESTAVA O PROBLEMA!
        dateFormat.setLenient(false);

        if (model instanceof ProjetosEtapasHierarquicoTableModel) {
            ProjetosEtapasHierarquicoTableModel hierarquicoModel = (ProjetosEtapasHierarquicoTableModel) model;
            List<ProjetoEtapaHierarquico> dados = hierarquicoModel.getDados();

            for (ProjetoEtapaHierarquico projetoEtapa : dados) {
                Object[] projeto = projetoEtapa.getProjeto();
                List<Object[]> etapas = projetoEtapa.getEtapas();

                TaskSeries seriesProjeto = new TaskSeries((String) projeto[1]);

                Date inicioPrev = parseDateSafe((String) projeto[2], dateFormat);
                Date fimPrev = parseDateSafe((String) projeto[3], dateFormat);
                Date inicioReal = parseDateSafe((String) projeto[4], dateFormat);
                Date fimReal = parseDateSafe((String) projeto[5], dateFormat);

                if (inicioPrev != null && fimPrev != null) {
                    seriesProjeto.add(new Task("Previsto", new org.jfree.data.time.SimpleTimePeriod(inicioPrev, fimPrev)));
                }
                if (inicioReal != null && fimReal != null) {
                    seriesProjeto.add(new Task("Realizado", new org.jfree.data.time.SimpleTimePeriod(inicioReal, fimReal)));
                }

                dataset.add(seriesProjeto);

                for (Object[] etapa : etapas) {
                    TaskSeries seriesEtapa = new TaskSeries("  " + (String) etapa[0]);

                    Date eInicioPrev = parseDateSafe((String) etapa[3], dateFormat);
                    Date eFimPrev = parseDateSafe((String) etapa[4], dateFormat);
                    Date eInicioReal = parseDateSafe((String) etapa[5], dateFormat);
                    Date eFimReal = parseDateSafe((String) etapa[6], dateFormat);

                    if (eInicioPrev != null && eFimPrev != null) {
                        seriesEtapa.add(new Task("Previsto", new org.jfree.data.time.SimpleTimePeriod(eInicioPrev, eFimPrev)));
                    }
                    if (eInicioReal != null && eFimReal != null) {
                        seriesEtapa.add(new Task("Realizado", new org.jfree.data.time.SimpleTimePeriod(eInicioReal, eFimReal)));
                    }

                    dataset.add(seriesEtapa);
                }
            }
        }
        return dataset;
    }

    private Date parseDateSafe(String dateStr, SimpleDateFormat format) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return format.parse(dateStr.trim());
        } catch (ParseException e) {
            System.err.println("Erro ao parsear data: " + dateStr);
            return null;
        }
    }

//    private Date date(LocalDate ld) {
//        return java.sql.Date.valueOf(ld);
//    }
    // ===================== RELATÓRIO PDF =====================
    private void gerarRelatorioPDF() throws Exception {
        TableModel model = tabelaProj.getModel();
        if (!(model instanceof ProjetosEtapasHierarquicoTableModel)) {
            throw new Exception("Modelo da tabela inválido.");
        }
        ProjetosEtapasHierarquicoTableModel hierarquicoModel = (ProjetosEtapasHierarquicoTableModel) model;
        List<ProjetoEtapaHierarquico> dados = hierarquicoModel.getDados();
        if (dados.isEmpty()) {
            throw new Exception("Nenhum projeto carregado na tabela.");
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos PDF", "pdf"));
        fileChooser.setSelectedFile(new File("relatorio_projetos.pdf"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File outputFile = fileChooser.getSelectedFile();
        String outputPath = outputFile.getAbsolutePath();
        if (!outputPath.toLowerCase().endsWith(".pdf")) {
            outputPath += ".pdf";
        }

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputPath));
        writer.setPageEvent(new HeaderFooterPageEvent());
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLUE);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        for (ProjetoEtapaHierarquico p : dados) {
            Object[] projeto = p.getProjeto();
            String nomeProjeto = (String) projeto[1];

            Paragraph title = new Paragraph("RELATÓRIO DE PROJETO: " + nomeProjeto, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(10);
            title.setSpacingAfter(15);
            document.add(title);

            document.add(new Paragraph("Projeto: " + nomeProjeto, headerFont));
            document.add(new Paragraph("Início Previsto: " + projeto[2], normalFont));
            document.add(new Paragraph("Fim Previsto: " + projeto[3], normalFont));
            document.add(new Paragraph("Início Realizado: " + projeto[4], normalFont));
            document.add(new Paragraph("Fim Realizado: " + projeto[5], normalFont));
            document.add(new Paragraph("Status: " + projeto[6], normalFont));
            document.add(new Paragraph("Valor Previsto Total: " + projeto[8], normalFont));
            document.add(new Paragraph("Valor Realizado Total: " + projeto[9], normalFont));
            document.add(new Paragraph("Percentual Financeiro: " + projeto[10], normalFont));
            document.add(new Paragraph(" "));

            if (!p.getEtapas().isEmpty()) {
                document.add(new Paragraph("ETAPAS:", headerFont));
                document.add(new Paragraph(" "));

                PdfPTable table = new PdfPTable(12);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{15, 10, 10, 12, 12, 12, 12, 8, 20, 12, 12, 10});

                // Cabeçalho da tabela
                String[] headers = {"Etapa", "Prioridade", "Responsável", "Início Prev.", "Fim Prev.", "Início Real.", "Fim Real.", "Status", "Observações", "R$ Previsto", "R$ Realizado", "% Executado"};
                for (String h : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    table.addCell(cell);
                }

                for (Object[] etapa : p.getEtapas()) {
                    for (int i = 0; i < 12; i++) {
                        String value = i < etapa.length && etapa[i] != null ? etapa[i].toString() : "";
                        table.addCell(new Phrase(value, normalFont));
                    }
                }
                document.add(table);
            } else {
                document.add(new Paragraph("Nenhuma etapa cadastrada.", normalFont));
            }

            document.newPage();
        }

        document.close();
        JOptionPane.showMessageDialog(this, "Relatório PDF gerado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setLocationRelativeTo(Object object) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private class HeaderFooterPageEvent extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Phrase footer = new Phrase(
                    String.format("Página %d - Gerado em %s", writer.getPageNumber(),
                            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                    FontFactory.getFont(FontFactory.HELVETICA, 8)
            );
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    footer,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 15, 0);
        }
    }


    private void btnConcluidosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConcluidosActionPerformed
        tabelaProj.setModel(new ProjetosEtapasHierarquicoTableModel("Concluído"));
    }//GEN-LAST:event_btnConcluidosActionPerformed

    private void btnNaoConcluidosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNaoConcluidosActionPerformed
        tabelaProj.setModel(new ProjetosEtapasHierarquicoTableModel("Em Andamento"));
    }//GEN-LAST:event_btnNaoConcluidosActionPerformed

    private void btnGantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGantActionPerformed
        exibirGrafico();
    }//GEN-LAST:event_btnGantActionPerformed

    private void btn_AbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_AbrirActionPerformed
        String texto = txt_proj.getText().trim();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite o ID do projeto.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int id = Integer.parseInt(texto);
            ProjetosEtapasHierarquicoTableModel model = new ProjetosEtapasHierarquicoTableModel(id);
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Projeto ID " + id + " não encontrado.", "Resultado", JOptionPane.INFORMATION_MESSAGE);
                tabelaProj.setModel(new ProjetosEtapasHierarquicoTableModel(""));
            } else {
                tabelaProj.setModel(model);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID deve ser um número válido.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btn_AbrirActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        try {
            gerarRelatorioPDF();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar PDF: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnImprimirActionPerformed

    private void jButtonFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFecharActionPerformed
//        try {
//            new TelaPrincipal().setVisible(true);
//            dispose();
//        } catch (IOException ex) {
//            Logger.getLogger(Form_Etapas.class.getName()).log(Level.SEVERE, null, ex);
//        }
        try {
            new TelaPrincipal().setVisible(true);
            dispose();
        } catch (IOException ex) {
            Logger.getLogger(Tela_Consulta_Projetos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonFecharActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new Tela_Consulta_Projetos().setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Erro crítico ao iniciar:\n" + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConcluidos;
    private javax.swing.JButton btnGant;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnNaoConcluidos;
    private javax.swing.JButton btn_Abrir;
    private javax.swing.JButton jButtonFechar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanelBotoes;
    private javax.swing.JPanel jPanelCorpo;
    private javax.swing.JPanel jPanelTitulo;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabelaProj;
    private javax.swing.JTextField txt_proj;
    // End of variables declaration//GEN-END:variables
}
