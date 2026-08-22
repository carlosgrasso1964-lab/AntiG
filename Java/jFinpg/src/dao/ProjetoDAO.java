package dao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Projeto;
import utilitarios.Conexao;

public class ProjetoDAO {

    // === CRIAR ===
    public void criar(Projeto projeto) throws SQLException {
        String sql = "INSERT INTO projetos (nome, data_inicio_prevista, data_fim_prevista, "
                   + "data_inicio_realizada, data_fim_realizada, status, "
                   + "valor_previsto_total, valor_realizado_total) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = Conexao.faz_conexao();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, projeto.getNome());
            stmt.setObject(2, projeto.getDataInicioPrevista());
            stmt.setObject(3, projeto.getDataFimPrevista());
            stmt.setObject(4, projeto.getDataInicioRealizada());
            stmt.setObject(5, projeto.getDataFimRealizada());
            stmt.setString(6, projeto.getStatus());
            stmt.setBigDecimal(7, projeto.getValorPrevistoTotal());
            stmt.setBigDecimal(8, projeto.getValorRealizadoTotal());
            stmt.executeUpdate();
        } finally {
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
    }

    // === LER ===
    public Projeto ler(int id) throws SQLException {
        String sql = "SELECT id, nome, data_inicio_prevista, data_fim_prevista, "
                   + "data_inicio_realizada, data_fim_realizada, status, "
                   + "valor_previsto_total, valor_realizado_total "
                   + "FROM projetos WHERE id = ?";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Projeto projeto = null;
        try {
            con = Conexao.faz_conexao();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                projeto = new Projeto();
                projeto.setId(rs.getInt("id"));
                projeto.setNome(rs.getString("nome"));
                java.sql.Date sqlDate;
                sqlDate = rs.getDate("data_inicio_prevista");
                projeto.setDataInicioPrevista(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = rs.getDate("data_fim_prevista");
                projeto.setDataFimPrevista(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = rs.getDate("data_inicio_realizada");
                projeto.setDataInicioRealizada(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = rs.getDate("data_fim_realizada");
                projeto.setDataFimRealizada(sqlDate != null ? sqlDate.toLocalDate() : null);
                projeto.setStatus(rs.getString("status"));
                projeto.setValorPrevistoTotal(rs.getBigDecimal("valor_previsto_total"));
                projeto.setValorRealizadoTotal(rs.getBigDecimal("valor_realizado_total"));
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
        return projeto;
    }

    // === ATUALIZAR ===
    public void atualizar(Projeto projeto) throws SQLException {
        String sql = "UPDATE projetos SET nome = ?, data_inicio_prevista = ?, data_fim_prevista = ?, "
                   + "data_inicio_realizada = ?, data_fim_realizada = ?, status = ?, "
                   + "valor_previsto_total = ?, valor_realizado_total = ? "
                   + "WHERE id = ?";

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = Conexao.faz_conexao();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, projeto.getNome());
            stmt.setObject(2, projeto.getDataInicioPrevista());
            stmt.setObject(3, projeto.getDataFimPrevista());
            stmt.setObject(4, projeto.getDataInicioRealizada());
            stmt.setObject(5, projeto.getDataFimRealizada());
            stmt.setString(6, projeto.getStatus());
            stmt.setBigDecimal(7, projeto.getValorPrevistoTotal());
            stmt.setBigDecimal(8, projeto.getValorRealizadoTotal());
            stmt.setInt(9, projeto.getId());
            stmt.executeUpdate();
        } finally {
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
    }

    // === DELETAR ===
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM projetos WHERE id = ?";
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = Conexao.faz_conexao();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } finally {
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
    }

    // === LISTAR TODOS ===
    public List<Projeto> listarTodos() throws SQLException {
        String sql = "SELECT id, nome, data_inicio_prevista, data_fim_prevista, "
                   + "data_inicio_realizada, data_fim_realizada, status, "
                   + "valor_previsto_total, valor_realizado_total "
                   + "FROM projetos ORDER BY id";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Projeto> projetos = new ArrayList<>();
        try {
            con = Conexao.faz_conexao();
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Projeto projeto = new Projeto();
                projeto.setId(rs.getInt("id"));
                projeto.setNome(rs.getString("nome"));
                java.sql.Date sqlDate;
                sqlDate = rs.getDate("data_inicio_prevista");
                projeto.setDataInicioPrevista(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = rs.getDate("data_fim_prevista");
                projeto.setDataFimPrevista(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = rs.getDate("data_inicio_realizada");
                projeto.setDataInicioRealizada(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = rs.getDate("data_fim_realizada");
                projeto.setDataFimRealizada(sqlDate != null ? sqlDate.toLocalDate() : null);
                projeto.setStatus(rs.getString("status"));
                projeto.setValorPrevistoTotal(rs.getBigDecimal("valor_previsto_total"));
                projeto.setValorRealizadoTotal(rs.getBigDecimal("valor_realizado_total"));
                projetos.add(projeto);
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
        return projetos;
    }

    // === GET NOME POR ID ===
    public String getNomeProjetoPorId(int id) throws SQLException {
        String sql = "SELECT nome FROM projetos WHERE id = ?";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String nome = "Projeto não encontrado";
        try {
            con = Conexao.faz_conexao();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                nome = rs.getString("nome");
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
        return nome;
    }
}