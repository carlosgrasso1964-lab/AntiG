package utilitarios;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    private Connection conexao;

    // Método para abrir a conexéo
    public void abrirConexao() throws SQLException {
        if (conexao == null || conexao.isClosed()) {
            String caminhoDb = System.getProperty("user.dir") + System.getProperty("file.separator") + "jfinslt.db";
            conexao = DriverManager.getConnection("jdbc:sqlite:" + caminhoDb);
        }
    }

    // Método para retornar a conexéo
    public Connection getConexao() throws SQLException {
        if (conexao == null || conexao.isClosed()) {
            abrirConexao(); // Garante que a conexéo está aberta antes de retornar
        }
        return conexao;
    }

    // Método para fechar a conexéo
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