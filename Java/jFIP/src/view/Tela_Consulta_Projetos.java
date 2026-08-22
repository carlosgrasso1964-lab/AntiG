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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
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
import org.jfree.data.time.TimePeriod;
import org.jfree.ui.RectangleEdge;

public class Tela_Consulta_Projetos extends javax.swing.JInternalFrame {

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Creates new form Tela_Cons_Projetos
     */
    public Tela_Consulta_Projetos() throws SQLException {
        initComponents();
        setTitle("Consulta de Projetos e Etapas");
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);

        // Carrega todos os projetos ao abrir
        tabelaProj.setModel(new ProjetosEtapasHierarquicoTableModel(""));
    }

    // ===================== GRÁFICO GANTT CORRIGIDO =====================
    private void exibirGrafico() throws ParseException {
        // Criar dataset para o gráfico de Gantt
        TaskSeriesCollection dataset = createDatasetFromTable(tabelaProj.getModel());

        // Criar gráfico
        JFreeChart chart = ChartFactory.createGanttChart(
                "Gráfico de Gantt - Cronograma de Projetos e Etapas",
                "Projeto/Etapa",
                "Tempo",
                dataset,
                true, // legendas
                true, // tooltips
                false // urls
        );

        CategoryPlot plot = chart.getCategoryPlot();

        // === CONFIGURAÇÃO DO EIXO DE TEMPO ===
        DateAxis rangeAxis = (DateAxis) plot.getRangeAxis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        rangeAxis.setDateFormatOverride(dateFormat);

        // Reduzir margens para evitar "invasão" visual
        rangeAxis.setLowerMargin(0.02);  // 2% no início
        rangeAxis.setUpperMargin(0.05);  // 5% no fim

        rangeAxis.setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 11));
        rangeAxis.setLabelFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));

        // === CÁLCULO MANUAL DO RANGE (corrigido para evitar erro de generics) ===
        Date minDate = null;
        Date maxDate = null;

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            TaskSeries series = dataset.getSeries(i);
            // Iteração segura sem erro de generics
            for (Object obj : series.getTasks()) {
                if (obj instanceof Task) {
                    Task task = (Task) obj;
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
        }

        if (minDate != null && maxDate != null) {
            long duration = maxDate.getTime() - minDate.getTime();
            long margin = duration / 20; // 5% de margem dinâmica

            rangeAxis.setRange(
                    new Date(minDate.getTime() - margin),
                    new Date(maxDate.getTime() + margin)
            );
        }

        // Posicionamento
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setPosition(RectangleEdge.BOTTOM);
        }

        // Aparência geral
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        // Exibir
        ChartFrame frame = new ChartFrame("Gráfico de Gantt", chart);
        frame.pack();
        frame.setVisible(true);

        // Centralizar
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

        txt_proj.setPreferredSize(new java.awt.Dimension(97, 23));

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

        // Formato brasileiro usado na tabela do MariaDB
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);

        if (model instanceof ProjetosEtapasHierarquicoTableModel) {
            ProjetosEtapasHierarquicoTableModel hierarquicoModel = (ProjetosEtapasHierarquicoTableModel) model;
            List<ProjetoEtapaHierarquico> dados = hierarquicoModel.getDados();

            for (ProjetoEtapaHierarquico projetoEtapa : dados) {
                Object[] projeto = projetoEtapa.getProjeto();
                List<Object[]> etapas = projetoEtapa.getEtapas();

                // === LOGS DO PROJETO ===
                System.out.println("=== PROJETO: " + projeto[1] + " ===");
                System.out.println("Início Previsto (string): '" + projeto[2] + "'");
                System.out.println("Fim Previsto (string): '" + projeto[3] + "'");
                System.out.println("Início Realizado (string): '" + projeto[4] + "'");
                System.out.println("Fim Realizado (string): '" + projeto[5] + "'");

                // === PROJETO (usa apenas suas próprias datas) ===
                TaskSeries seriesProjeto = new TaskSeries((String) projeto[1]);

                Date inicioPrev = parseDateSafe((String) projeto[2], dateFormat);
                Date fimPrev = parseDateSafe((String) projeto[3], dateFormat);
                Date inicioReal = parseDateSafe((String) projeto[4], dateFormat);
                Date fimReal = parseDateSafe((String) projeto[5], dateFormat);

                // Logs das datas parseadas do projeto
                System.out.println("Início Previsto (Date): " + inicioPrev);
                System.out.println("Fim Previsto (Date): " + fimPrev);
                System.out.println("Início Realizado (Date): " + inicioReal);
                System.out.println("Fim Realizado (Date): " + fimReal);

                // Adiciona as tasks do projeto
                if (inicioPrev != null && fimPrev != null) {
                    System.out.println(">>> Adicionando task Previsto do projeto: " + inicioPrev + " até " + fimPrev);
                    seriesProjeto.add(new Task("Previsto", new org.jfree.data.time.SimpleTimePeriod(inicioPrev, fimPrev)));
                } else {
                    System.out.println(">>> Task Previsto do projeto NÃO adicionada (datas incompletas)");
                }

                if (inicioReal != null && fimReal != null) {
                    System.out.println(">>> Adicionando task Realizado do projeto: " + inicioReal + " até " + fimReal);
                    seriesProjeto.add(new Task("Realizado", new org.jfree.data.time.SimpleTimePeriod(inicioReal, fimReal)));
                } else {
                    System.out.println(">>> Task Realizado do projeto NÃO adicionada (datas incompletas)");
                }

                dataset.add(seriesProjeto);

                // === ETAPAS ===
                System.out.println("--- Etapas do projeto " + projeto[1] + " (total: " + etapas.size() + ") ---");
                for (Object[] etapa : etapas) {
                    System.out.println("Etapa: " + etapa[0]);
                    System.out.println("  Início Previsto (string): '" + etapa[3] + "'");
                    System.out.println("  Fim Previsto (string): '" + etapa[4] + "'");
                    System.out.println("  Início Realizado (string): '" + etapa[5] + "'");
                    System.out.println("  Fim Realizado (string): '" + etapa[6] + "'");

                    TaskSeries seriesEtapa = new TaskSeries("  " + (String) etapa[0]); // indentação

                    Date eInicioPrev = parseDateSafe((String) etapa[3], dateFormat);
                    Date eFimPrev = parseDateSafe((String) etapa[4], dateFormat);
                    Date eInicioReal = parseDateSafe((String) etapa[5], dateFormat);
                    Date eFimReal = parseDateSafe((String) etapa[6], dateFormat);

                    System.out.println("  Início Previsto (Date): " + eInicioPrev);
                    System.out.println("  Fim Previsto (Date): " + eFimPrev);
                    System.out.println("  Início Realizado (Date): " + eInicioReal);
                    System.out.println("  Fim Realizado (Date): " + eFimReal);

                    if (eInicioPrev != null && eFimPrev != null) {
                        System.out.println("  >>> Adicionando task Previsto da etapa: " + eInicioPrev + " até " + eFimPrev);
                        seriesEtapa.add(new Task("Previsto", new org.jfree.data.time.SimpleTimePeriod(eInicioPrev, eFimPrev)));
                    }
                    if (eInicioReal != null && eFimReal != null) {
                        System.out.println("  >>> Adicionando task Realizado da etapa: " + eInicioReal + " até " + eFimReal);
                        seriesEtapa.add(new Task("Realizado", new org.jfree.data.time.SimpleTimePeriod(eInicioReal, eFimReal)));
                    }

                    dataset.add(seriesEtapa);
                }
                System.out.println("==========================================");
            }
        }
        return dataset;
    }
    
    // Mantenha este método auxiliar (já usado antes)
    private Date parseDateSafe(String dateStr, SimpleDateFormat format) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return format.parse(dateStr.trim());
        } catch (Exception e) {
            System.err.println("Erro ao parsear data: " + dateStr);
            return null;
        }
    }

//    private LocalDate parseDate(Object obj) {
//        if (obj == null || obj.toString().trim().isEmpty()) {
//            return null;
//        }
//        try {
//            return LocalDate.parse(obj.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//        } catch (Exception e) {
//            return null;
//        }
//    }

    private Date date(LocalDate ld) {
        return java.sql.Date.valueOf(ld);
    }

    // ===================== RELATÓRIO PDF =====================
    private void gerarRelatorioPDF() throws Exception {
       TableModel model = tabelaProj.getModel();
        if (!(model instanceof ProjetosEtapasHierarquicoTableModel)) {
            JOptionPane.showMessageDialog(this, "Nenhum dado carregado para gerar relatório.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ProjetosEtapasHierarquicoTableModel hierarquicoModel = (ProjetosEtapasHierarquicoTableModel) model;
        List<ProjetoEtapaHierarquico> listaProjetos = hierarquicoModel.getDados();

        if (listaProjetos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum projeto para gerar relatório.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // JFileChooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivo PDF", "pdf"));
        fileChooser.setSelectedFile(new File("Relatorio_Projetos_" + LocalDate.now() + ".pdf"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String caminho = fileChooser.getSelectedFile().getAbsolutePath();
        if (!caminho.toLowerCase().endsWith(".pdf")) {
            caminho += ".pdf";
        }

        // PDF
        Document documento = new Document(PageSize.A4.rotate(), 30, 30, 80, 60);
        PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(caminho));
        writer.setPageEvent(new HeaderFooterPageEvent());
        documento.open();

        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD, BaseColor.DARK_GRAY);
        Font subtituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font negritoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, new BaseColor(0, 102, 204));

        DecimalFormat df = new DecimalFormat("R$ #,##0.00");
        DecimalFormat pf = new DecimalFormat("0.0%");

        for (int i = 0; i < listaProjetos.size(); i++) {
            ProjetoEtapaHierarquico projetoEtapa = listaProjetos.get(i);
            Object[] projeto = projetoEtapa.getProjeto();
            String nomeProjeto = (String) projeto[1];

            // Cabeçalho
            documento.add(new Paragraph("RELATÓRIO DE PROJETO", tituloFont));
            documento.add(new Paragraph("Projeto: " + nomeProjeto, subtituloFont));
            documento.add(new Paragraph("Gerado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), normalFont));
            documento.add(Chunk.NEWLINE);

            // Tabela de etapas
            PdfPTable tabela = new PdfPTable(12);
            tabela.setWidthPercentage(100);
            tabela.setWidths(new float[]{15, 8, 10, 10, 10, 10, 10, 8, 20, 12, 12, 10});

            String[] cabecalho = {"Etapa", "Prioridade", "Responsável", "Início Prev.", "Fim Prev.",
                "Início Real", "Fim Real", "Status", "Observações",
                "R$ Previsto", "$ Realizado", "% Executado"};
            for (String c : cabecalho) {
                PdfPCell cell = new PdfPCell(new Phrase(c, negritoFont));
                cell.setBackgroundColor(new BaseColor(230, 230, 250));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                tabela.addCell(cell);
            }

            BigDecimal totalPrevisto = BigDecimal.ZERO;
            BigDecimal totalRealizado = BigDecimal.ZERO;

            for (Object[] etapa : projetoEtapa.getEtapas()) {
                BigDecimal prev = (BigDecimal) etapa[9];   // valor_previsto
                BigDecimal real = (BigDecimal) etapa[10];  // valor_realizado

                totalPrevisto = totalPrevisto.add(prev);
                totalRealizado = totalRealizado.add(real);

                BigDecimal perc = prev.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                        : real.divide(prev, 4, RoundingMode.HALF_UP);

                tabela.addCell(new Phrase((String) etapa[0], normalFont));
                tabela.addCell(new Phrase((String) etapa[1], normalFont));
                tabela.addCell(new Phrase((String) etapa[2], normalFont));
                tabela.addCell(new Phrase((String) etapa[3], normalFont));
                tabela.addCell(new Phrase((String) etapa[4], normalFont));
                tabela.addCell(new Phrase((String) etapa[5], normalFont));
                tabela.addCell(new Phrase((String) etapa[6], normalFont));
                tabela.addCell(new Phrase((String) etapa[7], normalFont));
                tabela.addCell(new Phrase((String) etapa[8], normalFont));

                PdfPCell cellPrev = new PdfPCell(new Phrase(df.format(prev), normalFont));
                cellPrev.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabela.addCell(cellPrev);

                PdfPCell cellReal = new PdfPCell(new Phrase(df.format(real), normalFont));
                cellReal.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabela.addCell(cellReal);

                PdfPCell cellPerc = new PdfPCell(new Phrase(pf.format(perc), normalFont));
                cellPerc.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabela.addCell(cellPerc);
            }

            documento.add(tabela);

            // TOTAL DO PROJETO
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(50);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.setSpacingBefore(20);

            BigDecimal percentualProjeto = totalPrevisto.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                    : totalRealizado.divide(totalPrevisto, 4, RoundingMode.HALF_UP);

            addTotalRow(totalTable, "TOTAL ORÇADO:", df.format(totalPrevisto), totalFont);
            addTotalRow(totalTable, "TOTAL REALIZADO:", df.format(totalRealizado), totalFont);
            addTotalRow(totalTable, "PERCENTUAL EXECUTADO:", pf.format(percentualProjeto), totalFont);

            documento.add(totalTable);

            // Nova página se não for o último projeto
            if (i < listaProjetos.size() - 1) {
                documento.newPage();
            }
        }

        documento.close();
        JOptionPane.showMessageDialog(this, "Relatório gerado com sucesso!\n" + caminho, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }
    
    
    // Método auxiliar para linhas de total
    private void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, font));
        cell1.setBorder(Rectangle.NO_BORDER);
        PdfPCell cell2 = new PdfPCell(new Phrase(value, font));
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell1);
        table.addCell(cell2);
    }

//    private void setLocationRelativeTo(Object object) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//    }
    
    private class HeaderFooterPageEvent extends PdfPageEventHelper {

     Font f = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Phrase footer = new Phrase(
                    "Gerado em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    + "  ?  Página " + writer.getPageNumber(), f);

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer,
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
        try {
            exibirGrafico();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
            JOptionPane.showMessageDialog(this, "Relatório gerado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar o relatório: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnImprimirActionPerformed

    private void jButtonFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFecharActionPerformed
        dispose();
    }//GEN-LAST:event_jButtonFecharActionPerformed

    /**
     * @param args the command line arguments
     */

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
