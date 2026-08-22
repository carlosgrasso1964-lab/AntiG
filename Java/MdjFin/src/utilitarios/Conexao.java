package utilitarios;

import java.sql.SQLException;
import org.mariadb.jdbc.Connection;
import java.sql.DriverManager;

public class Conexao {

    public static Connection faz_conexao() throws SQLException {
        try {
            String user = System.getenv("DB_USER");
            String password = System.getenv("DB_PASSWORD");
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conectado = (Connection) DriverManager.getConnection("jdbc:mariadb://localhost:3306/mdjfin?useSSL=false&allowPublicKeyRetrieval=true", user, password);
            conectado.setAutoCommit(true); // padrão
            return conectado;
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
