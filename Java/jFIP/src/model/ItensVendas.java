package model;

/**
 *
 * @author CARLOS
 */
public class ItensVendas {

    private int id;
    private Vendas vendas;
    private Produtos produtos;
    private double qtd;
    private double subtotal;
    private double precoMedioCusto;
    private double precoVenda;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Vendas getVendas() {
        return vendas;
    }

    public void setVendas(Vendas vendas) {
        this.vendas = vendas;
    }

    public Produtos getProdutos() {
        return produtos;
    }

    public void setProdutos(Produtos produtos) {
        this.produtos = produtos;
    }

    public double getQtd() {
        return qtd;
    }

    public void setQtd(double qtd) {
        this.qtd = qtd;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getPrecoMedioCusto() {
        return precoMedioCusto;
    }

    public void setPrecoMedioCusto(double precoMedioCusto) {
        this.precoMedioCusto = precoMedioCusto;
    }

    public double getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(double precoVenda) {
        this.precoVenda = precoVenda;
    }

}
