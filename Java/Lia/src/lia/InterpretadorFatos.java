package lia;

public class InterpretadorFatos {

    public static boolean interpretar(String comando) {

        comando = comando.toLowerCase();

        // ===============================
        // NETAS (plural)
        // ===============================
        if (comando.contains("minhas netas são")) {

            String nomes = comando.replace("minhas netas são", "").trim();

            String[] lista = nomes.split(" e |, ");

            for (String nome : lista) {

                MemoriaService.salvarFato("neta", nome.trim());

            }

            return true;
        }

        // ===============================
        // NETA (singular)
        // ===============================
        if (comando.contains("minha neta se chama")) {

            String nome = comando.replace("minha neta se chama", "").trim();

            MemoriaService.salvarFato("neta", nome);

            return true;
        }

        // ===============================
        // FILHO
        // ===============================
        if (comando.contains("meu filho se chama")) {

            String nome = comando.replace("meu filho se chama", "").trim();

            MemoriaService.salvarFato("filho", nome);

            return true;
        }

        // ===============================
        // CIDADE
        // ===============================
        if (comando.contains("moro em")) {

            String cidade = comando.replace("moro em", "").trim();

            MemoriaService.salvarFato("cidade", cidade);

            return true;
        }

        return false;
    }

}
