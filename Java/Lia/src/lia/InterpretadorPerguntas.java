package lia;

import java.util.List;

public class InterpretadorPerguntas {

    public static boolean interpretar(String comando, LiaGUI gui) {

        comando = comando.toLowerCase();

        // ================================
        // 1. EXTRAÇÃO SEMÂNTICA
        // ================================
        Intencao intencao = DetectorIntencao.detectar(comando);
        String entidade = DicionarioEntidades.detectarEntidade(comando);
        String relacao = DicionarioRelacoes.detectarRelacao(comando);

        // 🔥 resolver "ele / ela / dele / dela"
        if ("ele".equals(entidade) || "ela".equals(entidade) 
                || "dele".equals(entidade) || "dela".equals(entidade)) {
            entidade = Contexto.getUltimaEntidade();
        }

        // 🔥 usar contexto se não veio entidade
        if (entidade == null) {
            entidade = Contexto.getUltimaEntidade();
        }

        // 🔥 salvar contexto
        if (entidade != null) {
            Contexto.setUltimaEntidade(entidade);
        }

        // DEBUG
        System.out.println("INTENÇÃO: " + intencao);
        System.out.println("ENTIDADE: " + entidade);
        System.out.println("RELAÇÃO: " + relacao);

        // ================================
        // 2. PROCESSAMENTO
        // ================================
        switch (intencao) {

            // ============================
            // CONTAR
            // ============================
            case CONTAR:

                if (entidade == null) {
                    gui.falarLinha("Você pode me dizer de quem estamos falando?");
                    return true;
                }

                int total;

                if (relacao != null) {
                    total = MemoriaService.contarTripla(entidade, relacao);
                } else {
                    total = MemoriaService.contarFato(entidade);
                }

                String plural = pluralizar(entidade);

                if (total == 1) {
                    gui.falarLinha("Você tem 1 " + entidade + ".");
                } else {
                    gui.falarLinha("Você tem " + total + " " + plural + ".");
                }

                return true;

            // ============================
            // CONSULTAR
            // ============================
            case CONSULTAR:

                if (entidade == null) {
                    gui.falarLinha("Você pode me dizer de quem estamos falando?");
                    return true;
                }

                if (relacao == null) {
                    relacao = "nome";
                }

                String valor = MemoriaService.buscarFato(entidade, relacao);

                if (valor != null) {
                    gui.falarLinha(formatarRespostaConsulta(entidade, relacao, valor));
                } else {
                    gui.falarLinha("Ainda não sei essa informação.");
                }

                return true;

            // ============================
            // LISTAR
            // ============================
            case LISTAR:

                if (entidade == null) {
                    entidade = MemoriaDAO.buscarUltimoSujeito();
                }

                if (entidade == null) {
                    gui.falarLinha("Você pode me dizer de quem estamos falando?");
                    return true;
                }

                List<String> lista;

                if (relacao != null) {
                    lista = MemoriaService.buscarTripla(entidade, relacao);
                } else {
                    lista = MemoriaService.listarFato(entidade);
                }

                if (lista == null || lista.isEmpty()) {
                    gui.falarLinha("Não encontrei registros.");
                } else {
                    gui.falarLinha(formatarLista(entidade, lista));
                }

                return true;

            default:
                return false;
        }
    }
    
    

    // ========================================
    // FORMATAR RESPOSTA CONSULTA
    // ========================================
    private static String formatarRespostaConsulta(String entidade, String relacao, String valor) {

        if (valor == null) {
            return "Ainda não sei essa informação.";
        }

        String artigo;

        if ("filha".equals(entidade) || "neta".equals(entidade) || entidade.endsWith("a")) {
            artigo = "sua";
        } else {
            artigo = "seu";
        }

        if ("nome".equals(relacao)) {

            if ("filha".equals(entidade) || "neta".equals(entidade)) {
                return "O nome da sua " + entidade + " é " + valor;
            } else {
                return "O nome do seu " + entidade + " é " + valor;
            }
        }

        if ("cor".equals(relacao)) {
            String preposicao = entidade.endsWith("a") ? "da" : "do";
            return "A cor " + preposicao + " " + artigo + " " + entidade + " é " + valor;
        }

        return "O " + relacao + " do " + artigo + " " + entidade + " é " + valor;
    }

    // ========================================
    // FORMATAR LISTA
    // ========================================
    private static String formatarLista(String entidade, List<String> lista) {

        String plural = pluralizar(entidade);
        return plural + ": " + String.join(", ", lista);
    }

    // ========================================
    // PLURAL
    // ========================================
    private static String pluralizar(String entidade) {

        switch (entidade) {
            case "filha":
                return "filhas";
            case "filho":
                return "filhos";
            case "cachorro":
                return "cachorros";
            case "carro":
                return "carros";
            case "neta":
                return "netas";
            default:
                return entidade + "s";
        }
    }
}
