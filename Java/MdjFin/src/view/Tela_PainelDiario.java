package view;

import controller.PainelDiarioController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

public class Tela_PainelDiario extends JFrame {

    private JLabel lblSaldoInicial, lblTotalEntradas, lblTotalSaidas, lblSaldoFinal;
    private JLabel lblSaldoPoupanca, lblSugestaoAplicar, lblSugestaoSegura;
    private JLabel lblAlerta, lblMenorSaldoFuturo, lblDataMenorSaldo;
    private JTextArea txtObservacoes;
    private JTable tabelaEntradas, tabelaSaidas;
    private JButton btnAtualizar, btnFechar;

    private final PainelDiarioController controller = new PainelDiarioController();
    private Window parentWindow;
    private BigDecimal valorReservaAtual = new BigDecimal("500.00"); // Padrão que você usa no MySQL

    public Tela_PainelDiario() {
        this(null);
    }

    public Tela_PainelDiario(Window parent) {
        this.parentWindow = parent;
        setTitle("Painel Diário de Caixa (MySQL)");
        setSize(800, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();

        // Carregamento inicial automático
        SwingUtilities.invokeLater(() -> carregarDados());
    }

    private void initComponents() {
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Cabeçalho
        JPanel painelTitulo = new JPanel();
        JLabel titulo = new JLabel("Painel Diário Financeiro");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        painelTitulo.add(titulo);
        painelPrincipal.add(painelTitulo, BorderLayout.NORTH);

        JPanel painelCentro = new JPanel(new BorderLayout(10, 10));

        // Seção de Saldos (Grid)
        JPanel painelSaldos = new JPanel(new GridLayout(4, 3, 10, 10));
        painelSaldos.setBorder(BorderFactory.createTitledBorder("Saldos e Projeções"));

        lblSaldoInicial = criarLabelValor();
        lblTotalEntradas = criarLabelValor();
        lblTotalSaidas = criarLabelValor();
        lblSaldoFinal = criarLabelValor();
        lblSaldoPoupanca = criarLabelValor();
        lblSugestaoAplicar = criarLabelValor();
        lblSugestaoSegura = criarLabelValor();
        lblAlerta = criarLabelValor();
        lblMenorSaldoFuturo = criarLabelValor();
        lblDataMenorSaldo = criarLabelValor();

        painelSaldos.add(criarPainelComLabel("Saldo Inicial (Caixa/Bancos):", lblSaldoInicial));
        painelSaldos.add(criarPainelComLabel("Entradas do Dia:", lblTotalEntradas));
        painelSaldos.add(criarPainelComLabel("Saídas do Dia:", lblTotalSaidas));
        painelSaldos.add(criarPainelComLabel("Saldo Final Projetado:", lblSaldoFinal));
        painelSaldos.add(criarPainelComLabel("Reserva na Poupança:", lblSaldoPoupanca));
        painelSaldos.add(criarPainelComLabel("Sugestão Aplicação:", lblSugestaoAplicar));
        painelSaldos.add(criarPainelComLabel("Sugestão Segura (Futuro):", lblSugestaoSegura));
        painelSaldos.add(criarPainelComLabel("Alerta de Fluxo:", lblAlerta));
        painelSaldos.add(new JPanel()); // Espaçador
        painelSaldos.add(criarPainelComLabel("Menor Saldo (90 dias):", lblMenorSaldoFuturo));
        painelSaldos.add(criarPainelComLabel("Data do Menor Saldo:", lblDataMenorSaldo));

        painelCentro.add(painelSaldos, BorderLayout.NORTH);

        // Área de Texto para Observações
        txtObservacoes = new JTextArea(6, 50);
        txtObservacoes.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtObservacoes.setEditable(false);
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        JScrollPane scrollObservacoes = new JScrollPane(txtObservacoes);
        scrollObservacoes.setBorder(BorderFactory.createTitledBorder("Análise da LIA e Sugestões"));

        // Tabelas de Movimentação
        JPanel painelTabelas = new JPanel(new GridLayout(1, 2, 10, 10));

        tabelaEntradas = new JTable(new DefaultTableModel(new Object[]{"Descrição", "Valor"}, 0));
        tabelaSaidas = new JTable(new DefaultTableModel(new Object[]{"Descrição", "Valor"}, 0));

        painelTabelas.add(new JScrollPane(tabelaEntradas));
        painelTabelas.add(new JScrollPane(tabelaSaidas));

        JPanel painelConteudoMeio = new JPanel(new BorderLayout(5, 5));
        painelConteudoMeio.add(scrollObservacoes, BorderLayout.NORTH);
        painelConteudoMeio.add(painelTabelas, BorderLayout.CENTER);

        painelCentro.add(painelConteudoMeio, BorderLayout.CENTER);
        painelPrincipal.add(painelCentro, BorderLayout.CENTER);

        // Botões inferiores
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAtualizar = new JButton("Atualizar Dados");
        btnAtualizar.addActionListener(e -> carregarDados());
        btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());

        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnFechar);
        painelPrincipal.add(painelBotoes, BorderLayout.SOUTH);

        add(painelPrincipal);
    }

    private JLabel criarLabelValor() {
        JLabel label = new JLabel("R$ 0,00");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(0, 100, 200));
        return label;
    }

    private JPanel criarPainelComLabel(String rotulo, JLabel valor) {
        JPanel painel = new JPanel(new BorderLayout(2, 2));
        JLabel label = new JLabel(rotulo);
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        painel.add(label, BorderLayout.NORTH);
        painel.add(valor, BorderLayout.CENTER);
        return painel;
    }

    public void carregarDados() {
        // Solicita a reserva (mantendo a lógica que você já utiliza)
        String input = JOptionPane.showInputDialog(this, "Reserva fixa para cálculo de aplicação:", valorReservaAtual);
        if (input != null) {
            try {
                valorReservaAtual = new BigDecimal(input.replace(",", "."));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Valor inválido. Usando reserva anterior: " + valorReservaAtual);
            }
        }

        new Thread(() -> {
            try {
                Date hoje = new Date();

                // AJUSTE 1: Passando os dois parâmetros necessários: data e valorReservaAtual
                // Note que se o seu controller foi instanciado, usamos 'controller.gerarRelatorio'
                // Se o método for estático no Controller, usamos 'PainelDiarioController.gerarRelatorio'
                PainelDiarioController.RelatorioPainel rel = PainelDiarioController.gerarRelatorio(hoje, valorReservaAtual);

                // AJUSTE 2: Atualiza a interface
                SwingUtilities.invokeLater(() -> atualizarInterface(rel));

            } catch (Exception e) {
                // AJUSTE 3: Trocado de SQLException para Exception genérica, 
                // já que o Controller agora trata o banco internamente.
                SwingUtilities.invokeLater(()
                        -> JOptionPane.showMessageDialog(this, "Erro ao processar dados: " + e.getMessage()));
            }
        }).start();
    }

    private void atualizarInterface(PainelDiarioController.RelatorioPainel rel) {
        @SuppressWarnings("deprecation")
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        Color corVerde = new Color(0, 120, 0);
        Color corVermelha = new Color(180, 0, 0);

        lblSaldoInicial.setText(nf.format(rel.saldoInicial));
        lblTotalEntradas.setText(nf.format(rel.totalEntradas()));
        lblTotalSaidas.setText(nf.format(rel.totalSaidas().negate()));
        lblSaldoFinal.setText(nf.format(rel.saldoFinalDia));
        lblSaldoFinal.setForeground(rel.saldoFinalDia.signum() < 0 ? corVermelha : corVerde);

        lblSaldoPoupanca.setText(nf.format(rel.saldoPoupanca));
        lblSugestaoAplicar.setText(nf.format(rel.sugestaoAplicar));
        lblSugestaoSegura.setText(nf.format(rel.sugestaoSeguraAplicar));
        lblSugestaoSegura.setForeground(rel.sugestaoSeguraAplicar.signum() <= 0 ? corVermelha : corVerde);

        lblMenorSaldoFuturo.setText(nf.format(rel.menorSaldoProjetado));
        lblDataMenorSaldo.setText(rel.dataMenorSaldo);

        lblAlerta.setText(rel.alerta.isEmpty() ? "Normal" : rel.alerta);
        lblAlerta.setForeground(rel.alerta.isEmpty() ? corVerde : corVermelha);

        // Atualiza Tabelas
        DefaultTableModel mEntradas = (DefaultTableModel) tabelaEntradas.getModel();
        mEntradas.setRowCount(0);
        rel.entradas.forEach(m -> mEntradas.addRow(new Object[]{m.descricao, nf.format(m.valor)}));

        DefaultTableModel mSaidas = (DefaultTableModel) tabelaSaidas.getModel();
        mSaidas.setRowCount(0);
        rel.saidas.forEach(m -> mSaidas.addRow(new Object[]{m.descricao, nf.format(m.valor)}));

        // Texto de Análise
        StringBuilder sb = new StringBuilder();
        sb.append("ANÁLISE DE FLUXO:\n");
        sb.append("- Considerada reserva de segurança: ").append(nf.format(valorReservaAtual)).append("\n");
        if (rel.saldoFinalDia.signum() < 0) {
            sb.append("- ATENÇÃO: Saldo do dia negativo. Verifique resgate da poupança.\n");
        }
        if (rel.menorSaldoProjetado.signum() < 0) {
            sb.append("- RISCO FUTURO: Projeção aponta saldo negativo em ").append(rel.dataMenorSaldo).append("\n");
        }
        txtObservacoes.setText(sb.toString());
    }
}
