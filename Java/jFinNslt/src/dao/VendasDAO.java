package dao;

import utilitarios.Conexao;
import model.Vendas;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import model.CliFor;

public class VendasDAO {

    private Conexao conexao;

    public VendasDAO() {
        conexao = new Conexao();
    }

    // Método para salvar a venda com transações
    public void Salvar(Vendas venda) throws SQLException {
        conexao.abrirConexao(); // Abre a conexão
        Connection conn = conexao.getConexao(); // Obtém a conexão
        String sql = "INSERT INTO tb_vendas (cliente_id, data_venda, total_venda, observacoes) VALUES (?,?,?,?)";
        try (
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, venda.getClientes().getCodCliFor());
            stmt.setString(2, venda.getData_venda());
            stmt.setDouble(3, venda.getTotal_venda());
            stmt.setString(4, venda.getObservacoes());
            stmt.execute();
            JOptionPane.showMessageDialog(null, "Venda realizada com sucesso!");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao realizar a venda! " + e);
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
    }

    // Método para retornar o último ID de venda
    public int retornaUltimoIdVenda() throws SQLException {
        conexao.abrirConexao(); // Abre a conexão
        Connection conn = conexao.getConexao(); // Obtém a conexão
        int ultimoId = 0;
        String sql = "SELECT max(id) as id from tb_vendas";
        try (
                PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                ultimoId = rs.getInt("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao retornar o último Id da Venda! " + e);
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return ultimoId;
    }

    // Método para listar o histórico de vendas em um intervalo de datas
    public List<Vendas> historicoVendas(LocalDate data_inicio, LocalDate data_fim) throws SQLException {
        // Garantir que data_inicio seja menor ou igual a data_fim
        if (data_inicio.isAfter(data_fim)) {
            LocalDate temp = data_inicio;
            data_inicio = data_fim;
            data_fim = temp;
        }

        List<Vendas> lista = new ArrayList<>();
        conexao.abrirConexao(); // Abre a conexão
        Connection conn = conexao.getConexao(); // Obtém a conexão
        String sql = "SELECT v.id, c.nomeCliFor, strftime('%d/%m/%Y', v.data_venda) AS data_formatada, "
                + "v.total_venda, v.observacoes "
                + "FROM tb_vendas v "
                + "INNER JOIN tbclifor c ON v.cliente_id = c.codCliFor "
                + "WHERE DATE(v.data_venda) BETWEEN ? AND ?";
        try (
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Passa apenas o componente de data no formato yyyy-MM-dd
            stmt.setString(1, data_inicio.toString());
            stmt.setString(2, data_fim.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vendas v = new Vendas();
                    CliFor c = new CliFor();
                    v.setId(rs.getInt("id"));
                    c.setNomeCliFor(rs.getString("nomeCliFor"));
                    v.setClientes(c);
                    v.setData_venda(rs.getString("data_formatada"));
                    v.setTotal_venda(rs.getDouble("total_venda"));
                    v.setObservacoes(rs.getString("observacoes"));
                    lista.add(v);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar Histórico de Vendas! " + e.getMessage(), e);
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return lista; // Retorna a lista de vendas
    }

    // Método para retornar o total de vendas em um dia específico
    public double posicaoDoDia(LocalDate data_venda) throws SQLException, ClassNotFoundException {
        double total_do_dia = 0.00;
        conexao.abrirConexao(); // Abre a conexão
        Connection conn = conexao.getConexao(); // Obtém a conexão
        String sql = "SELECT sum(total_venda) AS total \n"
                + "FROM tb_vendas \n"
                + "WHERE DATE(data_venda) = ?";
        try (
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, data_venda.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total_do_dia = rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao retornar a posição do dia! " + e);
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return total_do_dia;
    }
}
