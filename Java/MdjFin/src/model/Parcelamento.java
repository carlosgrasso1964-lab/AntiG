package model;

import java.math.BigDecimal;

public class Parcelamento {

    private int idMov;
    private String recurso;
    private String vrecurso;
    private String clifor;
    private String vCliFor;
    private String dtlancto;
    private String dtEmi;
    private String dtVcto;
    private String documento;
    private String classif;
    private String descricao;
    private BigDecimal valor;
    private String statusMov;
    private String prev;

    // Construtor padrão
    public Parcelamento() {
    }

    // Getters e Setters
    public int getIdMov() {
        return idMov;
    }

    public void setIdMov(int idMov) {
        this.idMov = idMov;
    }

    public String getRecurso() {
        return recurso;
    }

    public void setRecurso(String recurso) {
        this.recurso = recurso;
    }

    public String getVrecurso() {
        return vrecurso;
    }

    public void setVrecurso(String vrecurso) {
        this.vrecurso = vrecurso;
    }

    public String getClifor() {
        return clifor;
    }

    public void setClifor(String clifor) {
        this.clifor = clifor;
    }

    public String getVCliFor() {
        return vCliFor;
    }

    public void setVCliFor(String vCliFor) {
        this.vCliFor = vCliFor;
    }

    public String getDtlancto() {
        return dtlancto;
    }

    public void setDtlancto(String dtlancto) {
        this.dtlancto = dtlancto;
    }

    public String getDtEmi() {
        return dtEmi;
    }

    public void setDtEmi(String dtEmi) {
        this.dtEmi = dtEmi;
    }

    public String getDtVcto() {
        return dtVcto;
    }

    public void setDtVcto(String dtVcto) {
        this.dtVcto = dtVcto;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getClassif() {
        return classif;
    }

    public void setClassif(String classif) {
        this.classif = classif;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getStatusMov() {
        return statusMov;
    }

    public void setStatusMov(String statusMov) {
        this.statusMov = statusMov;
    }

    public String getPrev() {
        return prev;
    }

    public void setPrev(String prev) {
        this.prev = prev;
    }
}