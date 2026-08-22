package modelos;

import java.util.ArrayList;
import java.util.List;

public class ProjetoEtapaHierarquico {
    private Object[] projeto;
    private List<Object[]> etapas = new ArrayList<>();

    public ProjetoEtapaHierarquico(Object[] projeto) {
        this.projeto = projeto;
    }

    public Object[] getProjeto() {
        return projeto;
    }

    public List<Object[]> getEtapas() {
        return etapas;
    }

    public void adicionarEtapa(Object[] etapa) {
        etapas.add(etapa);
    }
}