package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import model.ItensCompras;
import model.Produtos;
import utilitarios.Conexao;

public class ItensComprasDAO {

    // ==================== SALVAR ITEM DA COMPRA ====================
    public void salvar(Connection con, ItensCompras item, int compraId) throws SQLException {
        String sql = """
        INSERT INTO tb_itenscompras
        (compra_id, produto_id, qtd, preco_compra, subtotal)
        VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, compraId);
            ps.setInt(2, item.getProdutoId());
            ps.setDouble(3, item.getQtd());
            ps.setDouble(4, item.getPrecoCompra());
            ps.setDouble(5, item.getSubtotal());
            ps.executeUpdate();
            //System.out.println("Item salvo com sucesso! Compra: " + compraId + " | Produto: " + item.getProdutoId());
        }
    }

    // ==================== LISTAR ITENS DE UMA COMPRA ====================
    public List<ItensCompras> listarPorCompra(int compraId) throws SQLException {
        List<ItensCompras> lista = new ArrayList<>();

        String sql = """
            SELECT 
                p.id AS produto_id,
                p.descricao,
                i.qtd,
                i.preco_compra,
                i.subtotal
            FROM tb_itenscompras i
            INNER JOIN tb_produtos p ON i.produto_id = p.id
            WHERE i.compra_id = ?
            ORDER BY i.id
            """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, compraId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItensCompras item = new ItensCompras();

                    item.setProdutoId(rs.getInt("produto_id"));
                    item.setDescricao(rs.getString("descricao"));
                    item.setQtd(rs.getDouble("qtd"));
                    item.setPrecoCompra(rs.getDouble("preco_compra"));
                    item.setSubtotal(rs.getDouble("subtotal"));

                    lista.add(item);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Erro ao carregar itens da compra: " + e.getMessage());
            throw e;
        }
        return lista;
    }

    // ==================== EXCLUIR TODOS OS ITENS DE UMA COMPRA ====================
    public void excluirItensDaCompra(int compraId) throws SQLException {
        String sql = "DELETE FROM tb_itenscompras WHERE compra_id = ?";

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, compraId);
            ps.executeUpdate();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Erro ao excluir itens da compra: " + e.getMessage());
            throw e;
        }
    }

    // ==================== (BÔNUS) CALCULAR TOTAL DA COMPRA ====================
    public double calcularTotalCompra(int compraId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(subtotal), 0) AS total FROM tb_itenscompras WHERE compra_id = ?";

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, compraId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }
}
