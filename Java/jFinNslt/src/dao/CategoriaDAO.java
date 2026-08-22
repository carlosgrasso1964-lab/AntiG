package dao;

import java.sql.Connection;
import utilitarios.Conexao;
import model.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    //private Connection conn;
    private Conexao conexao;

    public CategoriaDAO() throws SQLException, ClassNotFoundException {
        this.conexao = new Conexao(); // Instanciando a classe Conexao
    }

    // Adicionar nova categoria
    public void salvar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO tb_categorias (nome, multiplicador) VALUES (?, ?)";
        try (Connection conn = conexao.getConexao(); // Obtém a conexão através da classe Conexao
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoria.getNome());
            stmt.setDouble(2, categoria.getMultiplicador());
            stmt.executeUpdate();
        }
    }

    // Atualizar categoria existente
    public void atualizar(Categoria categoria) throws SQLException {
        String sql = "UPDATE tb_categorias SET nome = ?, multiplicador = ? WHERE id = ?";
        try (Connection conn = conexao.getConexao(); // Obtém a conexão através da classe Conexao
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoria.getNome());
            stmt.setDouble(2, categoria.getMultiplicador());
            stmt.setInt(3, categoria.getId());
            stmt.executeUpdate();
        }
    }

    // Excluir categoria por ID
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM tb_categorias WHERE id = ?";
        try (Connection conn = conexao.getConexao(); // Obtém a conexão através da classe Conexao
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Listar todas as categorias
    public List<Categoria> listar() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM tb_categorias";
        try (Connection conn = conexao.getConexao(); // Obtém a conexão através da classe Conexao
                 Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Categoria categoria = new Categoria();  // A instância pode ser criada sem parâmetros se o construtor existir
                categoria.setId(rs.getInt("id"));
                categoria.setNome(rs.getString("nome"));
                categoria.setMultiplicador(rs.getDouble("multiplicador"));
                lista.add(categoria);
            }
        }
        return lista;
    }

    public String buscarNomeCategoria(int id) throws SQLException, ClassNotFoundException {
        String sql = "SELECT nome FROM tb_categorias WHERE id = ?";
        String nomeCategoria = "";
        try (Connection conn = new Conexao().getConexao(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nomeCategoria = rs.getString("nome");
            }
        }
        return nomeCategoria;
    }

    public Categoria BuscarCategoriaPorNome(String nome) throws SQLException, ClassNotFoundException {
        Categoria obj = null;
        String sql = "SELECT * FROM tb_categorias WHERE nome = ?";
        try (Connection con = new Conexao().getConexao(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, nome);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                obj = new Categoria();
                obj.setId(rs.getInt("id"));
                obj.setNome(rs.getString("nome"));
                obj.setMultiplicador(rs.getDouble("multiplicador")); // Certifique-se de que esta linha está correta!
            }
        }
        return obj;
    }

    public double buscarMultiplicador(int idCategoria) throws SQLException, ClassNotFoundException {
        double multiplicador = 1.0; // Valor padrão
        String sql = "SELECT multiplicador FROM tb_categorias WHERE id = ?";
        try (Connection conn = new Conexao().getConexao(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCategoria);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                multiplicador = rs.getDouble("multiplicador");
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        return multiplicador;
    }
}
