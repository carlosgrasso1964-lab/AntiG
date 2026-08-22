package utilitarios;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Conexao {

    public static String tipoDeConexao = "L"; // L = Local, N = Nuvem

    public static Connection faz_conexao() throws SQLException {
        try {
            String url, user, password;

            if ("N".equals(tipoDeConexao)) {
                url = System.getenv("DB_URL");
                user = System.getenv("DB_USERK");
                password = System.getenv("DB_PASSWORD");
            } else {
                url = System.getenv("DB_URL");
                user = System.getenv("DB_USER");
                password = System.getenv("DB_PASSWORD");
            }

            if (url == null || user == null || password == null) {
                throw new SQLException("Variáveis de ambiente do banco não configuradas!");
            }

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, password);
            con.setAutoCommit(true); // padrão
            return con;

        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL não encontrado!", e);
        } catch (SQLException e) {
            // NUNCA MAIS JOptionPane AQUI!!!
            System.err.println("ERRO CRÍTICO DE CONEXÃO COM O BANCO:");
            e.printStackTrace();
            throw e; // deixa subir, NUNCA chama Swing aqui!
        }
    }
    
    
//    public static String tipoDeConexao = "L"; // Default para Local
//    
//    // Configurar o tipo de conexão baseado na escolha do usuário
//    public static Connection faz_conexao() throws SQLException {
//        Connection conectado = null;
//        try {
//            String url;
//            String user;
//            String password;
//
//            // Verifique o tipo de conexão
//            if ("N".equals(tipoDeConexao)) { // Nuvem
//                url = System.getenv("DB_URL");
//                user = System.getenv("DB_USERK");
//                password = System.getenv("DB_PASSWORD");
//                //url = System.getenv("AW_URL");
//                //user = System.getenv("AW_USER");
//                //password = System.getenv("AW_PASSWORD");
//            } else { // Local
//                url = System.getenv("DB_URL");
//                user = System.getenv("DB_USER");
//                password = System.getenv("DB_PASSWORD");
//            }
//
//            // Verifique se as variáveis de ambiente estão definidas
//            if (url == null || user == null || password == null) {
//                throw new SQLException("Uma ou mais variáveis de ambiente estão faltando.");
//            }
//
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            conectado = DriverManager.getConnection(url, user, password);
//            
//        } catch (ClassNotFoundException | SQLException e) {
//            System.out.println(e);
//            JOptionPane.showMessageDialog(null, "Sem conexão com o Banco de dados");
//        }
//        
//        return conectado;
//    }
}
