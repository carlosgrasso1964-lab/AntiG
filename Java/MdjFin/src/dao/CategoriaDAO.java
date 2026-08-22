package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import model.Categoria;
import utilitarios.Conexao;

public class CategoriaDAO {

    // ==================== SALVAR NOVA CATEGORIA ====================
    public void salvar(Categoria categoria) throws SQLException {
        if (categoria.getNome() == null || categoria.getNome().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nome da categoria não pode estar vazio!");
            return;
        }

        String sql = "INSERT INTO tb_categorias (nome, multiplicador) VALUES (?, ?)";

        try (Connection con = Conexao.faz_conexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, categoria.getNome().trim());
            ps.setDouble(2, categoria.getMultiplicador());

            int linhas = ps.executeUpdate();

            if (linhas > 0) {
                // Pega o ID gerado automaticamente
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        categoria.setId(rs.getInt(1));
                    }
                }
                JOptionPane.showMessageDialog(null, "Categoria cadastrada com sucesso!");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar categoria: " + e.getMessage());
            throw e;
        }
    }

    // ==================== ATUALIZAR CATEGORIA ====================
    public void atualizar(Categoria categoria) throws SQLException {
        String sql = "UPDATE tb_categorias SET nome = ?, multiplicador = ? WHERE id = ?";

        try (Connection con = Conexao.faz_conexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoria.getNome().trim());
            ps.setDouble(2, categoria.getMultiplicador());
            ps.setInt(3, categoria.getId());

            int linhas = ps.executeUpdate();

            if (linhas > 0) {
                JOptionPane.showMessageDialog(null, "Categoria atualizada com sucesso!");
            } else {
                JOptionPane.showMessageDialog(null, "Categoria não encontrada para atualização.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar categoria: " + e.getMessage());
            throw e;
        }
    }

    // ==================== EXCLUIR CATEGORIA ====================
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM tb_categorias WHERE id = ?";

        try (Connection con = Conexao.faz_conexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int linhas = ps.executeUpdate();

            if (linhas > 0) {
                JOptionPane.showMessageDialog(null, "Categoria excluída com sucesso!");
            } else {
                JOptionPane.showMessageDialog(null, "Categoria não encontrada para exclusão.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Não foi possível excluir.\nPossivelmente há produtos vinculados a esta categoria.", 
                "Erro", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    // ==================== LISTAR TODAS AS CATEGORIAS ====================
    public List<Categoria> listar() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM tb_categorias ORDER BY nome";

        try (Connection con = Conexao.faz_conexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Categoria cat = new Categoria();
                cat.setId(rs.getInt("id"));
                cat.setNome(rs.getString("nome"));
                cat.setMultiplicador(rs.getDouble("multiplicador"));
                lista.add(cat);
            }
        }
        return lista;
    }

    // ==================== BUSCAR POR ID ====================
    public Categoria buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM tb_categorias WHERE id = ?";

        try (Connection con = Conexao.faz_conexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Categoria cat = new Categoria();
                    cat.setId(rs.getInt("id"));
                    cat.setNome(rs.getString("nome"));
                    cat.setMultiplicador(rs.getDouble("multiplicador"));
                    return cat;
                }
            }
        }
        return null;
    }

    // ==================== BUSCAR POR NOME ====================
    public Categoria buscarPorNome(String nome) throws SQLException {
        String sql = "SELECT * FROM tb_categorias WHERE nome = ?";

        try (Connection con = Conexao.faz_conexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nome.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Categoria cat = new Categoria();
                    cat.setId(rs.getInt("id"));
                    cat.setNome(rs.getString("nome"));
                    cat.setMultiplicador(rs.getDouble("multiplicador"));
                    return cat;
                }
            }
        }
        return null;
    }

    // ==================== BUSCAR MULTIPLICADOR POR ID ====================
    public double buscarMultiplicador(int idCategoria) throws SQLException {
        String sql = "SELECT multiplicador FROM tb_categorias WHERE id = ?";

        try (Connection con = Conexao.faz_conexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("multiplicador");
                }
            }
        }
        return 1.0; // valor padrão se não encontrar
    }

    // ==================== BUSCAR NOME POR ID (útil em relatórios/PDV) ====================
    public String buscarNomePorId(int id) throws SQLException {
        String sql = "SELECT nome FROM tb_categorias WHERE id = ?";

        try (Connection con = Conexao.faz_conexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nome");
                }
            }
        }
        return "Sem Categoria";
    }
}