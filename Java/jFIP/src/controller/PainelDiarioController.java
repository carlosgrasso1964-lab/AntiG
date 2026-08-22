package controller;

import utilitarios.Conexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PainelDiarioController {

    public static class MovimentoSimples {

        public String descricao;
        public BigDecimal valor;

        public MovimentoSimples(String descricao, BigDecimal valor) {
            this.descricao = descricao;
            this.valor = valor;
        }
    }

    public static class RelatorioPainel {

        public BigDecimal saldoInicial = BigDecimal.ZERO;
        public List<MovimentoSimples> entradas = new ArrayList<>();
        public List<MovimentoSimples> saidas = new ArrayList<>();
        public BigDecimal saldoFinalDia = BigDecimal.ZERO;
        public BigDecimal reserva = new BigDecimal("300.00");
        public BigDecimal sugestaoAplicar = BigDecimal.ZERO;
        public BigDecimal sugestaoResgate = BigDecimal.ZERO;
        public BigDecimal saldoPoupanca = BigDecimal.ZERO;
        public String alerta = "";
        public BigDecimal menorSaldoProjetado = BigDecimal.ZERO;
        public String dataMenorSaldo = "";
        public BigDecimal sugestaoSeguraAplicar = BigDecimal.ZERO;

        public BigDecimal totalEntradas() {
            BigDecimal total = BigDecimal.ZERO;
            for (MovimentoSimples m : entradas) {
                total = total.add(m.valor);
            }
            return total;
        }

        public BigDecimal totalSaidas() {
            BigDecimal total = BigDecimal.ZERO;
            for (MovimentoSimples m : saidas) {
                total = total.add(m.valor);
            }
            return total;
        }
    }

    public static RelatorioPainel gerarRelatorio(Date data, BigDecimal valorReserva) {
        RelatorioPainel rel = new RelatorioPainel();

        // Aplica a reserva fixa informada no front-end ou parâmetro
        if (valorReserva != null) {
            rel.reserva = valorReserva;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // Formatador para Moeda Brasileira (Correção do seu Alerta)
        @SuppressWarnings("deprecation")
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        String dataStr = sdf.format(data);
        Conexao conexao = new Conexao();

        try (Connection con = Conexao.faz_conexao();) {
            if (con == null) {
                rel.alerta = "Erro ao conectar no banco de dados";
                return rel;
            }

            // 1. Saldo Inicial (MySQL)
            String sqlSaldo = "SELECT SUM(saldos) FROM vsaldos WHERE conta IN ('Bancos', 'Caixa')";
            try (PreparedStatement stmt = con.prepareStatement(sqlSaldo); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    rel.saldoInicial = rs.getBigDecimal(1);
                    if (rel.saldoInicial == null) {
                        rel.saldoInicial = BigDecimal.ZERO;
                    }
                }
            }

            // 2. Entradas
            String sqlEntradas = "SELECT Descr, Valor FROM view_movimento "
                    + "WHERE dtVcto <= ? AND nome_C = 'Contas à Receber' AND dtApr IS NULL";
            try (PreparedStatement stmt = con.prepareStatement(sqlEntradas)) {
                stmt.setString(1, dataStr);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        rel.entradas.add(new MovimentoSimples(rs.getString("Descr"), rs.getBigDecimal("Valor")));
                    }
                }
            }

            // 3. Saídas (Ajustado para MySQL)
            String sqlSaidas = """
                SELECT Descr, Valor 
                FROM view_movimento 
                WHERE dtVcto <= ? 
                  AND (
                      (nome_C = 'Contas à Pagar' AND dtApr IS NULL)
                      OR 
                      (recurso = '0079' OR vrecurso = '2.001.003')
                  )
                  AND statusMov NOT IN ('PG', 'TD', 'SI')
                ORDER BY dtVcto ASC
                """;
            try (PreparedStatement stmt = con.prepareStatement(sqlSaidas)) {
                stmt.setString(1, dataStr);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        rel.saidas.add(new MovimentoSimples(rs.getString("Descr"), rs.getBigDecimal("Valor")));
                    }
                }
            }

            // 4. Saldo Poupança
            String sqlPoup = "SELECT SUM(saldos) FROM vsaldos WHERE conta = 'Conta Poupança'";
            try (PreparedStatement stmt = con.prepareStatement(sqlPoup); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    rel.saldoPoupanca = rs.getBigDecimal(1);
                    if (rel.saldoPoupanca == null) {
                        rel.saldoPoupanca = BigDecimal.ZERO;
                    }
                }
            }

            // Cálculos de Saldo
            rel.saldoFinalDia = rel.saldoInicial.add(rel.totalEntradas()).add(rel.totalSaidas());
            BigDecimal saldoAcumulado = rel.saldoFinalDia;
            rel.menorSaldoProjetado = saldoAcumulado;
            rel.dataMenorSaldo = dataStr;

            // 5. Fluxo Futuro (Ajuste de sintaxe para MySQL LIMIT)
            String sqlFuturo = "SELECT dtVcto, SUM(Valor) as total_dia FROM view_movimento "
                    + "WHERE dtVcto > ? AND dtApr IS NULL "
                    + "GROUP BY dtVcto ORDER BY dtVcto ASC LIMIT 120";
            try (PreparedStatement stmt = con.prepareStatement(sqlFuturo)) {
                stmt.setString(1, dataStr);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        BigDecimal movDia = rs.getBigDecimal("total_dia");
                        saldoAcumulado = saldoAcumulado.add(movDia);

                        if (saldoAcumulado.compareTo(rel.menorSaldoProjetado) < 0) {
                            rel.menorSaldoProjetado = saldoAcumulado;
                            rel.dataMenorSaldo = rs.getString("dtVcto");
                        }
                    }
                }
            }

            // Lógica de Alertas e Sugestões com FORMATAÇÃO CORRIGIDA
            if (rel.saldoFinalDia.compareTo(BigDecimal.ZERO) > 0) {
                if (rel.saldoFinalDia.compareTo(rel.reserva) > 0) {
                    rel.sugestaoAplicar = rel.saldoFinalDia.subtract(rel.reserva);
                }

                BigDecimal margemNecessaria = BigDecimal.ZERO;
                if (rel.menorSaldoProjetado.compareTo(rel.saldoFinalDia) < 0) {
                    margemNecessaria = rel.saldoFinalDia.subtract(rel.menorSaldoProjetado);
                }

                BigDecimal totalRetencao = rel.reserva.add(margemNecessaria);
                if (rel.saldoFinalDia.compareTo(totalRetencao) > 0) {
                    rel.sugestaoSeguraAplicar = rel.saldoFinalDia.subtract(totalRetencao);
                }

                if (rel.menorSaldoProjetado.compareTo(BigDecimal.ZERO) < 0) {
                    rel.alerta = "Fluxo futuro indica queda para " + nf.format(rel.menorSaldoProjetado) + " em " + rel.dataMenorSaldo;
                } else if (rel.sugestaoSeguraAplicar.compareTo(BigDecimal.ZERO) == 0) {
                    rel.alerta = "Saldo projetado futuro requer toda a margem.";
                } else {
                    // AQUI ESTAVA O ERRO: Adicionado nf.format()
                    rel.alerta = "Aplicar em poupança: " + nf.format(rel.sugestaoSeguraAplicar);
                }
            } else {
                rel.sugestaoResgate = rel.saldoFinalDia.abs();
                if (rel.sugestaoResgate.compareTo(rel.saldoPoupanca) > 0) {
                    rel.alerta = "SALDO INSUFICIENTE NA POUPANÇA! Sugestão: Tomar Empréstimo.";
                }
            }

        } catch (Exception e) {
            rel.alerta = "Erro ao gerar relatório: " + e.getMessage();
        }

        return rel;
    }
}
