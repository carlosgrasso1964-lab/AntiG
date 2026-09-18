package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import model.Tarefa;
import utilitarios.Conexao;

/**
 * DAO de tarefas do Calendario To-Do.
 * Adaptado para MariaDB (mdjfin) mantendo a mesma API do jFIP.
 *
 * @author CARLOS
 */
public class TarefaDAO {

    public TarefaDAO() {
        criarTabela();
    }

    // Cria tabela sob demanda (DDL MariaDB)
    public void criarTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS tb_tarefas ("
                + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                + "data DATE NOT NULL,"
                + "descricao VARCHAR(255) NOT NULL,"
                + "concluida INTEGER NOT NULL DEFAULT 0,"
                + "criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";
        try (Connection con = Conexao.faz_conexao(); Statement st = con.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao criar tabela de tarefas: " + e.getMessage());
        }
    }

    /**
     * Busca as tarefas de um mes (dataIni..dataFim) agrupadas por dia.
     */
    public Map<String, List<Tarefa>> listarMes(String dataIni, String dataFim) {
        Map<String, List<Tarefa>> porDia = new LinkedHashMap<>();
        String sql = "SELECT id, data, descricao, concluida FROM tb_tarefas "
                + "WHERE data BETWEEN ? AND ? ORDER BY data, id";
        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dataIni);
            ps.setString(2, dataFim);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Tarefa t = new Tarefa(
                            rs.getInt("id"),
                            rs.getString("data"),
                            rs.getString("descricao"),
                            rs.getInt("concluida") == 1
                    );
                    porDia.computeIfAbsent(t.getData(), k -> new ArrayList<>()).add(t);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao listar tarefas: " + e.getMessage());
        }
        return porDia;
    }

    /** Busca as tarefas de UM dia especifico (para o painel do dia). */
    public List<Tarefa> listarDia(String data) {
        Map<String, List<Tarefa>> mes = listarMes(data, data);
        return mes.getOrDefault(data, new ArrayList<>());
    }

    /** Insere nova tarefa. */
    public boolean adicionar(String data, String descricao) {
        String sql = "INSERT INTO tb_tarefas (data, descricao) VALUES (?, ?)";
        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, data);
            ps.setString(2, descricao);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao adicionar tarefa: " + e.getMessage());
            return false;
        }
    }

    /** Alterna concluida (0/1). */
    public boolean alternar(int id) {
        String sql = "UPDATE tb_tarefas SET concluida = 1 - concluida WHERE id = ?";
        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao alternar tarefa: " + e.getMessage());
            return false;
        }
    }

    /** Exclui tarefa. */
    public boolean excluir(int id) {
        String sql = "DELETE FROM tb_tarefas WHERE id = ?";
        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao excluir tarefa: " + e.getMessage());
            return false;
        }
    }
}
