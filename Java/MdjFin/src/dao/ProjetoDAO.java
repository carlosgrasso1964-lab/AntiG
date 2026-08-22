package dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import utilitarios.Conexao;

import model.Projeto;
import utilitarios.Conexao;

public class ProjetoDAO {

    private Conexao conexao;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ProjetoDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    // === CRIAR ===
    public void criar(Projeto projeto) throws SQLException {
        String sql = "INSERT INTO projetos (nome, data_inicio_prevista, data_fim_prevista, "
                + "data_inicio_realizada, data_fim_realizada, status, "
                + "valor_previsto_total, valor_realizado_total) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexao.faz_conexao(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        //try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, projeto.getNome());
            stmt.setString(2, dateToString(projeto.getDataInicioPrevista()));
            stmt.setString(3, dateToString(projeto.getDataFimPrevista()));
            stmt.setString(4, dateToString(projeto.getDataInicioRealizada()));
            stmt.setString(5, dateToString(projeto.getDataFimRealizada()));
            stmt.setString(6, projeto.getStatus());
            stmt.setBigDecimal(7, projeto.getValorPrevistoTotal() != null ? projeto.getValorPrevistoTotal() : BigDecimal.ZERO);
            stmt.setBigDecimal(8, projeto.getValorRealizadoTotal() != null ? projeto.getValorRealizadoTotal() : BigDecimal.ZERO);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    projeto.setId(rs.getInt(1));
                }
            }
        }
    }

    // === LER (com totais diretos do banco) ===
    public Projeto ler(int id) throws SQLException {
        String sql = "SELECT id, nome, data_inicio_prevista, data_fim_prevista, "
                + "data_inicio_realizada, data_fim_realizada, status, "
                + "valor_previsto_total, valor_realizado_total "
                + "FROM projetos WHERE id = ?";

        try (Connection conn = conexao.faz_conexao(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Projeto p = new Projeto();
                    p.setId(rs.getInt("id"));
                    p.setNome(rs.getString("nome"));
                    p.setStatus(rs.getString("status"));

                    // Datas
                    String str;
                    str = rs.getString("data_inicio_prevista");
                    if (str != null) {
                        p.setDataInicioPrevista(LocalDate.parse(str.substring(0, 10), DATE_FMT));
                    }
                    str = rs.getString("data_fim_prevista");
                    if (str != null) {
                        p.setDataFimPrevista(LocalDate.parse(str.substring(0, 10), DATE_FMT));
                    }
                    str = rs.getString("data_inicio_realizada");
                    if (str != null) {
                        p.setDataInicioRealizada(LocalDate.parse(str.substring(0, 10), DATE_FMT));
                    }
                    str = rs.getString("data_fim_realizada");
                    if (str != null) {
                        p.setDataFimRealizada(LocalDate.parse(str.substring(0, 10), DATE_FMT));
                    }

                    // Totais financeiros (agora direto da tabela)
                    BigDecimal previsto = rs.getBigDecimal("valor_previsto_total");
                    BigDecimal realizado = rs.getBigDecimal("valor_realizado_total");
                    p.setValorPrevistoTotal(previsto != null ? previsto : BigDecimal.ZERO);
                    p.setValorRealizadoTotal(realizado != null ? realizado : BigDecimal.ZERO);

                    return p;
                }
            }
        }
        return null;
    }

    // === ATUALIZAR ===
    public void atualizar(Projeto projeto) throws SQLException {
        String sql = "UPDATE projetos SET nome = ?, data_inicio_prevista = ?, data_fim_prevista = ?, "
                + "data_inicio_realizada = ?, data_fim_realizada = ?, status = ?, "
                + "valor_previsto_total = ?, valor_realizado_total = ? WHERE id = ?";

        try (Connection conn = conexao.faz_conexao(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, projeto.getNome());
            stmt.setString(2, dateToString(projeto.getDataInicioPrevista()));
            stmt.setString(3, dateToString(projeto.getDataFimPrevista()));
            stmt.setString(4, dateToString(projeto.getDataInicioRealizada()));
            stmt.setString(5, dateToString(projeto.getDataFimRealizada()));
            stmt.setString(6, projeto.getStatus());
            stmt.setBigDecimal(7, projeto.getValorPrevistoTotal() != null ? projeto.getValorPrevistoTotal() : BigDecimal.ZERO);
            stmt.setBigDecimal(8, projeto.getValorRealizadoTotal() != null ? projeto.getValorRealizadoTotal() : BigDecimal.ZERO);
            stmt.setInt(9, projeto.getId());

            stmt.executeUpdate();
        }
    }

    // === DELETAR ===
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM projetos WHERE id = ?";
        try (Connection conn = conexao.faz_conexao(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // === LISTAR TODOS (sem totais financeiros aqui para manter leve) ===
    public List<Projeto> listarTodos() throws SQLException {
        List<Projeto> projetos = new ArrayList<>();
        String sql = "SELECT id, nome, data_inicio_prevista, data_fim_prevista, "
                + "data_inicio_realizada, data_fim_realizada, status "
                + "FROM projetos ORDER BY id";

        try (Connection conn = conexao.faz_conexao(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Projeto p = new Projeto();
                p.setId(rs.getInt("id"));
                p.setNome(rs.getString("nome"));
                p.setStatus(rs.getString("status"));

                String s;
                s = rs.getString("data_inicio_prevista");
                if (s != null) {
                    p.setDataInicioPrevista(LocalDate.parse(s.substring(0, 10), DATE_FMT));
                }
                s = rs.getString("data_fim_prevista");
                if (s != null) {
                    p.setDataFimPrevista(LocalDate.parse(s.substring(0, 10), DATE_FMT));
                }
                s = rs.getString("data_inicio_realizada");
                if (s != null) {
                    p.setDataInicioRealizada(LocalDate.parse(s.substring(0, 10), DATE_FMT));
                }
                s = rs.getString("data_fim_realizada");
                if (s != null) {
                    p.setDataFimRealizada(LocalDate.parse(s.substring(0, 10), DATE_FMT));
                }

                projetos.add(p);
            }
        }
        return projetos;
    }

    // === GET NOME ===
    public String getNomeProjetoPorId(int id) throws SQLException {
        String sql = "SELECT nome FROM projetos WHERE id = ?";
        try (Connection conn = conexao.faz_conexao(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("nome") : "Projeto não encontrado";
            }
        }
    }

    // === MÉTODO AUXILIAR ===
    private String dateToString(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : null;
    }

    // === MANTIVE OPICIONAL: Percentual financeiro (agora baseado nos campos da tabela) ===
    public BigDecimal getPercentualFinanceiro(int projetoId) throws SQLException {
        Projeto p = ler(projetoId); // Reusa o método ler que já traz os totais
        if (p == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal previsto = p.getValorPrevistoTotal();
        BigDecimal realizado = p.getValorRealizadoTotal();

        if (previsto.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return realizado.divide(previsto, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }
}
