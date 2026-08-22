
package model;

import java.util.Date;

/**
 *
 * @author CARLOS
 */
public class Compras {
    private int id;
    private String fornecedorId;
    private CliFor fornecedor; // Novo atributo
    private Date dataCompra;
    private double totalCompra; // Alterado de valorTotal para totalCompra
    private String observacoes;

    // Construtor vazio
    public Compras() {
    }

    // Construtor com parâmetros
    public Compras(String fornecedorId, Date dataCompra, double totalCompra, String observacoes) { // Alterado para totalCompra
        this.fornecedorId = fornecedorId;
        this.dataCompra = dataCompra;
        this.totalCompra = totalCompra;
        this.observacoes = observacoes;
    }

    // Getters e setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFornecedorId() {
        return fornecedorId;
    }

    public void setFornecedorId(String fornecedorId) {
        this.fornecedorId = fornecedorId;
    }
    
    public CliFor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(CliFor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public Date getDataCompra() {
        return dataCompra;
    }

    public void setDataCompra(Date dataCompra) {
        this.dataCompra = dataCompra;
    }

    public double getTotalCompra() { // Alterado para getTotalCompra
        return totalCompra; // Alterado para totalCompra
    }

    public void setTotalCompra(double totalCompra) { // Alterado para setTotalCompra
        this.totalCompra = totalCompra; // Alterado para totalCompra
    }
    
    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    
    
}

