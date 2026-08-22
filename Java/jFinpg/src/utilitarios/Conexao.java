package utilitarios;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    public static Connection faz_conexao() throws SQLException {
        try {
            // Variáveis de ambiente para PostgreSQL local
            String user = System.getenv("PGLUSER");
            String password = System.getenv("PGLSENHA");
            String url = System.getenv("PGLURL");

            // Variáveis de ambiente para PostgreSQL na nuvem (comente/descomente conforme necessário)
            /*
            String user = System.getenv("PG_USER");
            String password = System.getenv("PG_PASSWORD");
            String url = System.getenv("PG_URL");
            */

            // Validação das variáveis
            if (user == null || user.isEmpty()) {
                throw new SQLException("Variável de ambiente PGLUSER não está definida.");
            }
            if (password == null || password.isEmpty()) {
                throw new SQLException("Variável de ambiente PGLSENHA não está definida.");
            }
            if (url == null || url.isEmpty()) {
                throw new SQLException("Variável de ambiente PGLURL não está definida.");
            }

            // Carrega o driver PostgreSQL
            Class.forName("org.postgresql.Driver");

            // Conecta
            Connection conectado = DriverManager.getConnection(url, user, password);
            conectado.setAutoCommit(true); // Boa prática: explicitar o comportamento padrão

            return conectado;

        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL não encontrado: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new SQLException("Erro ao conectar ao PostgreSQL: " + e.getMessage(), e);
        }
    }
}