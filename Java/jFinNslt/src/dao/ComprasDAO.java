package dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import model.Compras;
import model.ItensCompras;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import model.CliFor;

/**
 *
 * @author CARLOS
 */
public class ComprasDAO {

    //private Conexao conexao;
    private Connection conn;

    public ComprasDAO(Connection conn) {
        //Instanciando a classe
        this.conn = conn; // ou o nome correto do atributo para a conexão
    }

    public void registrarCompra(Compras compra, List<ItensCompras> itens, Connection conn) throws SQLException, ClassNotFoundException {
        try {
            conn.setAutoCommit(false); // Inicia a transação

            // Inserir a compra na tabela de compras
            String sqlCompra = "INSERT INTO tb_compras (fornecedor_id, data_compra, total_compra, observacoes) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmtCompra = conn.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS)) {
                stmtCompra.setString(1, compra.getFornecedorId());
                // Formata a data para 'YYYY-MM-DD'
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String dataFormatada = dateFormat.format(compra.getDataCompra());
                stmtCompra.setString(2, dataFormatada);

                // Ajuste para garantir precisão com BigDecimal
                stmtCompra.setBigDecimal(3, BigDecimal.valueOf(compra.getTotalCompra()).setScale(2, RoundingMode.HALF_UP));
                stmtCompra.setString(4, compra.getObservacoes());
                stmtCompra.executeUpdate();

                // Obter o ID gerado da compra
                ResultSet generatedKeys = stmtCompra.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int compraId = generatedKeys.getInt(1);
                    // Inserir itens da compra
                    String sqlItens = "INSERT INTO tb_itenscompras (compra_id, produto_id, qtd, subtotal, precoCompra) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement stmtItens = conn.prepareStatement(sqlItens)) {
                        for (ItensCompras item : itens) {
                            stmtItens.setInt(1, compraId);
                            stmtItens.setInt(2, item.getProdutoId());
                            stmtItens.setDouble(3, item.getQtd());

                            // Arredonda subtotal e precoCompra para 2 casas decimais
                            BigDecimal subtotalArredondado = BigDecimal.valueOf(item.getSubtotal()).setScale(2, RoundingMode.HALF_UP);
                            BigDecimal precoCompraArredondado = BigDecimal.valueOf(item.getPrecoCompra()).setScale(2, RoundingMode.HALF_UP);
                            stmtItens.setBigDecimal(4, subtotalArredondado);
                            stmtItens.setBigDecimal(5, precoCompraArredondado);

                            stmtItens.executeUpdate();

                            // Atualizar o estoque e o preço médio de custo do produto
                            ProdutosDAO produtosDAO = new ProdutosDAO();
                            double precoMedioNovo = produtosDAO.calcularPrecoMedioCusto(item.getProdutoId(), item.getQtd(), item.getSubtotal());

                            // Depuração para verificar o valor de precoMedioNovo
                            System.out.println("Preço médio novo calculado: " + precoMedioNovo);

                            // Validar precoMedioNovo antes de converter para BigDecimal
                            if (Double.isNaN(precoMedioNovo) || Double.isInfinite(precoMedioNovo)) {
                                throw new IllegalArgumentException("Erro: precoMedioNovo contém um valor inválido (" + precoMedioNovo + ")");
                            }

                            // Arredondar o preço médio para 2 casas decimais antes de passar para adicionarEstoque
                            BigDecimal precoMedioNovoArredondado = BigDecimal.valueOf(precoMedioNovo).setScale(2, RoundingMode.HALF_UP);
                            produtosDAO.adicionarEstoque(conn, item.getProdutoId(), item.getQtd(), precoMedioNovoArredondado.doubleValue());
                        }
                    }
                }

            } catch (SQLException e) {
                conn.rollback(); // Caso ocorra um erro, faz rollback
                throw new SQLException("Erro ao registrar compra: " + e.getMessage());
            }

            conn.commit(); // Se tudo ocorrer bem, realiza o commit
        } catch (SQLException e) {
            throw new SQLException("Erro ao registrar compra: " + e.getMessage());
        } finally {
            conn.setAutoCommit(true); // Restaura o auto commit após finalizar
        }
    }

    // Método para salvar a compra
    public int salvarCompra(Compras compra) throws SQLException {
        int idCompra = 0;
        try {

            // Inserção da compra com total, data e observações
            String sql = "INSERT INTO tb_compras (fornecedor_id, data_compra, total_compra, observacoes) VALUES (?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, compra.getFornecedorId());
                stmt.setDate(2, new java.sql.Date(compra.getDataCompra().getTime())); // Conversão correta da data
                stmt.setDouble(3, compra.getTotalCompra());
                stmt.setString(4, compra.getObservacoes()); // Adiciona o campo observações
                stmt.executeUpdate();

                // Obtém o ID da compra gerada
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    idCompra = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar a compra: " + e.getMessage());
        }
        return idCompra;
    }

    public void salvarItensCompra(int idCompra, List<ItensCompras> itensCompra) throws SQLException {
        String sql = "INSERT INTO tb_itenscompras (compra_id, produto_id, qtd, subtotal, precoCompra) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            //System.out.println("=== Início do Salvamento dos Itens da Compra ===");

            for (ItensCompras item : itensCompra) {
                // Logs de depuração
                //System.out.println("Salvando Item:");
                //System.out.println("ID da Compra: " + idCompra);
                //System.out.println("Produto ID: " + item.getProdutoId());
                //System.out.println("Quantidade: " + item.getQtd());
                //System.out.println("Preço Compra: " + item.getPrecoCompra());
                //System.out.println("Subtotal: " + item.getSubtotal());

                // Validações
                if (item.getProdutoId() <= 0 || item.getQtd() <= 0 || item.getPrecoCompra() <= 0) {
                    System.out.println("Valores inválidos para o item. Ignorando...");
                    continue; // Ignora este item e continua com os próximos
                }

                // Define os parâmetros no PreparedStatement
                stmt.setInt(1, idCompra);                      // ID da compra
                stmt.setInt(2, item.getProdutoId());           // ID do produto
                stmt.setDouble(3, item.getQtd());              // Quantidade comprada
                stmt.setDouble(4, item.getQtd() * item.getPrecoCompra()); // Subtotal calculado
                stmt.setDouble(5, item.getPrecoCompra());      // Preço de compra

                // Executa a inserção
                stmt.executeUpdate();
                //System.out.println("Item salvo com sucesso!");
            }

            //System.out.println("=== Fim do Salvamento dos Itens da Compra ===");
        } catch (SQLException e) {
            System.out.println("Erro ao salvar itens da compra: " + e.getMessage());
            e.printStackTrace();
            throw e; // Lança a exceção para tratamento em níveis superiores
        }
    }

    // Método para retornar o total de vendas em um dia específico
    public double posicaoDoDia(LocalDate data_compra) throws SQLException, ClassNotFoundException {
        double total_do_dia = 0.00;
        String sql = "SELECT SUM(total_compra) AS total \n"
                + "FROM tb_compras \n"
                + "WHERE DATE(data_compra) = DATE(?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Formata a data para o formato compatível com o banco (YYYY-MM-DD)
            stmt.setString(1, data_compra.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    total_do_dia = rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao retornar a posição do dia! " + e);
        }
        return total_do_dia;
    }

    // Método para listar o histórico de compras em um intervalo de datas
    public List<Compras> historicoCompras(LocalDate data_inicio, LocalDate data_fim) throws SQLException {

        List<Compras> lista = new ArrayList<>();
        String sql = "SELECT c.id, f.nomeCliFor AS fornecedor_nome, c.data_compra, c.total_compra, c.observacoes "
                + "FROM tb_compras c "
                + "INNER JOIN tbclifor f ON c.fornecedor_id = f.codCliFor "
                + "WHERE c.data_compra BETWEEN ? AND ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Converte as datas para o formato correto (yyyy-MM-dd) aceito pelo SQLite
            stmt.setString(1, data_inicio.toString());
            stmt.setString(2, data_fim.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Compras c = new Compras();
                    //Fornecedores f = new Fornecedores();
                    CliFor f = new CliFor();
                    c.setId(rs.getInt("id"));

                    // Recupera e define o nome do fornecedor
                    f.setNomeCliFor(rs.getString("fornecedor_nome"));
                    c.setFornecedor(f);

                    // Converte a data para Timestamp, assumindo que 'data_compra' está no formato 'yyyy-MM-dd'
                    String dataCompraStr = rs.getString("data_compra");
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        java.util.Date date = sdf.parse(dataCompraStr);
                        c.setDataCompra(new Timestamp(date.getTime()));
                    } catch (ParseException e) {
                        System.err.println("Erro ao converter a data: " + e.getMessage());
                        c.setDataCompra(new Timestamp(System.currentTimeMillis())); // Define um valor padrão em caso de erro
                    }

                    // Preenche os demais campos
                    c.setTotalCompra(rs.getDouble("total_compra"));
                    c.setObservacoes(rs.getString("observacoes"));

                    // Adiciona à lista
                    lista.add(c);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar Histórico de Compras! " + e.getMessage(), e);
        }
        return lista;
    }

    // Método para listar todas as compras
    public List<Compras> listarCompras() throws SQLException, ParseException {

        List<Compras> compras = new ArrayList<>();
        String sql = "SELECT c.id, c.fornecedor_id, f.nome AS fornecedor_nome, c.data_compra, c.total_compra, c.observacoes "
                + "FROM tb_compras c "
                + "INNER JOIN tb_fornecedores f ON c.fornecedor_id = f.id"; // Certifique-se de que 'f.id' é a chave primária de 'tb_fornecedores'

        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Compras compra = new Compras();
                CliFor fornecedor = new CliFor();

                // Preenche os campos de compra
                compra.setId(rs.getInt("id"));
                compra.setTotalCompra(rs.getDouble("total_compra"));
                compra.setObservacoes(rs.getString("observacoes"));

                // Converte a data de string para Timestamp
                String dataCompraStr = rs.getString("data_compra");
                if (dataCompraStr != null) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        java.util.Date date = sdf.parse(dataCompraStr);
                        compra.setDataCompra(new Timestamp(date.getTime()));
                    } catch (ParseException e) {
                        System.err.println("Erro ao converter a data: " + e.getMessage());
                        compra.setDataCompra(new Timestamp(System.currentTimeMillis())); // Valor padrão em caso de erro
                    }
                } else {
                    compra.setDataCompra(new Timestamp(System.currentTimeMillis())); // Valor padrão se data for nula
                }

                // Preenche os campos do fornecedor
                fornecedor.setCodCliFor(rs.getString("fornecedor_id"));
                fornecedor.setNomeCliFor(rs.getString("fornecedor_nome"));

                // Associa o fornecedor à compra
                compra.setFornecedor(fornecedor);

                // Adiciona a compra à lista
                compras.add(compra);
            }
        }
        return compras;
    }

}
