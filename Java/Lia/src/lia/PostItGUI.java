package lia;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PostItGUI extends JFrame {

    private JTextArea areaTexto;
    private String caminhoArquivo;

    // CONSTRUTOR NOVO (Para aceitar apenas o texto)
    public PostItGUI(String textoInicial) {
        // Ele chama o construtor principal passando o arquivo padrão
        this(textoInicial, "notas_lia.txt");
    }

    public PostItGUI(String textoInicial, String caminho) {
        this.caminhoArquivo = caminho;
        setTitle("Lia - Meus Lembretes");
        setSize(400, 500);
        setLayout(new BorderLayout());
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        areaTexto = new JTextArea(textoInicial);
        areaTexto.setBackground(new Color(255, 255, 200));
        areaTexto.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);

        add(new JScrollPane(areaTexto), BorderLayout.CENTER);

        JButton btnSalvar = new JButton("💾 Salvar Alterações");
        btnSalvar.setFont(new Font("Arial", Font.BOLD, 14));
        btnSalvar.addActionListener(e -> salvarArquivo());

        add(btnSalvar, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void salvarArquivo() {
        // Aqui usamos 'false' para substituir o arquivo antigo pela nova versão editada
        try (PrintWriter out = new PrintWriter(new FileWriter(caminhoArquivo, false))) {
            String textoArea = areaTexto.getText();
            String[] linhas = textoArea.split("\n");

            for (String linha : linhas) {
                String l = linha.trim();
                if (!l.isEmpty()) {
                    // Mantém o padrão que a Lia usa para ler depois
                    out.println(l + " @@@");
                }
            }
            JOptionPane.showMessageDialog(this, "Lembretes sincronizados!");
            //dispose();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + e.getMessage());
        }
    }

    public void adicionarTextoExterno(String novoTexto) {
        // Pula uma linha e adiciona o novo lembrete no JTextArea
        areaTexto.append("\n" + novoTexto);
        // Faz o scroll ir para o final automaticamente
        areaTexto.setCaretPosition(areaTexto.getDocument().getLength());
    }

}
