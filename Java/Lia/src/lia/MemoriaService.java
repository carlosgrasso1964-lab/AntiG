package lia;

public class MemoriaService {

    public static String buscarFato(String sujeito, String relacao) {

        var lista = MemoriaDAO.buscarPorRelacao(sujeito, relacao);

        if (!lista.isEmpty()) {
            return lista.get(0);
        }

        return null;
    }

    // 🔥 CORRIGIDO: AGORA SALVA CONTEXTO AUTOMATICAMENTE
    public static void salvarFato(String sujeito, String relacao, String objeto) {
        MemoriaDAO.salvarFatoTripla(sujeito, relacao, objeto);

        // ✅ ESSA LINHA RESOLVE O SEU PROBLEMA
        salvarContexto(sujeito, relacao);
    }

    // ======================================================
    // SALVAR CONVERSA
    // ======================================================
    public static void salvarConversa(String pergunta, String resposta) {
        MemoriaDAO.salvar(pergunta, resposta);
    }

    // ======================================================
    // RECUPERAR CONTEXTO
    // ======================================================
    public static String recuperarContexto(int limite) {
        return MemoriaDAO.recuperarContexto(limite);
    }

    // ======================================================
    // ADAPTADOR (LEGADO)
    // ======================================================
    public static void salvarFato(String chave, String valor) {
        salvarFato(chave, "nome", valor);
    }

    // ======================================================
    // MEMÓRIA INTELIGENTE
    // ======================================================
    public static boolean aprenderFato(String comando) {
        comando = comando.toLowerCase();

        if (comando.contains("minha neta se chama")) {
            String nome = comando.replace("minha neta se chama", "").trim();
            salvarFato("neta", nome);
            return true;
        }

        if (comando.contains("meu filho se chama")) {
            String nome = comando.replace("meu filho se chama", "").trim();
            salvarFato("filho", nome);
            return true;
        }

        if (comando.contains("moro em")) {
            String cidade = comando.replace("moro em", "").trim();
            salvarFato("cidade", cidade);
            return true;
        }

        return false;
    }

    public static String buscarMemoriaRelevante(String pergunta) {
        return MemoriaDAO.buscarMemoriaParecida(pergunta);
    }

    // ======================================================
    // CONTAGEM / LISTAGEM
    // ======================================================
    public static int contarFatos(String chave) {
        return MemoriaDAO.contarFatos(chave);
    }

    public static java.util.List<String> listarFatos(String chave) {
        return MemoriaDAO.listarFatos(chave);
    }

    public static void salvarTripla(String sujeito, String relacao, String objeto) {
        salvarFato(sujeito, relacao, objeto); // 🔥 usa método correto
    }

    public static java.util.List<String> buscarTripla(String sujeito, String relacao) {
        return MemoriaDAO.buscarPorRelacao(sujeito, relacao);
    }

    public static int contarTripla(String sujeito, String relacao) {
        return MemoriaDAO.contarTripla(sujeito, relacao);
    }

    // ======================================================
    // CONTEXTO (FONTE OFICIAL)
    // ======================================================
    private static String ultimoSujeito = null;
    private static String ultimaRelacao = null;

    public static void salvarContexto(String sujeito, String relacao) {

        // ❌ NÃO salvar relações como sujeito
        if (sujeito == null) {
            return;
        }

        if (sujeito.equals("cor") || sujeito.equals("nome")) {
            return;
        }

        ultimoSujeito = sujeito;
        ultimaRelacao = relacao;

        System.out.println("🔥 CONTEXTO SALVO: " + sujeito);
    }

    public static String getUltimoSujeito() {
        return ultimoSujeito;
    }

    public static String getUltimaRelacao() {
        return ultimaRelacao;
    }

    public static int contarFato(String sujeito) {
        return MemoriaDAO.contarPorSujeito(sujeito);
    }

    public static java.util.List<String> listarFato(String sujeito) {
        return MemoriaDAO.listarPorSujeito(sujeito);
    }
}
