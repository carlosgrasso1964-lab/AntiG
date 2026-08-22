package modelos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import utilitarios.Conexao;
import javax.swing.table.AbstractTableModel;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProjetosEtapasHierarquicoTableModel extends AbstractTableModel {

    private List<ProjetoEtapaHierarquico> dados = new ArrayList<>();
    private String[] colunas = {
        "ID Projeto", "Etapa", "Prioridade", "Responsável",
        "Início Previsto", "Fim Previsto", "Início Realizado", "Fim Realizado",
        "Status", "Observações", "R$ Previsto", "R$ Realizado", "% Realizado"
    };

    public ProjetosEtapasHierarquicoTableModel(String filtroStatus) {
        carregarDados(filtroStatus, null);
    }

    public ProjetosEtapasHierarquicoTableModel(int projetoId) {
        carregarDados(null, projetoId);
    }

    private void carregarDados(String filtroStatus, Integer projetoId) {
        dados.clear();
        LocalDate dataAtual = LocalDate.now();

        // ================== CARREGA PROJETOS ==================
        String sqlProjetos = "SELECT id, nome, data_inicio_prevista, data_fim_prevista, "
                + "data_inicio_realizada, data_fim_realizada, status FROM projetos";
        if (filtroStatus != null && !filtroStatus.isEmpty()) {
            sqlProjetos += " WHERE status = ?";
        } else if (projetoId != null) {
            sqlProjetos += " WHERE id = ?";
        }

        try (Connection conn = Conexao.faz_conexao(); PreparedStatement pstmt = conn.prepareStatement(sqlProjetos)) {

            if (filtroStatus != null && !filtroStatus.isEmpty()) {
                pstmt.setString(1, filtroStatus);
            } else if (projetoId != null) {
                pstmt.setInt(1, projetoId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    atualizarStatusProjetoSeNecessario(conn, rs, dataAtual);

                    Object[] projeto = new Object[7];
                    projeto[0] = rs.getInt("id");
                    projeto[1] = rs.getString("nome");
                    projeto[2] = formatarData(rs.getString("data_inicio_prevista"));
                    projeto[3] = formatarData(rs.getString("data_fim_prevista"));
                    projeto[4] = formatarData(rs.getString("data_inicio_realizada"));
                    projeto[5] = formatarData(rs.getString("data_fim_realizada"));
                    projeto[6] = rs.getString("status");

                    dados.add(new ProjetoEtapaHierarquico(projeto));
                }
            }

            // ================== CARREGA ETAPAS ==================
            String sqlEtapas = """
                SELECT id, projeto_id, nome, prioridade, responsavel,
                       data_inicio_prevista, data_fim_prevista,
                       data_inicio_realizada, data_fim_realizada,
                       status, observ,
                       COALESCE(valor_previsto, 0.00) AS valor_previsto,
                       COALESCE(valor_realizado, 0.00) AS valor_realizado
                FROM etapas
                """;
            if (projetoId != null) {
                sqlEtapas += " WHERE projeto_id = ?";
            }

            try (PreparedStatement pstmtEtapas = conn.prepareStatement(sqlEtapas)) {
                if (projetoId != null) {
                    pstmtEtapas.setInt(1, projetoId);
                }

                try (ResultSet rs = pstmtEtapas.executeQuery()) {
                    while (rs.next()) {
                        atualizarStatusEtapaSeNecessario(conn, rs, dataAtual);

                        BigDecimal previsto = rs.getBigDecimal("valor_previsto");
                        BigDecimal realizado = rs.getBigDecimal("valor_realizado");
                        String percentual = "0,0%";
                        if (previsto != null && previsto.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal perc = realizado.divide(previsto, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .setScale(1, RoundingMode.HALF_UP);
                            percentual = perc.toString().replace(".", ",") + "%";
                        }

                        Object[] etapa = new Object[12];
                        etapa[0] = rs.getString("nome");
                        etapa[1] = rs.getString("prioridade");
                        etapa[2] = rs.getString("responsavel");
                        etapa[3] = formatarData(rs.getString("data_inicio_prevista"));
                        etapa[4] = formatarData(rs.getString("data_fim_prevista"));
                        etapa[5] = formatarData(rs.getString("data_inicio_realizada"));
                        etapa[6] = formatarData(rs.getString("data_fim_realizada"));
                        etapa[7] = rs.getString("status");
                        etapa[8] = rs.getString("observ");
                        etapa[9] = previsto;
                        etapa[10] = realizado;
                        etapa[11] = percentual;

                        // Adiciona na hierarquia correta
                        for (ProjetoEtapaHierarquico pe : dados) {
                            if (pe.getProjeto()[0].equals(rs.getInt("projeto_id"))) {
                                pe.adicionarEtapa(etapa);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // === MÉTODO AUXILIAR PARA FORMATAR DATA (compatível com DATE ou DATETIME) ===
    private String formatarData(String dataStr) {
        if (dataStr == null || dataStr.trim().isEmpty()) {
            return "";
        }
        // Se for DATETIME, corta a hora
        if (dataStr.length() > 10) {
            dataStr = dataStr.substring(0, 10);
        }
        // Converte yyyy-MM-dd ? dd/MM/yyyy para exibição
        try {
            LocalDate date = LocalDate.parse(dataStr);
            return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return dataStr; // retorna como está se falhar
        }
    }

    // === ATUALIZAR STATUS PROJETO ===
    private void atualizarStatusProjetoSeNecessario(Connection conn, ResultSet rs, LocalDate hoje) throws SQLException {
        String status = rs.getString("status");
        if ("Cancelado".equals(status) || "Concluído".equals(status)) {
            return;
        }

        LocalDate dp = parseLocalDate(rs.getString("data_inicio_prevista"));
        LocalDate fp = parseLocalDate(rs.getString("data_fim_prevista"));
        LocalDate ir = parseLocalDate(rs.getString("data_inicio_realizada"));
        LocalDate fr = parseLocalDate(rs.getString("data_fim_realizada"));

        String novoStatus = status;
        if (fp != null && fp.isBefore(hoje) && (fr == null || fr.isAfter(fp))) {
            novoStatus = "Atrasado";
        } else if (dp != null && dp.isBefore(hoje) && ir == null) {
            novoStatus = "Atrasado";
        }

        if (!novoStatus.equals(status)) {
            atualizarStatusProjeto(conn, rs.getInt("id"), novoStatus);
        }
    }

    // === ATUALIZAR STATUS ETAPA ===
    private void atualizarStatusEtapaSeNecessario(Connection conn, ResultSet rs, LocalDate hoje) throws SQLException {
        String status = rs.getString("status");
        if ("Cancelada".equals(status) || "Concluída".equals(status)) {
            return;
        }

        LocalDate ip = parseLocalDate(rs.getString("data_inicio_prevista"));
        LocalDate fp = parseLocalDate(rs.getString("data_fim_prevista"));
        LocalDate ir = parseLocalDate(rs.getString("data_inicio_realizada"));

        String novoStatus = status;
        if (fp != null && fp.isBefore(hoje)) {
            novoStatus = "Atrasada";
        } else if (ip != null && ip.isBefore(hoje) && ir == null) {
            novoStatus = "Atrasada";
        }

        if (!novoStatus.equals(status)) {
            atualizarStatusEtapa(conn, rs.getInt("id"), novoStatus);
        }
    }

    // === MÉTODOS AUXILIARES DE PARSING E UPDATE ===
    private LocalDate parseLocalDate(String dataStr) {
        if (dataStr == null || dataStr.trim().isEmpty()) {
            return null;
        }
        if (dataStr.length() > 10) {
            dataStr = dataStr.substring(0, 10);
        }
        try {
            return LocalDate.parse(dataStr);
        } catch (Exception e) {
            return null;
        }
    }

    private void atualizarStatusProjeto(Connection conn, int id, String status) throws SQLException {
        try (PreparedStatement p = conn.prepareStatement("UPDATE projetos SET status = ? WHERE id = ?")) {
            p.setString(1, status);
            p.setInt(2, id);
            p.executeUpdate();
        }
    }

    private void atualizarStatusEtapa(Connection conn, int id, String status) throws SQLException {
        try (PreparedStatement p = conn.prepareStatement("UPDATE etapas SET status = ? WHERE id = ?")) {
            p.setString(1, status);
            p.setInt(2, id);
            p.executeUpdate();
        }
    }

    // ================== MÉTODOS DA TABELA ==================
    @Override
    public int getRowCount() {
        int total = 0;
        for (ProjetoEtapaHierarquico pe : dados) {
            total += 1 + pe.getEtapas().size();
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
        for (ProjetoEtapaHierarquico pe : dados) {
            // Linha do Projeto
            if (rowIndex == contador++) {
                Object[] p = pe.getProjeto();
                return switch (columnIndex) {
                    case 0 ->
                        p[0] + " - " + p[1];
                    case 4 ->
                        p[2];
                    case 5 ->
                        p[3];
                    case 6 ->
                        p[4];
                    case 7 ->
                        p[5];
                    case 8 ->
                        p[6];
                    default ->
                        "";
                };
            }
            // Linhas das Etapas
            for (Object[] etapa : pe.getEtapas()) {
                if (rowIndex == contador++) {
                    return switch (columnIndex) {
                        case 0 ->
                            "";
                        case 1 ->
                            etapa[0];
                        case 2 ->
                            etapa[1];
                        case 3 ->
                            etapa[2];
                        case 4 ->
                            etapa[3];
                        case 5 ->
                            etapa[4];
                        case 6 ->
                            etapa[5];
                        case 7 ->
                            etapa[6];
                        case 8 ->
                            etapa[7];
                        case 9 ->
                            etapa[8];
                        case 10 ->
                            String.format("R$ %.2f", (BigDecimal) etapa[9]);
                        case 11 ->
                            String.format("R$ %.2f", (BigDecimal) etapa[10]);
                        case 12 ->
                            etapa[11];
                        default ->
                            "";
                    };
                }
            }
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        return colunas[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex >= 10 && columnIndex <= 11 ? BigDecimal.class : String.class;
    }

    public List<ProjetoEtapaHierarquico> getDados() {
        return dados;
    }
}
