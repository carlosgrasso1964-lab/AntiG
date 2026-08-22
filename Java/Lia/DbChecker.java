
import java.sql.*;

public class DbChecker {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:lia_memoria.db";
        try (Connection conn = DriverManager.getConnection(url)) {
            System.out.println("--- Table: fatos ---");
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM fatos")) {
                while (rs.next()) {
                    System.out.println(rs.getInt("id") + " | " + rs.getString("chave") + " | " + rs.getString("valor"));
                }
            }
            System.out.println("--- Table: fatos_v2 ---");
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM fatos_v2")) {
                while (rs.next()) {
                    System.out.println(rs.getInt("id") + " | " + rs.getString("sujeito") + " | " + rs.getString("relacao") + " | " + rs.getString("objeto"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
