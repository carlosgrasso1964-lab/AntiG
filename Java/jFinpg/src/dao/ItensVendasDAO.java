package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import model.ItensVendas;
import model.Produtos;
import model.Vendas;
import utilitarios.Conexao;

public class ItensVendasDAO {

    // ==================== SALVAR ITEM DA VENDA ====================
    public void salvar(ItensVendas item) throws SQLException {
        if (item.getProdutos() == null || item.getProdutos().getId() <= 0) {
            throw new IllegalArgumentException("Produto inválido!");
        }
        if (item.getVendas() == null || item.getVendas().getId() <= 0) {
            throw new IllegalArgumentException("Venda não informada!");
        }
        if (item.getQtd() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero!");
        }

        String sql = """
        INSERT INTO tb_itensvendas 
        (venda_id, produto_id, qtd, preco_venda, subtotal, preco_medio_custo) 
        VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, item.getVendas().getId());
            ps.setInt(2, item.getProdutos().getId());
            ps.setDouble(3, item.getQtd());
            ps.setDouble(4, item.getProdutos().getPrecoVenda());
            ps.setDouble(5, item.getSubtotal());
            ps.setDouble(6, item.getPrecoMedioCusto()); // ESSA LINHA É OBRIGATÓRIA!!!

            ps.executeUpdate();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Erro ao salvar item da venda!\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    // ==================== LISTAR ITENS DE UMA VENDA ====================
    public List<ItensVendas> listarPorVenda(int vendaId) throws SQLException {
        List<ItensVendas> lista = new ArrayList<>();
        String sql = """
        SELECT
            p.id,
            p.descricao,
            i.preco_venda,           -- PREÇO UNITÁRIO DA ÉPOCA DA VENDA
            i.preco_medio_custo,       -- CUSTO MÉDIO PONDERADO DA ÉPOCA DA VENDA
            i.qtd,
            i.subtotal
        FROM tb_itensvendas i
        INNER JOIN tb_produtos p ON i.produto_id = p.id
        WHERE i.venda_id = ?
        ORDER BY i.id
        """;

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, vendaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Produtos produto = new Produtos();
                    produto.setId(rs.getInt("id"));
                    produto.setDescricao(rs.getString("descricao"));
                    // NÃO PRECISA setar preço nem custo médio aqui no produto!
                    // (ou se quiser, pode setar o preço atual, mas não vamos usar)

                    ItensVendas item = new ItensVendas();
                    item.setProdutos(produto);
                    item.setQtd(rs.getDouble("qtd"));
                    item.setSubtotal(rs.getDouble("subtotal"));

                    // AQUI ESTÁ O SEGREDO DA VIDA:
                    item.setPrecoVenda(rs.getDouble("preco_venda"));           // PREÇO DA ÉPOCA
                    item.setPrecoMedioCusto(rs.getDouble("preco_medio_custo"));  // CUSTO MÉDIO DA ÉPOCA

                    lista.add(item);
                }
            }
        }
        return lista;
    }

    // ==================== EXCLUIR TODOS OS ITENS DE UMA VENDA (útil ao cancelar venda) ====================
    public void excluirItensDaVenda(int vendaId) throws SQLException {
        String sql = "DELETE FROM tb_itensvendas WHERE venda_id = ?";
        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, vendaId);
            ps.executeUpdate();
        }
    }

    // ==================== (EXTRA) TOTAL DE ITENS DE UMA VENDA (opcional, mas útil no PDV) ====================
    public double calcularTotalVenda(int vendaId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(subtotal), 0) AS total FROM tb_itensvendas WHERE venda_id = ?";

        try (Connection con = Conexao.faz_conexao(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, vendaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }

    // SALVAR ITEM COM CONNECTION (USADO NO PDV)
    public void salvar(ItensVendas item, Connection con) throws SQLException {
        String sql = """
        INSERT INTO tb_itensvendas (
            venda_id, produto_id, qtd, preco_venda, subtotal, preco_medio_custo
        ) VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, item.getVendas().getId());
            ps.setInt(2, item.getProdutos().getId());
            ps.setDouble(3, item.getQtd());
            ps.setDouble(4, item.getProdutos().getPrecoVenda());     // ? preco_venda
            ps.setDouble(5, item.getSubtotal());                     // ? subtotal
            ps.setDouble(6, item.getPrecoMedioCusto());              // ? AQUI É O CUSTO MÉDIO!!!

            ps.executeUpdate();
        }
    }
}
