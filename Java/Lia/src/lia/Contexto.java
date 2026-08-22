package lia;

public class Contexto {

    private static String ultimaEntidade;

    public static void setUltimaEntidade(String entidade) {
        ultimaEntidade = entidade;
    }

    public static String getUltimaEntidade() {
        return ultimaEntidade;
    }

    public static void limpar() {
        ultimaEntidade = null;
    }
}