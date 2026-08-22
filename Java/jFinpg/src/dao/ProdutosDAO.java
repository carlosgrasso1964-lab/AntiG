package dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import model.Categoria;
import model.CliFor;
import model.Produtos;
import utilitarios.Conexao;

public class ProdutosDAO {

    // ==================== SALVAR PRODUTO ====================
    public void salvar(Produtos p) throws SQLException {
        String sql = """
            INSERT INTO tb_produtos 
            (descricao, preco, qtd_estoque, for_id, estoque_minimo, estoque_maximo, 
             preco_medio_custo, preco_venda, categoria_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getDescricao());
            ps.setDouble(2, p.getPreco());
            ps.setDouble(3, p.getQtd_estoque());
            ps.setString(4, p.getCliFor().getCodCliFor());
            ps.setDouble(5, p.getQtd_estoqueMinimo());
            ps.setDouble(6, p.getQtd_estoqueMaximo());
            ps.setDouble(7, p.getPrecoMedioCusto());
            ps.setDouble(8, p.getPrecoVenda());
            ps.setInt(9, p.getCategoria().getId());

            ps.executeUpdate();

            // Pega o ID gerado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }

            JOptionPane.showMessageDialog(null, "Produto cadastrado com sucesso!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar produto: " + e.getMessage());
            throw e;
        }
    }

    // ==================== ATUALIZAR PRODUTO ====================
    public void atualizar(Produtos p) throws SQLException {
        String sql = """
            UPDATE tb_produtos SET 
            descricao = ?, preco = ?, qtd_estoque = ?, estoque_minimo = ?, 
            estoque_maximo = ?, preco_medio_custo = ?, preco_venda = ?, 
            for_id = ?, categoria_id = ? 
            WHERE id = ?
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getDescricao());
            ps.setDouble(2, p.getPreco());
            ps.setDouble(3, p.getQtd_estoque());
            ps.setDouble(4, p.getQtd_estoqueMinimo());
            ps.setDouble(5, p.getQtd_estoqueMaximo());
            ps.setDouble(6, p.getPrecoMedioCusto());
            ps.setDouble(7, p.getPrecoVenda());
            ps.setString(8, p.getCliFor().getCodCliFor());
            ps.setInt(9, p.getCategoria().getId());
            ps.setInt(10, p.getId());

            int linhas = ps.executeUpdate();
            if (linhas > 0) {
                JOptionPane.showMessageDialog(null, "Produto atualizado com sucesso!");
            } else {
                JOptionPane.showMessageDialog(null, "Produto não encontrado para atualização.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar produto: " + e.getMessage());
            throw e;
        }
    }

    // ==================== EXCLUIR PRODUTO ====================
    public void excluir(int id) throws SQLException {
        int op = JOptionPane.showConfirmDialog(null,
                "Tem certeza que deseja excluir este produto?",
                "Confirmação", JOptionPane.YES_NO_OPTION);

        if (op == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM tb_produtos WHERE id = ?";

            try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, id);
                int linhas = ps.executeUpdate();

                if (linhas > 0) {
                    JOptionPane.showMessageDialog(null, "Produto excluído com sucesso!");
                } else {
                    JOptionPane.showMessageDialog(null, "Produto não encontrado.");
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Não foi possível excluir.\nPossivelmente há vendas/compras vinculadas.",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==================== BUSCAR POR ID ====================
    public Produtos buscarPorId(int id) throws SQLException {
        String sql = """
            SELECT p.*, f.nomeclifor, c.id AS cat_id, c.nome AS cat_nome 
            FROM tb_produtos p
            LEFT JOIN tbclifor f ON p.for_id = f.codclifor
            LEFT JOIN tb_categorias c ON p.categoria_id = c.id
            WHERE p.id = ?
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return montarProduto(rs);
                }
            }
        }
        return null;
    }

    public Produtos buscarPorIdComCusto(int id) throws SQLException {

        String sql = "SELECT p.*, p.preco_medio_custo AS preco_medio_custo FROM tb_produtos p WHERE p.id = ?";

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Produtos p = new Produtos();
                    p.setId(rs.getInt("id"));
                    p.setDescricao(rs.getString("descricao"));
                    p.setPrecoVenda(rs.getDouble("preco_venda"));
                    p.setQtd_estoque(rs.getDouble("qtd_estoque"));
                    p.setPrecoMedioCusto(rs.getDouble("preco_medio_custo"));
                    return p;
                }
            }
        }
        return null;
    }

    // ==================== BUSCAR POR NOME EXATO ====================
    public Produtos buscarPorNome(String nome) throws SQLException {
        String sql = """
            SELECT p.*, f.nomeclifor, c.id AS cat_id, c.nome AS cat_nome 
            FROM tb_produtos p
            LEFT JOIN tbclifor f ON p.for_id = f.codclifor
            LEFT JOIN tb_categorias c ON p.categoria_id = c.id
            WHERE p.descricao = ?
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return montarProduto(rs);
                }
            }
        }
        return null;
    }

    // ==================== LISTAR TODOS ====================
    public List<Produtos> listarTodos() throws SQLException {
        List<Produtos> lista = new ArrayList<>();
        String sql = """
            SELECT p.*, f.nomeclifor, c.id AS cat_id, c.nome AS cat_nome 
            FROM tb_produtos p
            LEFT JOIN tbclifor f ON p.for_id = f.codclifor
            LEFT JOIN tb_categorias c ON p.categoria_id = c.id
            ORDER BY p.descricao
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(montarProduto(rs));
            }
        }
        return lista;
    }

    // ==================== FILTRAR POR NOME (LIKE) ====================
    public List<Produtos> filtrarPorNome(String nome) throws SQLException {
        List<Produtos> lista = new ArrayList<>();
        String sql = """
            SELECT p.*, f.nomeclifor, c.id AS cat_id, c.nome AS cat_nome 
            FROM tb_produtos p
            LEFT JOIN tbclifor f ON p.for_id = f.codclifor
            LEFT JOIN tb_categorias c ON p.categoria_id = c.id
            WHERE p.descricao LIKE ?
            ORDER BY p.descricao
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + nome + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(montarProduto(rs));
                }
            }
        }
        return lista;
    }

    // ==================== MÉTODO AUXILIAR PARA MONTAR OBJETO ====================
    private Produtos montarProduto(ResultSet rs) throws SQLException {
        Produtos p = new Produtos();
        p.setId(rs.getInt("id"));
        p.setDescricao(rs.getString("descricao"));
        p.setPreco(rs.getDouble("preco"));
        p.setQtd_estoque(rs.getDouble("qtd_estoque"));
        p.setQtd_estoqueMinimo(rs.getDouble("estoque_minimo"));
        p.setQtd_estoqueMaximo(rs.getDouble("estoque_maximo"));
        p.setPrecoMedioCusto(rs.getDouble("preco_medio_custo"));
        p.setPrecoVenda(rs.getDouble("preco_venda"));

        CliFor forn = new CliFor();
        forn.setNomeCliFor(rs.getString("nomeclifor"));
        forn.setCodCliFor(rs.getString("for_id"));
        p.setCliFor(forn);

        Categoria cat = new Categoria();
        cat.setId(rs.getInt("cat_id"));
        cat.setNome(rs.getString("cat_nome"));
        p.setCategoria(cat);

        return p;
    }

    // ==================== BAIXA DE ESTOQUE (VENDA) ====================
    public void baixarEstoque(int produtoId, double qtdVendida) throws SQLException {
        String sql = """
            UPDATE tb_produtos 
            SET qtd_estoque = qtd_estoque - ? 
            WHERE id = ? AND qtd_estoque >= ?
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, qtdVendida);
            ps.setInt(2, produtoId);
            ps.setDouble(3, qtdVendida);

            int linhas = ps.executeUpdate();
            if (linhas == 0) {
                throw new SQLException("Estoque insuficiente ou produto não encontrado!");
            }
        }
    }

    // ==================== ENTRADA DE ESTOQUE + PREÇO MÉDIO PONDERADO ====================
    public void entradaEstoque(Connection con, int produtoId, double qtdEntrada, double custoTotal) throws SQLException {
        String sql = """
            UPDATE tb_produtos 
            SET qtd_estoque = qtd_estoque + ?,
                preco_medio_custo = ((preco_medio_custo * qtd_estoque) + ?) / (qtd_estoque + ?)
            WHERE id = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, qtdEntrada);
            ps.setDouble(2, custoTotal);
            ps.setDouble(3, qtdEntrada);
            ps.setInt(4, produtoId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Produto não encontrado (ID: " + produtoId + ")");
            }

            System.out.println("ESTOQUE ATUALIZADO COM SUCESSO ? Produto " + produtoId
                    + " | +Qtd: " + qtdEntrada + " | Custo Total: R$" + custoTotal);
        }
    }

    // ==================== CÁLCULO DO PREÇO MÉDIO PONDERADO (USADO NAS COMPRAS) ====================
    public double calcularPrecoMedioCusto(int produtoId, double qtdEntrada, double subtotal) throws SQLException {
        String sql = "SELECT qtd_estoque, preco_medio_custo FROM tb_produtos WHERE id = ?";

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, produtoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double estoqueAtual = rs.getDouble("qtd_estoque");
                    double precoMedioAtual = rs.getDouble("preco_medio_custo");

                    double novoEstoque = estoqueAtual + qtdEntrada;
                    double novoPrecoMedio = ((precoMedioAtual * estoqueAtual) + subtotal) / novoEstoque;

                    BigDecimal bd = BigDecimal.valueOf(novoPrecoMedio)
                            .setScale(2, RoundingMode.HALF_UP);

                    return bd.doubleValue();
                } else {
                    throw new SQLException("Produto não encontrado: ID " + produtoId);
                }
            }
        }
    }

    // ==================== ADICIONAR ESTOQUE COM PREÇO MÉDIO (COM CONEXÃO EXTERNA) ====================
    public void adicionarEstoque(Connection con, int produtoId, double qtdEntrada, double novoPrecoMedio) throws SQLException {
        String sql = """
            UPDATE tb_produtos 
            SET qtd_estoque = qtd_estoque + ?, 
                preco_medio_custo = ? 
            WHERE id = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, qtdEntrada);
            ps.setDouble(2, novoPrecoMedio);
            ps.setInt(3, produtoId);
            ps.executeUpdate();
        }
    }

    // ==================== BUSCAR DESCRIÇÃO POR ID (PDV) ====================
    public String buscarDescricaoPorId(int id) throws SQLException {
        String sql = "SELECT descricao FROM tb_produtos WHERE id = ?";
        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("descricao");
                }
            }
        }
        return "PRODUTO NÃO ENCONTRADO";
    }

    // BAIXA ESTOQUE COM CONNECTION (TRANSACAO SEGURA)
    public void baixaEstoque(int produtoId, double quantidadeVendida, Connection con) throws SQLException {
        String sql = "UPDATE tb_produtos SET qtd_estoque = qtd_estoque - ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, quantidadeVendida);
            ps.setInt(2, produtoId);
            ps.executeUpdate();
        }
    }
}
