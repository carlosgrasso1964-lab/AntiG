package utilitarios;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    private Connection conexao;

    // Método para abrir a conexão
    public void abrirConexao() throws SQLException {
        if (conexao == null || conexao.isClosed()) {
            conexao = DriverManager.getConnection("jdbc:sqlite:jfinslt.db");
        }
    }

    // Método para retornar a conexão
    public Connection getConexao() throws SQLException {
        if (conexao == null || conexao.isClosed()) {
            abrirConexao(); // Garante que a conexão está aberta antes de retornar
        }
        return conexao;
    }

    // Método para fechar a conexão
    public void fecharConexao() {
        if (conexao != null) {
            try {
                conexao.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}