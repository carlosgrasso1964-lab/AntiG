package view;

import controller.PainelDiarioController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Tela_PainelDiario extends JFrame {

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
    private TelaPrincipal telaPrincipal;
    private BigDecimal valorReservaAtual = new BigDecimal("500.00");

    public Tela_PainelDiario() {
        this(null);
    }

    public Tela_PainelDiario(TelaPrincipal principal) {
        this.telaPrincipal = principal;

        setTitle("Painel Diário Financeiro");
        setSize(750, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        
        SwingUtilities.invokeLater(() -> carregarDados());
    }

    private void initComponents() {
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel painelTitulo = new JPanel();
        JLabel titulo = new JLabel("Painel Diário Financeiro");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        painelTitulo.add(titulo);
        painelPrincipal.add(painelTitulo, BorderLayout.NORTH);

        JPanel painelCentro = new JPanel(new BorderLayout(10, 10));

        JPanel painelSaldos = new JPanel(new GridLayout(4, 3, 10, 10));
        painelSaldos.setBorder(BorderFactory.createTitledBorder("Saldos e Totais"));

        lblSaldoInicial = criarLabelValor("Saldo Inicial:");
        lblTotalEntradas = criarLabelValor("Total Entradas:");
        lblTotalSaidas = criarLabelValor("Total Saídas:");
        lblSaldoFinal = criarLabelValor("Saldo Final:");
        lblSaldoPoupanca = criarLabelValor("Poupança:");
        lblSugestaoAplicar = criarLabelValor("Sugestão:");
        lblSugestaoSegura = criarLabelValor("Segura:");
        lblAlerta = criarLabelValor("Alerta:");
        lblMenorSaldoFuturo = criarLabelValor("Menor Saldo:");
        lblDataMenorSaldo = criarLabelValor("Data:");

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

        JPanel painelTabelas = new JPanel(new GridLayout(1, 2, 10, 10));

        tabelaEntradas = new JTable(new DefaultTableModel(new Object[]{"Descrição", "Valor"}, 0));
        JScrollPane scrollEntradas = new JScrollPane(tabelaEntradas);
        scrollEntradas.setBorder(BorderFactory.createTitledBorder("Entradas do Dia"));

        tabelaSaidas = new JTable(new DefaultTableModel(new Object[]{"Descrição", "Valor"}, 0));
        JScrollPane scrollSaidas = new JScrollPane(tabelaSaidas);
        scrollSaidas.setBorder(BorderFactory.createTitledBorder("Saídas do Dia"));

        painelTabelas.add(scrollEntradas);
        painelTabelas.add(scrollSaidas);

        JPanel painelConteudoMeio = new JPanel(new BorderLayout(5, 5));
        painelConteudoMeio.add(scrollObservacoes, BorderLayout.NORTH);
        painelConteudoMeio.add(painelTabelas, BorderLayout.CENTER);

        painelCentro.add(painelConteudoMeio, BorderLayout.CENTER);
        painelPrincipal.add(painelCentro, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnAtualizar = new JButton("Atualizar");
        btnAtualizar.addActionListener(e -> carregarDados());

        btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> jButtonFecharActionPerformed(e));

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

    public void carregarDados() {
        String input = JOptionPane.showInputDialog(this, 
                "Informe o valor da reserva fixa para hoje:", 
                valorReservaAtual.toString());

        if (input != null && !input.trim().isEmpty()) {
            try {
                valorReservaAtual = new BigDecimal(input.replace(",", "."));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valor inválido! Mantendo padrão de R$ " + valorReservaAtual);
            }
        } else if (input == null) {
            return;
        }

        new Thread(() -> {
            try {
                Date dataHoje = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String dataStr = sdf.format(dataHoje);
                PainelDiarioController.RelatorioPainel rel = PainelDiarioController.gerarRelatorio(dataHoje, valorReservaAtual);
                 
                @SuppressWarnings("deprecation")
                NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                Color corVerde = new Color(0, 120, 0);
                Color corVermelho = new Color(180, 0, 0);

                SwingUtilities.invokeLater(() -> {
                    lblSaldoInicial.setText(nf.format(rel.saldoInicial));
                    lblTotalEntradas.setText(nf.format(rel.totalEntradas()));
                    lblTotalSaidas.setText(nf.format(rel.totalSaidas().negate()));
                    lblSaldoFinal.setText(nf.format(rel.saldoFinalDia));

                    lblSaldoFinal.setForeground(rel.saldoFinalDia.compareTo(BigDecimal.ZERO) < 0 ? corVermelho : corVerde);

                    lblSaldoPoupanca.setText(nf.format(rel.saldoPoupanca));
                    lblSugestaoAplicar.setText(nf.format(rel.sugestaoAplicar));
                    lblSugestaoSegura.setText(nf.format(rel.sugestaoSeguraAplicar));

                    if (rel.sugestaoSeguraAplicar.compareTo(BigDecimal.ZERO) > 0) {
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
                    if (rel.saldoFinalDia.compareTo(BigDecimal.ZERO) > 0) {
                        obs.append("- Reserva fixa: ").append(nf.format(valorReservaAtual)).append("\n");
                        obs.append("- Margem necessária para fluxo futuro: ").append(nf.format(rel.saldoFinalDia.subtract(rel.menorSaldoProjetado))).append("\n");
                        obs.append("\n- SUGESTÃO DE APLICAÇÃO (Hoje): ").append(nf.format(rel.sugestaoAplicar)).append("\n");
                        obs.append("=> SUGESTÃO SEGURA (Olhando o Futuro): ").append(nf.format(rel.sugestaoSeguraAplicar)).append("\n");
                    } else {
                        obs.append("- Saldo Negativo detectado!\n");
                        obs.append("- Poupança disponível: ").append(nf.format(rel.saldoPoupanca)).append("\n");
                        obs.append("- SUGESTÃO: Resgate de ").append(nf.format(rel.sugestaoResgate)).append(" da Poupança.\n");
                    }
                    txtObservacoes.setText(obs.toString());

                    DefaultTableModel mEntradas = (DefaultTableModel) tabelaEntradas.getModel();
                    mEntradas.setRowCount(0);
                    for (PainelDiarioController.MovimentoSimples m : rel.entradas) {
                        mEntradas.addRow(new Object[]{m.descricao, nf.format(m.valor)});
                    }

                    DefaultTableModel mSaidas = (DefaultTableModel) tabelaSaidas.getModel();
                    mSaidas.setRowCount(0);
                    for (PainelDiarioController.MovimentoSimples m : rel.saidas) {
                        mSaidas.addRow(new Object[]{m.descricao, nf.format(m.valor)});
                    }

                    setTitle("Painel Diário - " + dataStr);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }
    
    
    private void jButtonFecharActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            TelaPrincipal exibir;
            exibir = new TelaPrincipal();
            exibir.setVisible(true);
            setVisible(false);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Tela_PainelDiario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Tela_PainelDiario().setVisible(true));
    }
}
