package lia;

public class MemoriaInteligente {

    public static boolean tentarSalvar(String comando) {

        comando = comando.toLowerCase();

        String[][] padroes = {

            {"meu filho", "filho"},
            {"minha filha", "filha"},
            {"minha neta", "neta"},
            {"meu neto", "neto"},
            {"minha esposa", "esposa"},
            {"meu marido", "marido"},
            {"meu carro", "carro"},
            {"meu cachorro", "cachorro"},
            {"minha cidade", "cidade"},
            {"meu banco", "banco"}

        };

        for (String[] p : padroes) {

            if (comando.contains(p[0])) {

                String valor = extrairValor(comando);

                if (valor != null && valor.length() > 1) {

                    //MemoriaDAO.salvarFato(p[1], valor);
                    MemoriaDAO.salvarFato(p[1], "nome", valor);

                    System.out.println("🧠 Memória aprendida: " + p[1] + " = " + valor);

                    return true;
                }
            }
        }

        return false;
    }

    private static String extrairValor(String frase) {

        String[] separadores = {" é ", " se chama ", " chama ", " é um ", " é uma "};

        for (String sep : separadores) {

            if (frase.contains(sep)) {

                String[] partes = frase.split(sep);

                if (partes.length > 1) {
                    return partes[1].trim();
                }
            }
        }

        return null;
    }
}