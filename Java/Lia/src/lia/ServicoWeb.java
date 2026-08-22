package lia;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServicoWeb {

    private static final String URL_MOEDAS = "https://economia.awesomeapi.com.br/last/USD-BRL,EUR-BRL,GBP-BRL,BTC-BRL";
    private static final String URL_NOTICIAS_1 = "https://agenciabrasil.ebc.com.br/rss/ultimasnoticias/feed.xml";
    private static final String URL_NOTICIAS_2 = "https://rss.uol.com.br/feed/noticias.xml";

    public String obterCotacoes() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_MOEDAS))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return formatarRespostaMoedas(response.body());
            }
            return "Não consegui acessar os dados financeiros.";
        } catch (Exception e) {
            return "Erro ao conectar com o servidor de economia.";
        }
    }

    private String formatarRespostaMoedas(String jsonString) {

        try {

            JSONObject json = new JSONObject(jsonString);

            String dolar = json.getJSONObject("USDBRL").getString("bid");
            String euro = json.getJSONObject("EURBRL").getString("bid");
            String libra = json.getJSONObject("GBPBRL").getString("bid");
            String btc = json.getJSONObject("BTCBRL").getString("bid");

            String btcFormatado = btc.substring(0, btc.indexOf(".") + 3);

            return "💰 Mercado agora:\n\n"
                    + "Dólar: R$ " + dolar.substring(0, 4).replace(".", ",") + "\n"
                    + "Euro: R$ " + euro.substring(0, 4).replace(".", ",") + "\n"
                    + "Libra: R$ " + libra.substring(0, 4).replace(".", ",") + "\n"
                    + "Bitcoin: R$ " + btcFormatado.replace(".", ",");

        } catch (Exception e) {

            return "Erro ao processar dados do mercado.";

        }
    }

    public String obterNoticias() {
        try {

            String fonte1 = buscarFeed(URL_NOTICIAS_1);
            String fonte2 = buscarFeed(URL_NOTICIAS_2);

            String noticias1 = formatarRespostaNoticias(fonte1);
            String noticias2 = formatarRespostaNoticias(fonte2);

            return "\n📰 Agência Brasil:\n"
                    + noticias1
                    + "\n🌎 UOL Notícias:\n"
                    + noticias2;

        } catch (Exception e) {
            return "Erro ao buscar notícias.";
        }
    }

    private String formatarRespostaNoticias(String xml) {
        StringBuilder sb = new StringBuilder();
        sb.append("Aqui estão as últimas notícias do portal: \n");

        try {
            // Regex mais "agressivo" para capturar o que estiver entre <title> e </title>
            // Ignora espaços e quebras de linha
            Pattern pattern = Pattern.compile("<title>(.*?)</title>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(xml);

            int contador = 0;
            while (matcher.find() && contador < 6) {
                String titulo = matcher.group(1).trim();

                // Limpa possíveis tags CDATA ou sujeira que sobrou
                titulo = titulo.replace("<![CDATA[", "").replace("]]>", "");

                // Pula o primeiro título (que é o nome do site "UOL Notícias")
                if (contador > 1 && !titulo.isEmpty()) {
                    sb.append("🔹 ").append(titulo).append(". \n");
                }
                contador++;
            }

            if (contador <= 1) {
                return "Encontrei o portal, mas as manchetes estavam vazias.";
            }

        } catch (Exception e) {
            return "Erro ao processar o texto das notícias.";
        }

        return sb.toString();
    }

    private String buscarFeed(String url) throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "text/xml, application/xml")
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        }

        return "";
    }

    public String obterIndicesEconomicos() {

        try {

            String ipca = buscarIndice(
                    "https://api.bcb.gov.br/dados/serie/bcdata.sgs.433/dados/ultimos/1?formato=json");

            String igpm = buscarIndice(
                    "https://api.bcb.gov.br/dados/serie/bcdata.sgs.189/dados/ultimos/1?formato=json");

            return "📊 Índices econômicos recentes:\n\n"
                    + "IPCA: " + ipca + "%\n"
                    + "IGP-M: " + igpm + "%";

        } catch (Exception e) {
            return "Não consegui consultar os índices econômicos.";
        }
    }

    public String obterMercado() {

        try {

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.hgbrasil.com/finance"))
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            HttpResponse<String> response
                    = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());

            JSONObject results = json.getJSONObject("results");

            JSONObject moedas = results.getJSONObject("currencies");
            JSONObject bolsa = results.getJSONObject("stocks");

            double dolar = moedas.getJSONObject("USD").getDouble("buy");
            double euro = moedas.getJSONObject("EUR").getDouble("buy");
            double btc = moedas.getJSONObject("BTC").getDouble("buy");

            JSONObject ibov = bolsa.getJSONObject("IBOVESPA");

            double ibovPts = ibov.getDouble("points");
            double ibovVar = ibov.getDouble("variation");

            return "📊 Painel econômico.\n\n"
                    + "📈 IBOVESPA: " + String.format("%,.0f", ibovPts) + " pontos (" + ibovVar + "%).\n"
                    + "💵 Dólar: " + String.format("R$ %.2f", dolar) + ".\n"
                    + "💶 Euro: " + String.format("R$ %.2f", euro) + ".\n"
                    + "₿ Bitcoin: " + String.format("R$ %,.0f", btc) + ".";

        } catch (Exception e) {

            e.printStackTrace();
            return "Erro ao consultar mercado.";

        }
    }

    private String buscarIndice(String url) throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {

            String json = response.body();

            Pattern pattern = Pattern.compile("\"valor\":\"(.*?)\"");
            Matcher matcher = pattern.matcher(json);

            if (matcher.find()) {
                return matcher.group(1).replace(".", ",");
            }
        }

        return "N/D";
    }
}
