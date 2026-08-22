package model;

/**
 *
 * @author CARLOS
 */
public class CliFor {

    private String codCliFor;
    private String tipo;
    private String nomeCliFor;
    private String apelidoCliFor;
    private String email;
    private String celular;
    private String telefone;
    private String cep;
    private String endereco;
    private int numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String rg;
    private String cpf;
    private String contatoCliFor;
    private String obs;
    private Placon fkCliForGp;

    public String getCodCliFor() {
        return codCliFor;
    }

    public void setCodCliFor(String codCliFor) {
        this.codCliFor = codCliFor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNomeCliFor() {
        return nomeCliFor;
    }

    public void setNomeCliFor(String nomeCliFor) {
        this.nomeCliFor = nomeCliFor;
    }

    public String getApelidoCliFor() {
        return apelidoCliFor;
    }

    public void setApelidoCliFor(String apelidoCliFor) {
        this.apelidoCliFor = apelidoCliFor;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getContatoCliFor() {
        return contatoCliFor;
    }

    public void setContatoCliFor(String contatoCliFor) {
        this.contatoCliFor = contatoCliFor;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public Placon getFkCliForGp() {
        return fkCliForGp;
    }

    public void setFkCliForGp(Placon fkCliForGp) {
        this.fkCliForGp = fkCliForGp;
    }
    
    @Override
    public String toString() {
        return getNomeCliFor(); // Retorna o nome para ser exibido na JComboBox
    }
    
        // MÉTODO NOVO ? PRA USAR NO PDV E EM OUTROS LUGARES
    public int getId() {
        try {
            return Integer.parseInt(this.codCliFor);
        } catch (NumberFormatException e) {
            return 0; // ou -1 se preferir
        }
    }
}
