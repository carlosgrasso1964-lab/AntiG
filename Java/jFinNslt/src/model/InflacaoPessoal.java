package model;

import java.text.DecimalFormat;

public class InflacaoPessoal {
    private String categoria;
    private String grupo;
    private double gastoAnterior;
    private double gastoAtual;
    private double pesoCategoria;
    private double variacaoPercent;
    private double contribuicaoInflacao;

    // Getters e Setters
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }
    
    public double getGastoAnterior() { return gastoAnterior; }
    public void setGastoAnterior(double gastoAnterior) { this.gastoAnterior = gastoAnterior; }
    
    public double getGastoAtual() { return gastoAtual; }
    public void setGastoAtual(double gastoAtual) { this.gastoAtual = gastoAtual; }
    
    public double getPesoCategoria() { return pesoCategoria; }
    public void setPesoCategoria(double pesoCategoria) { this.pesoCategoria = pesoCategoria; }
    
    public double getVariacaoPercent() { return variacaoPercent; }
    public void setVariacaoPercent(double variacaoPercent) { this.variacaoPercent = variacaoPercent; }
    
    public double getContribuicaoInflacao() { return contribuicaoInflacao; }
    public void setContribuicaoInflacao(double contribuicaoInflacao) { this.contribuicaoInflacao = contribuicaoInflacao; }
}
