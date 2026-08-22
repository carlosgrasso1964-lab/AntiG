package model;

public class Transferencia {

    private String origemNr;
    private String origemClass;
    private String destinoNr;
    private String destinoClass;
    private String documento;
    private String data;
    private double valor;

    // Construtor
    public Transferencia(String origemNr, String origemClass, String destinoNr, String destinoClass,
            String documento, String data, double valor) {
        this.origemNr = origemNr;
        this.origemClass = origemClass;
        this.destinoNr = destinoNr;
        this.destinoClass = destinoClass;
        this.documento = documento;
        this.data = data;
        this.valor = valor;
    }

    // Getters e Setters
    public String getOrigemNr() {
        return origemNr;
    }

    public void setOrigemNr(String origemNr) {
        this.origemNr = origemNr;
    }

    public String getOrigemClass() {
        return origemClass;
    }

    public void setOrigemClass(String origemClass) {
        this.origemClass = origemClass;
    }

    public String getDestinoNr() {
        return destinoNr;
    }

    public void setDestinoNr(String destinoNr) {
        this.destinoNr = destinoNr;
    }

    public String getDestinoClass() {
        return destinoClass;
    }

    public void setDestinoClass(String destinoClass) {
        this.destinoClass = destinoClass;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
}
