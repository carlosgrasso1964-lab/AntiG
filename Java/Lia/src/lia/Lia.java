package lia;

import java.io.PrintStream;
import javax.swing.SwingUtilities;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import javax.swing.JOptionPane;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Lia {

    public static void main(String[] args) {

        System.setProperty("file.encoding", "UTF-8");

        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception e) {
        }

        try {
            FileChannel channel = FileChannel.open(Paths.get(System.getProperty("user.home"), "lia.lock"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            FileLock lock = channel.tryLock();
            if (lock == null) {
                JOptionPane.showMessageDialog(null, "Lia já está em execução!",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception e) {
        }

        SwingUtilities.invokeLater(() -> {
            LiaGUI liaGUI = new LiaGUI();

            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(new TelegramBotService(liaGUI));
                System.out.println("Telegram Bot iniciado com sucesso!");
            } catch (TelegramApiException e) {
                e.printStackTrace();
                System.err.println("Erro ao iniciar o Telegram Bot: " + e.getMessage());
            }
        });
    }
}
