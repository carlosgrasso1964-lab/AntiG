package lia;

public class AprendizAutomatico {

    public static boolean aprender(String comando, LiaGUI gui) {

        comando = comando.toLowerCase().trim();

        // =========================================
        // PADRÃO 1: "X é Y"
        // =========================================
        if (comando.contains(" é ")) {

            String[] partes = comando.split(" é ");

            if (partes.length == 2) {

                String esquerda = partes[0].trim();  // "meu carro"
                String direita = partes[1].trim();   // "vermelho"

                String entidade = DicionarioEntidades.detectarEntidade(esquerda);

                if (entidade != null) {

                    String valor = limparValor(direita);
                    String relacao = inferirRelacao(valor);

                    MemoriaService.salvarTripla(entidade, relacao, valor);
                    
                    // 🔥 SALVA CONTEXTO DA CONVERSA
                    MemoriaService.salvarContexto(entidade, relacao);
                    Contexto.setUltimaEntidade(entidade);
                    
                    gui.falarLinha("Aprendi que o " + entidade + " tem " + relacao + " " + valor);

                    return true;
                }
            }
        }

        // =========================================
        // PADRÃO 2: "meu X se chama Y"
        // =========================================
        if (comando.contains("se chama")) {

            String[] partes = comando.split("se chama");

            if (partes.length == 2) {

                String esquerda = partes[0].trim(); // "meu filho"
                String direita = partes[1].trim();  // "joão"

                String entidade = DicionarioEntidades.detectarEntidade(esquerda);

                if (entidade != null) {

                    String nome = limparValor(direita);

                    MemoriaService.salvarTripla(entidade, "nome", nome);
                    // 🔥 SALVA CONTEXTO DA CONVERSA
                    MemoriaService.salvarContexto(entidade, "nome");
                    Contexto.setUltimaEntidade(entidade);

                    gui.falarLinha("Aprendi o nome do seu " + entidade);

                    return true;
                }
            }
        }

        return false;
    }

    // =========================================
    // LIMPAR TEXTO
    // =========================================
    private static String limparValor(String texto) {

        texto = texto.replace("um ", "")
                     .replace("uma ", "")
                     .replace(".", "")
                     .trim();

        return texto;
    }

    // =========================================
    // INFERIR RELAÇÃO
    // =========================================
    private static String inferirRelacao(String valor) {

        // cores
        if (valor.matches("vermelho|azul|preto|branco|verde|amarelo|branca|vermelha|azul|amarela"))
            return "cor";

        // raças de cachorro (exemplo)
        if (valor.matches("poodle|labrador|bulldog|pastor"))
            return "raca";

        // fallback
        return "descricao";
    }
}