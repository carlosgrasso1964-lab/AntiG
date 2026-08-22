package lia;

public class DetectorIntencao {

    public static Intencao detectar(String comando) {

        comando = comando.toLowerCase();

        if (comando.contains("quantos") || comando.contains("quantas"))
            return Intencao.CONTAR;

        if (comando.contains("listar") || comando.contains("mostre"))
            return Intencao.LISTAR;

        if (comando.contains("qual") || comando.contains("quem") || comando.contains("como"))
            return Intencao.CONSULTAR;

        return Intencao.DESCONHECIDA;
    }
}