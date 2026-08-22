package lia;

public class Panorama {

    public static String gerar(ServicoWeb servicoWeb) {

        StringBuilder panorama = new StringBuilder();

        //panorama.append("Panorama do momento.\n\n");

        try {

            panorama.append("Notícias de Sorocaba:\n");
            panorama.append(NoticiasLocais.buscarNoticias());
            panorama.append("\n\n");

        } catch (Exception e) {

            panorama.append("Não consegui acessar notícias locais.\n\n");

        }

        try {

            panorama.append("Mercado financeiro:\n");

            String mercado = servicoWeb.obterMercado();

            panorama.append(mercado);

            panorama.append("\n\n");

        } catch (Exception e) {

            panorama.append("Não consegui acessar o mercado financeiro.\n\n");

        }

        return panorama.toString();
    }
}