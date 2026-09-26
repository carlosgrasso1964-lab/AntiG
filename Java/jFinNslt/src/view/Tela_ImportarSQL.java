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

public class Tela_ImportarSQL extends javax.swing.JFileChooser {

    public Tela_ImportarSQL() {
        setup();
    }

    private void setup() {
        setDialogTitle("Importar SQL - Lançamentos FinAS");
        setFileSelectionMode(JFileChooser.FILES_ONLY);
        setFileFilter(new FileNameExtensionFilter("Arquivos SQL (*.sql)", "sql"));
        setAcceptAllFileFilterUsed(false);

        // Open in user's home directory
        setSelectedFile(new File(System.getProperty("user.home"), "lancamentos.sql"));
    }

    public void executarImportacao(Component parent) {
        int resultado = showOpenDialog(parent);

        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File arquivo = getSelectedFile();
        if (!arquivo.exists()) {
            JOptionPane.showMessageDialog(parent,
                "Arquivo nÃ£o encontrado: " + arquivo.getAbsolutePath(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Read SQL file (UTF-8 from Android)
            List<String> statements = lerArquivoSQL(arquivo);

            if (statements.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                    "Nenhuma instrução SQL encontrada no arquivo.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Show confirmation
            int confirmacao = JOptionPane.showConfirmDialog(parent,
                "Encontradas " + statements.size() + " instrução(ões) SQL.\n" +
                "Deseja importar para o banco de dados?\n\n" +
                "Arquivo: " + arquivo.getName(),
                "Confirmar Importação",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (confirmacao != JOptionPane.YES_OPTION) {
                return;
            }

            // Execute SQL statements
            Conexao conexao = new Conexao();
            conexao.abrirConexao();
            Connection conn = conexao.getConexao();

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
                        errosDetalhe.append("? ").append(e.getMessage()).append("\n");
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conexao.fecharConexao();
            }

            // Show results
            String mensagem = "Importação concluí­da!\n\n" +
                "? Inseridos: " + inseridos;

            if (erros > 0) {
                mensagem += "\n? Erros: " + erros + "\n\nDetalhes:\n" + errosDetalhe.toString();
            }

            JOptionPane.showMessageDialog(parent,
                mensagem,
                erros > 0 ? "Importação com Erros" : "Sucesso",
                erros > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                "Erro ao importar: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private List<String> lerArquivoSQL(File arquivo) throws Exception {
        List<String> statements = new ArrayList<>();

        // Try UTF-8 first (Android FinAS generates UTF-8)
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(arquivo), StandardCharsets.UTF_8))) {
            String conteudo = lerCompleto(reader);
            statements = parseStatements(conteudo);
        }

        // If UTF-8 failed or empty, try ISO-8859-1
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

        // Remove SQL comments (-- and /* */)
        String limpo = conteudo.replaceAll("--[^\n]*", "");
        limpo = limpo.replaceAll("/\\*[\\s\\S]*?\\*/", "");

        // Split by semicolons, respecting quoted strings
        StringBuilder atual = new StringBuilder();
        boolean dentroAspas = false;
        char aspaChar = 0;

        for (int i = 0; i < limpo.length(); i++) {
            char c = limpo.charAt(i);

            if (!dentroAspas && (c == '\'' || c == '"')) {
                dentroAspas = true;
                aspaChar = c;
            } else if (dentroAspas && c == aspaChar) {
                // Check for escaped quotes ('')
                if (i + 1 < limpo.length() && limpo.charAt(i + 1) == aspaChar) {
                    atual.append(c);
                    i++; // skip escaped quote
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

        // Last statement (if no trailing semicolon)
        String ultimo = atual.toString().trim();
        if (!ultimo.isEmpty()) {
            statements.add(ultimo);
        }

        return statements;
    }
}
