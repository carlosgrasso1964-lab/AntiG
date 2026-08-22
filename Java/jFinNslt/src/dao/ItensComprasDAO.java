package dao;

import utilitarios.Conexao;
import model.ItensCompras;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import model.Compras;

public class ItensComprasDAO {

    private Conexao conexao;

    // Construtor que aceita a conexão
    public ItensComprasDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    public List<ItensCompras> listaItens(int compra_id) {

        List<ItensCompras> lista = new ArrayList<>();
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT p.id, p.descricao, i.qtd, i.precoCompra, i.subtotal FROM tb_itenscompras i "
                    + "INNER JOIN tb_produtos p ON (i.produto_id = p.id) "
                    + "WHERE i.compra_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, compra_id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Instanciação correta do objeto ItensCompras
                ItensCompras item = new ItensCompras();
                item.setProdutoId(rs.getInt("id"));
                item.setDescricao(rs.getString("descricao"));
                item.setQtd(rs.getDouble("qtd"));
                item.setPrecoCompra(rs.getDouble("precoCompra"));
                item.setSubtotal(rs.getDouble("subtotal"));
                lista.add(item);
            }
            rs.close(); // Fechar ResultSet
            stmt.close(); // Fechar Statement
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar a lista de itens! " + e);
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return lista;
    }

    // Método para listar todas as compras
    public List<Compras> listarCompras() throws SQLException {

        List<Compras> compras = new ArrayList<>();
        String sql = "SELECT id, fornecedor_id, data_compra, total_compra, observacoes FROM tb_compras";
        try (Connection conn = conexao.getConexao(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Compras compra = new Compras();
                compra.setId(rs.getInt("id"));
                compra.setFornecedorId(rs.getString("fornecedor_id"));
                compra.setDataCompra(rs.getTimestamp("data_compra"));
                compra.setTotalCompra(rs.getDouble("total_compra"));
                compra.setObservacoes(rs.getString("observacoes"));
                compras.add(compra);
            }
        }
        return compras;
    }

}
