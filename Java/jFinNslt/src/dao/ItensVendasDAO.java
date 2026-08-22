package dao;

import utilitarios.Conexao;
import model.ItensVendas;
import model.Produtos;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;

public class ItensVendasDAO {

    private Conexao conexao;

    public ItensVendasDAO() throws SQLException, ClassNotFoundException { //Metodo construtor (recebe o mesmo nome da Classe)
        //Instanciando a classe
        this.conexao = new Conexao();
    }

    public void salvar(ItensVendas obj) {
        try {
            if (obj.getProdutos() == null) {
                throw new NullPointerException("O produto não pode ser nulo.");
            }

            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão

            // SQL ajustado para incluir o campo precoMedioCusto
            String sql = "INSERT INTO tb_itensvendas (venda_id, produto_id, qtd, subtotal, precoMedioCusto) VALUES (?,?,?,?,?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Define os valores para os parâmetros da query
                stmt.setInt(1, obj.getVendas().getId());
                stmt.setInt(2, obj.getProdutos().getId());
                stmt.setDouble(3, obj.getQtd());
                stmt.setDouble(4, obj.getSubtotal());
                stmt.setDouble(5, obj.getProdutos().getPrecoMedioCusto()); // Obtém o preço médio de custo do produto

                stmt.execute(); // Executa a query
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar Itens da Venda!", e);
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
    }

    public List<ItensVendas> listaItens(int venda_id) {
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            List<ItensVendas> lista = new ArrayList<>();
            String sql = "SELECT p.id, p.descricao, i.qtd, p.preco_venda, i.subtotal FROM tb_itensvendas i "
                    + "INNER JOIN tb_produtos p ON (i.produto_id = p.id) "
                    + "WHERE i.venda_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, venda_id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ItensVendas item = new ItensVendas();
                Produtos p = new Produtos();
                // Atenção: remover os prefixos 'p.' e 'i.' ao acessar os dados do ResultSet
                p.setId(rs.getInt("id"));
                p.setDescricao(rs.getString("descricao"));
                p.setPreco(rs.getDouble("preco_venda"));
                item.setProdutos(p);
                item.setQtd(rs.getInt("qtd"));
                item.setSubtotal(rs.getDouble("subtotal"));
                lista.add(item);
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar a lista de itens! " + e);
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
    }
}
