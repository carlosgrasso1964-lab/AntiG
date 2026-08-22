package lia;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

public class ManusService {
    private final HttpClient httpClient;
    private final String manusApiKey;
    private final String MANUS_API_BASE_URL = "https://api.manus.ai/v1/tasks";

    public ManusService( ) {
        this.httpClient = HttpClient.newHttpClient( );
        this.manusApiKey = System.getenv("MANUS_API_KEY");
        if (this.manusApiKey == null || this.manusApiKey.isEmpty()) {
            System.err.println("Erro: Variável de ambiente MANUS_API_KEY não configurada.");
            throw new IllegalArgumentException("MANUS_API_KEY é necessária para usar o ManusService.");
        }
    }

    public String processarComando(String comando) {
        try {
            // 1. Criar a tarefa no Manus
            String taskId = criarTarefaManus(comando);
            if (taskId == null) {
                return "Erro: Não foi possível criar a tarefa no Manus.";
            }

            // 2. Polling para obter o resultado da tarefa
            return obterResultadoTarefaManus(taskId);

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao processar comando com Manus: " + e.getMessage();
        }
    }

    private String criarTarefaManus(String prompt) throws Exception {
        JSONObject requestBody = new JSONObject();
        requestBody.put("prompt", prompt);
        requestBody.put("agentProfile", "manus-1.6"); // Pode ser configurado para manus-1.6-lite ou manus-1.6-max

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MANUS_API_BASE_URL))
                .header("Content-Type", "application/json")
                .header("API_KEY", manusApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString( ));

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            return jsonResponse.getString("task_id");
        } else {
            System.err.println("Erro ao criar tarefa no Manus: " + response.statusCode() + " - " + response.body());
            return null;
        }
    }

    private String obterResultadoTarefaManus(String taskId) throws Exception {
        String taskStatus = "pending";
        String result = "";
        int attempts = 0;
        final int MAX_ATTEMPTS = 60; // Tentar por até 60 * 5 segundos = 5 minutos
        final long POLLING_INTERVAL_SECONDS = 5;

        while (!taskStatus.equals("completed") && !taskStatus.equals("failed") && attempts < MAX_ATTEMPTS) {
            TimeUnit.SECONDS.sleep(POLLING_INTERVAL_SECONDS);
            attempts++;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MANUS_API_BASE_URL + "/" + taskId))
                    .header("API_KEY", manusApiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString( ));

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                taskStatus = jsonResponse.getString("status");

                if (taskStatus.equals("completed")) {
                    JSONArray outputArray = jsonResponse.getJSONArray("output");
                    if (outputArray.length() > 0) {
                        // Pegar o último output, que geralmente contém o resultado final
                        JSONObject lastOutput = outputArray.getJSONObject(outputArray.length() - 1);
                        JSONArray contentArray = lastOutput.getJSONArray("content");
                        if (contentArray.length() > 0) {
                            JSONObject content = contentArray.getJSONObject(0);
                            if (content.has("text")) {
                                result = content.getString("text");
                            } else if (content.has("fileUrl")) {
                                result = "Tarefa concluída. Resultado disponível em: " + content.getString("fileUrl");
                            } else {
                                result = "Tarefa concluída, mas o formato do resultado não é texto ou URL.";
                            }
                        }
                    }
                } else if (taskStatus.equals("failed")) {
                    result = "Tarefa Manus falhou: " + jsonResponse.optString("error", "Erro desconhecido.");
                }
            } else {
                System.err.println("Erro ao obter status da tarefa Manus: " + response.statusCode() + " - " + response.body());
                return "Erro: Não foi possível obter o status da tarefa Manus.";
            }
        }

        if (attempts >= MAX_ATTEMPTS && !taskStatus.equals("completed") && !taskStatus.equals("failed")) {
            return "Erro: Tempo limite excedido para a tarefa Manus. Status atual: " + taskStatus;
        }

        return result;
    }
}
