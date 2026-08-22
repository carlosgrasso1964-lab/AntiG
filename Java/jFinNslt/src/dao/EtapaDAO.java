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

    public EtapaDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    // === CRIAR ETAPA (com valores financeiros) ===
    public void criar(Etapa etapa) throws SQLException {
        String sql = """
            INSERT INTO Etapas (
                projeto_id, nome, prioridade, responsavel,
                data_inicio_prevista, data_fim_prevista,
                data_inicio_realizada, data_fim_realizada,
                status, observ,
                valor_previsto, valor_realizado
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = conexao.getConexao(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, etapa.getProjetoId());
            stmt.setString(2, etapa.getNome());
            stmt.setString(3, etapa.getPrioridade());
            stmt.setString(4, etapa.getResponsavel());

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            stmt.setString(5, dateToString(etapa.getDataInicioPrevista(), fmt));
            stmt.setString(6, dateToString(etapa.getDataFimPrevista(), fmt));
            stmt.setString(7, dateToString(etapa.getDataInicioRealizada(), fmt));
            stmt.setString(8, dateToString(etapa.getDataFimRealizada(), fmt));

            stmt.setString(9, etapa.getStatus());
            stmt.setString(10, etapa.getObs());

            // Campos financeiros
            stmt.setBigDecimal(11, etapa.getValorPrevisto() != null ? etapa.getValorPrevisto() : BigDecimal.ZERO);
            stmt.setBigDecimal(12, etapa.getValorRealizado() != null ? etapa.getValorRealizado() : BigDecimal.ZERO);

            stmt.executeUpdate();
        }
    }

    // === LER UMA ETAPA ===
    public Etapa ler(int id) throws SQLException {
        String sql = """
            SELECT 
                id, projeto_id, nome, prioridade, responsavel,
                data_inicio_prevista, data_fim_prevista,
                data_inicio_realizada, data_fim_realizada,
                status, observ,
                valor_previsto, valor_realizado
            FROM Etapas WHERE id = ?
            """;

        try (Connection conn = conexao.getConexao(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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
            UPDATE Etapas SET
                projeto_id = ?, nome = ?, prioridade = ?, responsavel = ?,
                data_inicio_prevista = ?, data_fim_prevista = ?,
                data_inicio_realizada = ?, data_fim_realizada = ?,
                status = ?, observ = ?,
                valor_previsto = ?, valor_realizado = ?
            WHERE id = ?
            """;

        try (Connection conn = conexao.getConexao(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, etapa.getProjetoId());
            stmt.setString(2, etapa.getNome());
            stmt.setString(3, etapa.getPrioridade());
            stmt.setString(4, etapa.getResponsavel());

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            stmt.setString(5, dateToString(etapa.getDataInicioPrevista(), fmt));
            stmt.setString(6, dateToString(etapa.getDataFimPrevista(), fmt));
            stmt.setString(7, dateToString(etapa.getDataInicioRealizada(), fmt));
            stmt.setString(8, dateToString(etapa.getDataFimRealizada(), fmt));

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
        String sql = "DELETE FROM Etapas WHERE id = ?";
        try (Connection conn = conexao.getConexao(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // === LISTAR TODAS (opcional, mas útil) ===
    public List<Etapa> listarPorProjeto(int projetoId) throws SQLException {
        List<Etapa> etapas = new ArrayList<>();
        String sql = """
            SELECT * FROM Etapas WHERE projeto_id = ? ORDER BY data_inicio_prevista
            """;

        try (Connection conn = conexao.getConexao(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projetoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    etapas.add(mapearEtapa(rs));
                }
            }
        }
        return etapas;
    }

    private Etapa mapearEtapa(ResultSet rs) throws SQLException {
        Etapa etapa = new Etapa();
        etapa.setId(rs.getInt("id"));
        etapa.setProjetoId(rs.getInt("projeto_id"));
        etapa.setNome(rs.getString("nome"));
        etapa.setPrioridade(rs.getString("prioridade"));
        etapa.setResponsavel(rs.getString("responsavel"));
        etapa.setStatus(rs.getString("status"));
        etapa.setObs(rs.getString("observ"));

        // Valores financeiros
        BigDecimal previsto = rs.getBigDecimal("valor_previsto");
        etapa.setValorPrevisto(previsto != null ? previsto : BigDecimal.ZERO);

        BigDecimal realizado = rs.getBigDecimal("valor_realizado");
        etapa.setValorRealizado(realizado != null ? realizado : BigDecimal.ZERO);

        // Datas
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String s;

        s = rs.getString("data_inicio_prevista");
        if (s != null) {
            etapa.setDataInicioPrevista(LocalDate.parse(s, fmt));
        }
        s = rs.getString("data_fim_prevista");
        if (s != null) {
            etapa.setDataFimPrevista(LocalDate.parse(s, fmt));
        }
        s = rs.getString("data_inicio_realizada");
        if (s != null) {
            etapa.setDataInicioRealizada(LocalDate.parse(s, fmt));
        }
        s = rs.getString("data_fim_realizada");
        if (s != null) {
            etapa.setDataFimRealizada(LocalDate.parse(s, fmt));
        }

        return etapa;
    }

    // === MÉTODO AUXILIAR: Converte LocalDate ? String ou null ===
    private String dateToString(LocalDate date, DateTimeFormatter formatter) {
        return date != null ? date.format(formatter) : null;
    }

    // === MÉTODO AUXILIAR: Mapeia ResultSet ? Objeto Etapa ===
//    private Etapa mapearEtapa(ResultSet rs) throws SQLException {
//        Etapa etapa = new Etapa();
//
//        etapa.setId(rs.getInt("id"));
//        etapa.setProjetoId(rs.getInt("projeto_id"));
//        etapa.setNome(rs.getString("nome"));
//        etapa.setPrioridade(rs.getString("prioridade"));
//        etapa.setResponsavel(rs.getString("responsavel"));
//        etapa.setStatus(rs.getString("status"));
//        etapa.setObs(rs.getString("observ"));
//
//        // Valores financeiros
//        BigDecimal previsto = rs.getBigDecimal("valor_previsto");
//        etapa.setValorPrevisto(previsto != null ? previsto : BigDecimal.ZERO);
//
//        BigDecimal realizado = rs.getBigDecimal("valor_realizado");
//        etapa.setValorRealizado(realizado != null ? realizado : BigDecimal.ZERO);
//
//        // Datas
//        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        String str;
//
//        str = rs.getString("data_inicio_prevista");
//        if (str != null) {
//            etapa.setDataInicioPrevista(LocalDate.parse(str, fmt));
//        }
//        str = rs.getString("data_fim_prevista");
//        if (str != null) {
//            etapa.setDataFimPrevista(LocalDate.parse(str, fmt));
//        }
//        str = rs.getString("data_inicio_realizada");
//        if (str != null) {
//            etapa.setDataInicioRealizada(LocalDate.parse(str, fmt));
//        }
//        str = rs.getString("data_fim_realizada");
//        if (str != null) {
//            etapa.setDataFimRealizada(LocalDate.parse(str, fmt));
//        }
//
//        return etapa;
//    }
    // === LISTAR TODAS AS ETAPAS (de todos os projetos) ===
    // Esse método é usado pelo Form_Etapas para mostrar tudo na tabela
    public List<Etapa> listarTodos() throws SQLException {
        List<Etapa> etapas = new ArrayList<>();
        String sql = """
        SELECT * FROM Etapas ORDER BY projeto_id, data_inicio_prevista
        """;

        try (Connection conn = conexao.getConexao(); Statement stmt = conn.createStatement(); // <-- Statement normal
                 ResultSet rs = stmt.executeQuery(sql)) {                    // <-- executeQuery direto

            while (rs.next()) {
                etapas.add(mapearEtapa(rs));
            }
        }
        return etapas;
    }
}
