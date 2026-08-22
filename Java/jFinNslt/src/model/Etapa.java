package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Etapa {

    private int id;
    private int projetoId;
    private String nome;
    private String prioridade;
    private String responsavel;
    private LocalDate dataInicioPrevista;
    private LocalDate dataFimPrevista;
    private LocalDate dataInicioRealizada;
    private LocalDate dataFimRealizada;
    private String status;
    private String obs;
    private BigDecimal valorPrevisto;
    private BigDecimal valorRealizado;

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public Etapa() {
    }

//    public Etapa(int id, int projetoId, String nome, LocalDate dataInicioPrevista, LocalDate dataFimPrevista, LocalDate dataInicioRealizada, LocalDate dataFimRealizada, String status) {
//        this.id = id;
//        this.projetoId = projetoId;
//        this.nome = nome;
//        this.dataInicioPrevista = dataInicioPrevista;
//        this.dataFimPrevista = dataFimPrevista;
//        this.dataInicioRealizada = dataInicioRealizada;
//        this.dataFimRealizada = dataFimRealizada;
//        this.status = status;
//    }
    public Etapa(int id, int projetoId, String nome, String prioridade, String responsavel, LocalDate dataInicioPrevista, LocalDate dataFimPrevista, LocalDate dataInicioRealizada, LocalDate dataFimRealizada, String status, String obs) {
        this.id = id;
        this.projetoId = projetoId;
        this.nome = nome;
        this.prioridade = prioridade;
        this.responsavel = responsavel;
        this.dataInicioPrevista = dataInicioPrevista;
        this.dataFimPrevista = dataFimPrevista;
        this.dataInicioRealizada = dataInicioRealizada;
        this.dataFimRealizada = dataFimRealizada;
        this.status = status;
        this.obs = obs;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjetoId() {
        return projetoId;
    }

    public void setProjetoId(int projetoId) {
        this.projetoId = projetoId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataInicioPrevista() {
        return dataInicioPrevista;
    }

    public void setDataInicioPrevista(LocalDate dataInicioPrevista) {
        this.dataInicioPrevista = dataInicioPrevista;
    }

    public LocalDate getDataFimPrevista() {
        return dataFimPrevista;
    }

    public void setDataFimPrevista(LocalDate dataFimPrevista) {
        this.dataFimPrevista = dataFimPrevista;
    }

    public LocalDate getDataInicioRealizada() {
        return dataInicioRealizada;
    }

    public void setDataInicioRealizada(LocalDate dataInicioRealizada) {
        this.dataInicioRealizada = dataInicioRealizada;
    }

    public LocalDate getDataFimRealizada() {
        return dataFimRealizada;
    }

    public void setDataFimRealizada(LocalDate dataFimRealizada) {
        this.dataFimRealizada = dataFimRealizada;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getValorPrevisto() {
        return valorPrevisto == null ? BigDecimal.ZERO : valorPrevisto;
    }

    public void setValorPrevisto(BigDecimal valorPrevisto) {
        this.valorPrevisto = valorPrevisto;
    }

    public BigDecimal getValorRealizado() {
        return valorRealizado == null ? BigDecimal.ZERO : valorRealizado;
    }

    public void setValorRealizado(BigDecimal valorRealizado) {
        this.valorRealizado = valorRealizado;
    }

}
