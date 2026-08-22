package model;

import java.sql.Timestamp;

/**
 *
 * @author CARLOS
 */
public class Vendas {

    private int id;
    private CliFor clientes;
    private java.sql.Timestamp data_venda;
    private double total_venda;
    private String observacoes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CliFor getClientes() {
        return clientes;
    }

    public void setClientes(CliFor clientes) {
        this.clientes = clientes;
    }

    public Timestamp getData_venda() {
        return data_venda;
    }

    public void setData_venda(Timestamp data_venda) {
        this.data_venda = data_venda;
    }

    public double getTotal_venda() {
        return total_venda;
    }

    public void setTotal_venda(double total_venda) {
        this.total_venda = total_venda;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

}
