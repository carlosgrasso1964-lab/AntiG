package lia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoFinanceira {

    private static String getCaminhoBanco() {
        String caminhoPersonalizado = System.getProperty("lia.banco.financeiro");
        if (caminhoPersonalizado != null && !caminhoPersonalizado.isEmpty()) {
            return caminhoPersonalizado;
        }
        String envCaminho = System.getenv("LIA_BANCO_FINANCEIRO");
        if (envCaminho != null && !envCaminho.isEmpty()) {
            return envCaminho;
        }
        return null;
    }

    // 1. CAPTURA A CHAVE PRIMEIRO
    private static final String CAMINHO_PADRAO = System.getenv("LIA_BANCO_FINANCEIRO");

    public static Connection conectar() {
        try {
            String caminho = getCaminhoBanco();
            if (caminho == null || caminho.isEmpty()) {
                caminho = CAMINHO_PADRAO;
            }
            System.out.println("🔗 Conectando ao banco: " + caminho);
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + caminho);
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Erro ao conectar no banco financeiro: " + e.getMessage());
            return null;
        }
    }
}
