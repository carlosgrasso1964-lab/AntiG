package lia;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NoticiasLocais {

    public static String buscarNoticias() {

        StringBuilder noticias = new StringBuilder();
        
        noticias.insert(0, "Manchetes de Sorocaba agora. ");
        
        noticias.append(buscarCruzeiro());
        noticias.append(buscarIpanema());
        noticias.append(buscarZNorte());

        return noticias.toString();
    }

    // ===============================
    // JORNAL CRUZEIRO
    // ===============================
    private static String buscarCruzeiro() {

        StringBuilder noticias = new StringBuilder();

        try {

            Document doc = Jsoup.connect("https://www.jornalcruzeiro.com.br/")
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Elements titulos = doc.select("h1, h2, h3");

            noticias.append("\nJornal Cruzeiro:\n");

            int contador = 0;

            for (Element titulo : titulos) {

                String texto = titulo.text();

                if (texto.length() > 35) {

                    noticias.append("- ").append(texto).append("\n");

                    contador++;

                    if (contador >= 3) {
                        break;
                    }
                }
            }

        } catch (Exception e) {

            noticias.append("Não consegui acessar o Jornal Cruzeiro. ");

        }

        return noticias.toString();
    }

    // ===============================
    // JORNAL IPANEMA
    // ===============================
    private static String buscarIpanema() {

        StringBuilder noticias = new StringBuilder();

        try {

            Document doc = Jsoup.connect("https://www.jornalipanema.com.br/")
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Elements titulos = doc.select("h2, h3");

            noticias.append("\nJornal Ipanema:\n");

            int contador = 0;

            for (Element titulo : titulos) {

                String texto = titulo.text();

                if (texto.length() > 35) {

                    noticias.append("- ").append(texto).append("\n");

                    contador++;

                    if (contador >= 3) {
                        break;
                    }
                }
            }

        } catch (Exception e) {

            noticias.append("Não consegui acessar o Jornal Ipanema. ");

        }

        return noticias.toString();
    }

    // ===============================
    // JORNAL Z NORTE
    // ===============================
    private static String buscarZNorte() {

        StringBuilder noticias = new StringBuilder();

        try {

            Document doc = Jsoup.connect("https://jornalznorte.com.br/")
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Elements titulos = doc.select("h2, h3");
            
            noticias.append("");
            noticias.append("\nZ Norte:\n");

            int contador = 0;

            for (Element titulo : titulos) {

                String texto = titulo.text();

                if (texto.length() > 35) {

                    noticias.append("- ").append(texto).append("\n");

                    contador++;

                    if (contador >= 3) {
                        break;
                    }
                }
            }

        } catch (Exception e) {

            noticias.append("Não consegui acessar o Z Norte. ");

        }
        
        return noticias.toString();
    }
}
