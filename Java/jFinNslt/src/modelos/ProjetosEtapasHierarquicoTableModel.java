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
        Connection conexao = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conexao = new Conexao().getConexao();
            LocalDate dataAtual = LocalDate.now();
            dados.clear();

            // ================== CARREGA PROJETOS ==================
            String sqlProjetos = "SELECT id, nome, data_inicio_prevista, data_fim_prevista, "
                    + "data_inicio_realizada, data_fim_realizada, status FROM Projetos";

            if (filtroStatus != null && !filtroStatus.isEmpty()) {
                sqlProjetos += " WHERE status = ?";
            } else if (projetoId != null) {
                sqlProjetos += " WHERE id = ?";
            }

            pstmt = conexao.prepareStatement(sqlProjetos);
            if (filtroStatus != null && !filtroStatus.isEmpty()) {
                pstmt.setString(1, filtroStatus);
            } else if (projetoId != null) {
                pstmt.setInt(1, projetoId);
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] projeto = new Object[7];
                projeto[0] = rs.getInt("id");
                projeto[1] = rs.getString("nome");
                projeto[2] = rs.getString("data_inicio_prevista");
                projeto[3] = rs.getString("data_fim_prevista");
                projeto[4] = rs.getString("data_inicio_realizada");
                projeto[5] = rs.getString("data_fim_realizada");
                projeto[6] = rs.getString("status");

                // Atualiza status automático do projeto (se atrasado)
                atualizarStatusProjetoSeNecessario(conexao, rs, dataAtual);

                // Atualiza o status no array (caso tenha mudado)
                projeto[6] = rs.getString("status"); // pega novamente do banco após possível update

                dados.add(new ProjetoEtapaHierarquico(projeto));
            }
            rs.close();
            pstmt.close();

            // ================== CARREGA ETAPAS COM VALORES FINANCEIROS ==================
            String sqlEtapas = """
                SELECT 
                    id,
                    projeto_id,
                    nome,
                    prioridade,
                    responsavel,
                    data_inicio_prevista,
                    data_fim_prevista,
                    data_inicio_realizada,
                    data_fim_realizada,
                    status,
                    observ,
                    COALESCE(valor_previsto, 0.00) as valor_previsto,
                    COALESCE(valor_realizado, 0.00) as valor_realizado
                FROM Etapas
                """;

            if (projetoId != null) {
                sqlEtapas += " WHERE projeto_id = ?";
            }

            pstmt = conexao.prepareStatement(sqlEtapas);
            if (projetoId != null) {
                pstmt.setInt(1, projetoId);
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                // Atualiza status automático da etapa
                atualizarStatusEtapaSeNecessario(conexao, rs, dataAtual);

                BigDecimal previsto = rs.getBigDecimal("valor_previsto");
                BigDecimal realizado = rs.getBigDecimal("valor_realizado");

                BigDecimal percentual = previsto.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : realizado.divide(previsto, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .setScale(1, RoundingMode.HALF_UP);

                Object[] etapa = new Object[12];
                etapa[0] = rs.getString("nome");
                etapa[1] = rs.getString("prioridade");
                etapa[2] = rs.getString("responsavel");
                etapa[3] = rs.getString("data_inicio_prevista");
                etapa[4] = rs.getString("data_fim_prevista");
                etapa[5] = rs.getString("data_inicio_realizada");
                etapa[6] = rs.getString("data_fim_realizada");
                etapa[7] = rs.getString("status");
                etapa[8] = rs.getString("observ");
                etapa[9] = previsto;   // R$ Previsto
                etapa[10] = realizado;  // R$ Realizado
                etapa[11] = percentual + "%"; // % Realizado

                // Adiciona na estrutura hierárquica
                for (ProjetoEtapaHierarquico pe : dados) {
                    if ((Integer) pe.getProjeto()[0] == rs.getInt("projeto_id")) {
                        pe.adicionarEtapa(etapa);
                        break;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (conexao != null) {
                    conexao.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    // Métodos auxiliares para atualizar status automático (mantidos do seu código original)
    private void atualizarStatusProjetoSeNecessario(Connection conn, ResultSet rs, LocalDate hoje) throws SQLException {
        String status = rs.getString("status");
        if ("Cancelado".equals(status) || "Concluído".equals(status)) {
            return;
        }

        String inicioPrev = rs.getString("data_inicio_prevista");
        String fimPrev = rs.getString("data_fim_prevista");
        String inicioReal = rs.getString("data_inicio_realizada");
        String fimReal = rs.getString("data_fim_realizada");

        LocalDate dp = null, fp = null, ir = null, fr = null;
        try {
            if (inicioPrev != null && !inicioPrev.isEmpty()) {
                dp = LocalDate.parse(inicioPrev);
            }
            if (fimPrev != null && !fimPrev.isEmpty()) {
                fp = LocalDate.parse(fimPrev);
            }
            if (inicioReal != null && !inicioReal.isEmpty()) {
                ir = LocalDate.parse(inicioReal);
            }
            if (fimReal != null && !fimReal.isEmpty()) {
                fr = LocalDate.parse(fimReal);
            }
        } catch (Exception ignored) {
        }

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

    private void atualizarStatusEtapaSeNecessario(Connection conn, ResultSet rs, LocalDate hoje) throws SQLException {
        String status = rs.getString("status");
        if ("Cancelada".equals(status) || "Concluída".equals(status)) {
            return;
        }

        String inicioPrev = rs.getString("data_inicio_prevista");
        String fimPrev = rs.getString("data_fim_prevista");

        LocalDate ip = null, fp = null;
        try {
            if (inicioPrev != null && !inicioPrev.isEmpty()) {
                ip = LocalDate.parse(inicioPrev);
            }
            if (fimPrev != null && !fimPrev.isEmpty()) {
                fp = LocalDate.parse(fimPrev);
            }
        } catch (Exception ignored) {
        }

        String novoStatus = status;
        if (fp != null && fp.isBefore(hoje)) {
            novoStatus = "Atrasada";
        } else if (ip != null && ip.isBefore(hoje) && rs.getString("data_inicio_realizada") == null) {
            novoStatus = "Atrasada";
        }

        if (!novoStatus.equals(status)) {
            atualizarStatusEtapa(conn, rs.getInt("id"), novoStatus);
        }
    }

    private void atualizarStatusProjeto(Connection conn, int id, String status) throws SQLException {
        try (PreparedStatement p = conn.prepareStatement("UPDATE Projetos SET status = ? WHERE id = ?")) {
            p.setString(1, status);
            p.setInt(2, id);
            p.executeUpdate();
        }
    }

    private void atualizarStatusEtapa(Connection conn, int id, String status) throws SQLException {
        try (PreparedStatement p = conn.prepareStatement("UPDATE Etapas SET status = ? WHERE id = ?")) {
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
        //return colunas.length;
        return 13; // agora temos 13 colunas!
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int contador = 0;

        for (ProjetoEtapaHierarquico pe : dados) {
            // === LINHA DO PROJETO ===
            if (rowIndex == contador++) {
                Object[] p = pe.getProjeto();
                return switch (columnIndex) {
                    case 0 ->
                        p[0] + " - " + p[1];  // ID + Nome
                    case 4 ->
                        p[2];                 // Início Previsto
                    case 5 ->
                        p[3];                 // Fim Previsto
                    case 6 ->
                        p[4];                 // Início Realizado
                    case 7 ->
                        p[5];                 // Fim Realizado
                    case 8 ->
                        p[6];                 // Status
                    default ->
                        "";
                };
            }

            // === LINHAS DAS ETAPAS ===
            for (Object[] etapa : pe.getEtapas()) {
                if (rowIndex == contador++) {
                    // As colunas da etapa começam na coluna 1 (Etapa)
                    // Então: coluna 1 ? etapa[0], coluna 2 ? etapa[1], etc.
                    return switch (columnIndex) {
                        case 0 ->
                            ""; // ID Projeto vazio nas linhas de etapa
                        case 1 ->
                            etapa[0];  // Nome da Etapa
                        case 2 ->
                            etapa[1];  // Prioridade
                        case 3 ->
                            etapa[2];  // Responsável
                        case 4 ->
                            etapa[3];  // Início Previsto
                        case 5 ->
                            etapa[4];  // Fim Previsto
                        case 6 ->
                            etapa[5];  // Início Realizado
                        case 7 ->
                            etapa[6];  // Fim Realizado
                        case 8 ->
                            etapa[7];  // Status
                        case 9 ->
                            etapa[8];  // Observações
                        case 10 -> {
                            BigDecimal v = (BigDecimal) etapa[9];
                            yield String.format("R$ %.2f", v);
                        }
                        case 11 -> {
                            BigDecimal v = (BigDecimal) etapa[10];
                            yield String.format("R$ %.2f", v);
                        }
                        case 12 ->
                            etapa[11]; // % Realizado (já vem como String)
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
        return switch (columnIndex) {
            case 10, 11 ->
                BigDecimal.class;  // R$ Previsto e Realizado
            default ->
                String.class;
        };
    }

    public List<ProjetoEtapaHierarquico> getDados() {
        return dados;
    }
}
