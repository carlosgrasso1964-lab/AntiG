package dao;

import utilitarios.Conexao;
import model.Produtos;
import java.awt.HeadlessException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Categoria;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.CliFor;

public class ProdutosDAO {

    private Conexao conexao;

    public ProdutosDAO() throws SQLException, ClassNotFoundException { //Metodo construtor (recebe o mesmo nome da Classe)
        //Instanciando a classe
        conexao = new Conexao();
    }

    public void Salvar(Produtos obj) {
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            // SQL para inserção
            String sql = "INSERT INTO tb_produtos (descricao, preco, qtd_estoque, for_id, estoqueMinimo, estoqueMaximo, precoMedioCusto, preco_venda, categoria_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Define os parâmetros
                stmt.setString(1, obj.getDescricao());
                stmt.setDouble(2, obj.getPreco());
                stmt.setDouble(3, obj.getQtd_estoque());
                stmt.setString(4, obj.getCliFor().getCodCliFor());
                stmt.setDouble(5, obj.getQtd_estoqueMinimo());
                stmt.setDouble(6, obj.getQtd_estoqueMaximo());
                stmt.setDouble(7, obj.getPrecoMedioCusto());
                stmt.setDouble(8, obj.getPrecoVenda());
                stmt.setInt(9, obj.getCategoria().getId());
                // Executa o SQL
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Produto salvo com sucesso!");
            }
        } catch (SQLException | HeadlessException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar Produto: " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
    }

    public void Editar(Produtos obj) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE tb_produtos SET descricao=?, preco=?, qtd_estoque=?, estoqueMinimo=?, estoqueMaximo=?, precoMedioCusto=?, preco_venda=?, for_id=?, categoria_id=? WHERE id=?";
        try (Connection conn = new Conexao().getConexao(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, obj.getDescricao());
            stmt.setDouble(2, obj.getPreco());
            stmt.setDouble(3, obj.getQtd_estoque());
            stmt.setDouble(4, obj.getQtd_estoqueMinimo());
            stmt.setDouble(5, obj.getQtd_estoqueMaximo());
            stmt.setDouble(6, obj.getPrecoMedioCusto());
            stmt.setDouble(7, obj.getPrecoVenda());
            //System.out.println("Preço de venda enviado ao banco: " + obj.getPrecoVenda());
            //stmt.setInt(8, obj.getFornecedores().getId());
            stmt.setString(8, obj.getCliFor().getCodCliFor());
            stmt.setInt(9, obj.getCategoria().getId());
            stmt.setInt(10, obj.getId());
            stmt.executeUpdate();
        }
    }

    public void Excluir(Produtos obj) {
        int confirmacao = JOptionPane.showConfirmDialog(
                null,
                "Tem certeza de que deseja excluir o produto: " + obj.getDescricao() + "?",
                "Confirmação de Exclusão",
                JOptionPane.YES_NO_OPTION
        );
        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                conexao.abrirConexao(); // Abre a conexão
                Connection conn = conexao.getConexao(); // Obtém a conexão
                String sql = "DELETE FROM tb_produtos WHERE id=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, obj.getId());
                    int linhasAfetadas = stmt.executeUpdate(); // Retorna o número de linhas afetadas
                    if (linhasAfetadas > 0) {
                        JOptionPane.showMessageDialog(null, "Produto excluído com sucesso!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Nenhum produto encontrado com o ID informado.");
                    }
                }
            } catch (SQLException erro) {
                JOptionPane.showMessageDialog(null, "Erro ao excluir o produto: " + erro.getMessage());
            } finally {
                conexao.fecharConexao(); // Fecha a conexão no final
            }
        } else {
            JOptionPane.showMessageDialog(null, "Exclusão cancelada pelo usuário.");
        }
    }

    public Produtos BuscarProdutos(String nome) {
        Produtos obj = null; // Inicializa como null para indicar ausência caso não encontre
        CliFor f = null;
        Categoria c = null; // Nova categoria adicionada
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT p.id, p.descricao, p.preco, p.qtd_estoque, p.estoqueMinimo, "
                    + "p.estoqueMaximo, p.precoMedioCusto, p.preco_venda, p.categoria_id, f.nomeCliFor "
                    + "FROM tb_produtos p "
                    + "INNER JOIN tbclifor f ON (p.for_id = f.codCliFor) "
                    + "INNER JOIN tb_categorias c ON (p.categoria_id = c.id) "
                    + "WHERE p.descricao = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nome);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        obj = new Produtos();
                        f = new CliFor();
                        c = new Categoria();
                        obj.setId(rs.getInt("id"));
                        obj.setDescricao(rs.getString("descricao"));
                        obj.setPreco(rs.getDouble("preco"));
                        obj.setQtd_estoque(rs.getDouble("qtd_estoque"));
                        obj.setQtd_estoqueMinimo(rs.getDouble("estoqueMinimo"));
                        obj.setQtd_estoqueMaximo(rs.getDouble("estoqueMaximo"));
                        obj.setPrecoMedioCusto(rs.getDouble("precoMedioCusto"));
                        obj.setPrecoVenda(rs.getDouble("preco_venda"));
                        f.setNomeCliFor(rs.getString("nomeCliFor"));
                        obj.setCliFor(f);
                        c.setId(rs.getInt("categoria_id"));
                        obj.setCategoria(c);
                    }
                }
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar produto: " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        if (obj == null) {
            JOptionPane.showMessageDialog(null, "Produto não encontrado!");
        }
        
        return obj;
    }

    public Produtos BuscarProdutosCodigo(int id) {
        Produtos obj = null; // Inicializa como null para indicar ausência caso não encontre
        CliFor f = null;
        Categoria c = null; // Nova categoria adicionada
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT p.id, p.descricao, p.preco, p.qtd_estoque, p.estoqueMinimo, "
                    + "p.estoqueMaximo, p.precoMedioCusto, p.preco_venda, p.categoria_id, f.nome "
                    + "FROM tb_produtos p "
                    + "INNER JOIN tb_fornecedores f ON (p.for_id = f.id) "
                    + "INNER JOIN tb_categorias c ON (p.categoria_id = c.id) "
                    + "WHERE p.id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        obj = new Produtos();
                        f = new CliFor();
                        c = new Categoria();
                        obj.setId(rs.getInt("id"));
                        obj.setDescricao(rs.getString("descricao"));
                        obj.setPreco(rs.getDouble("preco"));
                        obj.setQtd_estoque(rs.getDouble("qtd_estoque"));
                        obj.setQtd_estoqueMinimo(rs.getDouble("estoqueMinimo"));
                        obj.setQtd_estoqueMaximo(rs.getDouble("estoqueMaximo"));
                        obj.setPrecoMedioCusto(rs.getDouble("precoMedioCusto"));
                        obj.setPrecoVenda(rs.getDouble("preco_venda"));
                        f.setNomeCliFor(rs.getString("nomeCliFor"));
                        obj.setCliFor(f);
                        c.setId(rs.getInt("categoria_id"));
                        obj.setCategoria(c);
                    }
                }
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar produto: " + erro.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }

        if (obj == null) {
            JOptionPane.showMessageDialog(null, "Produto não encontrado!");
        }
        return obj;
    }

    public List<Produtos> Listar() throws SQLException {
        List<Produtos> lista = new ArrayList<>();
        String sql = "SELECT p.id, p.descricao, p.qtd_estoque, p.estoqueMinimo, p.estoqueMaximo, "
                + "p.precoMedioCusto, p.preco_venda, f.nomeCliFor AS fornecedor, c.id AS categoria "
                + "FROM tb_produtos p "
                + "JOIN tbclifor f ON p.for_id = f.codCliFor "
                + "JOIN tb_categorias c ON p.categoria_id = c.id";
        try (Connection conn = conexao.getConexao() // Obtém a conexão
                ) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Produtos p = new Produtos();
                p.setId(rs.getInt("id"));
                p.setDescricao(rs.getString("descricao"));
                p.setQtd_estoque(rs.getDouble("qtd_estoque"));
                p.setQtd_estoqueMinimo(rs.getDouble("estoqueMinimo"));
                p.setQtd_estoqueMaximo(rs.getDouble("estoqueMaximo"));
                p.setPrecoMedioCusto(rs.getDouble("precoMedioCusto"));
                p.setPrecoVenda(rs.getDouble("preco_venda"));
                CliFor clifor = new CliFor();
                clifor.setNomeCliFor(rs.getString("fornecedor"));
                p.setCliFor(clifor);
                Categoria categoria = new Categoria();
                categoria.setId(rs.getInt("categoria"));
                p.setCategoria(categoria);
                lista.add(p);
            }
            rs.close();
            stmt.close();
        }
        return lista;
    }

    public List<Produtos> Filtrar(String nome) {
        List<Produtos> lista = new ArrayList<>();
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão
            String sql = "SELECT p.id, p.descricao, p.preco, p.qtd_estoque, p.estoqueMinimo, "
                    + "p.estoqueMaximo, p.precoMedioCusto, p.preco_venda, p.categoria_id, f.nome "
                    + "FROM tb_produtos p "
                    + "INNER JOIN tbclifor f ON p.for_id = f.codCliFor "
                    + "INNER JOIN tb_categorias c ON p.categoria_id = c.id "
                    + "WHERE p.descricao LIKE ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + nome + "%"); // Adiciona '%' para busca parcial
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Produtos obj = new Produtos();
                        CliFor f = new CliFor();
                        Categoria c = new Categoria();
                        obj.setId(rs.getInt("id"));
                        obj.setDescricao(rs.getString("descricao"));
                        obj.setPreco(rs.getDouble("preco"));
                        obj.setQtd_estoque(rs.getDouble("qtd_estoque"));
                        obj.setQtd_estoqueMinimo(rs.getDouble("estoqueMinimo"));
                        obj.setQtd_estoqueMaximo(rs.getDouble("estoqueMaximo"));
                        obj.setPrecoMedioCusto(rs.getDouble("precoMedioCusto"));
                        obj.setPrecoVenda(rs.getDouble("preco_venda"));
                        f.setNomeCliFor(rs.getString("nomeCliFor"));
                        obj.setCliFor(f);
                        c.setId(rs.getInt("categoria_id"));
                        obj.setCategoria(c);
                        lista.add(obj);
                    }
                }
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao filtrar produtos: " + erro.getMessage());
            erro.printStackTrace(); // Log do erro para depuração
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return lista; // Retorna a lista, mesmo que esteja vazia
    }

    public void adicionarEstoque(Connection conn, int idProduto, double quantidadeAdquirida, double precoMedioNovo) throws SQLException {
        try {
            // Consulta o estoque atual do produto
            String sqlConsulta = "SELECT qtd_estoque, precoMedioCusto FROM tb_produtos WHERE id = ?";
            try (PreparedStatement stmtConsulta = conn.prepareStatement(sqlConsulta)) {
                stmtConsulta.setInt(1, idProduto);
                ResultSet rs = stmtConsulta.executeQuery();
                double estoqueAtual = 0;
                double precoMedioAtual = 0;
                if (rs.next()) {
                    estoqueAtual = rs.getDouble("qtd_estoque");
                    precoMedioAtual = rs.getDouble("precoMedioCusto");
                }
                // Calcula o novo preço médio de custo com base no estoque atual e no preço médio novo
                double novoEstoque = estoqueAtual + quantidadeAdquirida;
                double novoPrecoMedio = ((precoMedioAtual * estoqueAtual) + (precoMedioNovo * quantidadeAdquirida)) / novoEstoque;
                // Arredonda o precoMedioNovo para 2 casas decimais
                BigDecimal precoArredondado = BigDecimal.valueOf(novoPrecoMedio).setScale(2, RoundingMode.HALF_UP);
                // Atualiza o estoque e o preço médio de custo
                String sqlUpdate = "UPDATE tb_produtos SET qtd_estoque = ?, precoMedioCusto = ? WHERE id = ?";
                try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                    stmtUpdate.setDouble(1, novoEstoque); // Novo estoque
                    stmtUpdate.setBigDecimal(2, precoArredondado); // Novo preço médio
                    stmtUpdate.setInt(3, idProduto);
                    stmtUpdate.executeUpdate();
                }
            }
        } catch (SQLException erro) {
            erro.printStackTrace();
            throw new SQLException("Erro ao atualizar estoque e preço médio de custo: " + erro.getMessage());
        }
    }

    public double obterMultiplicadorCategoria(int categoriaId, Connection conn) throws SQLException {
        Double multiplicador = 1.0; // Valor padrão caso não encontre nada no banco
        String sql = "SELECT multiplicador FROM tb_categorias WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                multiplicador = rs.getObject("multiplicador") != null ? rs.getDouble("multiplicador") : 1.0;
            }
        }
        return multiplicador;
    }

    public void adicionarEstoqueFC(Connection conn, int idProduto, double quantidadeAdquirida, double precoMedioNovo, double precoVendaNovo) throws SQLException {
        try {
            // Validação dos parâmetros de entrada
            if (idProduto <= 0) {
                throw new SQLException("Erro: ID do produto inválido.");
            }
            if (quantidadeAdquirida <= 0) {
                throw new SQLException("Erro: Quantidade adquirida inválida.");
            }
            if (precoMedioNovo <= 0 || precoVendaNovo <= 0) {
                throw new SQLException("Erro: Preço médio de custo ou preço de venda inválidos.");
            }
            // Consulta o estoque atual, estoque mínimo e máximo do produto
            String sqlConsulta = "SELECT qtd_estoque, estoqueMinimo, estoqueMaximo FROM tb_produtos WHERE id = ?";
            double estoqueAtual = 0;
            double estoqueMinimo = 0;
            double estoqueMaximo = 0;
            try (PreparedStatement stmtConsulta = conn.prepareStatement(sqlConsulta)) {
                stmtConsulta.setInt(1, idProduto);
                try (ResultSet rs = stmtConsulta.executeQuery()) {
                    if (rs.next()) {
                        estoqueAtual = rs.getDouble("qtd_estoque");
                        estoqueMinimo = rs.getDouble("estoqueMinimo");
                        estoqueMaximo = rs.getDouble("estoqueMaximo");
                    } else {
                        throw new SQLException("Erro: Produto não encontrado.");
                    }
                }
            }
            // Verificação para garantir que o novo estoque não ultrapasse o máximo
            double novoEstoque = estoqueAtual + quantidadeAdquirida;
            if (novoEstoque > estoqueMaximo) {
                throw new SQLException("Erro: A quantidade adquirida ultrapassa o estoque máximo permitido!");
            }
            // Arredonda o preço médio de custo e o preço de venda para 2 casas decimais
            BigDecimal precoMedioArredondado = BigDecimal.valueOf(precoMedioNovo).setScale(2, RoundingMode.HALF_UP);
            BigDecimal precoVendaArredondado = BigDecimal.valueOf(precoVendaNovo).setScale(2, RoundingMode.HALF_UP);
            // Atualiza o estoque, preço médio de custo e preço de venda
            String sqlUpdate = "UPDATE tb_produtos SET qtd_estoque = ?, precoMedioCusto = ?, preco_venda = ? WHERE id = ?";
            try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                stmtUpdate.setDouble(1, novoEstoque); // Novo estoque
                stmtUpdate.setBigDecimal(2, precoMedioArredondado); // Preço médio de custo atualizado
                stmtUpdate.setBigDecimal(3, precoVendaArredondado); // Preço de venda atualizado
                stmtUpdate.setInt(4, idProduto); // ID do produto
                stmtUpdate.executeUpdate();
            }
        } catch (SQLException erro) {
            erro.printStackTrace(); // Mantenha esse log, mas também adicione detalhes sobre o que falhou
            throw new SQLException("Erro ao atualizar estoque, preço médio de custo e preço de venda: " + erro.getMessage());
        }
    }

    public static void atualizarEstoqueEPrecoMedio(int produtoId, double estoqueFinal, double precoMedio, Connection conn) {
        String sql = "UPDATE tb_produtos SET qtd_estoque = ?, precoMedioCusto = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, estoqueFinal);
            stmt.setDouble(2, precoMedio);
            stmt.setInt(3, produtoId);

            //System.out.println("Estou em atualizarEstoqueEPreçoMedio, meu id é: " + produtoId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ProdutosDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void baixaEstoque(int id, double quantidadeVendida) {
        try {
            conexao.abrirConexao(); // Abre a conexão
            Connection conn = conexao.getConexao(); // Obtém a conexão

            double estoqueAtual = 0;
            double estoqueMinimo = 0;

            // Consulta o estoque atual e o estoque mínimo
            String sqlConsulta = "SELECT qtd_estoque, estoqueMinimo FROM tb_produtos WHERE id = ?";
            try (PreparedStatement stmtConsulta = conn.prepareStatement(sqlConsulta)) {
                stmtConsulta.setInt(1, id);
                try (ResultSet rs = stmtConsulta.executeQuery()) {
                    if (rs.next()) {
                        estoqueAtual = rs.getDouble("qtd_estoque");
                        estoqueMinimo = rs.getDouble("estoqueMinimo");
                    }
                }
            }

            // Calcula o novo estoque
            double novoEstoque = estoqueAtual - quantidadeVendida;

            // Verifica se o novo estoque será inferior ao estoque mínimo
            if (novoEstoque < estoqueMinimo) {
                throw new SQLException("Erro: A quantidade do estoque não pode ser inferior ao estoque mínimo.");
            }

            // Atualiza o estoque com a nova quantidade
            String sql = "UPDATE tb_produtos SET qtd_estoque = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, novoEstoque); // Atualiza o estoque
                stmt.setInt(2, id); // Identifica o produto
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Problemas ao tentar baixar a quantidade do estoque: " + e.getMessage());
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
    }

    public double retornaQtdAtualEstoque(int id) throws SQLException {
        double qtdAtualEstoque = 0.0;
        String sql = "SELECT qtd_estoque FROM tb_produtos WHERE id = ?";
        try (Connection conn = conexao.getConexao(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    qtdAtualEstoque = rs.getDouble("qtd_estoque");
                } else {
                    throw new SQLException("Produto com ID " + id + " não encontrado no estoque.");
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Erro ao retornar a quantidade atual do estoque: " + e.getMessage(), e);
        }
        return qtdAtualEstoque;
    }

    public Produtos buscarPorId(int id) throws SQLException {
        Produtos p = null;
        // Incluindo preco_venda e categoria_id na consulta
        String sql = "SELECT id, descricao, preco, preco_venda, qtd_estoque, estoqueMinimo, estoqueMaximo, precoMedioCusto, for_id, categoria_id FROM tb_produtos WHERE id = ?";
        // Abre a conexão
        conexao.abrirConexao();
        Connection conn = conexao.getConexao(); // Obtém a conexão
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                p = new Produtos();
                p.setId(rs.getInt("id"));
                p.setDescricao(rs.getString("descricao"));
                p.setPreco(rs.getDouble("preco"));
                p.setPrecoVenda(rs.getDouble("preco_venda")); // Preço de venda
                p.setQtd_estoque(rs.getDouble("qtd_estoque"));
                p.setQtd_estoqueMinimo(rs.getDouble("estoqueMinimo"));
                p.setQtd_estoqueMaximo(rs.getDouble("estoqueMaximo"));
                p.setPrecoMedioCusto(rs.getDouble("precoMedioCusto"));
                // Preenchendo o fornecedor
                CliFor f = new CliFor();
                f.setCodCliFor(rs.getString("for_id")); // Adicionando fornecedor, se necessário
                p.setCliFor(f); // Vincula o fornecedor ao produto
                // Adicionando o campo categoria_id
                int categoriaId = rs.getInt("categoria_id");
                Categoria categoria = new Categoria();
                categoria.setId(categoriaId); // Definindo o ID da categoria
                p.setCategoria(categoria); // Vinculando a categoria ao produto
            } else {
                throw new SQLException("Produto com ID " + id + " não encontrado.");
            }
        } finally {
            conexao.fecharConexao(); // Fecha a conexão no final
        }
        return p; // Retorna o produto ou null
    }

    public void atualizarPrecoMedioCusto(int id, double novoPrecoMedioCusto, double precoVenda, int categoriaId) throws SQLException {
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            // Comando SQL para atualizar o preço médio de custo, preço de venda e categoria
            String sql = "UPDATE tb_produtos SET precoMedioCusto = ?, preco_venda = ?, categoria_id = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Definindo os parâmetros na consulta
                stmt.setDouble(1, novoPrecoMedioCusto); // Novo preço médio de custo
                stmt.setDouble(2, precoVenda); // Novo preço de venda
                stmt.setInt(3, categoriaId); // Nova categoria
                stmt.setInt(4, id); // ID do produto
                stmt.executeUpdate(); // Executa a atualização
            }
        } catch (SQLException e) {
            throw new SQLException("Erro ao atualizar preço médio de custo, preço de venda ou categoria: " + e.getMessage());
        } finally {
            conexao.fecharConexao();
        }
    }

    public double calcularPrecoMedioCusto(int produtoId, double quantidadeNova, double subtotal) throws SQLException {

        // Obter o estoque atual e o preço médio de custo atual
        double estoqueAtual = obterEstoqueAtual(produtoId); // Método que já existe
        double precoMedioAtual = obterPrecoMedioCusto(produtoId); // Método que já existe

        // Exibir valores para depuração
        //System.out.println("Estoque atual: " + estoqueAtual);
        //System.out.println("Preço médio atual: " + precoMedioAtual);
        //System.out.println("Quantidade nova: " + quantidadeNova);
        //System.out.println("Subtotal: " + subtotal);
        // Calcular o novo estoque
        double novoEstoque = estoqueAtual + quantidadeNova;
        if (novoEstoque == 0) {
            System.out.println("Erro: Estoque final é zero. Não é possível calcular o preço médio.");
            throw new IllegalArgumentException("Erro: Estoque final é zero. Não é possível calcular o preço médio.");
        }

        // Calcular o novo preço médio de custo
        double novoPrecoMedio = ((precoMedioAtual * estoqueAtual) + subtotal) / novoEstoque;

        //System.out.println("Novo estoque: " + novoEstoque);
        //System.out.println("Novo preço médio: " + novoPrecoMedio);
        // Arredondamento para 2 casas decimais
        BigDecimal precoMedioArredondado = BigDecimal.valueOf(novoPrecoMedio).setScale(2, RoundingMode.HALF_UP);

        //return novoPrecoMedio;
        return precoMedioArredondado.doubleValue();
    }

    public double obterEstoqueAtual(int produtoId) throws SQLException {
        double estoqueAtual = 0.0;
        String sql = "SELECT qtd_estoque FROM tb_produtos WHERE id = ?";
        try (PreparedStatement stmt = conexao.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, produtoId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                estoqueAtual = rs.getDouble("qtd_estoque");
            }
        } catch (SQLException e) {
            throw new SQLException("Erro ao obter estoque atual: " + e.getMessage());
        } finally {
            conexao.fecharConexao();
        }
        // Validação final
        if (estoqueAtual < 0) {
            throw new IllegalArgumentException("Erro: Estoque atual inválido (valor negativo).");
        }
        return estoqueAtual;
    }

    public double obterPrecoMedioCusto(int produtoId) throws SQLException {
        double precoMedioCusto = 0.0;
        String sql = "SELECT precoMedioCusto FROM tb_produtos WHERE id = ?";
        try (PreparedStatement stmt = conexao.getConexao().prepareStatement(sql)) {
            stmt.setInt(1, produtoId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                precoMedioCusto = rs.getDouble("precoMedioCusto");
            }
        } catch (SQLException e) {
            throw new SQLException("Erro ao obter preço médio de custo: " + e.getMessage());
        }
        //System.out.println("Preço Médio de Custo: " + precoMedioCusto);
        return precoMedioCusto;
    }

    public String buscarDescricaoPorId(int id) {
        String descricao = null;
        try {
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();
            String sql = "SELECT descricao FROM tb_produtos WHERE id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    descricao = rs.getString("descricao");
                }
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar descrição do produto: " + erro.getMessage());
        } finally {
            conexao.fecharConexao();
        }
        return descricao;
    }

    public static Categoria buscarCategoriaPorId(int categoriaId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM tb_categorias WHERE id = ?";
        Categoria categoria = null;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    categoria = new Categoria();
                    categoria.setId(rs.getInt("id"));
                    categoria.setNome(rs.getString("descricao"));
                } else {
                    System.out.println("Categoria não encontrada!");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProdutosDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return categoria;  // Retorna a categoria encontrada ou null
    }

    public static Produtos buscarProdutoPorId(int produtoId, Connection conn) throws SQLException {
        // Log para verificar o produtoId antes da busca
        System.out.println("Buscando produto com ID: " + produtoId);

        String sql = "SELECT * FROM tb_produtos WHERE id = ?";
        Produtos produto = null;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, produtoId);  // Passe o ID do produto corretamente
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    produto = new Produtos();
                    produto.setId(rs.getInt("id"));
                    produto.setDescricao(rs.getString("descricao"));
                    produto.setPreco(rs.getDouble("preco"));
                    produto.setQtd_estoque(rs.getDouble("qtd_estoque"));
                    produto.setQtd_estoqueMinimo(rs.getDouble("estoqueMinimo"));
                    produto.setQtd_estoqueMaximo(rs.getDouble("estoqueMaximo"));
                    produto.setPrecoMedioCusto(rs.getDouble("precoMedioCusto"));
                    produto.setPrecoVenda(rs.getDouble("preco_venda"));

                    // Log para confirmar que o produto foi encontrado
                    //System.out.println("Produto encontrado: " + produto);
                } else {
                    System.out.println("Produto não encontrado!");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProdutosDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return produto;  // Retorna o produto encontrado ou null caso não tenha sido encontrado
    }

    public Produtos buscarPorNome(String nome) throws SQLException {
        Produtos produto = null;
        String sql = "SELECT * FROM tb_produtos WHERE descricao = ?";
        try (PreparedStatement stmt = conexao.getConexao().prepareStatement(sql)) {
            stmt.setString(1, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    produto = new Produtos();
                    produto.setId(rs.getInt("id"));
                    produto.setDescricao(rs.getString("descricao"));
                    produto.setPreco(rs.getDouble("preco"));
                    produto.setQtd_estoque(rs.getDouble("qtd_estoque"));
                }
            }
        }
        return produto;
    }

}
