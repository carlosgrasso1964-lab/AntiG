package lia;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PainelDiarioFinanceiro extends JFrame {

    private JLabel lblSaldoInicial;
    private JLabel lblTotalEntradas;
    private JLabel lblTotalSaidas;
    private JLabel lblSaldoFinal;
    private JLabel lblSaldoPoupanca;
    private JLabel lblSugestaoAplicar;
    private JLabel lblSugestaoSegura;
    private JLabel lblAlerta;
    private JLabel lblMenorSaldoFuturo;
    private JLabel lblDataMenorSaldo;
    private JTextArea txtObservacoes;
    private JTable tabelaEntradas;
    private JTable tabelaSaidas;
    private JButton btnAtualizar;
    private JButton btnFechar;
    private JLabel lblReservaAtual; // Exibe a reserva em uso

    // Valor da reserva fixa — editável pelo usuário (igual ao jFinNslt)
    private java.math.BigDecimal valorReservaAtual = new java.math.BigDecimal("500.00");

    public PainelDiarioFinanceiro() {
        setTitle("📊 Painel Diário Financeiro");
        setSize(750, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        carregarDados();
    }

    private void initComponents() {
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Título + Reserva Atual
        JPanel painelTitulo = new JPanel(new BorderLayout(10, 0));
        JLabel titulo = new JLabel("📊 Painel Diário Financeiro");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblReservaAtual = new JLabel("Reserva: R$ 500,00");
        lblReservaAtual.setFont(new Font("Arial", Font.PLAIN, 13));
        lblReservaAtual.setForeground(new Color(0, 100, 180));
        painelTitulo.add(titulo, BorderLayout.WEST);
        painelTitulo.add(lblReservaAtual, BorderLayout.EAST);
        JPanel painelTituloWrapper = new JPanel();
        painelTituloWrapper.add(painelTitulo);
        painelPrincipal.add(painelTituloWrapper, BorderLayout.NORTH);

        JPanel painelCentro = new JPanel(new BorderLayout(10, 10));

        JPanel painelSaldos = new JPanel(new GridLayout(4, 3, 10, 10));
        painelSaldos.setBorder(BorderFactory.createTitledBorder("Saldos e Totais"));

        lblSaldoInicial = criarLabelValor("Saldo Inicial (Bancos + Caixa):");
        lblTotalEntradas = criarLabelValor("Total Entradas:");
        lblTotalSaidas = criarLabelValor("Total Saídas:");
        lblSaldoFinal = criarLabelValor("Saldo Final do Dia:");
        lblSaldoPoupanca = criarLabelValor("Saldo Poupança:");
        lblSugestaoAplicar = criarLabelValor("Sugestão Aplicar:");
        lblSugestaoSegura = criarLabelValor("Sugestão Segura:");
        lblAlerta = criarLabelValor("Alerta:");
        lblMenorSaldoFuturo = criarLabelValor("Menor Saldo Futuro:");
        lblDataMenorSaldo = criarLabelValor("Data Menor Saldo:");

        painelSaldos.add(criarPainelComLabel("Saldo Inicial:", lblSaldoInicial));
        painelSaldos.add(criarPainelComLabel("Entradas:", lblTotalEntradas));
        painelSaldos.add(criarPainelComLabel("Saídas:", lblTotalSaidas));
        painelSaldos.add(criarPainelComLabel("Saldo Final:", lblSaldoFinal));
        painelSaldos.add(criarPainelComLabel("Poupança:", lblSaldoPoupanca));
        painelSaldos.add(criarPainelComLabel("Sugestão:", lblSugestaoAplicar));
        painelSaldos.add(criarPainelComLabel("Segura:", lblSugestaoSegura));
        painelSaldos.add(criarPainelComLabel("Alerta:", lblAlerta));
        painelSaldos.add(new JPanel());
        painelSaldos.add(criarPainelComLabel("Menor Saldo:", lblMenorSaldoFuturo));
        painelSaldos.add(criarPainelComLabel("Data:", lblDataMenorSaldo));
        painelSaldos.add(new JPanel());

        painelCentro.add(painelSaldos, BorderLayout.NORTH);

        txtObservacoes = new JTextArea(5, 50);
        txtObservacoes.setFont(new Font("Arial", Font.PLAIN, 12));
        txtObservacoes.setEditable(false);
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        JScrollPane scrollObservacoes = new JScrollPane(txtObservacoes);
        scrollObservacoes.setBorder(BorderFactory.createTitledBorder("Observações e Sugestões"));

        painelCentro.add(scrollObservacoes, BorderLayout.CENTER);

        JPanel painelTabelas = new JPanel(new GridLayout(1, 2, 10, 10));

        DefaultTableModel modelEntradas = new DefaultTableModel(new Object[]{"Descrição", "Valor"}, 0);
        tabelaEntradas = new JTable(modelEntradas);
        JScrollPane scrollEntradas = new JScrollPane(tabelaEntradas);
        scrollEntradas.setBorder(BorderFactory.createTitledBorder("Entradas do Dia"));

        DefaultTableModel modelSaidas = new DefaultTableModel(new Object[]{"Descrição", "Valor"}, 0);
        tabelaSaidas = new JTable(modelSaidas);
        JScrollPane scrollSaidas = new JScrollPane(tabelaSaidas);
        scrollSaidas.setBorder(BorderFactory.createTitledBorder("Saídas do Dia"));

        painelTabelas.add(scrollEntradas);
        painelTabelas.add(scrollSaidas);

        painelCentro.add(painelTabelas, BorderLayout.CENTER);
        painelPrincipal.add(painelCentro, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAtualizar = new JButton("🔄 Atualizar");
        btnAtualizar.addActionListener(e -> carregarDados());
        btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnFechar);
        painelPrincipal.add(painelBotoes, BorderLayout.SOUTH);

        add(painelPrincipal);
    }

    private JLabel criarLabelValor(String texto) {
        JLabel label = new JLabel("R$ 0,00");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    private JPanel criarPainelComLabel(String rotulo, JLabel valor) {
        JPanel painel = new JPanel(new BorderLayout(5, 5));
        JLabel label = new JLabel(rotulo);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        painel.add(label, BorderLayout.NORTH);
        valor.setForeground(new Color(0, 100, 200));
        painel.add(valor, BorderLayout.CENTER);
        return painel;
    }

    private void carregarDados() {
        // Pede o valor da reserva antes de carregar — igual ao Tela_PainelDiario do jFinNslt
        String input = JOptionPane.showInputDialog(this,
                "Informe o valor da reserva fixa para hoje:",
                valorReservaAtual.toString());

        if (input == null) {
            return; // Usuário cancelou
        }
        if (!input.trim().isEmpty()) {
            try {
                valorReservaAtual = new java.math.BigDecimal(input.replace(",", "."));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Valor inválido! Mantendo padrão de R$ " + valorReservaAtual);
            }
        }

        // Atualiza o label do cabeçalho com o valor em uso
        @SuppressWarnings("deprecation")
        java.text.NumberFormat nfLabel = java.text.NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        lblReservaAtual.setText("Reserva: " + nfLabel.format(valorReservaAtual));

        new Thread(() -> {
            try {
                Date dataHoje = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String dataStr = sdf.format(dataHoje);

                PainelDiarioFinanceiroService.RelatorioPainel rel =
                    PainelDiarioFinanceiroService.gerarRelatorio(dataHoje, valorReservaAtual);

                @SuppressWarnings("deprecation")
                NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                
                Color corVerde = new Color(0, 120, 0);
                Color corVermelho = new Color(180, 0, 0);

                SwingUtilities.invokeLater(() -> {
                    lblSaldoInicial.setText(nf.format(rel.saldoInicial));
                    lblTotalEntradas.setText(nf.format(rel.totalEntradas()));
                    lblTotalSaidas.setText(nf.format(rel.totalSaidas().negate()));
                    lblSaldoFinal.setText(nf.format(rel.saldoFinalDia));
                    if (rel.saldoFinalDia.compareTo(java.math.BigDecimal.ZERO) < 0) {
                        lblSaldoFinal.setForeground(corVermelho);
                    } else {
                        lblSaldoFinal.setForeground(corVerde);
                    }
                    lblSaldoPoupanca.setText(nf.format(rel.saldoPoupanca));
                    
                    lblSugestaoAplicar.setText(nf.format(rel.sugestaoAplicar));
                    lblSugestaoSegura.setText(nf.format(rel.sugestaoSeguraAplicar));
                    
                    if (rel.sugestaoSeguraAplicar.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        lblSugestaoSegura.setForeground(corVerde);
                        lblSugestaoAplicar.setForeground(corVerde);
                    } else {
                        lblSugestaoSegura.setForeground(corVermelho);
                        lblSugestaoAplicar.setForeground(corVermelho);
                    }
                    
                    lblMenorSaldoFuturo.setText(nf.format(rel.menorSaldoProjetado));
                    lblDataMenorSaldo.setText(rel.dataMenorSaldo);

                    if (rel.alerta != null && !rel.alerta.isEmpty()) {
                        lblAlerta.setText(rel.alerta);
                        lblAlerta.setForeground(corVermelho);
                    } else {
                        lblAlerta.setText("Nenhum");
                        lblAlerta.setForeground(corVerde);
                    }

                    StringBuilder obs = new StringBuilder();
                    if (rel.saldoFinalDia.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        obs.append("- Reserva fixa: ").append(nf.format(valorReservaAtual)).append("\n");
                        obs.append("- Margem necessária para fluxo futuro: R$ ").append(nf.format(rel.saldoFinalDia.subtract(rel.menorSaldoProjetado))).append("\n");
                        obs.append("\n- SUGESTÃO DE APLICAÇÃO (Hoje): R$ ").append(nf.format(rel.sugestaoAplicar)).append("\n");
                        obs.append("=> SUGESTÃO SEGURA (Olhando o Futuro): R$ ").append(nf.format(rel.sugestaoSeguraAplicar)).append("\n");
                    } else {
                        obs.append("- Saldo Negativo detectado!\n");
                        obs.append("- Poupança disponível: R$ ").append(nf.format(rel.saldoPoupanca)).append("\n");
                        obs.append("- SUGESTÃO: Resgate de R$ ").append(nf.format(rel.sugestaoResgate)).append(" da Poupança.\n");
                    }
                    
                    if (rel.alerta != null && !rel.alerta.isEmpty()) {
                        obs.append("\n>>> ").append(rel.alerta).append("\n");
                    }
                    txtObservacoes.setText(obs.toString());

                    DefaultTableModel modelEntradas = (DefaultTableModel) tabelaEntradas.getModel();
                    modelEntradas.setRowCount(0);
                    for (PainelDiarioFinanceiroService.MovimentoSimples m : rel.entradas) {
                        modelEntradas.addRow(new Object[]{m.descricao, nf.format(m.valor)});
                    }

                    DefaultTableModel modelSaidas = (DefaultTableModel) tabelaSaidas.getModel();
                    modelSaidas.setRowCount(0);
                    for (PainelDiarioFinanceiroService.MovimentoSimples m : rel.saidas) {
                        modelSaidas.addRow(new Object[]{m.descricao, nf.format(m.valor)});
                    }

                    setTitle("📊 Painel Diário - " + dataStr);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage());
                });
            }
        }).start();
    }

    public static void abrir() {
        SwingUtilities.invokeLater(() -> {
            PainelDiarioFinanceiro painel = new PainelDiarioFinanceiro();
            painel.setVisible(true);
        });
    }
}