package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import model.CliFor;
import model.Vendas;
import utilitarios.Conexao;

public class VendasDAO {

    // ==================== SALVAR VENDA (COM RETORNO DO ID GERADO) ====================
    public int salvar(Vendas venda) throws SQLException {
        String sql = """
            INSERT INTO tb_vendas 
            (cliente_id, data_venda, total_venda, observacoes) 
            VALUES (?, ?, ?, ?)
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, venda.getClientes().getCodCliFor());  // mais seguro ainda!
            ps.setObject(2, java.sql.Date.valueOf(LocalDate.now())); // Data atual
            ps.setDouble(3, venda.getTotal_venda());
            ps.setString(4, venda.getObservacoes() != null ? venda.getObservacoes() : "");

            ps.executeUpdate();

            // Pega o ID da venda gerada
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    JOptionPane.showMessageDialog(null,
                            "Venda realizada com sucesso! Nº " + idGerado,
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    return idGerado;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Erro ao salvar venda: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
        return 0;
    }

    // ==================== RETORNAR ÚLTIMO ID DE VENDA ====================
    public int getUltimoIdVenda() throws SQLException {
        String sql = "SELECT MAX(id) AS id FROM tb_vendas";

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return 0;
    }

    // ==================== HISTÓRICO DE VENDAS POR PERÍODO ====================
    public List<Vendas> historicoVendas(LocalDate dataInicio, LocalDate dataFim) throws SQLException {
        List<Vendas> lista = new ArrayList<>();

        // Garante que dataInicio <= dataFim
        if (dataInicio.isAfter(dataFim)) {
            LocalDate temp = dataInicio;
            dataInicio = dataFim;
            dataFim = temp;
        }

        String sql = """
            SELECT v.id, v.data_venda, v.total_venda, v.observacoes, c.nomeclifor
            FROM tb_vendas v
            LEFT JOIN tbclifor c ON v.cliente_id = c.codclifor
            WHERE DATE(v.data_venda) BETWEEN ? AND ?
            ORDER BY v.data_venda DESC, v.id DESC
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

//            ps.setString(1, dataInicio.toString());
//            ps.setString(2, dataFim.toString());
            ps.setDate(1, java.sql.Date.valueOf(dataInicio));
            ps.setDate(2, java.sql.Date.valueOf(dataFim));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vendas v = new Vendas();
                    CliFor cliente = new CliFor();

                    v.setId(rs.getInt("id"));
                    v.setData_venda(rs.getTimestamp("data_venda"));  // ? SIMPLES, DIRETO E PROFISSIONAL!
                    v.setTotal_venda(rs.getDouble("total_venda"));
                    v.setObservacoes(rs.getString("observacoes"));

                    cliente.setNomeCliFor(rs.getString("nomeCliFor"));
                    v.setClientes(cliente);

                    lista.add(v);
                }
            }
        }
        return lista;
    }

    public double posicaoDoDia(LocalDate data) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(total_venda), 0) AS total
            FROM tb_vendas
            WHERE DATE(data_venda) = ?
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, data);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }

    // ==================== BUSCAR VENDA POR ID (COM CLIENTE) ====================
    public Vendas buscarPorId(int idVenda) throws SQLException {
        String sql = """
            SELECT v.*, c.nomeclifor 
            FROM tb_vendas v
            LEFT JOIN tbclifor c ON v.cliente_id = c.codCliFor
            WHERE v.id = ?
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenda);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Vendas v = new Vendas();
                    CliFor cliente = new CliFor();

                    v.setId(rs.getInt("id"));
                    v.setData_venda(rs.getTimestamp("data_venda"));
                    v.setTotal_venda(rs.getDouble("total_venda"));
                    v.setObservacoes(rs.getString("observacoes"));

                    cliente.setNomeCliFor(rs.getString("nomeclifor"));
                    cliente.setCodCliFor(rs.getString("cliente_id"));
                    v.setClientes(cliente);

                    return v;
                }
            }
        }
        return null;
    }

    // SALVAR VENDA COM TRANSACAO (USADO NO PDV)
    public void salvar(Vendas v, Connection con) throws SQLException {
        String sql = """
            INSERT INTO tb_vendas (
                cliente_id, data_venda, total_venda, observacoes
            ) VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getClientes().getId()); // ou codCliFor, dependendo do seu campo
            ps.setTimestamp(2, v.getData_venda());
            ps.setDouble(3, v.getTotal_venda());
            ps.setString(4, v.getObservacoes());
            ps.executeUpdate();

            // Pega o ID gerado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    v.setId(rs.getInt(1));
                }
            }
        }
    }

    // RETORNA O ÚLTIMO ID (CASO PRECISE FORA DA TRANSACAO)
    public int retornaUltimoIdVenda(Connection con) throws SQLException {
        String sql = "SELECT MAX(id) FROM tb_vendas";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
}
