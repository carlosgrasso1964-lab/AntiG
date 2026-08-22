package modelos;

import utilitarios.Conexao;
import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProjetosEtapasHierarquicoTableModel extends AbstractTableModel {

    private final List<ProjetoEtapaHierarquico> dados = new ArrayList<>();
    private final DateTimeFormatter fmtBR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String[] colunas = {
        "ID Projeto", "Etapa", "Prioridade", "Responsável",
        "Início Previsto", "Fim Previsto", "Início Realizado", "Fim Realizado",
        "Status", "Observações", "R$ Previsto", "R$ Realizado", "% Executado"
    };

    public ProjetosEtapasHierarquicoTableModel(String filtroStatus) {
        carregarDados(filtroStatus, null);
    }

    public ProjetosEtapasHierarquicoTableModel(int projetoId) {
        carregarDados(null, projetoId);
    }

    private void carregarDados(String filtroStatus, Integer projetoId) {
        dados.clear();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = Conexao.faz_conexao();
            LocalDate hoje = LocalDate.now();

            // =============== CARREGA PROJETOS ===============
            String sqlProjetos = "SELECT id, nome, data_inicio_prevista, data_fim_prevista, "
                               + "data_inicio_realizada, data_fim_realizada, status, "
                               + "valor_previsto_total, valor_realizado_total FROM projetos";

            if (filtroStatus != null && !filtroStatus.isEmpty()) {
                sqlProjetos += " WHERE status = ?";
            } else if (projetoId != null) {
                sqlProjetos += " WHERE id = ?";
            }
            sqlProjetos += " ORDER BY id";

            pstmt = con.prepareStatement(sqlProjetos);
            if (filtroStatus != null && !filtroStatus.isEmpty()) {
                pstmt.setString(1, filtroStatus);
            } else if (projetoId != null) {
                pstmt.setInt(1, projetoId);
            }

            rs = pstmt.executeQuery();
            while (rs.next()) {
                int idProj = rs.getInt("id");
                String nomeProj = rs.getString("nome");
                String statusProj = rs.getString("status");
                BigDecimal valorPrevTotal = rs.getBigDecimal("valor_previsto_total");
                BigDecimal valorRealTotal = rs.getBigDecimal("valor_realizado_total");

                LocalDate inicioPrev = toLocalDate(rs.getDate("data_inicio_prevista"));
                LocalDate fimPrev = toLocalDate(rs.getDate("data_fim_prevista"));
                LocalDate inicioReal = toLocalDate(rs.getDate("data_inicio_realizada"));
                LocalDate fimReal = toLocalDate(rs.getDate("data_fim_realizada"));

                // Atualização automática de status do projeto
                atualizarStatusProjetoSeNecessario(con, rs, idProj, statusProj, inicioPrev, fimPrev, inicioReal, fimReal, hoje);

                Object[] projeto = {
                    idProj,
                    nomeProj,
                    formatarData(inicioPrev),
                    formatarData(fimPrev),
                    formatarData(inicioReal),
                    formatarData(fimReal),
                    statusProj,
                    "", // Observações (não tem no projeto)
                    valorPrevTotal != null ? valorPrevTotal : BigDecimal.ZERO,
                    valorRealTotal != null ? valorRealTotal : BigDecimal.ZERO,
                    calcularPercentual(valorPrevTotal, valorRealTotal)
                };
                dados.add(new ProjetoEtapaHierarquico(projeto));
            }
            rs.close();
             pstmt.close();

            // =============== CARREGA ETAPAS ===============
            String sqlEtapas = "SELECT id, projeto_id, nome, prioridade, responsavel, "
                             + "data_inicio_prevista, data_fim_prevista, data_inicio_realizada, "
                             + "data_fim_realizada, status, observ, valor_previsto, valor_realizado "
                             + "FROM etapas";

            List<Object> params = new ArrayList<>();
            if (projetoId != null) {
                sqlEtapas += " WHERE projeto_id = ?";
                params.add(projetoId);
            }
            sqlEtapas += " ORDER BY projeto_id, id";

            pstmt = con.prepareStatement(sqlEtapas);
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            rs = pstmt.executeQuery();
            while (rs.next()) {
                int projIdEtapa = rs.getInt("projeto_id");
                String nomeEtapa = rs.getString("nome");
                String prioridade = rs.getString("prioridade");
                String responsavel = rs.getString("responsavel");
                String statusEtapa = rs.getString("status");
                String observ = rs.getString("observ");
                BigDecimal valorPrev = rs.getBigDecimal("valor_previsto");
                BigDecimal valorReal = rs.getBigDecimal("valor_realizado");

                LocalDate inicioPrevE = toLocalDate(rs.getDate("data_inicio_prevista"));
                LocalDate fimPrevE = toLocalDate(rs.getDate("data_fim_prevista"));
                LocalDate inicioRealE = toLocalDate(rs.getDate("data_inicio_realizada"));
                LocalDate fimRealE = toLocalDate(rs.getDate("data_fim_realizada"));

                // Atualização automática de status da etapa
                atualizarStatusEtapaSeNecessario(con, rs, statusEtapa, inicioPrevE, fimPrevE, inicioRealE, hoje);

                Object[] etapa = {
                    nomeEtapa,
                    prioridade != null ? prioridade : "",
                    responsavel != null ? responsavel : "",
                    formatarData(inicioPrevE),
                    formatarData(fimPrevE),
                    formatarData(inicioRealE),
                    formatarData(fimRealE),
                    statusEtapa,
                    observ != null ? observ : "",
                    valorPrev != null ? valorPrev : BigDecimal.ZERO,
                    valorReal != null ? valorReal : BigDecimal.ZERO,
                    calcularPercentual(valorPrev, valorReal)
                };

                // Adiciona na hierarquia
                for (ProjetoEtapaHierarquico p : dados) {
                    if ((int) p.getProjeto()[0] == projIdEtapa) {
                        p.adicionarEtapa(etapa);
                        break;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception ignored) {}
            try { if (con != null) con.close(); } catch (Exception ignored) {}
        }
    }

    // === MÉTODOS AUXILIARES ===
    private LocalDate toLocalDate(java.sql.Date date) {
        return date != null ? date.toLocalDate() : null;
    }

    private String formatarData(LocalDate date) {
        return date != null ? date.format(fmtBR) : "";
    }

    private String calcularPercentual(BigDecimal previsto, BigDecimal realizado) {
        if (previsto == null || realizado == null || previsto.compareTo(BigDecimal.ZERO) == 0) {
            return "0,0%";
        }
        BigDecimal perc = realizado.divide(previsto, 4, RoundingMode.HALF_UP)
                                   .multiply(BigDecimal.valueOf(100))
                                   .setScale(1, RoundingMode.HALF_UP);
        return perc.toString().replace(".", ",") + "%";
    }

    private void atualizarStatusProjetoSeNecessario(Connection con, ResultSet rs, int id, String statusAtual,
                                                    LocalDate inicioPrev, LocalDate fimPrev, LocalDate inicioReal,
                                                    LocalDate fimReal, LocalDate hoje) throws SQLException {
        if ("Cancelado".equals(statusAtual) || "Concluído".equals(statusAtual)) return;

        String novoStatus = statusAtual;
        if (fimPrev != null && fimPrev.isBefore(hoje) && (fimReal == null || fimReal.isAfter(fimPrev))) {
            novoStatus = "Atrasado";
        } else if (inicioPrev != null && inicioPrev.isBefore(hoje) && inicioReal == null) {
            novoStatus = "Atrasado";
        }

        if (!novoStatus.equals(statusAtual)) {
            atualizarStatusProjeto(con, id, novoStatus);
        }
    }

    private void atualizarStatusEtapaSeNecessario(Connection con, ResultSet rs, String statusAtual,
                                                  LocalDate inicioPrev, LocalDate fimPrev, LocalDate inicioReal,
                                                  LocalDate hoje) throws SQLException {
        if ("Cancelada".equals(statusAtual) || "Concluída".equals(statusAtual)) return;

        String novoStatus = statusAtual;
        if (fimPrev != null && fimPrev.isBefore(hoje)) {
            novoStatus = "Atrasada";
        } else if (inicioPrev != null && inicioPrev.isBefore(hoje) && inicioReal == null) {
            novoStatus = "Atrasada";
        }

        if (!novoStatus.equals(statusAtual)) {
            atualizarStatusEtapa(con, rs.getInt("id"), novoStatus);
        }
    }

    private void atualizarStatusProjeto(Connection con, int id, String status) throws SQLException {
        try (PreparedStatement p = con.prepareStatement("UPDATE projetos SET status = ? WHERE id = ?")) {
            p.setString(1, status);
            p.setInt(2, id);
            p.executeUpdate();
        }
    }

    private void atualizarStatusEtapa(Connection con, int id, String status) throws SQLException {
        try (PreparedStatement p = con.prepareStatement("UPDATE etapas SET status = ? WHERE id = ?")) {
            p.setString(1, status);
            p.setInt(2, id);
            p.executeUpdate();
        }
    }

    // === MÉTODOS DA TABELA ===
    @Override
    public int getRowCount() {
        int total = 0;
        for (ProjetoEtapaHierarquico p : dados) {
            total += 1 + p.getEtapas().size();
        }
        return total;
    }

    @Override
    public int getColumnCount() {
        return colunas.length; // 13 colunas
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int contador = 0;
        for (ProjetoEtapaHierarquico p : dados) {
            // Linha do projeto
            if (rowIndex == contador++) {
                Object[] proj = p.getProjeto();
                return switch (columnIndex) {
                    case 0 -> proj[0] + " - " + proj[1];
                    case 1 -> ""; // Etapa vazia
                    case 2 -> ""; // Prioridade vazia
                    case 3 -> ""; // Responsável vazio
                    case 4 -> proj[2]; // Início Previsto
                    case 5 -> proj[3]; // Fim Previsto
                    case 6 -> proj[4]; // Início Realizado
                    case 7 -> proj[5]; // Fim Realizado
                    case 8 -> proj[6]; // Status
                    case 9 -> proj[7]; // Observações
                    case 10 -> "R$ " + proj[8];
                    case 11 -> "R$ " + proj[9];
                    case 12 -> proj[10];
                    default -> "";
                };
            }
            // Linhas das etapas
            for (Object[] etapa : p.getEtapas()) {
                if (rowIndex == contador++) {
                    return switch (columnIndex) {
                        case 0 -> "";
                        case 1 -> etapa[0]; // Nome da etapa
                        case 2 -> etapa[1];
                        case 3 -> etapa[2];
                        case 4 -> etapa[3];
                        case 5 -> etapa[4];
                        case 6 -> etapa[5];
                        case 7 -> etapa[6];
                        case 8 -> etapa[7];
                        case 9 -> etapa[8];
                        case 10 -> "R$ " + etapa[9];
                        case 11 -> "R$ " + etapa[10];
                        case 12 -> etapa[11];
                        default -> "";
                    };
                }
            }
        }
        return "";
    }

    @Override
    public String getColumnName(int column) {
        return colunas[column];
    }

    public List<ProjetoEtapaHierarquico> getDados() {
        return dados;
    }
}