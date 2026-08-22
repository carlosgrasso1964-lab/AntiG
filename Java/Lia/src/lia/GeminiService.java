package lia;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GeminiService {

    // 1. Criamos a lista que guardará as últimas 10 interações (contexto)
    private List<String> historicoConversa = new ArrayList<>();
    private final int LIMITE_HISTORICO = 10;

    // 1. CAPTURA A CHAVE PRIMEIRO
    String ailia = System.getenv("KEYLIA");

    public String consultarIA(String pergunta) {

        try {

            if (ailia == null || ailia.isEmpty()) {
                System.out.println("Chave não encontrada!!!");
            }

            //listarModelosDisponiveis(); //Para quando quiser testar modelos disponíveis
            
            // 2. ENDEREÇO (Vamos usar o v1/gemini-1.5-flash, que é o mais estável)
            String endereco = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + ailia;
            /*
            Sugestão de upgrade para a Lia:
            Se quiser que ela use o que há de mais avançado (versão 3.1), use este endereço:
            https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent?key=
             */
            
            // 3. ADICIONA AO HISTÓRICO
            historicoConversa.add("Usuário: " + pergunta);

            // 4. MONTA O CONTEXTO
            StringBuilder contextoFull = new StringBuilder();
            contextoFull.append("Você é a Lia. Responda curto. Histórico:\n");
            for (String linha : historicoConversa) {
                contextoFull.append(linha).append("\n");
            }

            // 5. MONTA O JSON (Usando a sua biblioteca org.json)
            JSONObject textPart = new JSONObject().put("text", contextoFull.toString());
            JSONArray parts = new JSONArray().put(textPart);
            JSONObject content = new JSONObject().put("parts", parts);
            JSONArray contents = new JSONArray().put(content);
            JSONObject corpo = new JSONObject().put("contents", contents);

            // 6. EXECUTA A REQUISIÇÃO
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endereco))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(corpo.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                return "Carlos, atingimos o limite de consultas ao Google por agora. Vamos aguardar um minutinho?";
            }
            
            if (response.statusCode() == 200) {
                JSONObject jsonResposta = new JSONObject(response.body());
                String respostaIA = jsonResposta.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                historicoConversa.add("Lia: " + respostaIA);
                while (historicoConversa.size() > LIMITE_HISTORICO) {
                    historicoConversa.remove(0);
                }
                return respostaIA;
                
            } else {
                // ISSO AQUI VAI TE AJUDAR A VER SE A CHAVE TÁ CHEGANDO
                System.out.println("DEBUG LIA: Chave usada termina em: ..." + (ailia.length() > 5 ? ailia.substring(ailia.length() - 4) : "NULL"));
                System.out.println("DEBUG LIA Erro: " + response.body());
                return "Erro " + response.statusCode();
            }
            
        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }

    }

//    public void listarModelosDisponiveis() {
//        String ailia = System.getenv("KEYLIA");
//        String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + ailia;
//
//        try {
//            HttpClient client = HttpClient.newHttpClient();
//            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//            System.out.println("=== MODELOS QUE SUA CHAVE ENXERGA ===");
//            System.out.println(response.body());
//            System.out.println("=====================================");
//        } catch (Exception e) {
//            System.out.println("Erro ao listar modelos: " + e.getMessage());
//        }
//    }

    public void limparMemoria() {
        this.historicoConversa.clear();
        System.out.println("🧹 Memória de contexto da Lia foi limpa.");
    }
}
