package lia;

/**
 *
 * @author Carlos
 */
public class DicionarioRelacoes {

    public static String detectarRelacao(String comando) {

        //if (comando.contains("nome")) return "nome";
        if (comando.contains("nome") || comando.contains("chama")) return "nome";
        if (comando.contains("cor")) return "cor";
        if (comando.contains("tipo")) return "tipo";
        if (comando.contains("raça")) return "raca";

        return null;
    }
}
