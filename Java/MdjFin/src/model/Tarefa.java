package model;

/**
 * Modelo de tarefa do Calendario To-Do (tabela tb_tarefas do MySQL jfin).
 * Espelha o calendario.php do jfip-web: id, data, descricao, concluida.
 *
 * @author CARLOS
 */
public class Tarefa {

    private int id;
    private String data;      // YYYY-MM-DD
    private String descricao;
    private boolean concluida;

    public Tarefa() {
    }

    public Tarefa(int id, String data, String descricao, boolean concluida) {
        this.id = id;
        this.data = data;
        this.descricao = descricao;
        this.concluida = concluida;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isConcluida() {
        return concluida;
    }

    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }
}
