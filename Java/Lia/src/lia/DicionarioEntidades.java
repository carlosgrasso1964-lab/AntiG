package lia;

public class DicionarioEntidades {

    public static String detectarEntidade(String comando) {

        comando = comando.toLowerCase().replaceAll("[^a-zà-ú0-9\\s]", "");

        if (comando.contains("filha") || comando.contains("filhas")) {
            return "filha";
        }
        if (comando.contains("filho") || comando.contains("filhos")) {
            return "filho";
        }
        if (comando.contains("neta") || comando.contains("netas")) {
            return "neta";
        }
        if (comando.contains("neto") || comando.contains("netos")) {
            return "neto";
        }
        if (comando.contains("cachorro") || comando.contains("cachorros")) {
            return "cachorro";
        }
        if (comando.contains("gato") || comando.contains("gatos")) {
            return "gato";
        }
        if (comando.contains("carro") || comando.contains("carros")) {
            return "carro";
        }
        if (comando.contains("cidade")) {
            return "cidade";
        }

        // 🔥 PRONOMES → NÃO retorna "ele"
        String[] palavras = comando.split("\\s+");

        for (String p : palavras) {
            if (p.equals("ele") || p.equals("ela")
                    || p.equals("dele") || p.equals("dela")) {

                return null; // ⚠️ deixa o contexto resolver
            }
        }

        return null;
    }
}
