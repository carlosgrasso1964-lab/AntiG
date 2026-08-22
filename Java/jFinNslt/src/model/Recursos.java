
package model;

/**
 *
 * @author CARLOS
 */

public class Recursos {
    private String codigo;
    private String nomebco;
    private String agencia;
    private String fluxo;
    private double limite;
    private String abertura;
    private String encerramento;
    private String status;
    private Placon fk_gpprinc;

    public Recursos(String codigo, String nomebco, String agencia, String fluxo, double limite, String abertura, String encerramento, String status, Placon fk_gpprinc) {
        this.codigo = codigo;
        this.nomebco = nomebco;
        this.agencia = agencia;
        this.fluxo = fluxo;
        this.limite = limite;
        this.abertura = abertura;
        this.encerramento = encerramento;
        this.status = status;
        this.fk_gpprinc = fk_gpprinc;
    }

    public Recursos() {
        
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNomebco() {
        return nomebco;
    }

    public void setNomebco(String nomebco) {
        this.nomebco = nomebco;
    }

    public String getAgencia() {
        return agencia;
    }

    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }

    public String getFluxo() {
        return fluxo;
    }

    public void setFluxo(String fluxo) {
        this.fluxo = fluxo;
    }

    public double getLimite() {
        return limite;
    }

    public void setLimite(double limite) {
        this.limite = limite;
    }

    public String getAbertura() {
        return abertura;
    }

    public void setAbertura(String abertura) {
        this.abertura = abertura;
    }

    public String getEncerramento() {
        return encerramento;
    }

    public void setEncerramento(String encerramento) {
        this.encerramento = encerramento;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Placon getFk_gpprinc() {
        return fk_gpprinc;
    }

    public void setFk_gpprinc(Placon fk_gpprinc) {
        this.fk_gpprinc = fk_gpprinc;
    }
    
    
}
