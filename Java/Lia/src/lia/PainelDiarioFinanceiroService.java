package lia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PainelDiarioFinanceiroService {

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
        public BigDecimal reserva = new BigDecimal("0.00"); //Aqui defino o valor para reserva
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
        // Atribui o valor recebido à reserva do relatório (igual ao PainelDiarioController do jFinNslt)
        if (valorReserva != null) {
            rel.reserva = valorReserva;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dataStr = sdf.format(data);

        try (Connection con = ConexaoFinanceira.conectar()) {
            if (con == null) {
                rel.alerta = "Erro ao conectar no banco de dados";
                return rel;
            }

            String sqlSaldo = "SELECT SUM(saldos) FROM vsaldos WHERE conta IN ('Bancos', 'Caixa')";
            try (PreparedStatement stmt = con.prepareStatement(sqlSaldo); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    rel.saldoInicial = rs.getBigDecimal(1);
                    if (rel.saldoInicial == null) rel.saldoInicial = BigDecimal.ZERO;
                }
            }

            String sqlEntradas = "SELECT Descr, Valor FROM view_movimento WHERE dtVcto <= ? AND nome_C = 'Contas à Receber' AND (dtApr IS NULL OR dtApr = '')";
            try (PreparedStatement stmt = con.prepareStatement(sqlEntradas)) {
                stmt.setString(1, dataStr);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        rel.entradas.add(new MovimentoSimples(rs.getString("Descr"), rs.getBigDecimal("Valor")));
                    }
                }
            }

            //String sqlSaidas = "SELECT Descr, Valor FROM view_movimento WHERE dtVcto = ? AND nome_C = 'Contas à Pagar' AND (dtApr IS NULL OR dtApr = '')";
            String sqlSaidas = """
                SELECT Descr, Valor 
                FROM view_movimento 
                WHERE dtVcto <= ? 
                  AND (
                      (nome_C = 'Contas à Pagar' AND (dtApr IS NULL OR dtApr = ''))
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

            String sqlPoup = "SELECT SUM(saldos) FROM vsaldos WHERE conta = 'Conta Poupança'";
            try (PreparedStatement stmt = con.prepareStatement(sqlPoup); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    rel.saldoPoupanca = rs.getBigDecimal(1);
                    if (rel.saldoPoupanca == null) rel.saldoPoupanca = BigDecimal.ZERO;
                }
            }

            rel.saldoFinalDia = rel.saldoInicial.add(rel.totalEntradas()).add(rel.totalSaidas());

            BigDecimal saldoAcumulado = rel.saldoFinalDia;
            rel.menorSaldoProjetado = saldoAcumulado;
            rel.dataMenorSaldo = dataStr;

            String sqlFuturo = "SELECT dtVcto, SUM(Valor) as total_dia FROM view_movimento "
                    + "WHERE dtVcto > ? AND (dtApr IS NULL OR dtApr = '') "
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
                } else {
                    rel.sugestaoSeguraAplicar = BigDecimal.ZERO;
                }
                
                if (rel.menorSaldoProjetado.compareTo(BigDecimal.ZERO) < 0) {
                    rel.alerta = "Fluxo futuro indica queda para " + rel.menorSaldoProjetado + " em " + rel.dataMenorSaldo;
                } else if (rel.sugestaoSeguraAplicar.compareTo(BigDecimal.ZERO) == 0) {
                    rel.alerta = "Saldo projeto futuro requer toda a margem.";
                } else {
                    rel.alerta = "Aplicar em poupança: " + rel.sugestaoSeguraAplicar;
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

    public static String gerarRelatorioTexto(Date data, BigDecimal valorReserva) {
        RelatorioPainel rel = gerarRelatorio(data, valorReserva);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder();

        @SuppressWarnings("deprecation")
        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"));

        sb.append("📊 *Painel Diário - ").append(sdf.format(data)).append("*\n\n");
        sb.append("💰 *Saldos e Totais*\n");
        sb.append("Saldo Inicial: ").append(nf.format(rel.saldoInicial)).append("\n");
        sb.append("Total Entradas: ").append(nf.format(rel.totalEntradas())).append("\n");
        sb.append("Total Saídas: ").append(nf.format(rel.totalSaidas().negate())).append("\n");
        sb.append("Saldo Final do Dia: ").append(nf.format(rel.saldoFinalDia)).append("\n");
        sb.append("Saldo Poupança: ").append(nf.format(rel.saldoPoupanca)).append("\n\n");

        sb.append("💡 *Sugestões*\n");
        sb.append("Sugestão Aplicar: ").append(nf.format(rel.sugestaoAplicar)).append("\n");
        sb.append("Sugestão Segura: ").append(nf.format(rel.sugestaoSeguraAplicar)).append("\n");

        if (rel.alerta != null && !rel.alerta.isEmpty()) {
            sb.append("\n⚠️ *Alerta*: ").append(rel.alerta).append("\n");
        } else {
            sb.append("\n✅ *Alerta*: Nenhum\n");
        }

        if (!rel.entradas.isEmpty()) {
            sb.append("\n📈 *Entradas do Dia*\n");
            for (MovimentoSimples m : rel.entradas) {
                sb.append("- ").append(m.descricao).append(": ").append(nf.format(m.valor)).append("\n");
            }
        }

        if (!rel.saidas.isEmpty()) {
            sb.append("\n📉 *Saídas do Dia*\n");
            for (MovimentoSimples m : rel.saidas) {
                sb.append("- ").append(m.descricao).append(": ").append(nf.format(m.valor)).append("\n");
            }
        }

        return sb.toString();
    }
}