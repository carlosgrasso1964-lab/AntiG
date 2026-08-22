package dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.Projeto;
import utilitarios.Conexao;

public class ProjetoDAO {

    private Conexao conexao;

    public ProjetoDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    // === CRIAR (sem alterações financeiras) ===
    public void criar(Projeto projeto) throws SQLException {
        String sql = "INSERT INTO Projetos (nome, data_inicio_prevista, data_fim_prevista, " +
                     "data_inicio_realizada, data_fim_realizada, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, projeto.getNome());
            stmt.setString(2, dateToString(projeto.getDataInicioPrevista()));
            stmt.setString(3, dateToString(projeto.getDataFimPrevista()));
            stmt.setString(4, dateToString(projeto.getDataInicioRealizada()));
            stmt.setString(5, dateToString(projeto.getDataFimRealizada()));
            stmt.setString(6, projeto.getStatus());

            stmt.executeUpdate();

            // Pega o ID gerado
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    projeto.setId(rs.getInt(1));
                }
            }
        }
    }

    // === LER COM TOTAIS FINANCEIROS ===
    public Projeto ler(int id) throws SQLException {
        String sql = """
            SELECT p.id, p.nome,
            p.data_inicio_prevista, p.data_fim_prevista,
            p.data_inicio_realizada, p.data_fim_realizada,
            p.status,
            COALESCE(SUM(e.valor_previsto), 0.00) as total_previsto,
            COALESCE(SUM(e.valor_realizado), 0.00) as total_realizado
            FROM Projetos p
            LEFT JOIN Etapas e ON e.projeto_id = p.id
            WHERE p.id = ?
            GROUP BY p.id
            """;

        try (Connection conn = conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Projeto p = new Projeto();
                    p.setId(rs.getInt("id"));
                    p.setNome(rs.getString("nome"));
                    p.setStatus(rs.getString("status"));

                    // Datas
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String str;
                    str = rs.getString("data_inicio_prevista");     if (str != null) p.setDataInicioPrevista(LocalDate.parse(str, fmt));
                    str = rs.getString("data_fim_prevista");        if (str != null) p.setDataFimPrevista(LocalDate.parse(str, fmt));
                    str = rs.getString("data_inicio_realizada");    if (str != null) p.setDataInicioRealizada(LocalDate.parse(str, fmt));
                    str = rs.getString("data_fim_realizada");       if (str != null) p.setDataFimRealizada(LocalDate.parse(str, fmt));

                    // === TOTAIS FINANCEIROS ===
                    BigDecimal totalPrevisto = rs.getBigDecimal("total_previsto");
                    BigDecimal totalRealizado = rs.getBigDecimal("total_realizado");

                    p.setValorPrevistoTotal(totalPrevisto != null ? totalPrevisto : BigDecimal.ZERO);
                    p.setValorRealizadoTotal(totalRealizado != null ? totalRealizado : BigDecimal.ZERO);

                    // Você pode adicionar esses campos no model.Projeto se quiser
                    // Ou só usar aqui mesmo

                    return p;
                }
            }
        }
        return null;
    }

    // === MÉTODOS ÚTEIS PARA TOTAIS FINANCEIROS ===
    public BigDecimal getValorPrevistoTotal(int projetoId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(valor_previsto), 0.00) FROM Etapas WHERE projeto_id = ?";
        try (Connection conn = conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projetoId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    public BigDecimal getValorRealizadoTotal(int projetoId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(valor_realizado), 0.00) FROM Etapas WHERE projeto_id = ?";
        try (Connection conn = conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projetoId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    public BigDecimal getPercentualFinanceiro(int projetoId) throws SQLException {
        BigDecimal previsto = getValorPrevistoTotal(projetoId);
        BigDecimal realizado = getValorRealizadoTotal(projetoId);

        if (previsto.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return realizado.divide(previsto, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(1, RoundingMode.HALF_UP);
    }

    // === ATUALIZAR (sem mudanças financeiras) ===
    public void atualizar(Projeto projeto) throws SQLException {
        String sql = "UPDATE Projetos SET nome = ?, data_inicio_prevista = ?, data_fim_prevista = ?, " +
                     "data_inicio_realizada = ?, data_fim_realizada = ?, status = ? WHERE id = ?";

        try (Connection conn = conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, projeto.getNome());
            stmt.setString(2, dateToString(projeto.getDataInicioPrevista()));
            stmt.setString(3, dateToString(projeto.getDataFimPrevista()));
            stmt.setString(4, dateToString(projeto.getDataInicioRealizada()));
            stmt.setString(5, dateToString(projeto.getDataFimRealizada()));
            stmt.setString(6, projeto.getStatus());
            stmt.setInt(7, projeto.getId());

            stmt.executeUpdate();
        }
    }

    // === DELETAR, LISTAR TODOS, GET NOME === (mantidos como estão)
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM Projetos WHERE id = ?";
        try (Connection conn = conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Projeto> listarTodos() throws SQLException {
        List<Projeto> projetos = new ArrayList<>();
        String sql = "SELECT id, nome, data_inicio_prevista, data_fim_prevista, " +
                     "data_inicio_realizada, data_fim_realizada, status FROM Projetos ORDER BY id";

        try (Connection conn = conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            while (rs.next()) {
                Projeto p = new Projeto();
                p.setId(rs.getInt("id"));
                p.setNome(rs.getString("nome"));
                p.setStatus(rs.getString("status"));

                String s;
                s = rs.getString("data_inicio_prevista");    if (s != null) p.setDataInicioPrevista(LocalDate.parse(s, fmt));
                s = rs.getString("data_fim_prevista");       if (s != null) p.setDataFimPrevista(LocalDate.parse(s, fmt));
                s = rs.getString("data_inicio_realizada");   if (s != null) p.setDataInicioRealizada(LocalDate.parse(s, fmt));
                s = rs.getString("data_fim_realizada");      if (s != null) p.setDataFimRealizada(LocalDate.parse(s, fmt));

                projetos.add(p);
            }
        }
        return projetos;
    }

    public String getNomeProjetoPorId(int id) throws SQLException {
        String sql = "SELECT nome FROM Projetos WHERE id = ?";
        try (Connection conn = conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("nome") : "Projeto não encontrado";
            }
        }
    }

    // === MÉTODO AUXILIAR ===
    private String dateToString(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
    }
}