
package model;

/**
 *
 * @author Usuário
 */
public class Categoria {
    private int id;
    private String nome;
    private Double multiplicador;
   
    // Construtor sem parâmetros
    public Categoria() {
    }

    // Construtor que aceita nome e multiplicador
    public Categoria(String nome, double multiplicador) {
        this.nome = nome;
        this.multiplicador = multiplicador;
    }
    
    // Getter e setter para o ID da categoria
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter e setter para o nome da categoria
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // Getter e setter para o multiplicador
    public Double getMultiplicador() {
        return multiplicador;
    }

    public void setMultiplicador(Double multiplicador) {
        this.multiplicador = multiplicador;
    }
}