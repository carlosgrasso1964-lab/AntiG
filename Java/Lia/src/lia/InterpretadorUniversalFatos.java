package lia;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InterpretadorUniversalFatos {

    public static Fato interpretar(String frase) {

        frase = frase.toLowerCase().trim();

        // ================================
        // PADRÃO 1
        // ================================
        Pattern p1 = Pattern.compile("(meu|minha) (\\w+) (se chama|chama|é chamada|é chamado) (.+)");
        Matcher m1 = p1.matcher(frase);

        if (m1.find()) {
            String entidade = m1.group(2);
            String nome = m1.group(4).trim();
            return new Fato(entidade, "nome", nome);
        }

        // ================================
        // PADRÃO 2
        // ================================
        Pattern p2 = Pattern.compile("tenho (um|uma) (\\w+) (chamado|chamada) (.+)");
        Matcher m2 = p2.matcher(frase);

        if (m2.find()) {
            String entidade = m2.group(2);
            String nome = m2.group(4).trim();
            return new Fato(entidade, "nome", nome);
        }

        // ================================
        // PADRÃO 3
        // ================================
        Pattern p3 = Pattern.compile("(meu|minha) (\\w+) é (.+)");
        Matcher m3 = p3.matcher(frase);

        if (m3.find()) {

            String entidade = m3.group(2);

            String valor = m3.group(3)
                    .replace("um ", "")
                    .replace("uma ", "")
                    .trim();

            String relacao = inferirRelacao(valor);

            if (relacao == null || relacao.isBlank()) {
                relacao = "descricao";
            }

            return new Fato(entidade, relacao, valor);
        }

        // 🔥 ESSA LINHA É OBRIGATÓRIA
        return null;
        //return new Fato("desconhecido", "desconhecido", frase);
    }

    private static String inferirRelacao(String valor) {

        valor = valor.toLowerCase();

        // CORES
        if (valor.matches("preto|branco|azul|vermelho|prata|cinza|verde|amarelo|branca|vermelha|azul|amarela|verde|rosado|laranja|roxo")) {
            return "cor";
        }

        // MARCAS
        if (valor.matches("toyota|honda|ford|chevrolet|fiat")) {
            return "marca";
        }

        // TIPOS
        if (valor.matches("suv|sedan|hatch")) {
            return "tipo";
        }

        // 🔥 FALLBACK INTELIGENTE
        return "descricao"; // NUNCA null
    }
}
