package lia;

/**
 *
 * @author Carlos
 */
public class Fato {
    private String sujeito;
    private String relacao;
    private String objeto;
    
    public Fato(String sujeito, String relacao, String objeto) {
        this.sujeito = sujeito;
        this.relacao = relacao;
        this.objeto = objeto;
    }

    public String getSujeito() { return sujeito; }
    public String getRelacao() { return relacao; }
    public String getObjeto() { return objeto; }
    

    public void setSujeito(String sujeito) {
        this.sujeito = sujeito;
    }

 
    public void setRelacao(String relacao) {
        this.relacao = relacao;
    }

    
    public void setObjeto(String objeto) {
        this.objeto = objeto;
    }

}
