package dao;

import model.Etapa;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import utilitarios.Conexao;

public class EtapaDAO {
    private Conexao conexao;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public EtapaDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    // === CRIAR ETAPA ===
    public void criar(Etapa etapa) throws SQLException {
        String sql = """
            INSERT INTO etapas (
                projeto_id, nome, prioridade, responsavel,
                data_inicio_prevista, data_fim_prevista,
                data_inicio_realizada, data_fim_realizada,
                status, observ,
                valor_previsto, valor_realizado
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = conexao.faz_conexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, etapa.getProjetoId());
            stmt.setString(2, etapa.getNome());
            stmt.setString(3, etapa.getPrioridade());
            stmt.setString(4, etapa.getResponsavel());
            stmt.setString(5, dateToString(etapa.getDataInicioPrevista()));
            stmt.setString(6, dateToString(etapa.getDataFimPrevista()));
            stmt.setString(7, dateToString(etapa.getDataInicioRealizada()));
            stmt.setString(8, dateToString(etapa.getDataFimRealizada()));
            stmt.setString(9, etapa.getStatus());
            stmt.setString(10, etapa.getObs());
            stmt.setBigDecimal(11, etapa.getValorPrevisto() != null ? etapa.getValorPrevisto() : BigDecimal.ZERO);
            stmt.setBigDecimal(12, etapa.getValorRealizado() != null ? etapa.getValorRealizado() : BigDecimal.ZERO);

            stmt.executeUpdate();

            // Opcional: capturar ID gerado (caso precise logo após criar)
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    etapa.setId(rs.getInt(1));
                }
            }
        }
    }

    // === LER UMA ETAPA ===
    public Etapa ler(int id) throws SQLException {
        String sql = """
            SELECT id, projeto_id, nome, prioridade, responsavel,
                   data_inicio_prevista, data_fim_prevista,
                   data_inicio_realizada, data_fim_realizada,
                   status, observ,
                   valor_previsto, valor_realizado
            FROM etapas WHERE id = ?
            """;

        try (Connection conn = conexao.faz_conexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEtapa(rs);
                }
            }
        }
        return null;
    }

    // === ATUALIZAR ETAPA ===
    public void atualizar(Etapa etapa) throws SQLException {
        String sql = """
            UPDATE etapas SET
                projeto_id = ?, nome = ?, prioridade = ?, responsavel = ?,
                data_inicio_prevista = ?, data_fim_prevista = ?,
                data_inicio_realizada = ?, data_fim_realizada = ?,
                status = ?, observ = ?,
                valor_previsto = ?, valor_realizado = ?
            WHERE id = ?
            """;

        try (Connection conn = conexao.faz_conexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, etapa.getProjetoId());
            stmt.setString(2, etapa.getNome());
            stmt.setString(3, etapa.getPrioridade());
            stmt.setString(4, etapa.getResponsavel());
            stmt.setString(5, dateToString(etapa.getDataInicioPrevista()));
            stmt.setString(6, dateToString(etapa.getDataFimPrevista()));
            stmt.setString(7, dateToString(etapa.getDataInicioRealizada()));
            stmt.setString(8, dateToString(etapa.getDataFimRealizada()));
            stmt.setString(9, etapa.getStatus());
            stmt.setString(10, etapa.getObs());
            stmt.setBigDecimal(11, etapa.getValorPrevisto() != null ? etapa.getValorPrevisto() : BigDecimal.ZERO);
            stmt.setBigDecimal(12, etapa.getValorRealizado() != null ? etapa.getValorRealizado() : BigDecimal.ZERO);
            stmt.setInt(13, etapa.getId());

            stmt.executeUpdate();
        }
    }

    // === DELETAR ===
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM etapas WHERE id = ?";
        try (Connection conn = conexao.faz_conexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // === LISTAR POR PROJETO ===
    public List<Etapa> listarPorProjeto(int projetoId) throws SQLException {
        List<Etapa> etapas = new ArrayList<>();
        String sql = "SELECT * FROM etapas WHERE projeto_id = ? ORDER BY data_inicio_prevista";

        try (Connection conn = conexao.faz_conexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projetoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    etapas.add(mapearEtapa(rs));
                }
            }
        }
        return etapas;
    }

    // === LISTAR TODAS AS ETAPAS (de todos os projetos) ===
    public List<Etapa> listarTodos() throws SQLException {
        List<Etapa> etapas = new ArrayList<>();
        String sql = "SELECT * FROM etapas ORDER BY projeto_id, data_inicio_prevista";

        try (Connection conn = conexao.faz_conexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                etapas.add(mapearEtapa(rs));
            }
        }
        return etapas;
    }

    // === MAPEAR RESULTSET PARA OBJETO ETAPA ===
    private Etapa mapearEtapa(ResultSet rs) throws SQLException {
        Etapa etapa = new Etapa();
        etapa.setId(rs.getInt("id"));
        etapa.setProjetoId(rs.getInt("projeto_id"));
        etapa.setNome(rs.getString("nome"));
        etapa.setPrioridade(rs.getString("prioridade"));
        etapa.setResponsavel(rs.getString("responsavel"));
        etapa.setStatus(rs.getString("status"));
        etapa.setObs(rs.getString("observ")); // <- coluna no MariaDB é "observ"

        // Valores financeiros
        BigDecimal previsto = rs.getBigDecimal("valor_previsto");
        etapa.setValorPrevisto(previsto != null ? previsto : BigDecimal.ZERO);
        BigDecimal realizado = rs.getBigDecimal("valor_realizado");
        etapa.setValorRealizado(realizado != null ? realizado : BigDecimal.ZERO);

        // Datas (compatível com DATE ou DATETIME)
        String s;
        s = rs.getString("data_inicio_prevista");
        if (s != null && s.length() >= 10) {
            etapa.setDataInicioPrevista(LocalDate.parse(s.substring(0, 10), DATE_FMT));
        }
        s = rs.getString("data_fim_prevista");
        if (s != null && s.length() >= 10) {
            etapa.setDataFimPrevista(LocalDate.parse(s.substring(0, 10), DATE_FMT));
        }
        s = rs.getString("data_inicio_realizada");
        if (s != null && s.length() >= 10) {
            etapa.setDataInicioRealizada(LocalDate.parse(s.substring(0, 10), DATE_FMT));
        }
        s = rs.getString("data_fim_realizada");
        if (s != null && s.length() >= 10) {
            etapa.setDataFimRealizada(LocalDate.parse(s.substring(0, 10), DATE_FMT));
        }

        return etapa;
    }

    // === MÉTODO AUXILIAR PARA DATAS ===
    private String dateToString(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : null;
    }
}