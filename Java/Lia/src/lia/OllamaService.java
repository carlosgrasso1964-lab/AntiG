package lia;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public class OllamaService {

    public String consultarLocal(String pergunta, String modelo) {

        try {

            String endereco = "http://localhost:11434/api/generate";

            JSONObject json = new JSONObject();
            json.put("model", modelo);
            json.put("prompt", pergunta);
            json.put("stream", false);

            JSONObject options = new JSONObject();
            options.put("num_predict", 120);

            json.put("options", options);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endereco))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response
                    = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {

                JSONObject resposta = new JSONObject(response.body());
                return resposta.getString("response");

            } else {
                return "Erro Ollama: " + response.statusCode();
            }

        } catch (Exception e) {
            return "Erro local: " + e.getMessage();
        }
    }
}
