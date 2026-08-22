package dao;

import model.Etapa;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utilitarios.Conexao;

public class EtapaDAO {

    // === CRIAR ===
    public void criar(Etapa etapa) throws SQLException {
        String sql = "INSERT INTO etapas (projeto_id, nome, prioridade, responsavel, "
                   + "data_inicio_prevista, data_fim_prevista, data_inicio_realizada, "
                   + "data_fim_realizada, status, observ, valor_previsto, valor_realizado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = Conexao.faz_conexao();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, etapa.getProjetoId());
            stmt.setString(2, etapa.getNome());
            stmt.setString(3, etapa.getPrioridade());
            stmt.setString(4, etapa.getResponsavel());
            stmt.setObject(5, etapa.getDataInicioPrevista());
            stmt.setObject(6, etapa.getDataFimPrevista());
            stmt.setObject(7, etapa.getDataInicioRealizada());
            stmt.setObject(8, etapa.getDataFimRealizada());
            stmt.setString(9, etapa.getStatus());
            stmt.setString(10, etapa.getObs());
            stmt.setBigDecimal(11, etapa.getValorPrevisto());
            stmt.setBigDecimal(12, etapa.getValorRealizado());
            stmt.executeUpdate();
        } finally {
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
    }

    // === LER ===
    public Etapa ler(int id) throws SQLException {
        String sql = "SELECT id, projeto_id, nome, prioridade, responsavel, "
                   + "data_inicio_prevista, data_fim_prevista, data_inicio_realizada, "
                   + "data_fim_realizada, status, observ, valor_previsto, valor_realizado "
                   + "FROM etapas WHERE id = ?";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Etapa etapa = null;
        try {
            con = Conexao.faz_conexao();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                etapa = new Etapa();
                etapa.setId(rs.getInt("id"));
                etapa.setProjetoId(rs.getInt("projeto_id"));
                etapa.setNome(rs.getString("nome"));
                etapa.setPrioridade(rs.getString("prioridade"));
                etapa.setResponsavel(rs.getString("responsavel"));

                java.sql.Date sqlDate;
                sqlDate = rs.getDate("data_inicio_prevista");
                etapa.setDataInicioPrevista(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = rs.getDate("data_fim_prevista");
                etapa.setDataFimPrevista(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = rs.getDate("data_inicio_realizada");
                etapa.setDataInicioRealizada(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = rs.getDate("data_fim_realizada");
                etapa.setDataFimRealizada(sqlDate != null ? sqlDate.toLocalDate() : null);

                etapa.setStatus(rs.getString("status"));
                etapa.setObs(rs.getString("observ"));

                // Campos financeiros
                etapa.setValorPrevisto(rs.getBigDecimal("valor_previsto"));
                etapa.setValorRealizado(rs.getBigDecimal("valor_realizado"));
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
        return etapa;
    }

    // === ATUALIZAR ===
    public void atualizar(Etapa etapa) throws SQLException {
        String sql = "UPDATE etapas SET projeto_id = ?, nome = ?, prioridade = ?, responsavel = ?, "
                   + "data_inicio_prevista = ?, data_fim_prevista = ?, data_inicio_realizada = ?, "
                   + "data_fim_realizada = ?, status = ?, observ = ?, "
                   + "valor_previsto = ?, valor_realizado = ? "
                   + "WHERE id = ?";

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = Conexao.faz_conexao();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, etapa.getProjetoId());
            stmt.setString(2, etapa.getNome());
            stmt.setString(3, etapa.getPrioridade());
            stmt.setString(4, etapa.getResponsavel());
            stmt.setObject(5, etapa.getDataInicioPrevista());
            stmt.setObject(6, etapa.getDataFimPrevista());
            stmt.setObject(7, etapa.getDataInicioRealizada());
            stmt.setObject(8, etapa.getDataFimRealizada());
            stmt.setString(9, etapa.getStatus());
            stmt.setString(10, etapa.getObs());
            stmt.setBigDecimal(11, etapa.getValorPrevisto());
            stmt.setBigDecimal(12, etapa.getValorRealizado());
            stmt.setInt(13, etapa.getId());
            stmt.executeUpdate();
        } finally {
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
    }

    // === DELETAR ===
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM etapas WHERE id = ?";
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
    public List<Etapa> listarTodos() throws SQLException {
        String sql = "SELECT id, projeto_id, nome, prioridade, responsavel, "
                   + "data_inicio_prevista, data_fim_prevista, data_inicio_realizada, "
                   + "data_fim_realizada, status, observ, valor_previsto, valor_realizado "
                   + "FROM etapas ORDER BY id";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Etapa> etapas = new ArrayList<>();
        try {
            con = Conexao.faz_conexao();
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Etapa etapa = new Etapa();
                etapa.setId(rs.getInt("id"));
                etapa.setProjetoId(rs.getInt("projeto_id"));
                etapa.setNome(rs.getString("nome"));
                etapa.setPrioridade(rs.getString("prioridade"));
                etapa.setResponsavel(rs.getString("responsavel"));

                java.sql.Date sqlDate;
                sqlDate = rs.getDate("data_inicio_prevista");
                etapa.setDataInicioPrevista(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = rs.getDate("data_fim_prevista");
                etapa.setDataFimPrevista(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = rs.getDate("data_inicio_realizada");
                etapa.setDataInicioRealizada(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = rs.getDate("data_fim_realizada");
                etapa.setDataFimRealizada(sqlDate != null ? sqlDate.toLocalDate() : null);

                etapa.setStatus(rs.getString("status"));
                etapa.setObs(rs.getString("observ"));

                // Campos financeiros
                etapa.setValorPrevisto(rs.getBigDecimal("valor_previsto"));
                etapa.setValorRealizado(rs.getBigDecimal("valor_realizado"));

                etapas.add(etapa);
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
        return etapas;
    }

    // === LISTAR POR PROJETO (útil, se você tiver) ===
    public List<Etapa> listarPorProjeto(int projetoId) throws SQLException {
        String sql = "SELECT id, projeto_id, nome, prioridade, responsavel, "
                   + "data_inicio_prevista, data_fim_prevista, data_inicio_realizada, "
                   + "data_fim_realizada, status, observ, valor_previsto, valor_realizado "
                   + "FROM etapas WHERE projeto_id = ? ORDER BY data_inicio_prevista";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Etapa> etapas = new ArrayList<>();
        try {
            con = Conexao.faz_conexao();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, projetoId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Etapa etapa = new Etapa();
                // ... (mesmo mapeamento do listarTodos)
                etapa.setId(rs.getInt("id"));
                etapa.setProjetoId(rs.getInt("projeto_id"));
                etapa.setNome(rs.getString("nome"));
                etapa.setPrioridade(rs.getString("prioridade"));
                etapa.setResponsavel(rs.getString("responsavel"));

                java.sql.Date sqlDate = rs.getDate("data_inicio_prevista");
                etapa.setDataInicioPrevista(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = rs.getDate("data_fim_prevista");
                etapa.setDataFimPrevista(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = rs.getDate("data_inicio_realizada");
                etapa.setDataInicioRealizada(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = rs.getDate("data_fim_realizada");
                etapa.setDataFimRealizada(sqlDate != null ? sqlDate.toLocalDate() : null);

                etapa.setStatus(rs.getString("status"));
                etapa.setObs(rs.getString("observ"));
                etapa.setValorPrevisto(rs.getBigDecimal("valor_previsto"));
                etapa.setValorRealizado(rs.getBigDecimal("valor_realizado"));

                etapas.add(etapa);
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
        return etapas;
    }
}