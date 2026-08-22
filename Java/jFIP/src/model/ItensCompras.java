package model;

/**
 *
 * @author CARLOS
 */
public class ItensCompras {

    private int produtoId;
    private String descricao;  // Novo campo para a descrição do produto
    private double qtd;
    private double precoCompra;
    private double subtotal;
    private int compraId;  // <--- COLE ESSA LINHA (dentro da classe, junto com os outros atributos)

    // Construtor padrão
    public ItensCompras(int produtoId1, double qtd1, double preco, double qtdAtual, double precoAtual) {
        this.produtoId = 0;
        this.descricao = "";
        this.qtd = 0.0;
        this.precoCompra = 0.0;
        this.subtotal = 0.0;
    }

    // Construtor com todos os parâmetros, incluindo descricao
    public ItensCompras(int produtoId, String descricao, double qtd, double precoCompra) {
        this.produtoId = produtoId;
        this.descricao = descricao;
        this.qtd = qtd;
        this.precoCompra = precoCompra;
        this.subtotal = qtd * precoCompra;  // O subtotal é calculado
    }
    
    // Construtor vazio
    public ItensCompras() {
    }

    // Outros construtores conforme necessário...
    public int getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(int produtoId) {
        this.produtoId = produtoId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getQtd() {
        return qtd;
    }

    public void setQtd(double qtd) {
        this.qtd = qtd;
    }

    public double getPrecoCompra() {
        return precoCompra;
    }

    public void setPrecoCompra(double precoCompra) {
        this.precoCompra = precoCompra;
    }

    public double getSubtotal() {
        return qtd * precoCompra;  // Calcula o subtotal dinamicamente
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;  // Permite definir o subtotal manualmente, se necessário
    }
    
        public int getCompraId() {
        return compraId;
    }

    public void setCompraId(int compraId) {
        this.compraId = compraId;
    }
}
