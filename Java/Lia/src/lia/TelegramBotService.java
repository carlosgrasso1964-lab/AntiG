package lia;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramBotService extends TelegramLongPollingBot {

    private final String botUsername;
    //private final String botToken;
    private final LiaGUI liaGUI;// Supondo que LiaAgent é a classe principal do seu agente

//    public TelegramBotService(LiaGUI liaGUI) {
//        this.liaGUI = liaGUI;
//        this.botUsername = System.getenv("TELEGRAM_BOT_NAME");
//        this.botToken = System.getenv("TELEGRAM_BOT_TOKEN");
//        if (this.botUsername == null || this.botToken == null) {
//            throw new IllegalArgumentException("As variáveis de ambiente TELEGRAM_BOT_NAME e TELEGRAM_BOT_TOKEN devem ser configuradas.");
//        }
//    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
    
    public TelegramBotService(LiaGUI liaGUI) {
        super(System.getenv("TELEGRAM_BOT_TOKEN")); // Passa o token para a classe pai
        this.liaGUI = liaGUI;
        this.botUsername = System.getenv("TELEGRAM_BOT_NAME");
        if (this.botUsername == null) {
            throw new IllegalArgumentException("TELEGRAM_BOT_NAME deve ser configurada.");
        }
    }

//    @Override
//    public String getBotToken() {
//        return botToken;
//    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // TODO: Implementar a lógica de restrição de chat_id aqui
            // String allowedChatId = System.getenv("TELEGRAM_ALLOWED_CHAT_ID");
            // if (allowedChatId != null && !String.valueOf(chatId).equals(allowedChatId)) {
            //     sendMessage(chatId, "Desculpe, este bot está configurado para responder apenas a um chat específico.");
            //     return;
            // }
            System.out.println("Mensagem recebida de " + update.getMessage().getFrom().getFirstName() + ": " + messageText);

            String respostaLia = liaGUI.processarComandoTelegram(messageText);

            sendLongMessage(chatId, respostaLia);
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendLongMessage(long chatId, String text) {
        final int MAX_MESSAGE_LENGTH = 4096; // Limite de caracteres do Telegram
        if (text.length() <= MAX_MESSAGE_LENGTH) {
            sendMessage(chatId, text);
        } else {
            // Quebra a mensagem em partes menores para respeitar o limite do Telegram
            int startIndex = 0;
            while (startIndex < text.length()) {
                int endIndex = Math.min(startIndex + MAX_MESSAGE_LENGTH, text.length());
                String part = text.substring(startIndex, endIndex);
                sendMessage(chatId, part);
                startIndex = endIndex;
            }
        }
    }

    // Classe LiaAgent de exemplo para compilação. Substitua pela sua implementação real.
    public static class LiaAgent {

        public String processarComando(String comando) {
            System.out.println("Lia processando comando: " + comando);
            // Simula o roteamento para diferentes backends
            if (comando.toLowerCase().contains("gemini")) {
                return "Resposta do Gemini para: " + comando;
            } else if (comando.toLowerCase().contains("ollama")) {
                return "Resposta do Ollama para: " + comando;
            } else if (comando.toLowerCase().contains("manus")) {
                return "Resposta do Manus para: " + comando;
            } else if (comando.toLowerCase().contains("dólar")) {
                return "O preço do dólar hoje é R$5,20.";
            } else if (comando.toLowerCase().contains("polimorfismo")) {
                return "Polimorfismo é a capacidade de um objeto assumir diferentes formas.";
            }
            return "Comando não reconhecido pela Lia: " + comando;
        }
    }
}
