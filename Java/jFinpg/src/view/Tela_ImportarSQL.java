package view;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import utilitarios.Conexao;

public class Tela_ImportarSQL extends javax.swing.JFrame {

    public Tela_ImportarSQL() {
        setup();
    }

    private void setup() {
        setTitle("Importar SQL - Lan\u00e7amentos FinAS");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(420, 160);
        setLocationRelativeTo(null);
        setResizable(false);

        JLabel lblTitulo = new JLabel("Importar arquivo SQL (.sql)", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton btnSelecionar = new JButton("Selecionar arquivo...");
        btnSelecionar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnSelecionar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSelecionar.addActionListener(e -> selecionarArquivo());

        JButton btnFechar = new JButton("Fechar");
        btnFechar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnFechar.addActionListener(e -> dispose());

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        painelBotoes.add(btnSelecionar);
        painelBotoes.add(btnFechar);

        JPanel painel = new JPanel(new BorderLayout(0, 20));
        painel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        painel.add(lblTitulo, BorderLayout.NORTH);
        painel.add(painelBotoes, BorderLayout.CENTER);

        setContentPane(painel);
    }

    private void selecionarArquivo() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Selecionar arquivo SQL");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileNameExtensionFilter("Arquivos SQL (*.sql)", "sql"));
        fc.setAcceptAllFileFilterUsed(false);
        fc.setSelectedFile(new File(System.getProperty("user.home"), "lancamentos.sql"));

        int resultado = fc.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File arquivo = fc.getSelectedFile();
        if (!arquivo.exists()) {
            JOptionPane.showMessageDialog(this,
                "Arquivo n\u00e3o encontrado: " + arquivo.getAbsolutePath(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            List<String> statements = lerArquivoSQL(arquivo);

            if (statements.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Nenhuma instru\u00e7\u00e3o SQL encontrada no arquivo.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirmacao = JOptionPane.showConfirmDialog(this,
                "Encontradas " + statements.size() + " instru\u00e7\u00e3o(\u00f5es) SQL.\n"
                + "Deseja importar para o banco de dados?\n\n"
                + "Arquivo: " + arquivo.getName(),
                "Confirmar Importa\u00e7\u00e3o",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (confirmacao != JOptionPane.YES_OPTION) {
                return;
            }

            Connection conn = Conexao.faz_conexao();

            int inseridos = 0;
            int erros = 0;
            StringBuilder errosDetalhe = new StringBuilder();

            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                for (String sql : statements) {
                    try {
                        stmt.execute(sql);
                        inseridos++;
                    } catch (Exception e) {
                        erros++;
                        errosDetalhe.append("\u2022 ").append(e.getMessage()).append("\n");
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.close();
            }

            String mensagem = "Importa\u00e7\u00e3o conclu\u00edda!\n\n"
                + "\u2713 Inseridos: " + inseridos;

            if (erros > 0) {
                mensagem += "\n\u2717 Erros: " + erros + "\n\nDetalhes:\n" + errosDetalhe.toString();
            }

            JOptionPane.showMessageDialog(this,
                mensagem,
                erros > 0 ? "Importa\u00e7\u00e3o com Erros" : "Sucesso",
                erros > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao importar: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private List<String> lerArquivoSQL(File arquivo) throws Exception {
        List<String> statements = new ArrayList<>();

        // Tenta UTF-8 primeiro (Android FinAS gera UTF-8)
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(arquivo), StandardCharsets.UTF_8))) {
            String conteudo = lerCompleto(reader);
            statements = parseStatements(conteudo);
        }

        // Se UTF-8 falhou ou vazio, tenta ISO-8859-1
        if (statements.isEmpty()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(arquivo), "ISO-8859-1"))) {
                String conteudo = lerCompleto(reader);
                statements = parseStatements(conteudo);
            }
        }

        return statements;
    }

    private String lerCompleto(BufferedReader reader) throws Exception {
        StringBuilder sb = new StringBuilder();
        String linha;
        while ((linha = reader.readLine()) != null) {
            sb.append(linha).append("\n");
        }
        return sb.toString();
    }

    private List<String> parseStatements(String conteudo) {
        List<String> statements = new ArrayList<>();

        // Remove coment\u00e1rios SQL (-- e /* */)
        String limpo = conteudo.replaceAll("--[^\n]*", "");
        limpo = limpo.replaceAll("/\\*[\\s\\S]*?\\*/", "");

        // Divide por ponto e v\u00edrgula, respeitando strings entre aspas
        StringBuilder atual = new StringBuilder();
        boolean dentroAspas = false;
        char aspaChar = 0;

        for (int i = 0; i < limpo.length(); i++) {
            char c = limpo.charAt(i);

            if (!dentroAspas && (c == '\'' || c == '\"')) {
                dentroAspas = true;
                aspaChar = c;
            } else if (dentroAspas && c == aspaChar) {
                // Verifica aspas escapadas ('')
                if (i + 1 < limpo.length() && limpo.charAt(i + 1) == aspaChar) {
                    atual.append(c);
                    i++; // pula aspas escapada
                } else {
                    dentroAspas = false;
                }
            }

            if (c == ';' && !dentroAspas) {
                String stmt = atual.toString().trim();
                if (!stmt.isEmpty()) {
                    statements.add(stmt);
                }
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }

        // \u00daltima instru\u00e7\u00e3o (sem ponto e v\u00edrgula no final)
        String ultimo = atual.toString().trim();
        if (!ultimo.isEmpty()) {
            statements.add(ultimo);
        }

        return statements;
    }
}
