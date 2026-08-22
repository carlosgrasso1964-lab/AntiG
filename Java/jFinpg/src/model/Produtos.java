package model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Produtos {

    private int id;
    private String descricao;
    private double preco;
    private double qtd_estoque;
    private double qtd_estoqueMinimo;
    private double qtd_estoqueMaximo;
    private CliFor clifor;
    private double precoMedioCusto; // Campo para armazenar o preço médio de custo
    private double preco_venda; // Novo campo
    private Categoria categoria; // Novo campo

    // Construtor padrão (vazio)
    public Produtos() {
    }

    // Construtor com todos os campos
    public Produtos(int id, String descricao, double preco, double qtd_estoque, double qtd_estoqueMinimo,
            double qtd_estoqueMaximo, CliFor fornecedores, double precoMedioCusto,
            double preco_venda, Categoria categoria) {
        this.id = id;
        this.descricao = descricao;
        this.preco = preco;
        this.qtd_estoque = qtd_estoque;
        this.qtd_estoqueMinimo = qtd_estoqueMinimo;
        this.qtd_estoqueMaximo = qtd_estoqueMaximo;
        this.clifor = clifor;
        this.precoMedioCusto = precoMedioCusto;
        this.preco_venda = preco_venda;
        this.categoria = categoria;
    }

    // Construtor simplificado para listagem
    public Produtos(int id, String descricao, double preco, double qtd_estoque) {
        this.id = id;
        this.descricao = descricao;
        this.preco = preco;
        this.qtd_estoque = qtd_estoque;
    }

    // Getters e Setters (mantidos como estão)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public double getQtd_estoque() {
        return qtd_estoque;
    }

    public void setQtd_estoque(double qtd_estoque) {
        this.qtd_estoque = qtd_estoque;
    }

    public double getQtd_estoqueMinimo() {
        return qtd_estoqueMinimo;
    }

    public void setQtd_estoqueMinimo(double qtd_estoqueMinimo) {
        this.qtd_estoqueMinimo = qtd_estoqueMinimo;
    }

    public double getQtd_estoqueMaximo() {
        return qtd_estoqueMaximo;
    }

    public void setQtd_estoqueMaximo(double qtd_estoqueMaximo) {
        this.qtd_estoqueMaximo = qtd_estoqueMaximo;
    }

    public CliFor getCliFor() {
        return clifor;
    }

    public void setCliFor(CliFor clifor) {
        this.clifor = clifor;
    }

    public double getPrecoMedioCusto() {
        return precoMedioCusto;
    }

    public void setPrecoMedioCusto(double precoMedioCusto) {
        this.precoMedioCusto = precoMedioCusto;
    }

    public double getPrecoVenda() {
        return preco_venda;
    }

    public void setPrecoVenda(double preco_venda) {
        this.preco_venda = preco_venda;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    // Métodos de formatação
    public String getQtd_Formatada() {
        return String.format("%.3f", qtd_estoque);
    }

    public String getQtd_FormatadaMinimo() {
        return String.format("%.3f", qtd_estoqueMinimo);
    }

    public String getQtd_FormatadaMaximo() {
        return String.format("%.3f", qtd_estoqueMaximo);
    }

//    public void calcularPrecoVenda() {
//        if (categoria != null && categoria.getMultiplicador() != null) {
//            BigDecimal precoCusto = BigDecimal.valueOf(this.precoMedioCusto);
//            BigDecimal multiplicador = BigDecimal.valueOf(categoria.getMultiplicador());
//            this.preco_venda = precoCusto.multiply(multiplicador)
//                    .setScale(2, RoundingMode.HALF_UP)
//                    .doubleValue();
//            //System.out.println("Preço de venda calculado com sucesso: " + this.preco_venda);
//        } else {
//            System.out.println("Erro: Categoria ou multiplicador inválido ao calcular preço de venda.");
//        }
//    }
        /**
     * Calcula automaticamente o preço de venda com base no preço médio de custo
     * e no multiplicador da categoria.
     * Usa BigDecimal para precisão monetária.
     */
    public void calcularPrecoVenda() {
        if (categoria != null && categoria.getMultiplicador() > 0 && precoMedioCusto > 0) {
            BigDecimal custo = BigDecimal.valueOf(precoMedioCusto);
            BigDecimal multiplicador = BigDecimal.valueOf(categoria.getMultiplicador());
            BigDecimal resultado = custo.multiply(multiplicador);
            this.preco_venda = resultado.setScale(2, RoundingMode.HALF_UP).doubleValue();
        } else {
            this.preco_venda = 0.0;
        }
    }
}
