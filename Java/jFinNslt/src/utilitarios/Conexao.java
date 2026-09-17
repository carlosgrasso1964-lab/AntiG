package utilitarios;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    private Connection conexao;

    // Método para abrir a conexï¿½o
    public void abrirConexao() throws SQLException {
        if (conexao == null || conexao.isClosed()) {
            String caminhoDb = System.getProperty("user.dir") + System.getProperty("file.separator") + "jfinslt.db";
            conexao = DriverManager.getConnection("jdbc:sqlite:" + caminhoDb);
        }
    }

    // Método para retornar a conexï¿½o
    public Connection getConexao() throws SQLException {
        if (conexao == null || conexao.isClosed()) {
            abrirConexao(); // Garante que a conexï¿½o estï¿½ aberta antes de retornar
        }
        return conexao;
    }

    // Método para fechar a conexï¿½o
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